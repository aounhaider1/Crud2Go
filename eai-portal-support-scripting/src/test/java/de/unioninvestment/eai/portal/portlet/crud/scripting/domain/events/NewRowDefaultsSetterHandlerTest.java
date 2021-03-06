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
package de.unioninvestment.eai.portal.portlet.crud.scripting.domain.events;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import groovy.lang.Closure;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.util.converter.Converter;

import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.CreateEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
import de.unioninvestment.eai.portal.support.vaadin.junit.LiferayContext;

public class NewRowDefaultsSetterHandlerTest {

	private NewRowDefaultsSetterHandler handler;
	private Map<String, Closure<?>> defaultValues;

	@Mock
	private Closure<String> gString1;

	private CreateEvent event;

	@Mock
	private DataContainer sourceMock;
	@Mock
	private ContainerRow rowMock;

	@Mock
	private EditorSupport editorSupportMock;

	@Mock
	private Converter<String, Timestamp> propertyFormatterMock;

	@Rule
	public LiferayContext vaadinContext = new LiferayContext();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		event = new CreateEvent(sourceMock, rowMock);

		defaultValues = new HashMap<String, Closure<?>>();
		defaultValues.put("column1", gString1);

		handler = new NewRowDefaultsSetterHandler(defaultValues);

		// when(gString1.toString()).thenReturn("2");
		when(gString1.call(any())).thenReturn("2");

		doReturn(Timestamp.class).when(sourceMock).getType(anyString());

		when(sourceMock.findEditor(anyString())).thenReturn(editorSupportMock);
		doReturn(propertyFormatterMock).when(editorSupportMock).createFormatter(Timestamp.class, null);
		when(
				propertyFormatterMock.convertToModel("2", Timestamp.class,
						Locale.GERMANY)).thenReturn(
				new Timestamp(new GregorianCalendar(2011, 0, 1)
						.getTimeInMillis()));
	}

	@Test
	public void shouldSetDefaultValue() {
		handler.onCreate(event);

		// assertThat((String) rowMock.asMap().get("column1"), is("2"));
		verify(rowMock).setText("column1", "2");
	}

	@Test
	public void shouldNotSetDefaultOnEmptyValue() {
		when(gString1.call(any())).thenReturn("");

		handler.onCreate(event);

		// assertThat((String) rowMock.asMap().get("column1"), is("2"));
		verifyZeroInteractions(rowMock);
	}
}
