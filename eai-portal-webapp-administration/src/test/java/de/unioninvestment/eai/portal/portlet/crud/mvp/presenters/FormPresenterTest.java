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

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.unioninvestment.eai.portal.portlet.crud.config.FormConfig;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Form;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormActions;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormFields;
import de.unioninvestment.eai.portal.portlet.crud.mvp.views.FormView;

public class FormPresenterTest {
	@Mock
	private FormView formViewMock;

	@Mock
	private FormFields fieldsMock;

	private FormPresenter formPresenter;

	@Mock
	private FormAction actionMock;

	private FormActions actions;

	private Form form;

	private FormConfig formConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		formConfig = new FormConfig();
		formConfig.setColumns(2);
		actions = new FormActions(asList(actionMock));
		form = new Form(formConfig, fieldsMock, actions);
		formPresenter = new FormPresenter(formViewMock, form);
	}

	@Test
	public void shouldInitializeView() {
		verify(formViewMock).initialize(formPresenter, form);
	}

	@Test
	public void shouldDelegateActionHandlingToModel() {
		formPresenter.executeAction(actionMock);

		verify(actionMock).execute();
	}
}
