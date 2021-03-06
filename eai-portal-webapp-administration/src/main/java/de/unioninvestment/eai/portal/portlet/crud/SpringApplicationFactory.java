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
package de.unioninvestment.eai.portal.portlet.crud;

import com.cybercom.vaadin.spring.UIScope;
import com.google.common.base.Preconditions;
import de.unioninvestment.eai.portal.portlet.crud.CrudUI.LifecycleEvent;
import de.unioninvestment.eai.portal.portlet.crud.config.PreferenceConfig;
import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPoolFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.TechnicalCrudPortletException;
import de.unioninvestment.eai.portal.portlet.crud.domain.form.ResetFormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Preference;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.UserFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.portal.Portal;
import de.unioninvestment.eai.portal.portlet.crud.domain.support.PreferencesRepository;
import de.unioninvestment.eai.portal.portlet.crud.export.streaming.ExcelExporter;
import de.unioninvestment.eai.portal.portlet.crud.liferay.CrudPortletLayoutListener;
import de.unioninvestment.eai.portal.portlet.crud.persistence.DefaultConfigurationDao;
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptModelFactory;
import de.unioninvestment.eai.portal.portlet.crud.services.ConfigurationService;
import de.unioninvestment.eai.portal.portlet.crud.services.DefaultConfigurationService;
import de.unioninvestment.eai.portal.support.scripting.ConfigurationScriptsCompiler;
import de.unioninvestment.eai.portal.support.scripting.ScriptBuilder;
import de.unioninvestment.eai.portal.support.scripting.ScriptCompiler;
import de.unioninvestment.eai.portal.support.vaadin.database.DatabaseDialect;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
import de.unioninvestment.eai.portal.support.vaadin.table.DisplaySupport;
import de.unioninvestment.eai.portal.support.vaadin.validation.FieldValidatorFactory;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Spring ApplicationContext Konfiguration.
 * 
 * @author carsten.mjartan
 */
@Configuration
public class SpringApplicationFactory implements PreferencesRepository {

	@Autowired
	private DefaultConfigurationDao configurationDao;

	@Autowired
	private Settings settings;

	@Resource(name = "dataTypes")
	private List<Object> dataTypeHelpers;

    @Autowired
    private ConnectionPoolFactory connectionPoolFactory;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private Portal portal;

    @Value("${portlet.crud.databaseBackend.dialect}")
    private DatabaseDialect databaseDialect;

    @Bean
	static UIScope uiScope() {
		return new UIScope();
	}

	@PostConstruct
	public void initialize() {
		CrudUI.logLifecycleEvent(LifecycleEvent.CRUD2GO_INIT);

        try {
            CrudPortletLayoutListener
                    .setConfigurationService(configurationService());

        } catch (JAXBException e) {
            throw new TechnicalCrudPortletException("Error creating ConfigurationService", e);
        } catch (SAXException e) {
            throw new TechnicalCrudPortletException("Error creating ConfigurationService", e);
        }
    }
	
	@PreDestroy
	public void shutdown() {
		CrudUI.logLifecycleEvent(LifecycleEvent.CRUD2GO_SHUTDOWN);
	}
	
	/**
	 * Erzeugt eine Instanz der Klasse EventBus.
	 * 
	 * @return EventBus
	 */
	@Bean
	@Lazy
	@Scope("ui")
	public EventBus eventBus() {
		return new EventBus();
	}

	/**
	 * Erzeugt eine Instanz der Klasse ScriptBuilder.
	 * 
	 * @return ScriptBuilder
	 */
	@Bean()
	@Lazy
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
	public ScriptBuilder scriptBuilder() {
		return new ScriptBuilder();
	}

	@Bean
	public TaskScheduler scheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.afterPropertiesSet();
		return scheduler;
	}

	/**
	 * Erzeugt eine Instanz der Klasse FieldValidatorFactory.
	 * 
	 * @return FieldValidatorFactory
	 */
	@Bean
	public FieldValidatorFactory fieldValidatorFactory() {
		return new FieldValidatorFactory();
	}

	/**
	 * Erzeugt eine Instanz der Klasse ResetFormAction.
	 * 
	 * @return ResetFormAction
	 */
	@Bean
	public ResetFormAction resetFormAction() {
		return new ResetFormAction();
	}

	/**
	 * Erzeugt eine Instanz der Klasse ConfigurationService.
	 * 
	 * @return ConfigurationService
	 * @throws SAXException
	 *             wenn das XSD Syntaxfehler enthält
	 * @throws JAXBException
	 *             wenn das XSD Fehler enthält
	 */
	@Bean
	public ConfigurationService configurationService() throws JAXBException,
			SAXException {
		return new DefaultConfigurationService(configurationDao,
				new ConfigurationScriptsCompiler(scriptCompiler()),
				settings);
	}

    @Bean
    public ScriptCompiler scriptCompiler() {
        return new ScriptCompiler();
    }

    /**
	 * @return eine Liste datentypspezifischer Hilfsklassen in Reihenfolge der
	 *         Relevanz
	 */
	@Bean
	public List<DisplaySupport> displaySupport() {
		return getDataTypeHelpers(DisplaySupport.class);
	}

	/**
	 * @return eine Liste datentypspezifischer Hilfsklassen in Reihenfolge der
	 *         Relevanz
	 */
	@Bean
	public List<EditorSupport> editorSupport() {
		return getDataTypeHelpers(EditorSupport.class);
	}

	/*
	 * Caching
	 */

	@Bean
	public CacheManager cacheManager() throws IOException {
		return CacheManager.newInstance(cacheConfigLocation().getInputStream());
	}

	private ClassPathResource cacheConfigLocation() {
		ClassPathResource configLocation = new ClassPathResource(
				"eai/ehcache.xml",
				SpringApplicationFactory.class.getClassLoader());
		if (!configLocation.exists()) {
			configLocation = new ClassPathResource("ehcache.xml",
					SpringApplicationFactory.class.getClassLoader());
		}
		return configLocation;
	}

	@Bean
	@Qualifier("portletCache")
	public Ehcache portletCache() throws IOException {
		Ehcache ehcache = cacheManager().getEhcache("crudPortletCache");
		Preconditions.checkNotNull(ehcache, "crudPortletCache not configured in ehcache.xml");
		return ehcache;
	}

	@Bean
	@Qualifier("optionListCache")
	public Ehcache optionListCache(CacheManager cacheManager)
			throws IOException {
		Ehcache ehcache = cacheManager().getEhcache("optionListCache");
		Preconditions.checkNotNull(ehcache, "optionListCache not configured in ehcache.xml");
		return ehcache;
	}

	/**
	 * @param <T>
	 *            der Typ der benötigten Hilfsklassen
	 * @param clazz
	 *            der Typ der benötigten Hilfsklassen
	 * @return eine Liste der Hilfsklassen aus dem Spring ApplicationContext
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> getDataTypeHelpers(Class<T> clazz) {

		LinkedList<T> results = new LinkedList<T>();
		for (Object helper : dataTypeHelpers) {
			if (clazz.isAssignableFrom(helper.getClass())) {
				results.add((T) helper);
			}
		}
		return results;
	}

    @Bean
    public ExcelExporter excelExporter() {
        return new ExcelExporter(settings.getExcelRowAccessWindowSize(), settings.getExcelFontName());
    }

    @Override
    public Preference getPreference(Portlet requestor, PreferenceConfig config) {
        return new Preference(config);
    }

    @Bean
    ScriptModelFactory scriptModelFactory() {
        return new ScriptModelFactory(connectionPoolFactory,
                userFactory, portal, scriptCompiler(), databaseDialect, false);
    }

}
