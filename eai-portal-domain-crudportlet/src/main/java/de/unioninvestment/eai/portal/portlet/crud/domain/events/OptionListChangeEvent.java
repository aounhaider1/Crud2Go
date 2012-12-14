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
package de.unioninvestment.eai.portal.portlet.crud.domain.events;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.OptionList;
import de.unioninvestment.eai.portal.support.vaadin.mvp.SourceEvent;

/**
 * Ein Event das die Datenaktualisierungen an die Optionlist weitgibt.
 * 
 * @author markus.bonsch
 * 
 */
public class OptionListChangeEvent implements
		SourceEvent<OptionListChangeEventHandler, OptionList> {

	private static final long serialVersionUID = 42L;
	private final OptionList optionList;
	private final boolean initialized;

	/**
	 * Konstruktor.
	 * 
	 * @param optionList
	 *            Drop-Down
	 */
	public OptionListChangeEvent(OptionList optionList, boolean initialized) {
		this.optionList = optionList;
		this.initialized = initialized;
	}

	@Override
	public void dispatch(OptionListChangeEventHandler eventHandler) {
		eventHandler.onOptionListChange(this);
	}

	@Override
	public OptionList getSource() {
		return optionList;
	}

	public boolean isInitialized() {
		return initialized;
	}

}