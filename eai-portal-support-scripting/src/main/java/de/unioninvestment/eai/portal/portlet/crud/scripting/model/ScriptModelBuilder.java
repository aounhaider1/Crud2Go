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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import de.unioninvestment.eai.portal.portlet.crud.config.*;
import de.unioninvestment.eai.portal.portlet.crud.config.SelectConfig.Dynamic;
import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
import de.unioninvestment.eai.portal.portlet.crud.config.validation.ConfigurationException;
import de.unioninvestment.eai.portal.portlet.crud.domain.container.JmxDelegate;
import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPoolFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.BusinessException;
import de.unioninvestment.eai.portal.portlet.crud.domain.form.SearchFormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.*;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.authentication.Realm;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.CustomFilterFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.filter.SQLWhereFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.CurrentUser;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.UserFactory;
import de.unioninvestment.eai.portal.portlet.crud.domain.support.AuditLogger;
import de.unioninvestment.eai.portal.portlet.crud.domain.util.Util;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.ConfirmationDialogProvider;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.DynamicOptionList;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.NotificationProvider;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.container.database.QueryDeleteStatementGenerator;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.container.database.QueryInsertStatementGenerator;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.container.database.QueryUpdateStatementGenerator;
import de.unioninvestment.eai.portal.portlet.crud.scripting.domain.events.NewRowDefaultsSetterHandler;
import de.unioninvestment.eai.portal.support.scripting.*;
import de.unioninvestment.eai.portal.support.scripting.http.HttpProvider;
import de.unioninvestment.eai.portal.support.vaadin.container.GenericDelegate;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
import groovy.lang.Closure;
import groovy.lang.GString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse zur Erstellung der Scripting-Modell-Objektstruktur. Nicht für die
 * Nutzung aus Scripten bestimmt.
 * 
 * @author carsten.mjartan
 */
public class ScriptModelBuilder {

    private Logger LOGGER = LoggerFactory.getLogger(ScriptModelBuilder.class);

	private final ScriptModelFactory factory;
	private final ConnectionPoolFactory connectionPoolFactory;
	private final ScriptBuilder scriptBuilder;

	private final Map<Object, Object> configs;
	private final Portlet portlet;
	private final UserFactory userFactory;
	private ScriptPortlet scriptPortlet;

	private EventBus eventBus;
	private AuditLogger auditLogger;
    private ScriptCompiler scriptCompiler;

    // for testing
    private boolean runMainScript = true;

    enum Operation {
        INSERT("generateInsertStatement"),
        UPDATE("generateUpdateStatement"),
        DELETE("generateDeleteStatement");

        private final String methodName;

        private Operation(String methodName) {
            this.methodName = methodName;
        }
        public String getMethodName() {
            return methodName;
        }
    }

	/**
	 * Konstruktor mit Parameter.
	 * 
	 * @param factory
	 *            ScriptModel-Factory
	 * @param connectionPoolFactory
	 *            ConnectionPool-Factory
	 * @param userFactory
	 *            User-Factory
	 * @param scriptBuilder
	 *            ScriptBuilder
	 * @param portlet
	 *            Portletmodel
	 * @param modelToConfigMapping
	 *            Map mit Config
	 */
	public ScriptModelBuilder(ScriptModelFactory factory, EventBus eventBus,
			ConnectionPoolFactory connectionPoolFactory,
			UserFactory userFactory, ScriptCompiler scriptCompiler, ScriptBuilder scriptBuilder,
			Portlet portlet, Map<Object, Object> modelToConfigMapping) {
		this.factory = factory;
		this.eventBus = eventBus;
		this.connectionPoolFactory = connectionPoolFactory;
		this.userFactory = userFactory;
        this.scriptCompiler = scriptCompiler;
        this.scriptBuilder = scriptBuilder;
		this.portlet = portlet;
		this.configs = modelToConfigMapping;
	}

