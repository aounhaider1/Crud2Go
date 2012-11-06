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
package de.unioninvestment.eai.portal.support.scripting;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class JMXProviderTest {
	private JMXProvider jmxProvider;

	@SuppressWarnings("rawtypes")
	private Map connectionArgs;

	@SuppressWarnings("rawtypes")
	@Before
	public void setUp() {
		jmxProvider = new JMXProvider(this);

		connectionArgs = new HashMap();
	}

	@Test
	public void shouldReturnJMXWrapperInstance() {
		ScriptJMXWrapper jmxWrapper = jmxProvider.doCall(connectionArgs);

		assertThat(jmxWrapper, is(ScriptJMXWrapper.class));
	}

	@Test
	public void shouldReturnJMXWrapperInstanceForURL() {
		ScriptJMXWrapper jmxWrapper = jmxProvider
				.doCall("service:jmx:rmi:///jndi/rmi://server:1090/jmxconnector");

		assertThat(jmxWrapper, is(ScriptJMXWrapper.class));
	}

	@Test
	public void shouldReturnJMXWrapperInstanceForServerAndPort() {
		ScriptJMXWrapper jmxWrapper = jmxProvider.doCall("server:1090");

		assertThat(jmxWrapper, is(ScriptJMXWrapper.class));
	}
}
