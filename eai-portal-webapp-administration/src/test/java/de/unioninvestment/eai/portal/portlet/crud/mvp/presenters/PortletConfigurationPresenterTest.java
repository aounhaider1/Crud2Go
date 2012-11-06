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
package de.unioninvestment.eai.portal.portlet.crud.mvp.presenters;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.ui.Button;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window.Notification;

import de.unioninvestment.eai.portal.portlet.crud.CrudPortletApplication;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Role;
import de.unioninvestment.eai.portal.portlet.crud.mvp.events.ConfigurationUpdatedEvent;
import de.unioninvestment.eai.portal.portlet.crud.mvp.presenters.PortletConfigurationPresenter.ConfigUploadFinishedListener;
import de.unioninvestment.eai.portal.portlet.crud.mvp.presenters.PortletConfigurationPresenter.ConfigurationReceiver;
import de.unioninvestment.eai.portal.portlet.crud.mvp.views.PortletConfigurationView;
import de.unioninvestment.eai.portal.portlet.crud.persistence.ConfigurationMetaData;
import de.unioninvestment.eai.portal.portlet.crud.services.ConfigurationService;
import de.unioninvestment.eai.portal.portlet.crud.validation.ConfigurationUploadValidator;
import de.unioninvestment.eai.portal.portlet.test.commons.SpringPortletContextTest;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;

public class PortletConfigurationPresenterTest extends SpringPortletContextTest {

	private PortletConfigurationPresenter portletConfigurationPresenter;

	private CrudPortletApplication app;

	@Mock
	private PortletConfigurationView viewMock;

	@Mock
	private ConfigurationService configurationServiceMock;

	@Mock
	private EventBus eventBusMock;

	@Mock
	private Upload uploadMock = new Upload();

	@Mock
	private Button uploadVcsButton = new Button();

	@Mock
	private ResourceResponse responseMock;

	@Mock
	private ResourceRequest requestMock;

	@Mock
	private Portlet portletMock;

	@Mock
	private Role roleMock;

	@Mock
	private ThemeDisplay themeDisplayMock;

	private static final String testWinId = "2";

	@Before
	public void setUp() throws SQLException {
		MockitoAnnotations.initMocks(this);
		app = new CrudPortletApplication() {
			public String getPortletId() {
				return testWinId;
			}

			public long getCommunityId() {
				return 18004L;
			}
		};
		app.onRequestStart(requestMock, responseMock);
		when(viewMock.getUpload()).thenReturn(uploadMock);
		when(viewMock.getUploadVcsButton()).thenReturn(uploadVcsButton);
	}

	@Test
	public void shouldInitConfigurationViewWithoutConfigData() {

		when(
				configurationServiceMock.getConfigurationMetaData(testWinId,
						18004)).thenReturn(null);

		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		verify(viewMock).setStatus(
				"portlet.crud.page.status.config.notAvailable");
	}

	@Test
	public void shouldInitConfigurationViewWithConfigData() {

		ConfigurationMetaData metaDataMock = mock(ConfigurationMetaData.class);
		Date testDate = new Date();
		String testUser = "testUser";
		when(metaDataMock.getCreated()).thenReturn(testDate);
		when(metaDataMock.getUser()).thenReturn(testUser);

		when(
				configurationServiceMock.getConfigurationMetaData(testWinId,
						18004L)).thenReturn(metaDataMock);

		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		verify(viewMock).setStatus("portlet.crud.page.status.config.available",
				testUser, testDate, null);
	}

	@Test
	public void shouldUpdateValidConfigXML() {
		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		ConfigUploadFinishedListener configUploadListener = portletConfigurationPresenter.new ConfigUploadFinishedListener();
		ConfigurationUploadValidator validator = mock(ConfigurationUploadValidator.class);
		ConfigurationReceiver receiver = mock(ConfigurationReceiver.class);

		configUploadListener.setValidator(validator);
		portletConfigurationPresenter.setReceiver(receiver);

		when(validator.isValid(anyObject())).thenReturn(true);
		when(receiver.getConfigurationXML()).thenReturn(new byte[] { 0 });
		when(receiver.getFilename()).thenReturn("fileName");

		when(requestMock.getAttribute(WebKeys.PORTLET_ID))
				.thenReturn(testWinId);
		when(requestMock.getRemoteUser()).thenReturn("horst");

		configUploadListener.uploadFinished(new Upload.FinishedEvent(
				uploadMock, "", "", System.currentTimeMillis()));

		verify(configurationServiceMock).storeConfigurationFile(eq("fileName"),
				any(byte[].class), eq(testWinId), eq(18004L), eq("horst"));
		verify(eventBusMock).fireEvent(any(ConfigurationUpdatedEvent.class));
		verify(uploadMock).setVisible(true);
	}

	@Test
	public void shouldUpdateInvalidConfigXML() {
		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		ConfigUploadFinishedListener configUploadListener = portletConfigurationPresenter.new ConfigUploadFinishedListener();
		ConfigurationUploadValidator validator = mock(ConfigurationUploadValidator.class);
		ConfigurationReceiver receiver = mock(ConfigurationReceiver.class);

		configUploadListener.setValidator(validator);
		portletConfigurationPresenter.setReceiver(receiver);

		when(validator.isValid(anyObject())).thenReturn(false);
		when(receiver.getConfigurationXML()).thenReturn(new byte[] { 0 });

		configUploadListener.uploadFinished(new Upload.FinishedEvent(
				uploadMock, "", "", System.currentTimeMillis()));

		verify(viewMock).showNotification(
				"Die Konfiguration entspricht nicht dem gültigen XSD!",
				Notification.TYPE_ERROR_MESSAGE);
	}

	@Test
	public void shouldResetBufferBeforeUpload() {
		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		ConfigurationReceiver receiver = portletConfigurationPresenter.new ConfigurationReceiver();
		receiver.getBaos().write(369);

		receiver.receiveUpload("TestFile", "xml");

		assertArrayEquals(new byte[] {}, receiver.getConfigurationXML());
		assertEquals("TestFile", receiver.getFilename());
		assertEquals("xml", receiver.getMimetype());
	}

	@Test
	public void shouldUpdateViewWithNewSecurityRolesOnRefresh() {
		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		Set<Role> roles = singleton(roleMock);
		when(portletMock.getRoles()).thenReturn(roles);

		portletConfigurationPresenter.refresh(portletMock);

		verify(viewMock).displaySecurity(roles);
	}

	@Test
	public void shouldInformViewToRemoveSecurityConfig() {
		portletConfigurationPresenter = new PortletConfigurationPresenter(
				viewMock, configurationServiceMock, eventBusMock);

		portletConfigurationPresenter.refresh(null);

		verify(viewMock).hideSecurity();
	}
}
