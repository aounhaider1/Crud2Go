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

import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
import de.unioninvestment.eai.portal.support.vaadin.mvp.SourceEvent;

/**
 * Event für neuerstellte Container-Zeilen. Wird z. Zt. nicht über den
 * {@link EventBus} gesendet.
 * 
 * @author carsten.mjartan
 */
public class CreateEvent implements
		SourceEvent<CreateEventHandler, DataContainer> {

	private static final long serialVersionUID = 1L;

	private DataContainer source;

	private final ContainerRow row;

	/**
	 * @param source
	 *            die Datenquelle
	 * @param row
	 *            die neue Zeile
	 */
	public CreateEvent(DataContainer source, ContainerRow row) {
		this.source = source;
		this.row = row;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataContainer getSource() {
		return source;
	}

	/**
	 * @return die neu erstellte Zeile
	 */
	public ContainerRow getRow() {
		return row;
	}

	@Override
	public void dispatch(CreateEventHandler eventHandler) {
		eventHandler.onCreate(this);
	}

}
