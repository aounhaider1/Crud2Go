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

package de.unioninvestment.eai.portal.portlet.crud.scripting.model;

import groovy.lang.Closure;

import java.io.InputStream;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;

public class ScriptDownloadFactory {
	
	private Table table;

	public ScriptDownloadFactory(Table table) {
		this.table = table;
	}

	/**
	 * Create the downloadable content.
	 * 
	 * @param filename
	 *            Name of the created file.
	 * @param mimeType
	 *            MIME-Type of the created file.
	 * @param totalProgressSize
	 *            The total size of the progress bar.
	 * @param closure
	 *            Creates the file's content.
	 */
	public void create(String filename, String mimeType,
			Integer totalProgressSize, Closure<InputStream> closure) {
		
		ScriptDownload download = new ScriptDownload(filename, mimeType, totalProgressSize, closure);
		table.download(download);
	}

}