	/**
	 * Baut das Scriptportletmodel.
	 * 
	 * @return Scriptportletmodel
	 */
	public ScriptPortlet build() {
		CurrentUser currentUser = userFactory.getCurrentUser(portlet);
		auditLogger = new AuditLogger(currentUser);

		PortletConfig config = ((Config) configs.get(portlet))
				.getPortletConfig();
		scriptBuilder.registerMainScript(findMainScript(config));

		scriptPortlet = factory.getScriptPortlet(portlet);

		if (portlet.getPage() != null) {
			ScriptPage scriptPage = buildScriptPage(portlet.getPage());
			scriptPortlet.setPage(scriptPage);
		} else if (portlet.getTabs() != null) {
			ScriptTabs scriptTabs = buildScriptTabs(portlet.getTabs());
			scriptPortlet.setTabs(scriptTabs);
		} else {
			throw new UnsupportedOperationException(
					"Expected at least tabs or page in portlet");
		}

		buildScriptDialogs();

		scriptPortlet.setOnLoad(scriptBuilder.buildClosure(config
				.getOnLoad()));
		scriptPortlet.setOnReload(scriptBuilder.buildClosure(config
				.getOnReload()));
		scriptPortlet.setOnRefresh(scriptBuilder.buildClosure(config
				.getOnRefresh()));

		scriptBuilder.addBindingVariable("portal", factory.getScriptPortal());
		scriptBuilder.addBindingVariable("portlet", scriptPortlet);
		scriptBuilder.addBindingVariable("sql",
				new SqlProvider(scriptBuilder.getMainScript(),
						connectionPoolFactory));
		scriptBuilder.addBindingVariable("jmx",
				new JMXProvider(scriptBuilder.getMainScript()));
		scriptBuilder.addBindingVariable("http",
				new HttpProvider(scriptBuilder.getMainScript(), portlet));
		scriptBuilder.addBindingVariable("log",
				LoggerFactory.getLogger("portlet.crud.scripting.main"));

		ScriptUser scriptUser = factory.getScriptCurrentUser(currentUser);
		scriptBuilder.addBindingVariable("currentUser", scriptUser);

		scriptBuilder.addBindingVariable("showPopup", factory.getPopupProvider(
				scriptBuilder.getMainScript(), eventBus));
		scriptBuilder.addBindingVariable("confirm",
				new ConfirmationDialogProvider(scriptBuilder.getMainScript()));
		scriptBuilder.addBindingVariable("showWarning",
				new NotificationProvider(scriptBuilder.getMainScript(),
						NotificationProvider.Type.WARNING));
		scriptBuilder
				.addBindingVariable("showError", new NotificationProvider(
						scriptBuilder.getMainScript(),
						NotificationProvider.Type.ERROR));
		scriptBuilder.addBindingVariable("showInfo", new NotificationProvider(
				scriptBuilder.getMainScript(), NotificationProvider.Type.INFO));

		scriptBuilder.addBindingVariable("audit", new ScriptAuditLogger(
				scriptBuilder.getMainScript(), auditLogger));

		for (ScriptConfig script : findPropertyScripts(config).values()) {
			scriptBuilder.registerAndRunPropertyScript(script.getProperty(),
					script.getValue());
		}

        scriptBuilder.updateBindingsOfMainScript();
        if (runMainScript) {
		    scriptBuilder.runMainScript();
        }

		return scriptPortlet;
	}

	private ScriptConfig findMainScript(PortletConfig config) {
		ScriptConfig mainScript = null;
		for (ScriptConfig script : config.getScript()) {
			if (script.getProperty() == null) {
				if (mainScript == null) {
					mainScript = script;
				} else {
					throw new ConfigurationException(
							"Only one main script allowed");
				}
			}
		}
		return mainScript;
	}

	private Map<String, ScriptConfig> findPropertyScripts(PortletConfig config) {
		Map<String, ScriptConfig> propertyScripts = new HashMap<String, ScriptConfig>();
		for (ScriptConfig script : config.getScript()) {
			String property = script.getProperty();
			if (property != null) {
				if (propertyScripts.containsKey(property)) {
					throw new ConfigurationException(
							"Script property must be unique: " + property);
				} else {
					propertyScripts.put(property, script);
				}
			}
		}
		return propertyScripts;
	}

	/**
	 * Baut die Repräsentationen aller Dialog im Skript-Modell.
	 */
	private void buildScriptDialogs() {
		Collection<Dialog> dialogCollection = portlet.getDialogsById().values();
		for (Dialog dialog : dialogCollection) {
			ScriptDialog scriptDialog = new ScriptDialog(dialog);
			scriptPortlet.addDialog(scriptDialog);
			scriptPortlet.addElementById(dialog.getId(), scriptDialog);
			for (Component c : dialog.getElements()) {
				ScriptComponent scriptComponent = buildScriptComponent(c);
				scriptDialog.addElement(scriptComponent);
			}
		}
	}

