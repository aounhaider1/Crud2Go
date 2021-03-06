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

import com.vaadin.ui.Button;
import de.unioninvestment.eai.portal.portlet.crud.config.*;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.BusinessException;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelSupport;
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptPortlet;
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptRow;
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptTab;
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptTabs;
import de.unioninvestment.eai.portal.support.vaadin.groovy.VaadinBuilder;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.*;

@ContextConfiguration({ "/eai-portal-web-test-applicationcontext.xml" })
public class ConfigurationScriptsCompilerTest extends ModelSupport {
	private ScriptCompiler scriptCompilerMock = mock(ScriptCompiler.class);

	private ConfigurationScriptsCompiler compiler;

	@Before
	public void setUp() {
		compiler = new ConfigurationScriptsCompiler(scriptCompilerMock);
	}

	@Test
	public void shouldCompileExistingScript() throws JAXBException,
			SAXException, ScriptingException {

		PortletConfig portletConfig = createConfiguration("validScriptConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock)
				.compileScript(
						"log.info \"Hello World\"\nlog.debug 'MainPortletScript.groovy execution finished...'",
						"MainPortletScript.groovy");
	}

	@Test
	public void shouldCompileScriptsWithSrcAttribute() throws JAXBException,
			SAXException, ScriptingException {

		PortletConfig portletConfig = createConfiguration("validMultiScriptConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock).compileScript(
				contains("log.info \"Hello Shared\""), eq("shared.groovy"));
		verify(scriptCompilerMock).compileScript(
				contains("log.info \"Hello Main\""),
				eq("MainPortletScript.groovy"));
	}

	@Test
	public void shouldCompileEmptyScriptIfNothingConfigured()
			throws JAXBException, SAXException, ScriptingException {

		PortletConfig portletConfig = createConfiguration("validConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock).compileScript("");
	}

	@Test
	public void shouldCompileClosureScripts() throws JAXBException,
			SAXException, ScriptingException {
		PortletConfig portletConfig = createConfiguration("validTableActionConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock)
				.compileScript(
						"def aufruf() { println \"aufruf erfolgreich\" }\nlog.debug 'MainPortletScript.groovy execution finished...'",
						"MainPortletScript.groovy");
		verify(scriptCompilerMock).compileScript("{ it -> aufruf() }");
		// assertThat(config.getScript().getValue().getClazz(), notNullValue());
	}

	@Test
	public void shouldCompileAllClosureScripts() throws JAXBException,
			SAXException, ScriptingException, InstantiationException,
			IllegalAccessException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validAllClosuresConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		assertThat(portletConfig.getOnRefresh().getClazz(), notNullValue());
		assertThat(portletConfig.getOnLoad().getClazz(), notNullValue());
		assertThat(portletConfig.getOnReload().getClazz(), notNullValue());
		assertThat(getTabs(portletConfig).getOnChange().getClazz(),
				notNullValue());
		assertThat(getTab(portletConfig).getOnShow().getClazz(), notNullValue());
		assertThat(getTab(portletConfig).getOnHide().getClazz(), notNullValue());

		assertThat(getTable(portletConfig).getOnModeChange().getClazz(),
				notNullValue());
		assertThat(getTable(portletConfig).getOnSelectionChange().getClazz(),
				notNullValue());
		assertThat(getTable(portletConfig).getOnRowChange().getClazz(),
				notNullValue());
		assertThat(getTable(portletConfig).getRowEditable().getClazz(),
				notNullValue());
		assertThat(getTable(portletConfig).getRowDeletable().getClazz(),
				notNullValue());
		assertThat(getTable(portletConfig).getRowValidator().getClazz(),
				notNullValue());

		assertThat(getTable(portletConfig).getColumns().getColumn().get(2)
				.getDefault().getClazz(), notNullValue());
		assertThat(getTable(portletConfig).getColumns().getColumn().get(1)
				.getValidator().getClazz(), notNullValue());
		assertThat(getTable(portletConfig).getColumns().getColumn().get(9)
				.getGenerator().getClazz(), notNullValue());
		assertThat(getTable(portletConfig).getColumns().getColumn().get(9)
				.getGeneratedValue().getClazz(), notNullValue());

		assertThat(getTableAction(portletConfig).getOnExecution().getClazz(),
				notNullValue());
		assertThat(getTableActions(portletConfig).get(1).getExport()
				.getFilename().getClazz(), notNullValue());
		assertThat(getTableActions(portletConfig).get(2).getDownload()
				.getGenerator().getClazz(), notNullValue());

		assertThat(getFormAction(portletConfig).getOnExecution().getClazz(),
				notNullValue());
		assertThat(getFormField(portletConfig).getOnValueChange().getClazz(),
				notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getOnCreate().getClazz(),
				notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getOnDelete().getClazz(),
				notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getOnInsert().getClazz(),
				notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getOnUpdate().getClazz(),
				notNullValue());

		assertThat(getDatabaseQuery(portletConfig).getInsert().getStatement()
				.getClazz(), notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getUpdate().getStatement()
				.getClazz(), notNullValue());
		assertThat(getDatabaseQuery(portletConfig).getDelete().getStatement()
				.getClazz(), notNullValue());

		assertThat(getComponentInCompoundSearch(portletConfig).getGenerator()
				.getClazz(), notNullValue());

		// TODO add missing closures

		assertScriptExecution(portletConfig);
	}

	private ScriptComponentConfig getComponentInCompoundSearch(
			PortletConfig portletConfig) {
		CompoundSearchConfig search = (CompoundSearchConfig) portletConfig
				.getPage().getElements().get(0);
		return (ScriptComponentConfig) search.getDetails().getElements().get(0);
	}

	@Test
	public void shouldCompileClosureScriptDynamicDropDown()
			throws JAXBException, SAXException, ScriptingException,
			InstantiationException, IllegalAccessException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validDynamicDropDownConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		assertThat(getDynamicSelect(portletConfig).getDynamic().getOptions()
				.getClazz(), notNullValue());

		Script script = portletConfig.getScript().get(0).getValue().getClazz()
				.newInstance();
		script.getBinding()
				.setVariable(
						"log",
						LoggerFactory
								.getLogger(ConfigurationScriptsCompilerTest.class));
		script.run();

		Script dynamicClosureScript = getDynamicSelect(portletConfig)
				.getDynamic().getOptions().getClazz().newInstance();
		@SuppressWarnings("unchecked")
		Closure<List<Integer>> dynamicClosure = (Closure<List<Integer>>) dynamicClosureScript
				.run();
		dynamicClosure.setDelegate(script);

		ScriptRow scriptRowMock = mock(ScriptRow.class);
		java.util.List<Integer> ret = dynamicClosure.call(scriptRowMock, null);

		assertThat(ret.get(1), is(Integer.class));
		verify(scriptRowMock).getValues();
	}

	@Test
	public void shouldCompileComponentGeneratorScript() throws JAXBException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validCustomComponentConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		ScriptComponentConfig scriptComponentConfig = (ScriptComponentConfig) portletConfig
				.getPage().getElements().get(0);

		assertThat(scriptComponentConfig.getGenerator().getClazz(),
				notNullValue());
	}

