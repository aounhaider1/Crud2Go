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
package de.unioninvestment.eai.portal.portlet.crud.domain.model.user;

import org.springframework.stereotype.Component;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;

/**
 * Zugriff auf die aktuellen Benutzerinformationen.
 * 
 */
@Component
public class UserFactory {

	/**
	 * Gibt den aktuellen Benutzer zurück.
	 * 
	 * @param portlet
	 *            Portlet-Model-Klasse
	 * 
	 * @return den Benutzer oder <code>null</code>, wenn keine Anmeldung
	 *         vorliegt
	 */
	public CurrentUser getCurrentUser(Portlet portlet) {
		return new CurrentUser(portlet.getRoles());
	}

	/**
	 * 
	 * @return Benutzername
	 */
	public String getCurrentUserName() {
		CurrentUser currentUser = new CurrentUser(null);
		String name = currentUser.getName();
		name = name == null ? "Anonymous" : name;

		return name;
	}
}