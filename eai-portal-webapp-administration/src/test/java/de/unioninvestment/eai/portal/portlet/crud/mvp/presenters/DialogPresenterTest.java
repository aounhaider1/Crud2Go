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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.VerticalLayout;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.Dialog;
import de.unioninvestment.eai.portal.portlet.crud.mvp.views.PanelContentView;

public class DialogPresenterTest {

	@Mock
	private Dialog dialogMock;

	@Mock
	private DialogPresenter dialogPresenter;

	@Before
	public void setUp() {
		PanelContentView viewMock = (PanelContentView) mock(
				VerticalLayout.class,
				withSettings().extraInterfaces(PanelContentView.class));
		MockitoAnnotations.initMocks(this);
		dialogPresenter = new DialogPresenter(viewMock, dialogMock);
	}

	@Test
	public void goBackShouldDelegateToParentPresenter() {
		// given a dialog presenter
		PanelPresenter parentPanelPresenter = mock(PanelPresenter.class);
		dialogPresenter.notifyAboutBeingAttached(parentPanelPresenter);

		// when the back button is clicked
		dialogPresenter.detach();

		// then the call should be delegated to the parent panel presenter
		verify(parentPanelPresenter).detachDialog();
	}
}