	private ScriptTabs buildScriptTabs(Tabs tabs) {
		ScriptTabs scriptTabs = factory.getScriptTabs(tabs);

		TabsConfig tc = (TabsConfig) configs.get(tabs);
		Closure<?> onChange = scriptBuilder.buildClosure(tc.getOnChange());
		scriptTabs.setOnChange(onChange);

		for (Tab tab : tabs.getElements()) {
			ScriptTab scriptTab = buildScriptTab(tab);
			scriptTabs.addElement(scriptTab);
			if (tab.getId() != null) {
				scriptPortlet.addElementById(tab.getId(), scriptTab);
			}
		}
		return scriptTabs;
	}

	private ScriptTab buildScriptTab(Tab tab) {
		ScriptTab scriptTab = factory.getScriptTab(tab);
		populateTabClosures(tab, scriptTab);
		for (Component c : tab.getElements()) {
			ScriptComponent scriptComponent = buildScriptComponent(c);
			scriptTab.addElement(scriptComponent);
		}
		return scriptTab;
	}

	private void populateTabClosures(Tab tab, ScriptTab scriptTab) {
		TabConfig tc = (TabConfig) configs.get(tab);
		scriptTab.setOnShow(scriptBuilder.buildClosure(tc.getOnShow()));
		scriptTab.setOnHide(scriptBuilder.buildClosure(tc.getOnHide()));
	}

	private ScriptCompoundSearch buildScriptCompoundSearch(
			CompoundSearch compoundSearch) {
		ScriptCompoundSearch scriptCompoundSearch = factory
				.getScriptCompoundSearch(compoundSearch);
		scriptPortlet.addElementById(compoundSearch.getId(),
				scriptCompoundSearch);

		for (Component re : compoundSearch.getElements()) {
			scriptCompoundSearch.addElement(buildScriptComponent(re));
		}
		return scriptCompoundSearch;
	}

	private ScriptRegion buildScriptRegion(Region region) {
		ScriptRegion scriptRegion = factory.getScriptRegion(region);
		populateRegionClosures(region, scriptRegion);
		if (region.getId() != null) {
			scriptPortlet.addElementById(region.getId(), scriptRegion);
		}
		for (Component re : region.getElements()) {
			scriptRegion.addElement(buildScriptComponent(re));
		}
		return scriptRegion;
	}

	private void populateRegionClosures(Region region, ScriptRegion scriptRegion) {
		RegionConfig rc = (RegionConfig) configs.get(region);
		scriptRegion.setOnExpand(scriptBuilder.buildClosure(rc.getOnExpand()));
		scriptRegion.setOnCollapse(scriptBuilder.buildClosure(rc
				.getOnCollapse()));
	}

	private ScriptPage buildScriptPage(Page page) {
		ScriptPage scriptPage = factory.getScriptPage(page);
		for (Component c : page.getElements()) {
			ScriptComponent scriptComponent = buildScriptComponent(c);
			scriptPage.addElement(scriptComponent);
		}
		return scriptPage;
	}

	private ScriptComponent buildScriptComponent(Component component) {
		if (component instanceof Table) {
			return buildScriptTable((Table) component);
		} else if (component instanceof Form) {
			return buildScriptForm((Form) component);
		} else if (component instanceof Tabs) {
			return buildScriptTabs((Tabs) component);
		} else if (component instanceof CustomComponent) {
			return buildScriptCustomComponent((CustomComponent) component);
		} else if (component instanceof Region) {
			return buildScriptRegion((Region) component);
		} else if (component instanceof CompoundSearch) {
			return buildScriptCompoundSearch((CompoundSearch) component);
		} else if (component instanceof TextArea) {
			return buildScriptTextArea((TextArea) component);
		} else {
			throw new UnsupportedOperationException("Component of type '"
					+ component.getClass().getName() + "' not supported");
		}
	}

	private ScriptComponent buildScriptCustomComponent(CustomComponent component) {
		ScriptComponentConfig scc = (ScriptComponentConfig) configs
				.get(component);
		GroovyScript generatorScript = scc.getGenerator();
		Closure<Object> generatorClosure = scriptBuilder
				.buildClosure(generatorScript);
		component.setGenerator(new CustomComponentGeneratorImpl(
				generatorClosure));

		ScriptCustomComponent scriptCustomComponent = new ScriptCustomComponent(
				component);
		if (component.getId() != null) {
			scriptPortlet.addElementById(component.getId(),
					scriptCustomComponent);
		}

		return scriptCustomComponent;
	}

	private ScriptComponent buildScriptTextArea(TextArea component) {
		return factory.getScriptTextArea(component);
	}

