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
package de.unioninvestment.eai.portal.portlet.crud.ui.security;

import com.vaadin.data.util.converter.Converter;
import de.unioninvestment.eai.portal.support.vaadin.context.Context;
import org.springframework.util.Assert;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.PasswordField;

/**
 * This is a more secure variant of {@link PasswordField}. The Password is not
 * sent to the client, but will be sent in plaintext to the server if changed,
 * so securing the connection via encryption is recommended.
 * 
 * @author carsten.mjartan
 */
public class SecurePasswordField extends PasswordField {

	private static final long serialVersionUID = 1L;

	private static final String XXX = "xxxxxxxx";

	private Property<String> realDataSource;

	private String realValue;
    private Converter<String, String> converter;

    public SecurePasswordField(String caption, Property<String> dataSource) {
		super(caption);
		Assert.isAssignable(String.class, dataSource.getType(),
				"SecurePasswordField only works for String properties");
		this.realDataSource = dataSource;

		realValue = realDataSource.getValue();
		setPropertyDataSource(new ObjectProperty<String>(
				placeholder(realValue), String.class));
	}

	private String placeholder(Object realValue) {
		return realValue == null ? null : XXX;
	}

	@Override
	protected void setValue(String newValue, boolean repaintIsNotNeeded)
			throws ReadOnlyException, ConversionException {
		if (newValue == null) {
			realValue = newValue;

		} else if (realValue == null || !newValue.equals(XXX)) {
			realValue = newValue;
		}
		super.setValue(placeholder(newValue), repaintIsNotNeeded);
		if (!isBuffered() && realDataSource != null) {
            updateRealDataSource();
        }
	}

    private void updateRealDataSource() {
        if (converter != null) {
            String convertedValue = converter.convertToModel(realValue, String.class, Context.getLocale());
            realDataSource.setValue(convertedValue);
        } else {
            realDataSource.setValue(realValue);
        }
    }

    @Override
	public void commit() throws SourceException, InvalidValueException {
		super.commit();
        updateRealDataSource();
    }

	@Override
	public void discard() throws SourceException {
		super.discard();
        realValue = realDataSource.getValue();
        if (converter != null) {
            realValue = converter.convertToPresentation(realValue, String.class, Context.getLocale());
        }
	}

    @Override
    public void setConverter(Converter<String, ?> converter) {
        this.converter = (Converter<String, String>) converter;
    }
}
