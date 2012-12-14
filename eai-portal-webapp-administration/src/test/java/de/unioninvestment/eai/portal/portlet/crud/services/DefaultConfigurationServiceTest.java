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
package de.unioninvestment.eai.portal.portlet.crud.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.SAXException;

import de.unioninvestment.eai.portal.portlet.crud.config.PortletConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
import de.unioninvestment.eai.portal.portlet.crud.persistence.ConfigurationDao;
import de.unioninvestment.eai.portal.portlet.crud.persistence.ConfigurationMetaData;
import de.unioninvestment.eai.portal.support.scripting.ConfigurationScriptsCompiler;

public class DefaultConfigurationServiceTest {

	private static final class FileStreamingConfigurationDao extends
			ConfigurationDao {

		private final InputStream configXmlStream;

		private FileStreamingConfigurationDao(JdbcTemplate jdbcTemplate,
				InputStream configXmlStream) {
			super(jdbcTemplate);
			this.configXmlStream = configXmlStream;
		}

		@Override
		public <T> T readConfigStream(String portletId, long communityId,
				StreamProcessor<T> processor) {
			return processor.process(configXmlStream);
		}
	}

	private static final class ExceptionThrowingConfigurationDao extends
			ConfigurationDao {

		private ExceptionThrowingConfigurationDao() {
			super(null);
		}

		@Override
		public <T> T readConfigStream(String windowId, long communityId,
				StreamProcessor<T> processor) {
			throw new EmptyResultDataAccessException(1);
		}
	}

	@Mock
	private ConfigurationDao daoMock;

	private DefaultConfigurationService service;

	@Mock
	private ConfigurationScriptsCompiler compilerMock;

	@Before
	public void setUp() throws JAXBException, SAXException {
		MockitoAnnotations.initMocks(this);
		service = new DefaultConfigurationService(daoMock, compilerMock);
	}

	@Test
	public void shouldReturnNullIfConfigNotFound() throws JAXBException,
			SAXException {

		service = new DefaultConfigurationService(
				new ExceptionThrowingConfigurationDao(), compilerMock);

		Config config = service.getPortletConfig("myWindowId", 17002L);

		assertNull(config);
	}

	@Test
	public void shouldReturnNullWithInvalidConfiguration()
			throws JAXBException, SAXException {
		createServiceForConfig("invalidConfig.xml.txt");

		Config config = service.getPortletConfig("myWindowId", 17002L);

		assertNull(config);
	}

	@Test
	public void shouldReturnDeserializedConfigurationByWindowId()
			throws JAXBException, SAXException {
		createServiceForConfig("validConfig.xml");

		PortletConfig config = service.getPortletConfig("myWindowId", 17002L)
				.getPortletConfig();

		assertNotNull(config);
	}

	@Test
	public void shouldStoreConfigurationFile() {
		service.storeConfigurationFile("bla", "xml".getBytes(), "myWindowId",
				17002L, "cmj");
		verify(daoMock).saveOrUpdateConfigData("myWindowId", 17002L, "bla",
				"xml".getBytes(), "cmj");
	}

	@Test
	public void shouldReturnMetaDataFromDao() {
		ConfigurationMetaData data = new ConfigurationMetaData("user",
				new Date(), null);
		when(daoMock.readConfigMetaData("myWindowId", 17002L)).thenReturn(data);

		assertEquals(data,
				service.getConfigurationMetaData("myWindowId", 17002L));
	}

	@Test
	public void shouldReturnNullWhenMetaDataIsMissing() {
		when(daoMock.readConfigMetaData("myWindowId", 17002L)).thenThrow(
				new EmptyResultDataAccessException(1));

		assertNull(service.getConfigurationMetaData("myWindowId", 17002L));
	}

	private void createServiceForConfig(String configXml) throws JAXBException,
			SAXException {
		final InputStream configXmlStream = getClass().getClassLoader()
				.getResourceAsStream(configXml);

		ConfigurationDao dao = new FileStreamingConfigurationDao(null,
				configXmlStream);
		service = new DefaultConfigurationService(dao, compilerMock);
	}
}