	private ScriptForm buildScriptForm(Form form) {
		ScriptForm scriptForm = factory.getScriptForm(form);

		if (form.getId() != null) {
			scriptPortlet.addElementById(form.getId(), scriptForm);
		}

		for (FormField field : form.getFields()) {
			ScriptFormField scriptFormField = buildScriptFormField(field);
			scriptForm.addField(scriptFormField);
		}
		registerQueryOptionLists(form);
		for (FormAction formAction : form.getActions()) {
			ScriptFormAction scriptFormAction = buildScriptFormAction(formAction);

			if (formAction.getActionHandler() instanceof SearchFormAction) {
				SearchFormAction actionHandler = (SearchFormAction) formAction
						.getActionHandler();
				SQLWhereFactory whereFactory = new ScriptFormSQLWhereFactory(
						scriptBuilder.getMainScript(), scriptFormAction);
				actionHandler.setWhereFactory(whereFactory);

				registerCustomFilters(actionHandler);
			}

			populateFormActionExecutionClosure(formAction, scriptFormAction);
			scriptForm.addAction(scriptFormAction);
		}
		return scriptForm;
	}

	private void registerCustomFilters(SearchFormAction actionHandler) {
		CustomFilterFactory filterFactory = new ScriptCustomFilterFactory(
				scriptBuilder);
		actionHandler.setCustomFilterFactory(filterFactory);
	}

	private ScriptFormField buildScriptFormField(FormField formField) {
		ScriptFormField scriptFormField = factory.getFormField(formField);
		populateFormFieldChangeExecutionClosure(formField, scriptFormField);

		FormFieldConfig config = (FormFieldConfig) configs.get(formField);

		if (OptionListFormField.class.isAssignableFrom(formField.getClass())
				&& config.getSelect().getDynamic() != null) {
			populateDynamicSelectionsOnFormField(
					(OptionListFormField) formField, config.getSelect());
		}

		return scriptFormField;
	}

	private void populateDynamicSelectionsOnFormField(
			OptionListFormField formField, SelectConfig config) {
		Closure<?> dynamicSelectionClosure = scriptBuilder.buildClosure(config
				.getDynamic().getOptions());
		DynamicOptionList optionList = factory.getDynamicOptionList(
				dynamicSelectionClosure, config, eventBus);
		formField.setOptionList(optionList);
	}

	private ScriptFormAction buildScriptFormAction(FormAction action) {
		ScriptFormAction formAction = factory.getFormAction(action);
		scriptPortlet.addElementById(formAction.getId(), formAction);
		return formAction;
	}

	private ScriptTable buildScriptTable(Table table) {
		buildTableStyleRenderer(table);

		addNewRowDefaultsSetterHandler(table);

		populateDynamicSelectionsOnDomainTable(table);

		populateGeneratedColumnsClosures(table);

		ScriptTable scriptTable = factory.getScriptTable(table);

		ScriptContainer scriptContainer = buildScriptContainer(table
				.getContainer());
        if (table.getContainer() instanceof DatabaseQueryContainer) {
            prepareContainerForDynamicStatements(table, (ScriptDatabaseQueryContainer) scriptContainer);
        }

		scriptTable.setContainer(scriptContainer);

		populateQueryDelegateOnDomainContainer(table.getColumns(),
				table.getContainer(), scriptContainer);

		registerOptionLists(table);

		for (TableAction action : table.getActions()) {
			ScriptTableAction scriptTableAction = buildScriptTableAction(action);
			scriptTable.addAction(scriptTableAction);
		}

		populateTableOnEditClosure(table, scriptTable);
		populateOnSelectionChangeClosure(table, scriptTable);
		populateTableOnDoubleClickClosure(table, scriptTable);
		populateTableOnInitializeClosure(table, scriptTable);
		populateTableRowChangeClosure(table, scriptTable);
		populateTableRowValidator(table, scriptTable);

		populateDynamicEditableClosures(table);
		applyValidatorClosures(table, scriptTable);

		scriptPortlet.addElementById(table.getId(), scriptTable);

		return scriptTable;
	}

    private void prepareContainerForDynamicStatements(Table table, ScriptDatabaseQueryContainer scriptContainer) {
        DatabaseQueryContainer container = (DatabaseQueryContainer) table.getContainer();
        scriptContainer.setInsertGenerator(new QueryInsertStatementGenerator(table));
        scriptContainer.setUpdateGenerator(new QueryUpdateStatementGenerator(table));
        scriptContainer.setDeleteGenerator(new QueryDeleteStatementGenerator(table));
    }

    private void populateTableRowValidator(Table table, ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		if (tc.getRowValidator() != null) {
			Closure<Object> rowValidatorClosure = scriptBuilder.buildClosure(tc
					.getRowValidator());
			// TODO custom row-validator error message
			table.setRowValidator(new ScriptValidator(scriptTable, rowValidatorClosure, tc.getValidationMessage()));
		}
	}

