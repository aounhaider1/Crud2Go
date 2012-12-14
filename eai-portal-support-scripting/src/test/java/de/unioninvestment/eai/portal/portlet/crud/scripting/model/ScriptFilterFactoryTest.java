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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import groovy.lang.GString;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.All;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Any;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Contains;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.EndsWith;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Equal;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Filter;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Greater;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.Less;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.SQLFilter;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.StartsWith;
import de.unioninvestment.eai.portal.support.scripting.ScriptBuilder;
import de.unioninvestment.eai.portal.support.scripting.ScriptCompiler;

public class ScriptFilterFactoryTest {

	private ScriptFilterFactory fac;

	private List<Filter> filters;

	private ScriptBuilder scriptBuilder;

	private Map<String, Object> durableNamedArgument = new HashMap<String, Object>();

	@Mock
	private DataContainer databaseContainerMock;

	@Captor
	private ArgumentCaptor<Filter> filterCaptor;

	@Mock
	private GString gStringMock;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		filters = new ArrayList<Filter>();
		fac = new ScriptFilterFactory(filters);

		durableNamedArgument.put("durable", true);
	}

	/*************/

	@Test
	public void shouldBuildEqualFilter() {
		fac.equal("a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Equal) filters.get(0), is(new Equal("a", "b")));
	}

	@Test
	public void shouldBuildEqualFilter2() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("durable", true);

		fac.equal(map, "a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Equal) filters.get(0), is(new Equal("a", "b", true)));
	}

	/*************/

	@Test
	public void shouldBuildGreaterFilter() {
		fac.greater("a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Greater) filters.get(0), is(new Greater("a", "b", false)));
	}

	@Test
	public void shouldBuildGreaterFilter2() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("durable", true);

		fac.greater(map, "a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Greater) filters.get(0), is(new Greater("a", "b", false,
				true)));
	}

	/*************/

	@Test
	public void shouldBuildGreaterOrEqualFilter() {
		fac.greaterOrEqual("a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Greater) filters.get(0), is(new Greater("a", "b", true)));
	}

	@Test
	public void shouldBuildGreaterOrEqualFilter2() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("durable", true);

		fac.greaterOrEqual(map, "a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Greater) filters.get(0), is(new Greater("a", "b", true,
				true)));
	}

	/*************/

	@Test
	public void shouldBuildLessFilter() {
		fac.less("a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Less) filters.get(0), is(new Less("a", "b", false, false)));
	}

	@Test
	public void shouldBuildLessFilter2() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("durable", true);

		fac.less(map, "a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Less) filters.get(0), is(new Less("a", "b", false, true)));
	}

	/*************/

	@Test
	public void shouldBuildLessOrEqualFilter() {
		fac.lessOrEqual("a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Less) filters.get(0), is(new Less("a", "b", true)));
	}

	@Test
	public void shouldBuildOrEqualFilter2() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("durable", true);

		fac.lessOrEqual(map, "a", "b");

		assertThat(filters.size(), is(1));
		assertThat((Less) filters.get(0), is(new Less("a", "b", true, true)));
	}

	/*************/

	@Test
	public void shouldbuildDurableAnyFilter() throws InstantiationException,
			IllegalAccessException {
		ScriptCompiler compiler = new ScriptCompiler();
		ScriptContainer container = new ScriptSqlContainer(
				databaseContainerMock);

		Class<Script> compileScript = compiler
				.compileScript("container.addFilters { any durable:true, { equal \"myCol1\", \"myVal1\" } }");
		Script instance = compileScript.newInstance();
		instance.getBinding().setVariable("container", container);
		instance.run();

		verify(databaseContainerMock).addFilters(
				asList((Filter) new Any(asList((Filter) new Equal("myCol1",
						"myVal1")), true)));
	}

	@Test
	public void shouldbuildStartsWithFilter() {
		fac.startsWith("myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(StartsWith.class));

		StartsWith filter = (StartsWith) filters.get(0);
		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
	}

	@Test
	public void shouldbuildDurableStartsWithFilter() {
		fac.startsWith(durableNamedArgument, "myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(StartsWith.class));

		StartsWith filter = (StartsWith) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
		assertThat(filter.isDurable(), is(true));
	}

	@Test
	public void shouldbuildEndsWithFilter() {
		fac.endsWith("myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(EndsWith.class));

		EndsWith filter = (EndsWith) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
	}

	@Test
	public void shouldbuildDurableEndsWithFilter() {
		fac.endsWith(durableNamedArgument, "myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(EndsWith.class));

		EndsWith filter = (EndsWith) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
		assertThat(filter.isDurable(), is(true));
	}

	@Test
	public void shouldbuildContainsFilter() {
		fac.contains("myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(Contains.class));

		Contains filter = (Contains) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
	}

	@Test
	public void shouldbuildSqlFilterForString() {

		fac.where("myCol1", "in ('test')");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(SQLFilter.class));

		SQLFilter filter = (SQLFilter) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat(filter.getWhereString(), is("in ('test')"));
		assertThat(filter.getValues(), is(emptyList()));
	}

	@Test
	public void shouldbuildDurableSqlFilterForString() {

		fac.where(durableNamedArgument, "myCol1", "in ('test')");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(SQLFilter.class));

		SQLFilter filter = (SQLFilter) filters.get(0);

		assertThat(filter.isDurable(), is(true));
	}

	@Test
	public void shouldbuildSqlFilterForGString() {

		// gString: "in ($x)", x=2
		when(gStringMock.getStrings()).thenReturn(new String[] { "in (", ")" });
		when(gStringMock.getValues()).thenReturn(new Object[] { 2 });
		fac.where("myCol1", gStringMock);

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(SQLFilter.class));

		SQLFilter filter = (SQLFilter) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat(filter.getWhereString(), is("in (?)"));
		assertThat(filter.getValues(), is(asList((Object) 2)));
	}

	@Test
	public void shouldBuildDurableSqlFilter() {

		// gString: "in ($x)", x=2
		when(gStringMock.getStrings()).thenReturn(new String[] { "in (", ")" });
		when(gStringMock.getValues()).thenReturn(new Object[] { 2 });
		fac.where(durableNamedArgument, "myCol1", gStringMock);

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(SQLFilter.class));

		SQLFilter filter = (SQLFilter) filters.get(0);

		assertThat(filter.isDurable(), is(true));
	}

	@Test
	public void shouldbuildDurableContainsFilter() {
		fac.contains(durableNamedArgument, "myCol1", "myVal1");

		assertThat(filters.size(), is(1));
		assertThat(filters.get(0), new IsInstanceOf(Contains.class));

		Contains filter = (Contains) filters.get(0);

		assertThat(filter.getColumn(), is("myCol1"));
		assertThat((String) filter.getValue(), is("myVal1"));
	}

	@Test
	public void shouldbuildAllFilter() throws InstantiationException,
			IllegalAccessException {
		runScript("container.addFilters { all { equal \"myCol1\", \"myVal1\" } }");

		verify(databaseContainerMock).addFilters(
				asList((Filter) new All(asList((Filter) new Equal("myCol1",
						"myVal1")))));
	}

	@Test
	public void shouldbuildDurableAllFilter() throws InstantiationException,
			IllegalAccessException {
		runScript("container.addFilters { all durable:true, { equal \"myCol1\", \"myVal1\" } }");

		verify(databaseContainerMock).addFilters(
				asList((Filter) new All(asList((Filter) new Equal("myCol1",
						"myVal1")), true)));
	}

	@Test
	public void shouldBuildNothingFilter() {
		fac.nothing();
		assertThat(filters.size(), is(1));
		assertThat(filters.iterator().next().isDurable(), is(false));
	}

	@Test
	public void shouldBuildDurableNothingFilter() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("durable", true);
		fac.nothing(args);
		assertThat(filters.size(), is(1));
		assertThat(filters.iterator().next().isDurable(), is(true));
	}

	private void runScript(String script) throws InstantiationException,
			IllegalAccessException {
		ScriptCompiler compiler = new ScriptCompiler();
		ScriptContainer container = new ScriptSqlContainer(
				databaseContainerMock);
		Class<Script> compileScript = compiler.compileScript(script);
		Script instance = compileScript.newInstance();
		instance.getBinding().setVariable("container", container);
		instance.run();
	}
}