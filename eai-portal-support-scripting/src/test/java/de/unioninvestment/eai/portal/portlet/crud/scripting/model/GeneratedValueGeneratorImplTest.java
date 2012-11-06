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
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRowId;

public class GeneratedValueGeneratorImplTest {

	@Mock
	private Closure<Object> closureMock;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldRunClosureWithArguments() {
		// given
		ContainerRowId containerRowIdMock = Mockito.mock(ContainerRowId.class);

		ContainerRow rowMock = Mockito.mock(ContainerRow.class);
		Mockito.when(rowMock.getId()).thenReturn(containerRowIdMock);

		Mockito.when(closureMock.call(Mockito.any(ScriptRow.class)))
				.thenReturn("1234");

		// when
		GeneratedValueGeneratorImpl columnGenerator = new GeneratedValueGeneratorImpl(
				closureMock);
		Object result = columnGenerator.getValue(rowMock);

		// then
		Assert.assertNotNull(result);
		Assert.assertEquals("1234", result);
	}

}