	private void applyValidatorClosures(Table table, ScriptTable scriptTable) {
		if (table.getColumns() != null) {
			for (TableColumn column : table.getColumns()) {
				ColumnConfig columnConfig = (ColumnConfig) configs.get(column);
				if (columnConfig.getValidator() != null) {
					Closure<Object> closure = scriptBuilder
							.buildClosure(columnConfig.getValidator());
					column.addValidator(new ScriptFieldValidator(scriptTable, closure,
							columnConfig.getValidationMessage()));
				}
			}
		}
	}

	private void populateDynamicEditableClosures(Table table) {
		TableConfig tc = (TableConfig) configs.get(table);
		GroovyScript rowEditableScript = tc.getRowEditable();
		if (rowEditableScript != null) {
			Closure<Object> rowEditableClosure = scriptBuilder
					.buildClosure(rowEditableScript);
			table.setRowEditableChecker(new RowEditableCheckerImpl(table,
					rowEditableClosure));
		}
		GroovyScript rowDeletableScript = tc.getRowDeletable();
		if (rowDeletableScript != null) {
			Closure<Object> rowDeletableClosure = scriptBuilder
					.buildClosure(rowDeletableScript);
			table.setRowDeletableChecker(new RowDeletableCheckerImpl(table,
					rowDeletableClosure));
		}

		if (table.getColumns() != null) {
			for (TableColumn column : table.getColumns()) {
				ColumnConfig columnConfig = (ColumnConfig) configs.get(column);
				GroovyScript fieldEditableScript = columnConfig.getEditable();
				if (fieldEditableScript != null
						&& !Util.isPlainBoolean(fieldEditableScript)) {
					Closure<Object> fieldEditableClosure = scriptBuilder
							.buildClosure(fieldEditableScript);
					column.setEditableChecker(new FieldEditableCheckerImpl(
							table, column.getName(), fieldEditableClosure));
				}
			}
		}
	}

	/**
	 * Schreibt alle QueryOptionLists in das Portlet, um mit GetElementById
	 * schneller drauf zugreifen zu können.
	 * 
	 * @param table
	 *            Tabelle
	 */
	private void registerOptionLists(Table table) {
		if (table.getColumns() != null) {
			for (TableColumn column : table.getColumns()) {
				if (column instanceof SelectionTableColumn) {
					OptionList optionList = ((SelectionTableColumn) column)
							.getOptionList();
					if (optionList != null && optionList.getId() != null) {
						scriptPortlet.addElementById(optionList.getId(),
								new ScriptOptionList(optionList));
					}
				}

			}
		}
	}

	/**
	 * Schreibt alle QueryOptionLists in das Portlet, um mit GetElementById
	 * schneller drauf zugreifen zu können.
	 * 
	 * @param form
	 *            Formular
	 */
	private void registerQueryOptionLists(Form form) {
		if (form.getFields() != null) {
			for (FormField field : form.getFields()) {
				if (field instanceof OptionListFormField) {
					OptionList optionList = ((OptionListFormField) field)
							.getOptionList();

					scriptPortlet.addElementById(optionList.getId(),
							new ScriptOptionList(optionList));
				}
			}
		}
	}

	private void populateQueryDelegateOnDomainContainer(TableColumns columns,
            DataContainer container, ScriptContainer scriptContainer) {
		if (container instanceof DatabaseQueryContainer) {
			DatabaseQueryContainer databaseQueryContainer = (DatabaseQueryContainer) container;
			DatabaseQueryConfig config = (DatabaseQueryConfig) configs
					.get(container);

			StatementWrapper insertStatement = wrapStatement(config.getInsert(), Operation.INSERT);
			StatementWrapper updateStatement = wrapStatement(config.getUpdate(), Operation.UPDATE);
			StatementWrapper deleteStatement = wrapStatement(config.getDelete(), Operation.DELETE);

			CurrentUser currentUser = userFactory.getCurrentUser(portlet);

			ScriptDatabaseModificationsDelegate delegate = factory
					.getDatabaseQueryDelegate(databaseQueryContainer,
							config.getQuery(), insertStatement,
							updateStatement, deleteStatement, (ScriptDatabaseQueryContainer) scriptContainer,
							columns.getPrimaryKeyNames(), currentUser);

			databaseQueryContainer.setDatabaseQueryDelegate(delegate);
		}

	}

