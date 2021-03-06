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
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FieldEditableChecker;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;

/**
 * Implementierung für {@link FieldEditableChecker}.
 * 
 * @author Bastian Krol
 */
public class FieldEditableCheckerImpl implements FieldEditableChecker {

	private final Closure<Object> closure;
	private final Table table;
	private final String columnName;

	/**
	 * Konstruktor.
	 * 
	 * @param table
	 *            die Tabelle
	 * @param columnName
	 *            der Spaltenname
	 * @param closure
	 *            die Groovy-Closure für den dynamischen Schreibschutz
	 */
	public FieldEditableCheckerImpl(Table table, String columnName,
			Closure<Object> closure) {
		this.table = table;
		this.columnName = columnName;
		this.closure = closure;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.unioninvestment.eai.portal.portlet.crud.domain.model.FieldEditableChecker#isEditable(de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow)
	 */
	@Override
	public boolean isEditable(ContainerRow row) {
		ScriptRow scriptRow = new ScriptRow(row);
		Object result = closure.call(table, columnName, scriptRow);
		return (Boolean) result;
	}
}