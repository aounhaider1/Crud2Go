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

import java.util.Map;
import java.util.NoSuchElementException;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

import de.unioninvestment.eai.portal.portlet.crud.domain.exception.ContainerException;
import de.unioninvestment.eai.portal.portlet.crud.domain.support.map.TransformedEntryMap;
import de.unioninvestment.eai.portal.portlet.crud.domain.support.map.ValueTransformer;

/**
 * Abstrakte Modellklasse einer Datenzeile.
 * 
 * @author markus.bonsch
 * 
 */
public abstract class ContainerRow implements Cloneable, ValuesRow {

	private static final FieldToValueTransformer VALUE_TRANSFORMER = new FieldToValueTransformer();

	private static final class FieldToValueTransformer implements
			ValueTransformer<ContainerField, Object> {
		@Override
		public Object transform(ContainerField a) {
			return a.getValue();
		}
	}

	private final class FieldToValueMap extends
			TransformedEntryMap<String, ContainerField, Object> {
		private static final long serialVersionUID = 1L;

		private FieldToValueMap(Map<String, ContainerField> delegate,
				ValueTransformer<ContainerField, Object> transformer) {
			super(delegate, transformer);
		}

		@Override
		public Object put(String key, Object value) {
			ContainerField field = getFields().get(key);
			Object oldValue = field.getValue();
			field.setValue(value);
			return oldValue;
		}
	}

	protected final DataContainer container;
	private Map<String,Object> values;

	public ContainerRow(DataContainer container) {
		this.container = container;
	}

	/**
	 * Gibt die Primärschlüssel zurück.
	 * 
	 * @return Primärschlüssel
	 */
	public abstract ContainerRowId getId();

	/**
	 * Gibt alle Felder der Zeile zurück.
	 * 
	 * @return Alle Felder der Zeile
	 */
	public abstract Map<String, ContainerField> getFields();

	/**
	 * @param singletonMap
	 *            for Testing
	 */
	abstract void setFields(Map<String, ContainerField> singletonMap);

	/**
	 * Liefert ähnlich {@link #getInternalRow()} ein Item für die Zeile zurück,
	 * ersetzt aber CLob-Properties, so dass sie sich nach außen wie
	 * String-Properties verhalten.
	 * 
	 * @return ein Item
	 */
	public abstract Item getInternalRow();

	public Item getFormItem() {
		Item item = getInternalRow();
		Item wrapper = new PropertysetItem();
		for (Object id : item.getItemPropertyIds()) {
			String columnName = (String) id;
			if (container.isCLob(columnName)) {
				ContainerClob clob = container.getCLob(getId(), columnName);
				wrapper.addItemProperty(columnName, clob.getPropertyValue());
			} else {
				wrapper.addItemProperty(columnName,
						item.getItemProperty(columnName));
			}
		}
		return wrapper;
	}

	/**
	 * Erstellt einen Klon der aktuellen Zeile. Dabei werden nur die Felder
	 * kopiert, die nicht readonly sind.
	 * 
	 * @return Zeile
	 * @throws CloneNotSupportedException
	 *             CloneNotSupportedException
	 */
	@Override
	public abstract ContainerRow clone() throws CloneNotSupportedException;

	/**
	 * @return <code>true</code>, falls die Zeile schreibgeschützt ist
	 */
	public abstract boolean isReadonly();

	/**
	 * Setzt den Wert einer Spalte.
	 * 
	 * @param name
	 *            SpaltenId
	 * @param value
	 *            Spalteninhalt
	 */
	public void setValue(String name, Object value) {
		ContainerField containerField = getFields().get(name);
		if (containerField == null) {
			throw new NoSuchElementException("Value for name '" + name
					+ "' not exist!");
		}

		containerField.setValue(value);
	}

	/**
	 * Gibt anhand der SpaltenId den Inhalt zurück.
	 * 
	 * @param name
	 *            SpaltenId
	 * @return Spalteninhalt
	 */
	public Object getValue(String name) {
		final ContainerField containerField = getFields().get(name);
		if (containerField == null) {
			throw new ContainerException("Value for name '" + name
					+ "' not exist!");

		} else {
			return containerField.getValue();
		}
	}

	public Map<String, Object> getValues() {
		if (values == null) {
			values = new FieldToValueMap(getFields(), VALUE_TRANSFORMER);
		}
		return values;
	}

	/**
	 * Setzt den Wert der Spalte so, als ob es über die Oberfläche gesetzt
	 * werden würde.
	 * 
	 * @param name
	 *            SpaltenId
	 * @param value
	 *            Spalteninhalt als String
	 */
	public void setText(String name, String value) {
		ContainerField containerField = getFields().get(name);

		if (containerField != null) {
			containerField.setText(value);

		} else {
			throw new NoSuchElementException("Value for name '" + name
					+ "' not exist!");
		}
	}

	/**
	 * @return <code>true</code>, falls die Tabellenzeile modifiziert wurde
	 */
	public boolean isModified() {
		return false;
	}

	/**
	 * @return <code>true</code>, falls die Tabellenzeile eine neue
	 *         ungespeicherte Zeile darstellt
	 */
	public boolean isNewItem() {
		return false;
	}

	/**
	 * @return <code>true</code>, falls die Tabellenzeile als gelöscht markiert
	 *         wurde
	 */
	public boolean isDeleted() {
		return false;
	}

}