	private StatementWrapper wrapStatement(StatementConfig statement, Operation operation) {
		if (statement != null) {
            Closure<?> statementClosure;
            if (statement.getStatement() != null && !Strings.isNullOrEmpty(statement.getStatement().getSource())) {
                statementClosure = scriptBuilder.buildClosure(statement
                        .getStatement());
            } else {
                statementClosure = createGeneratedStatementClosure(statement, operation);
            }
			return new StatementWrapper(statementClosure, statement.getType());
		}
		return null;
	}

    private Closure<?> createGeneratedStatementClosure(StatementConfig statement, Operation operation) {
        String closureString = "{ container, row, connection -> " +
                "container." + operation.getMethodName() + "(row) }";
        Closure<?> stmtClosure;
        try {
            stmtClosure = (Closure<GString>) scriptCompiler
                    .compileScript(closureString).newInstance().run();
            stmtClosure.setDelegate(scriptBuilder.getMainScript());
            return stmtClosure;

        } catch (Exception e) {
            LOGGER.error("Error compiling script", e);
            throw new BusinessException("portlet.crud.error.compilingScript");
        }
    }

    private void addNewRowDefaultsSetterHandler(Table table) {
		DataContainer container = table.getContainer();

		Map<String, Closure<?>> columnsDefaultValuesMap = buildDefaultsMap(table);
		container.addCreateEventHandler(new NewRowDefaultsSetterHandler(
				columnsDefaultValuesMap));
	}

	private Map<String, Closure<?>> buildDefaultsMap(Table table) {
		Map<String, Closure<?>> columnsDefaultValuesMap = new HashMap<String, Closure<?>>();

		TableColumns columns = table.getColumns();
		if (columns == null) {
			return columnsDefaultValuesMap;
		}

		for (TableColumn tableColumn : columns) {
			ColumnConfig columnConfig = (ColumnConfig) configs.get(tableColumn);

			if (columnConfig != null) {
				Closure<?> defaultValueClosure = scriptBuilder
						.buildClosure(columnConfig.getDefault());
				if (defaultValueClosure != null) {
					columnsDefaultValuesMap.put(columnConfig.getName(),
							defaultValueClosure);
				}
			}

		}

		return columnsDefaultValuesMap;
	}

	private void populateDynamicSelectionsOnDomainTable(Table table) {
		TableConfig config = (TableConfig) configs.get(table);
		if (config.getColumns() != null) {
			for (ColumnConfig columnConfig : config.getColumns().getColumn()) {
				if (columnConfig.getSelect() != null
						&& columnConfig.getSelect().getDynamic() != null) {
					Dynamic dynamicConfig = columnConfig.getSelect()
							.getDynamic();

					Closure<?> dynamicSelectionClosure = scriptBuilder
							.buildClosure(dynamicConfig.getOptions());
					DynamicOptionList optionList = factory
							.getDynamicOptionList(dynamicSelectionClosure,
									columnConfig.getSelect(), table, eventBus);
					((SelectionTableColumn) table.getColumns().get(
							columnConfig.getName())).setOptionList(optionList);
				}
			}
		}
	}

	private void populateGeneratedColumnsClosures(Table table) {
		if (table.getColumns() != null) {
			for (TableColumn column : table.getColumns()) {
				ColumnConfig columnConfig = (ColumnConfig) configs.get(column);

				if (columnConfig.getGenerator() != null) {
					populateScriptColumnGenerator(column, columnConfig);
				}
				if (columnConfig.getGeneratedValue() != null
						&& columnConfig.getGeneratedType() != null) {
					populateGeneratedValueGenerator(column, columnConfig);
				}
			}
		}
	}

	private void populateGeneratedValueGenerator(TableColumn column,
			ColumnConfig columnConfig) {
		GroovyScript generatedValueScript = columnConfig.getGeneratedValue();

		Closure<Object> generatedValueClosure = scriptBuilder
				.buildClosure(generatedValueScript);

		column.setGeneratedValueGenerator(new GeneratedValueGeneratorImpl(
				generatedValueClosure));
	}

	private void populateScriptColumnGenerator(TableColumn column,
			ColumnConfig columnConfig) {
		if (portlet.allowsDisplayGeneratedContent()) {

			GroovyScript generatedColumnsScript = columnConfig.getGenerator();

			Closure<Object> generatedColumnsClosure = scriptBuilder
					.buildClosure(generatedColumnsScript);

			column.setCustomColumnGenerator(new CustomColumnGeneratorImpl(
					generatedColumnsClosure));
		} else {
			// display empty cell content if user is not allowed to view
			// generated content
			column.setCustomColumnGenerator(new CustomColumnGenerator() {
				@Override
				public com.vaadin.ui.Component generate(ContainerRow row) {
					return null;
				}
			});
		}
	}

