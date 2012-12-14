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

import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.xml.sax.SAXException;

import de.unioninvestment.eai.portal.portlet.crud.config.PortletConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.converter.PortletConfigurationUnmarshaller;
import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPoolFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.form.ResetFormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.test.commons.DomainSpringPortletContextTest;
import de.unioninvestment.eai.portal.support.vaadin.LiferayApplicationMock;
import de.unioninvestment.eai.portal.support.vaadin.validation.FieldValidatorFactory;

public abstract class ModelSupport extends DomainSpringPortletContextTest {

	private static final String TEST_PORTLET_ID = "PortletId";
	private static final long TEST_COMMUNITY_ID = 17808L;

	private PortletConfigurationUnmarshaller unmarshaller;

	protected ConnectionPoolFactory connectionPoolFactory;
	protected ResetFormAction resetFormAction;
	protected FieldValidatorFactory fieldValidatorFactory;
	protected int defaultSelectWidth = 300;

	private Map<String, Long> resourceIds = new HashMap<String, Long>();
	private ExecutorService prefetchExecutor = Executors
			.newSingleThreadExecutor();

	public ModelSupport() {
		try {
			unmarshaller = new PortletConfigurationUnmarshaller();
			new LiferayApplicationMock(null, null, TEST_PORTLET_ID,
					TEST_COMMUNITY_ID);

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void initializeDependencies() {
		connectionPoolFactory = mock(ConnectionPoolFactory.class);
		resetFormAction = mock(ResetFormAction.class);
		fieldValidatorFactory = mock(FieldValidatorFactory.class);
		defaultSelectWidth = 300;

		resourceIds.put(TEST_PORTLET_ID + "_" + TEST_COMMUNITY_ID + "_admin",
				1l);
		resourceIds.put(
				TEST_PORTLET_ID + "_" + TEST_COMMUNITY_ID + "_benutzer", 1l);
	}

	protected Portlet createModel(PortletConfig configuration) {
		ModelBuilder modelBuilder = createModelBuilder(configuration);
		return modelBuilder.build();
	}

	protected ModelBuilder createModelBuilder(PortletConfig configuration) {
		ModelFactory factory = new ModelFactory(connectionPoolFactory,
				prefetchExecutor, resetFormAction, fieldValidatorFactory,
				defaultSelectWidth);
		return factory.getBuilder(new Config(configuration, resourceIds));
	}

	protected PortletConfig createConfiguration(String configRessource)
			throws JAXBException {

		InputStream stream = ModelSupport.class.getClassLoader()
				.getResourceAsStream(configRessource);
		return unmarshaller.unmarshal(stream);
	}

	protected void setPrefetchExecutor(ExecutorService prefetchExecutor) {
		this.prefetchExecutor = prefetchExecutor;
	}
}