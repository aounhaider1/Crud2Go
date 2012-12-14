/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.unioninvestment.eai.portal.portlet.crud.domain.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import de.unioninvestment.eai.portal.portlet.crud.config.InitializeTypeConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.SelectConfig;
import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPool;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.OptionListChangeEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.OptionListChangeEventHandler;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.TechnicalCrudPortletException;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventRouter;

/**
 * 
 * Modell-Klasse für Auswahl-Boxen. Sofern die Query mit
 * <code>initialize="async"</code> konfiguriert wurde, wird bei der
 * Initialisierung und beim Refresh die Optionsliste asynchron vorgeladen.
 * 
 * @author max.hartmann
 * @author carsten.mjartan
 * 
 */
public class QueryOptionList implements OptionList {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(QueryOptionList.class);

	private volatile Future<Map<String, String>> future;
	private volatile Map<String, String> options;

	private String query;
	private ConnectionPool connectionPool;
	private Object lock = new Object();
	private String id;
	private boolean lazy;
	private boolean prefetched;
	private EventRouter<OptionListChangeEventHandler, OptionListChangeEvent> changeEventRouter = new EventRouter<OptionListChangeEventHandler, OptionListChangeEvent>();
	private final ExecutorService prefetchExecutor;

	/**
	 * Konstruktor mit Parametern. Wird verwendet, wenn die Optionen aus der
	 * Daten Datenbank gelesen werden sollen.
	 * 
	 * @param config
	 *            Konfiguration der Auswahl-Box
	 * @param connectionPool
	 *            Connection-Pool
	 * @param asyncExecutor
	 *            executes the prefetch operation, if configured
	 */
	public QueryOptionList(SelectConfig config, ConnectionPool connectionPool,
			ExecutorService asyncExecutor) {
		this.connectionPool = connectionPool;
		this.prefetchExecutor = asyncExecutor;

		this.id = config.getId();
		this.query = config.getQuery().getValue();

		InitializeTypeConfig initialize = config.getQuery().getInitialize();
		this.lazy = initialize.equals(InitializeTypeConfig.LAZY)
				|| initialize.equals(InitializeTypeConfig.ASYNC);
		this.prefetched = initialize.equals(InitializeTypeConfig.ASYNC);
		if (prefetched) {
			startPrefetch();
		}
	}

	private void startPrefetch() {
		cancelOlderPrefetch();
		future = prefetchExecutor.submit(new Callable() {
			@Override
			public Object call() throws Exception {
				LOGGER.debug("Prefetching option list for Query '{}'", query);
				synchronized (lock) {
					options = loadOptions();
					changeEventRouter.fireEvent(new OptionListChangeEvent(
							QueryOptionList.this, true));
					return options;
				}
			}
		});
	}

	private void cancelOlderPrefetch() {
		Future<Map<String, String>> currentFuture = future;
		if (currentFuture != null) {
			currentFuture.cancel(true);
		}
		future = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getOptions(SelectionContext context) {
		synchronized (lock) {
			if (options == null) {
				if (future != null) {
					waitForOptionsFromFuture();
					future = null;
				}
				if (options == null) {
					options = loadOptions();
				}
			}
			return options;
		}
	}

	private void waitForOptionsFromFuture() {
		try {
			future.get();

		} catch (InterruptedException e) {
			// Task interrupted (by refresh?) => continue reading
			// synchronously

		} catch (ExecutionException e) {
			throw new TechnicalCrudPortletException(
					"Error acquiring result of prefetch operation",
					e.getCause());
		}
	}

	/**
	 * 
	 * Nur für UnitTests Liefert die Einträge. Beachten, dass die Werte nicht
	 * aus der DB geladen werden.
	 * 
	 * @return Einträge im Objekt
	 */
	Map<String, String> getOptions() {
		return options;
	}

	protected Map<String, String> loadOptions() {
		LOGGER.debug("Loading option list for Query '{}'", query);
		long startTime = System.currentTimeMillis();

		String nullSafeQuery = nullSafeQuery(query);
		final LinkedHashMap<String, String> newOptions = new LinkedHashMap<String, String>();
		connectionPool.executeWithJdbcTemplate(nullSafeQuery,
				new RowMapper<Object>() {
					@Override
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						newOptions.put(rs.getString("key"),
								rs.getString("title"));
						return null;
					}
				});

		long duration = System.currentTimeMillis() - startTime;
		LOGGER.debug("Finished loading option list for Query '{}' ({}ms)",
				query, duration);
		return newOptions;
	}

	/**
	 * Macht eine Abfrage NULL-Sicher.
	 * 
	 * @param query
	 *            Abfrage
	 * @return NULL-Sichere-Abfrage
	 */
	static String nullSafeQuery(String query) {
		StringBuilder sb = new StringBuilder();
		sb.append("select key, title from (");
		sb.append(query);
		sb.append(") where key is not null and title is not null");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle(String key, SelectionContext context) {
		if (getOptions(context) != null) {
			return getOptions(context).get(key);
		}
		return null;
	}

	/**
	 * Entfernt die gepufferten Werte.
	 */
	@Override
	public void refresh() {
		synchronized (lock) {
			options = null;
			if (prefetched) {
				startPrefetch();
			} else {
				changeEventRouter.fireEvent(new OptionListChangeEvent(this,
						false));
			}
		}
	}

	public String getId() {
		return id;
	}

	@Override
	public void addChangeListener(OptionListChangeEventHandler handler) {
		changeEventRouter.addHandler(handler);
	}

	@Override
	public void removeChangeListener(OptionListChangeEventHandler handler) {
		changeEventRouter.removeHandler(handler);
	}

	/**
	 * Information für das Frontend, wann die Optionsliste aus der DB zu laden
	 * ist.
	 */
	@Override
	public boolean isLazy() {
		return lazy;
	}

	@Override
	public boolean isInitialized() {
		return options != null;
	}

	/**
	 * @param options
	 *            for testing
	 */
	void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public boolean isPrefetched() {
		return prefetched;
	}
}