	private void buildTableStyleRenderer(Table table) {
		TableConfig tableConfig = (TableConfig) configs.get(table);
		if (tableConfig.getRowStyle() != null) {
			Closure<Object> rowStyleClosure = scriptBuilder
					.buildClosure(tableConfig.getRowStyle());

			table.setRowStyleRenderer(new DynamicRowStyleRenderer(table,
					rowStyleClosure));
		}

		if (tableConfig.getColumns() != null) {
			for (ColumnConfig columnConfig : tableConfig.getColumns()
					.getColumn()) {
				if (columnConfig.getStyle() != null) {

					Closure<Object> columnClosure = scriptBuilder
							.buildClosure(columnConfig.getStyle());

					DynamicColumnStyleRenderer columnStyleRenderer = new DynamicColumnStyleRenderer(
							table, columnClosure, columnConfig.getName());

					table.getColumns().get(columnConfig.getName())
							.setColumnStyleRenderer(columnStyleRenderer);
				}
			}
		}
	}

	private ScriptContainer buildScriptContainer(DataContainer container) {

		ScriptContainer scriptContainer;
		if (container instanceof ReSTContainer) {
			ReSTContainer restContainer = (ReSTContainer) container;
			ReSTContainerConfig config = (ReSTContainerConfig) configs
					.get(container);
			Realm realm = config.getRealm() == null ? null : portlet
					.getAuthenticationRealms().get(config.getRealm());
			GenericDelegate restDelegate = factory.getReSTDelegate(config,
					restContainer, realm, scriptBuilder, auditLogger);
			restContainer.setDelegate(restDelegate);
			scriptContainer = factory.getScriptReSTContainer(restContainer);

		} else if (container instanceof JMXContainer) {
			GenericDelegate delegate = ((JMXContainer) container).getDelegate();
			JmxDelegate jmxDelegate = (JmxDelegate) delegate;
			scriptContainer = factory.getScriptJmxContainer(container,
					jmxDelegate);

		} else if (container instanceof GenericDataContainer) {
			// only ScriptContainer left
			populateBackendToDataContainer((GenericDataContainer) container);
			scriptContainer = factory.getScriptContainer(container);

		} else if (container instanceof AbstractDatabaseContainer) {
			if (container instanceof DatabaseQueryContainer) {
				scriptContainer = factory
						.getScriptDatabaseQueryContainer(container);
			} else {
				scriptContainer = factory.getScriptDatabaseContainer(container);
			}
		} else {
			throw new UnsupportedOperationException("Container of type "
					+ container.getClass().getName() + " is not supported");
		}

		populateOnInsertClosure(container, scriptContainer);
		populateOnCreateClosure(container, scriptContainer);
		populateOnCommitClosure(container, scriptContainer);
		populateOnDeleteClosure(container, scriptContainer);
		populateOnUpdateClosure(container, scriptContainer);

		return scriptContainer;
	}

	private void populateBackendToDataContainer(GenericDataContainer container) {
		GroovyScript delegateScript = ((ScriptContainerConfig) configs
				.get(container)).getDelegate();
		Closure<Object> delegateClosure = scriptBuilder
				.buildClosure(delegateScript);
		container.setDelegate(new ScriptContainerDelegate(delegateClosure,
				container));
	}

	private void populateOnUpdateClosure(DataContainer container,
			ScriptContainer scriptContainer) {
		ContainerConfig containerConfig = (ContainerConfig) configs
				.get(container);
		Closure<?> onUpdate = scriptBuilder.buildClosure(containerConfig
				.getOnUpdate());
		scriptContainer.setOnUpdate(onUpdate);
	}

	private void populateOnDeleteClosure(DataContainer container,
			ScriptContainer scriptContainer) {
		ContainerConfig containerConfig = (ContainerConfig) configs
				.get(container);
		Closure<?> onDelete = scriptBuilder.buildClosure(containerConfig
				.getOnDelete());
		scriptContainer.setOnDelete(onDelete);
	}

	private void populateOnCommitClosure(DataContainer container,
			ScriptContainer scriptContainer) {
		ContainerConfig containerConfig = (ContainerConfig) configs
				.get(container);
		Closure<?> onCommit = scriptBuilder.buildClosure(containerConfig
				.getOnCommit());
		scriptContainer.setOnCommit(onCommit);
	}

