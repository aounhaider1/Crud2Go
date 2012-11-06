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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import de.unioninvestment.eai.portal.portlet.crud.config.FormFieldConfig;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.QueryOptionList;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.OptionListFormField;

public class OptionListFormFieldTest {

	private OptionListFormField fieldSelectionFormField;

	@Test
	public void shoudReturnSelection(){
		FormFieldConfig configMock=mock(FormFieldConfig.class);
		OptionList selectionMock=mock(QueryOptionList.class);
		
		fieldSelectionFormField = new OptionListFormField(configMock,
				selectionMock);

		assertThat(fieldSelectionFormField.getOptionList(), is(selectionMock));
	}

}
