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

import static org.mockito.Mockito.verify;
import groovy.lang.Closure;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.unioninvestment.eai.portal.portlet.crud.domain.events.TabChangeEvent;
import de.unioninvestment.eai.portal.portlet.crud.domain.events.TabChangeEventHandler;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Tabs;


public class ScriptTabsTest {
	@Mock
	private Tabs tabsMock;
	
	private ScriptTabs stabs;

	@Mock
	private Closure<?> onChangeMock;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		stabs = new ScriptTabs(tabsMock);
	}
	
	@Test
	public void shouldFireChangeEvent() {
		ArgumentCaptor<TabChangeEventHandler> changeEventCaptor = ArgumentCaptor.forClass(TabChangeEventHandler.class);
		verify(tabsMock).addTabChangeEventListener(changeEventCaptor.capture());
		
		stabs.setOnChange(onChangeMock);
		
		TabChangeEvent changeEvent = new TabChangeEvent(tabsMock);
		changeEventCaptor.getValue().onTabChange(changeEvent);
		
		verify(onChangeMock).call(stabs);
	}
}
