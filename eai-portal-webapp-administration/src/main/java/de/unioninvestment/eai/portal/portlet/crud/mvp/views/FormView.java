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
package de.unioninvestment.eai.portal.portlet.crud.mvp.views;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.Form;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormAction;
import de.unioninvestment.eai.portal.support.vaadin.mvp.View;

/**
 * {@link View} für ein Formular, bestehend aus Formularfeldern und
 * Aktions-Buttons.
 * 
 * @author carsten.mjartan
 */
public interface FormView extends View {

	/**
	 * {@link Presenter} Interface für die Formular Logik.
	 * 
	 */
	public interface Presenter {
		/**
		 * @param action
		 *            Akton die ausgeführt werden soll
		 */
		void executeAction(FormAction action);
	}

	/**
	 * Initialisierung des Views.
	 * 
	 * @param presenter
	 *            die Presenter Klasse
	 * @param model
	 *            das Form Modell
	 */
	void initialize(Presenter presenter, Form model);

	/**
	 * Liefert die SearchAction des Formulars.
	 * 
	 * @return - SearchAction des Formulars, null falls keine vorhanden.
	 */
	FormAction getSearchAction();

}
