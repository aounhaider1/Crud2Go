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

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import de.unioninvestment.eai.portal.support.vaadin.junit.ContextMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SecurePasswordFieldTest {

    @Mock
    private Converter<String, String> converterMock;

    @Rule
    public ContextMock contextMock = new ContextMock();

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
	public void shouldReturnNullOnNullPassword() {
		SecurePasswordField field = new SecurePasswordField("Test",
				new ObjectProperty<String>(null, String.class));
		assertThat(field.getValue(), nullValue());
	}

	@Test
	public void shouldReturnPlaceholderOnGivenPassword() {
		SecurePasswordField field = new SecurePasswordField("Test",
				new ObjectProperty<String>("abcde", String.class));
		assertThat((String) field.getValue(), is("xxxxxxxx"));
	}

	@Test
	public void shouldChangeRealPasswordToNull() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);

		field.setValue(null);

		assertThat(realDataSource.getValue(), nullValue());
	}

	@Test
	public void shouldChangeRealPasswordToNewValue() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);

		field.setValue("edcba");

		assertThat((String) realDataSource.getValue(), is("edcba"));
	}

	@Test
	public void shouldNotChangeRealPasswordIfPlaceholderIsSet() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);

		field.setValue("xxxxxxxx");

		assertThat((String) realDataSource.getValue(), is("abcde"));
	}

	@Test
	public void shouldChangeRealPasswordIfPlaceholderIsSetAndPreviouslyNull() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>(null, String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);

		field.setValue("xxxxxxxx");

		assertThat((String) realDataSource.getValue(), is("xxxxxxxx"));
	}

	@Test
	public void shouldNotChangeRealValueInWriteThroughMode() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);
		field.setBuffered(true);

		field.setValue("ecdba");

		assertThat((String) realDataSource.getValue(), is("abcde"));
	}

	@Test
	public void shouldChangeRealValueOnlyOnCommitInWriteThroughMode() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);
		field.setBuffered(true);

		field.setValue("ecdba");
		field.commit();

		assertThat((String) realDataSource.getValue(), is("ecdba"));
	}

	@Test
	public void shouldRevertToOldValueInWriteThroughMode() {
		ObjectProperty<String> realDataSource = new ObjectProperty<String>("abcde",
				String.class);
		SecurePasswordField field = new SecurePasswordField("Test",
				realDataSource);
		field.setBuffered(true);

		field.setValue("ecdba");
		field.discard();
		field.commit();

		assertThat((String) realDataSource.getValue(), is("abcde"));
	}

    @Test
    public void shouldSupportConverterForSettingValue() {
        when(contextMock.getProvider().getLocale()).thenReturn(Locale.GERMANY);
        ObjectProperty<String> realDataSource = new ObjectProperty<String>("encryptedValue",
                String.class);
        SecurePasswordField field = new SecurePasswordField("Test",
                realDataSource);
        field.setConverter(converterMock);
        field.setBuffered(true);

        when(converterMock.convertToPresentation("encryptedValue", String.class, Locale.GERMANY)).thenReturn("plainTextValue");
        when(converterMock.convertToModel("plainTextValue", String.class, Locale.GERMANY)).thenReturn("encryptedValue");

        field.setValue("plainTextValue");
        field.commit();

        assertThat((String) realDataSource.getValue(), is("encryptedValue"));

        field.setValue("ddd");
        field.discard();
        assertThat((String) field.getValue(), is("xxxxxxxx"));

    }


    @Test
    public void shouldSupportConverterEmptyValue() {
        when(contextMock.getProvider().getLocale()).thenReturn(Locale.GERMANY);
        ObjectProperty<String> realDataSource = new ObjectProperty<String>(null,
                String.class);
        SecurePasswordField field = new SecurePasswordField("Test",
                realDataSource);
        field.setConverter(converterMock);
        field.setBuffered(true);

        when(converterMock.convertToPresentation("encryptedValue", String.class, Locale.GERMANY)).thenReturn("plainTextValue");
        when(converterMock.convertToModel("plainTextValue", String.class, Locale.GERMANY)).thenReturn("encryptedValue");

        field.setValue("plainTextValue");
        field.commit();

        assertThat((String) realDataSource.getValue(), is("encryptedValue"));

        field.setValue("ddd");
        field.discard();
        assertThat((String) field.getValue(), is("xxxxxxxx"));

    }
}
