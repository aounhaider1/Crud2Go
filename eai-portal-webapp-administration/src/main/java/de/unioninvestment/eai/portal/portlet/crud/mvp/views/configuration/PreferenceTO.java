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
package de.unioninvestment.eai.portal.portlet.crud.mvp.views.configuration;

/**
 * A transport object for a preference that can be modified by a view on the
 * portlet preferenes tab.
 * 
 * @author carsten.mjartan
 */
public class PreferenceTO {

	private String preferenceKey;
	private String title;
	private boolean password;
	private String encryptionAlgorithm;
	private String defaultValue;

	public PreferenceTO(String preferenceKey, String title,
			String defaultValue, boolean password, String encryptionAlgorithm) {
		this.preferenceKey = preferenceKey;
		this.title = title;
		this.defaultValue = defaultValue;
		this.password = password;
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

	public String getTitle() {
		return title;
	}

	public boolean isPassword() {
		return password;
	}

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

}
