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

import com.vaadin.addon.sqlcontainer.ColumnProperty;
import com.vaadin.data.util.PropertyFormatter;

import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.ContainerException;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer.TransactionCallback;
import de.unioninvestment.eai.portal.support.vaadin.table.DisplaySupport;

/**
 * Modell Klasse eines Tabellen Feldwertes.
 * 
 * @author markus.bonsch
 * 
 */
public class DatabaseContainerField extends ContainerField {

	private final ColumnProperty property;

	private final DataContainer container;

	private final DatabaseContainerRow row;

	/**
	 * Konstruktor.
	 * 
	 * @param row
	 *            die Tabellenzeile
	 * @param property
	 *            Vaadin-Property des Feldes.
	 * @param container
	 *            Datenbancontainer
	 */
	public DatabaseContainerField(DatabaseContainerRow row,
			ColumnProperty property, DataContainer container) {
		this.row = row;
		this.property = property;
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(final Object value) {
		if (isImmutable()) {
			throw new ContainerException("Field '" + property.getPropertyId()
					+ "' is immutable!");

		} else if (isReadonly()) {
			throw new ContainerException("Field '" + property.getPropertyId()
					+ "' is readonly!");

		} else if (row.isTransactional()) {
			container
					.withExistingTransaction(new TransactionCallback<Object>() {
						@Override
						public Object doInTransaction() {
							property.setValue(value);
							return null;
						}
					});
		} else {
			property.setValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(final String value) {
		if (row.isTransactional()) {
			container
					.withExistingTransaction(new TransactionCallback<Object>() {
						@Override
						public Object doInTransaction() {
							return setTextInternal(value);
						}
					});
		} else {
			setTextInternal(value);
		}
	}

	private Object setTextInternal(final String value) {
		EditorSupport editorSupport = container.findEditor((String) property
				.getPropertyId());

		if (isImmutable()) {
			throw new ContainerException("Field '" + property.getPropertyId()
					+ "' is immutable!");
		}
		if (!isReadonly() && editorSupport != null) {
			PropertyFormatter formatter = editorSupport.createFormatter(
					property.getType(),
					container.getFormat((String) property.getPropertyId()));

			if (formatter != null) {
				formatter.setPropertyDataSource(property);
				formatter.setValue(value);

			} else {
				property.setValue(value);
			}
		} else {
			throw new ContainerException("Field '" + property.getPropertyId()
					+ "' is readonly!");
		}
		return null;
	}

	/**
	 * 
	 * @return Value als String
	 */
	public String getText() {
		DisplaySupport displaySupport = container
				.findDisplayer((String) property.getPropertyId());

		if (displaySupport != null) {
			return displaySupport.formatPropertyValue(property,
					container.getFormat((String) property.getPropertyId()));
		}
		return null;
	}

	/**
	 * Gibt den Wert des Feldes zurück.
	 * 
	 * @return Wert des Feldes
	 */
	public Object getValue() {
		if (row.isTransactional()) {
			return container
					.withExistingTransaction(new TransactionCallback<Object>() {
						@Override
						public Object doInTransaction() {
							if (container.isCLob(getName())) {
								return container.getCLob(row.getId(), getName());
							} else if (container.isBLob(getName())) {
								return container.getBLob(row.getId(), getName());
							} else {
								return property.getValue();
							}
						}
					});
		} else {
			if (container.isCLob(getName())) {
				return container.getCLob(row.getId(), getName());
			} else if (container.isBLob(getName())) {
				return container.getBLob(row.getId(), getName());
			} else {
				return property.getValue();
			}
		}
	}

	public String getName() {
		return property.getPropertyId();
	}

	public boolean isImmutable() {
		return row.isImmutable();
	}

	ColumnProperty getProperty() {
		return property;
	}

	DataContainer getContainer() {
		return container;
	}

	@Override
	public boolean isModified() {
		return property.isModified();
	}

	@Override
	public boolean isReadonly() {
		return row.isReadonly() || property.isReadOnly();
	}

	@Override
	public boolean isRequired() {
		return !property.isNullable();
	}

	@Override
	public Class<?> getType() {
		return container.getType(getName());
	}
}
