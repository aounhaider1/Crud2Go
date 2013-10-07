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
package de.unioninvestment.eai.portal.portlet.test.commons;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import de.unioninvestment.eai.portal.support.vaadin.context.BackgroundThreadContextProvider;
import de.unioninvestment.eai.portal.support.vaadin.context.Context;

@ContextConfiguration({ "/eai-portal-web-test-applicationcontext.xml" })
public abstract class SpringPortletContextTest extends
		AbstractJUnit4SpringContextTests {

	@Before
	public void configurePortletUtils() {
		Context.setProvider(new BackgroundThreadContextProvider(applicationContext, Locale.GERMANY));
	}

	@After
	public void resetAppCtx() {
		Context.setProvider(null);
	}
}
