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

import de.unioninvestment.eai.portal.portlet.crud.domain.events.ShowEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.ShowEventHandler;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Form;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormActions;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormFields;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.LazyInitializable;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Tab;
import de.unioninvestment.eai.portal.portlet.crud.mvp.views.FormView;

/**
 * Formularsteuerung.
 * 
 * @author carsten.mjartan
 */
public class FormPresenter extends AbstractComponentPresenter<Form, FormView>
		implements FormView.Presenter, ShowEventHandler<Tab> {

	private static final long serialVersionUID = 2L;

	private boolean isInitializeView = false;

	/**
	 * Konstruktor.
	 * 
	 * @param view
	 *            das Anzeigeobjekt
	 * @param model
	 *            Formulare-Modell-Klasse
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FormPresenter(FormView view, Form model) {
		super(view, model);

		if (model.getPanel() instanceof LazyInitializable) {
			((LazyInitializable) model.getPanel()).addShowEventListener(this);
		} else {
			initializeView();
		}
	}

	private void initializeView() {
		FormFields fields = getModel().getFields();
		FormActions actions = getModel().getActions();

		getView().initialize(this, this.getModel());
		isInitializeView = true;

		if (fields.hasDefaultValue() && actions.getSearchAction() != null) {
			executeAction(getView().getSearchAction());
		}
	}

	/**
	 * Ist nur als übergangslösung zu sehen. Sollte so nicht gemacht werden.
	 * {@inheritDoc}
	 */
	@Override
	public void executeAction(FormAction action) {
		action.execute();
	}

	@Override
	public void onShow(ShowEvent<Tab> event) {
		if (!isInitializeView) {
			initializeView();
		}
	}

}
