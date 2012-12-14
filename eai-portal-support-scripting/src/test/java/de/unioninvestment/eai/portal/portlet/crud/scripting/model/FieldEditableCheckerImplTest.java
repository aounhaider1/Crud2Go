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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;

public class FieldEditableCheckerImplTest {
	@Mock
	private Table tableMock;

	@Mock
	private Closure<Boolean> closureMock;

	@Mock
	private ContainerRow rowMock;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCallClosureWithArguments() {
		// given
		boolean expectedResult = true;
		Mockito.when(closureMock.call(Mockito.any(Table.class), Mockito.any(String.class), Mockito.any(ScriptRow.class))).thenReturn(expectedResult);

		// when
		String columnName = "test-column-name";
		FieldEditableCheckerImpl editableChecker = new FieldEditableCheckerImpl(tableMock, columnName, closureMock);
		boolean result = editableChecker.isEditable(rowMock);
		
		// then
		Assert.assertEquals(expectedResult, result);
	}
}