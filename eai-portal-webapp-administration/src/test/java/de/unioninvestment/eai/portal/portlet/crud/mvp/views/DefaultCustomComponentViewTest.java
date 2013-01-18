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
package de.unioninvestment.eai.portal.portlet.crud.mvp.views;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.vaadin.terminal.Sizeable;

public class DefaultCustomComponentViewTest {

	@Test
	public void shouldHaveWidth100PercentAnHeightUndefined() {
		DefaultCustomComponentView view = new DefaultCustomComponentView(null,
				null);
		assertThat(view.getWidth(), is(100f));
		assertThat(view.getWidthUnits(), is(Sizeable.UNITS_PERCENTAGE));
		assertThat(view.getHeight(), is(Sizeable.SIZE_UNDEFINED));
	}
}
