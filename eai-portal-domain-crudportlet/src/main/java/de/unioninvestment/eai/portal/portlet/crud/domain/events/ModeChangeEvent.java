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

import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
import de.unioninvestment.eai.portal.support.vaadin.mvp.SourceEvent;

/**
 * Allgemeiner Event für Zustandsänderungen. Wird z. Zt. nicht über den
 * {@link EventBus} gesendet.
 * 
 * @param <T>
 *            der Typ der Event-Quelle
 * @param <M>
 *            der Typ des Modus (i. d. R. ein enum)
 * 
 * @author carsten.mjartan
 */
public class ModeChangeEvent<T, M> implements
		SourceEvent<ModeChangeEventHandler<T, M>, T> {

	private static final long serialVersionUID = 1L;

	private final T source;

	private final M mode;

	/**
	 * @param source
	 *            die Event-Quelle
	 * @param mode
	 *            neuer Modus
	 */
	public ModeChangeEvent(T source, M mode) {
		this.source = source;
		this.mode = mode;
	}

	@Override
	public void dispatch(ModeChangeEventHandler<T, M> eventHandler) {
		eventHandler.onModeChange(this);
	}

	@Override
	public T getSource() {
		return source;
	}

	/**
	 * @return der neue Modus
	 */
	public M getMode() {
		return mode;
	}

}
