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
package de.unioninvestment.eai.portal.portlet.crud.domain.model;

/**
 * 
 * Checkbox Modelobjekt für die Tabelleneditierung.
 * 
 * @author markus.bonsch
 * 
 */
public class CheckBoxTableColumn extends TableColumn {

	private static final long serialVersionUID = 1L;

	private String checkedValue;
	private String uncheckedValue;

	public static class Builder extends TableColumn.Init<Builder> {
		private String checkedValue;
		private String uncheckedValue;

		@Override
		protected Builder self() {
			return this;
		}

		public Builder checkedValue(String checkedValue) {
			this.checkedValue = checkedValue;
			return self();
		}

		public Builder uncheckedValue(String uncheckedValue) {
			this.uncheckedValue = uncheckedValue;
			return self();
		}

		@Override
		public CheckBoxTableColumn build() {
			return new CheckBoxTableColumn(this);
		}

	}

	public CheckBoxTableColumn(Builder builder) {
		super(builder);
		this.checkedValue = builder.checkedValue;
		this.uncheckedValue = builder.uncheckedValue;
	}

	/**
	 * @return den Wert für den Checked-Zustand
	 */
	public String getCheckedValue() {
		return checkedValue;
	}

	/**
	 * @return den Wert für den Unchecked-Zustand
	 */
	public String getUncheckedValue() {
		return uncheckedValue;
	}

}