	@Test
	public void shouldCompileCustomFilterScript() throws JAXBException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validScriptContainerConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		FormConfig formConfig = (FormConfig) portletConfig.getPage()
				.getElements().get(0);
		FormActionConfig actionConfig = formConfig.getAction().get(0);
		List<FilterConfig> filters = actionConfig.getSearch().getApplyFilters()
				.getFilters();
		List<FilterConfig> nestedFilters = ((AnyFilterConfig) filters.get(0))
				.getFilters();
		CustomFilterConfig customFilterConfig = (CustomFilterConfig) nestedFilters
				.get(nestedFilters.size() - 1);

		assertThat(customFilterConfig.getFilter().getClazz(), notNullValue());
	}

	@Test
	public void shouldCompileCustomFilterScriptInSubfilters()
			throws JAXBException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validScriptContainerConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		FormConfig formConfig = (FormConfig) portletConfig.getPage()
				.getElements().get(0);
		FormActionConfig actionConfig = formConfig.getAction().get(0);
		List<FilterConfig> filters = actionConfig.getSearch().getApplyFilters()
				.getFilters();
		AnyFilterConfig anyFilterConfig = (AnyFilterConfig) filters.get(0);
		CustomFilterConfig customFilterConfig = (CustomFilterConfig) anyFilterConfig
				.getFilters().get(anyFilterConfig.getFilters().size() - 1);

		assertThat(customFilterConfig.getFilter().getClazz(), notNullValue());
	}

	@Test
	public void shouldReturnScriptComponentButton() throws JAXBException,
			InstantiationException, IllegalAccessException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validCustomComponentConfig.xml");
		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		ScriptComponentConfig scriptComponentConfig = (ScriptComponentConfig) portletConfig
				.getPage().getElements().get(0);
		Script generatorScript = scriptComponentConfig.getGenerator()
				.getClazz().newInstance();

		Closure<?> generatorClosure = (Closure<?>) generatorScript.run();

		Script mainScript = portletConfig.getScript().get(0).getValue()
				.getClazz().newInstance();
		generatorClosure.setDelegate(mainScript);

		Object result = generatorClosure.call(new VaadinBuilder());

		assertThat(result, new InstanceOf(Button.class));
	}

	@Test
	public void shouldCompileTableStyleClosures() throws JAXBException,
			InstantiationException, IllegalAccessException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validTableStyleConfig.xml");

		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		Script mainScript = portletConfig.getScript().get(0).getValue()
				.getClazz().newInstance();

		TableConfig tableConfig = (TableConfig) portletConfig.getPage()
				.getElements().get(0);
		assertThat(tableConfig.getRowStyle().getClazz(), notNullValue());

		Script rowScript = tableConfig.getRowStyle().getClazz().newInstance();
		@SuppressWarnings("unchecked")
		Closure<String> rowClosure = (Closure<String>) rowScript.run();
		rowClosure.setDelegate(mainScript);

		ScriptRow scriptRowMock = mock(ScriptRow.class);
		String rowResult = rowClosure.call(scriptRowMock);

		assertThat(rowResult, is("rowStyle"));

		assertThat(tableConfig.getColumns().getColumn().size(), is(1));

		int j = 0;
		for (ColumnConfig columnConfig : tableConfig.getColumns().getColumn()) {
			assertThat(columnConfig.getStyle().getClazz(), notNullValue());

			Script columnScript = columnConfig.getStyle().getClazz()
					.newInstance();
			@SuppressWarnings("unchecked")
			Closure<String> columnClosure = (Closure<String>) columnScript
					.run();
			columnClosure.setDelegate(mainScript);

			String columnResult = columnClosure.call(scriptRowMock,
					"columnName" + j);

			assertThat(columnResult, is("style-columnName" + j));
			j++;
		}

	}

	@Test
	public void shouldShowScriptPositionOnScriptErrors() throws JAXBException,
			SAXException, ScriptingException {

		PortletConfig portletConfig = createConfiguration("validScriptConfig.xml");
		when(
				scriptCompilerMock
						.compileScript(
								"log.info \"Hello World\"\nlog.debug 'MainPortletScript.groovy execution finished...'",
								"MainPortletScript.groovy")).thenThrow(
				new ScriptingException(null,
						"portlet.crud.error.compilingScript"));

		try {
			compiler.compileAllScripts(portletConfig);
			fail();
		} catch (BusinessException e) {
			assertThat(e.getCode(), is("portlet.crud.error.compilingScript"));
		}
	}

	@Test
	public void shouldCompileGStringDefaultColumnValue() throws JAXBException,
			InstantiationException, IllegalAccessException {
		ScriptCompiler scriptCompiler = new ScriptCompiler();
		PortletConfig portletConfig = createConfiguration("validDefaultColumnValueConfig.xml");

		new ConfigurationScriptsCompiler(scriptCompiler)
				.compileAllScripts(portletConfig);

		Script mainScript = portletConfig.getScript().get(0).getValue()
				.getClazz().newInstance();

		TableConfig tableConfig = (TableConfig) portletConfig.getPage()
				.getElements().get(0);

		assertThat(tableConfig.getColumns().getColumn().get(2).getDefault()
				.getClazz(), notNullValue());

		Script defaulValueScript = tableConfig.getColumns().getColumn().get(2)
				.getDefault().getClazz().newInstance();
		Closure<?> defaultValueClosure = (Closure<?>) defaulValueScript.run();
		defaultValueClosure.setDelegate(mainScript);

		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String datumString = df.format(dt);
		Object object = defaultValueClosure.call(datumString);

		assertThat(object.toString(), is(datumString));
	}

	@Test
	public void shouldCompileStatementClosure() throws JAXBException,
			InstantiationException, IllegalAccessException {

		PortletConfig portletConfig = createConfiguration("validScriptCrudConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock).compileScript(contains("def doInsert(row)"),
				eq("MainPortletScript.groovy"));
		verify(scriptCompilerMock).compileScript(contains("doInsert(row) //"));
		verify(scriptCompilerMock).compileScript(contains("doUpdate(row) //"));
		verify(scriptCompilerMock).compileScript(contains("doDelete(row) //"));
	}

	@Test
	public void shouldCompileScriptContainerDelegate() throws JAXBException {
		PortletConfig portletConfig = createConfiguration("validScriptContainerConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock, times(3)).compileScript(anyString());
	}

	@Test
	public void shouldCompileReSTContainerScripts() throws JAXBException {
		PortletConfig portletConfig = createConfiguration("validReSTContainerFullXmlConfig.xml");
		when(scriptCompilerMock.compileScript(anyString())).thenReturn(
				Script.class);

		compiler.compileAllScripts(portletConfig);

		TableConfig tableConfig = (TableConfig) portletConfig.getPage()
				.getElements().get(0);
		ReSTContainerConfig container = tableConfig.getRestContainer();
		List<ReSTAttributeConfig> attributes = container.getQuery()
				.getAttribute();
		assertScriptCompiled(container.getQuery().getCollection());
		assertScriptCompiled(attributes.get(0).getPath());
		assertScriptCompiled(attributes.get(1).getPath());
		assertScriptCompiled(attributes.get(2).getPath());
		assertScriptCompiled(container.getInsert().getValue());
		assertScriptCompiled(container.getInsert().getUrl());
		assertScriptCompiled(container.getUpdate().getValue());
		assertScriptCompiled(container.getUpdate().getUrl());
		assertScriptCompiled(container.getDelete().getUrl());
	}

	private void assertScriptCompiled(GroovyScript collection) {
		assertThat(collection.getClazz(), notNullValue());
	}

	@Test
	public void shouldCompileSqlStatementGString() throws JAXBException,
			InstantiationException, IllegalAccessException {

		PortletConfig portletConfig = createConfiguration("validQueryConfig.xml");

		compiler.compileAllScripts(portletConfig);

		verify(scriptCompilerMock).compileScript("");
		verify(scriptCompilerMock, times(3)).compileScript(
				startsWith("{ container,row,connection -> \"\"\""));
	}

	@Test
	public void shouldCompileSimpleCompoundSearch() throws JAXBException,
	InstantiationException, IllegalAccessException {
		
		PortletConfig portletConfig = createConfiguration("validCompoundSearchSimpleConfig.xml");
		
		compiler.compileAllScripts(portletConfig);
	}
	
	private SelectConfig getDynamicSelect(PortletConfig portletConfig) {
		ColumnsConfig columns = ((TableConfig) portletConfig.getPage()
				.getElements().get(0)).getColumns();
		for (ColumnConfig cfg : columns.getColumn()) {
			if (cfg.getSelect() != null && cfg.getSelect().getDynamic() != null) {
				return cfg.getSelect();
			}
		}
		return null;
	}

	private void assertScriptExecution(PortletConfig portletConfig)
			throws InstantiationException, IllegalAccessException {
		// mocking
		ScriptPortlet portletMock = mock(ScriptPortlet.class);
		ScriptTabs tabsMock = mock(ScriptTabs.class);
		ScriptTab tabMock = mock(ScriptTab.class);
		ScriptTab tabMock2 = mock(ScriptTab.class);
		Map<String, ScriptTab> elements = new LinkedHashMap<String, ScriptTab>();
		elements.put("tab1", tabMock);
		elements.put("tab2", tabMock2);
		when(portletMock.getTabs()).thenReturn(tabsMock);
		when(tabsMock.getElements()).thenReturn(elements);
		when(tabsMock.getActiveTab()).thenReturn(tabMock);
		when(tabMock.getId()).thenReturn("tab1");

		Script script = portletConfig.getScript().get(0).getValue().getClazz()
				.newInstance();
		script.getBinding().setVariable("portlet", portletMock);
		script.getBinding()
				.setVariable(
						"log",
						LoggerFactory
								.getLogger(ConfigurationScriptsCompilerTest.class));
		script.run();

		@SuppressWarnings("unchecked")
		Closure<ScriptTab> closure = ((Closure<ScriptTab>) getTabs(
				portletConfig).getOnChange().getClazz().newInstance().run());
		closure.setDelegate(script);
	}

	private TabsConfig getTabs(PortletConfig portletConfig) {
		return (TabsConfig) portletConfig.getPage().getElements().get(2);
	}

	private TabConfig getTab(PortletConfig portletConfig) {
		return getTabs(portletConfig).getTab().get(0);
	}

	private TableConfig getTable(PortletConfig portletConfig) {
		return ((TableConfig) getTab(portletConfig).getElements().get(0));
	}

	private FormConfig getForm(PortletConfig portletConfig) {
		return (FormConfig) portletConfig.getPage().getElements().get(1);
	}

	private FormActionConfig getFormAction(PortletConfig portletConfig) {
		return getForm(portletConfig).getAction().get(0);
	}

	private FormFieldConfig getFormField(PortletConfig portletConfig) {
		return getForm(portletConfig).getField().get(0);
	}

	private TableActionConfig getTableAction(PortletConfig portletConfig) {
		return getTableActions(portletConfig).get(0);
	}

	private List<TableActionConfig> getTableActions(PortletConfig portletConfig) {
		return getTable(portletConfig).getAction();
	}

	private DatabaseQueryConfig getDatabaseQuery(PortletConfig portletConfig) {
		return getTable(portletConfig).getDatabaseQuery();
	}
}
