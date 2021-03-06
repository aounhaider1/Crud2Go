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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.RowItem;

import de.unioninvestment.eai.portal.portlet.crud.domain.container.TimeoutableQueryDelegate;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.BeforeCommitEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.CommitEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.ContainerException;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Filter;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.RegExpFilter;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.SQLFilter;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;

/**
 * Oberklasse für Container, die ihre Datensätze in einer Datenbank ablegen.
 */
public abstract class AbstractDatabaseContainer extends AbstractDataContainer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractDatabaseContainer.class);

	private static final long serialVersionUID = 1L;

	protected TimeoutableQueryDelegate queryDelegate;

	protected String datasource;
    protected String tablename;

    public AbstractDatabaseContainer(EventBus eventBus,
			Map<String, String> displayPattern,
			List<ContainerOrder> defaultOrder, FilterPolicy filterPolicy) {
		super(eventBus, displayPattern, defaultOrder, filterPolicy);
	}

	@Override
	public void replaceFilters(List<Filter> newFilters, boolean removeDurable,
			boolean forceReplace, int timeout) {
		int previousTimeout = this.queryDelegate.getQueryTimeout();
		this.queryDelegate.setQueryTimeout(timeout);
		try {
			this.replaceFilters(newFilters, removeDurable, forceReplace);
		} finally {
			this.queryDelegate.setQueryTimeout(previousTimeout);
		}
	}

	/**
	 * @return der Name der DataSource
	 */
	public String getDatasource() {
		return datasource;
	}

    public String getTablename() {
        return tablename;
    }

    public java.lang.Class<?> getType(String name) {
		Class<?> type = getVaadinContainer().getType(name);
		if (type == null) {

            LOGGER.info(
                    getNoTypeInformationForColumnMessage(name));
			return null;
		} else if (Clob.class.isAssignableFrom(type)) {
			return ContainerClob.class;
		} else if (Blob.class.isAssignableFrom(type)) {
			return ContainerBlob.class;
		} else {
			return type;
		}
	}

    protected String getNoTypeInformationForColumnMessage(String name) {
        return MessageFormat.format("Could not retrieve type information for column ''{0}''. Does it exist in the backend?", name);
    }

	@Override
	public boolean isCLob(String columnName) {
		return ContainerClob.class.equals(getType(columnName));
	}

	@Override
	public boolean isBLob(String columnName) {
		return ContainerBlob.class.equals(getType(columnName));
	}

	@Override
	protected SQLContainerEventWrapper getVaadinContainer() {
		return (SQLContainerEventWrapper) super.getVaadinContainer();
	}

	@Override
	public void commit() {

		getBeforeCommitEventRouter().fireEvent(new BeforeCommitEvent(this));
		if (getVaadinContainer().isModified()) {
			try {
				getVaadinContainer().commit();

			} catch (SQLException e) {
				throw new ContainerException(e);
			}
			getOnCommitEventRouter().fireEvent(new CommitEvent(this));
		} else {
			getVaadinContainer().fireContentsChange();
		}
	}

	@Override
	public void rollback() {
		try {
			getVaadinContainer().rollback();

		} catch (SQLException e) {
			throw new ContainerException(e);
		}
	}

	@Override
	public void refresh() {
		getVaadinContainer().refresh();
	}

	@Override
	public ContainerRow convertItemToRow(Item item, boolean transactional,
			boolean immutable) {
		DatabaseContainerRowId rowId = new DatabaseContainerRowId(
				((RowItem) item).getId(), getPrimaryKeyColumns());
		return new DatabaseContainerRow((RowItem) item, rowId, this,
				transactional, immutable);
	}

	@Override
	public ContainerRowId convertInternalRowId(Object s) {
		final RowId rowId;
		if (s instanceof RowId) {
			rowId = (RowId) s;
		} else if (s instanceof RowItem) {
			rowId = ((RowItem) s).getId();
		} else {
			throw new IllegalArgumentException(s.getClass() + " nicht erlaubt.");
		}

		return new DatabaseContainerRowId(rowId, getPrimaryKeyColumns());
	}

	com.vaadin.data.Container.Filter buildVaadinFilter(Filter filter) {
		if (filter instanceof SQLFilter) {
			return buildSQLFilter(filter);
		} else if (filter instanceof RegExpFilter) {
			return buildRegExpFilter(filter);
		} else {
			return super.buildVaadinFilter(filter);
		}
	}

	private com.vaadin.data.Container.Filter buildRegExpFilter(Filter filter) {
		RegExpFilter sqlFilter = (RegExpFilter) filter;
		return new de.unioninvestment.eai.portal.support.vaadin.filter.DatabaseRegExpFilter(
				sqlFilter.getColumn(), sqlFilter.getPattern(),
				sqlFilter.getModifiers());
	}

	private com.vaadin.data.Container.Filter buildSQLFilter(Filter filter) {
		SQLFilter sqlFilter = (SQLFilter) filter;
		return new de.unioninvestment.eai.portal.support.vaadin.filter.SQLFilter(
				sqlFilter.getColumn(), sqlFilter.getWhereString(),
				sqlFilter.getValues());
	}

	/**
	 * @param queryDelegate
	 *            for testing
	 */
	void setQueryDelegate(TimeoutableQueryDelegate queryDelegate) {
		this.queryDelegate = queryDelegate;
	}

	@Override
	public List<String> getColumns() {
		ArrayList<String> columns = new ArrayList<String>((Collection<String>) getVaadinContainer().getContainerPropertyIds());
		columns.remove("rownum");
		return columns;
	}

}