	private void populateOnCreateClosure(DataContainer container,
			ScriptContainer scriptContainer) {
		ContainerConfig containerConfig = (ContainerConfig) configs
				.get(container);
		Closure<?> onCreate = scriptBuilder.buildClosure(containerConfig
				.getOnCreate());
		scriptContainer.setOnCreate(onCreate);
	}

	private void populateOnInsertClosure(DataContainer container,
			ScriptContainer scriptContainer) {
		ContainerConfig containerConfig = (ContainerConfig) configs
				.get(container);
		Closure<?> onInsert = scriptBuilder.buildClosure(containerConfig
				.getOnInsert());
		scriptContainer.setOnInsert(onInsert);
	}

	private void populateOnSelectionChangeClosure(Table table,
			ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		Closure<?> onSelectionChange = scriptBuilder.buildClosure(tc
				.getOnSelectionChange());
		scriptTable.setOnSelectionChange(onSelectionChange);
	}

	private void populateTableOnDoubleClickClosure(Table table,
			ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		Closure<?> onDoubleClick = scriptBuilder.buildClosure(tc
				.getOnDoubleClick());
		scriptTable.setOnDoubleClick(onDoubleClick);
	}

	private void populateTableOnInitializeClosure(Table table,
			ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		Closure<?> onInitialize = scriptBuilder.buildClosure(tc
				.getOnInitialize());
		scriptTable.setOnInitialize(onInitialize);
	}

	private void populateTableOnEditClosure(Table table, ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		Closure<?> onChange = scriptBuilder.buildClosure(tc.getOnModeChange());
		scriptTable.setOnModeChange(onChange);
	}

	private void populateTableRowChangeClosure(Table table,
			ScriptTable scriptTable) {
		TableConfig tc = (TableConfig) configs.get(table);
		Closure<?> onRowChange = scriptBuilder
				.buildClosure(tc.getOnRowChange());
		scriptTable.setOnRowChange(onRowChange);
	}

	private ScriptTableAction buildScriptTableAction(TableAction action) {
		ScriptTableAction scriptTableAction = factory
				.getScriptTableAction(action);

		populateTableActionExecutionClosure(action, scriptTableAction);
		scriptPortlet.addElementById(scriptTableAction.getId(),
				scriptTableAction);

		return scriptTableAction;
	}

	private void populateTableActionExecutionClosure(TableAction action,
			ScriptTableAction scriptTableAction) {
		TableActionConfig tac = (TableActionConfig) configs.get(action);
		populateOnExecutionClosure(scriptTableAction, tac);
		populateExportFilenameGenerator(action, tac);
		populateDownloadGenerator(scriptTableAction, tac);
	}

	private void populateDownloadGenerator(ScriptTableAction scriptTableAction,
			TableActionConfig tac) {
		if (tac.getDownload() != null
				&& tac.getDownload().getGenerator() != null) {
			Closure<?> downloadGenerator = scriptBuilder.buildClosure(tac
					.getDownload().getGenerator());
			scriptTableAction.setDownloadGenerator(downloadGenerator);
		}
	}

	private void populateOnExecutionClosure(
			ScriptTableAction scriptTableAction, TableActionConfig tac) {
		if (tac.getOnExecution() != null) {
			Closure<?> onExecution = scriptBuilder.buildClosure(tac
					.getOnExecution());
			scriptTableAction.setOnExecution(onExecution);
		}
	}

	private void populateExportFilenameGenerator(TableAction action,
			TableActionConfig tac) {
		if (tac.getExport() != null && tac.getExport().getFilename() != null) {
			final Closure<?> filenameGenerator = scriptBuilder.buildClosure(tac
					.getExport().getFilename());
			action.setExportFilenameGenerator(new Supplier<String>() {
				@Override
				public String get() {
					return (String) filenameGenerator.call();
				}
			});
		}
	}

	private void populateFormActionExecutionClosure(FormAction action,
			ScriptFormAction scriptFormAction) {
		FormActionConfig fac = (FormActionConfig) configs.get(action);
		Closure<?> onExecution = scriptBuilder.buildClosure(fac
				.getOnExecution());

		scriptFormAction.setOnExecution(onExecution);
	}

	private void populateFormFieldChangeExecutionClosure(FormField formField,
			ScriptFormField scriptFormField) {
		FormFieldConfig fac = (FormFieldConfig) configs.get(formField);
		Closure<?> onExecution = scriptBuilder.buildClosure(fac
				.getOnValueChange());

		scriptFormField.setOnChange(onExecution);
	}

    /**
     * running the main script can be disabled for testing reasons
     *
     * @param runMainScript
     */
    public void setRunMainScript(boolean runMainScript) {
        this.runMainScript = runMainScript;
    }

}
