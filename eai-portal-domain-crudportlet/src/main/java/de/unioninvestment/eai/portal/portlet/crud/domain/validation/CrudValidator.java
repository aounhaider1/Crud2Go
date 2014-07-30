package de.unioninvestment.eai.portal.portlet.crud.domain.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.unioninvestment.eai.portal.portlet.crud.config.AllFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.AnyFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.ComparisonFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.CustomFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.FilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.FormActionConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.IncludeFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.NotFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.PortletConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.SQLFilterConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.SearchConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.TableConfig;
import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
import de.unioninvestment.eai.portal.portlet.crud.config.visitor.ConfigurationProcessor;
import de.unioninvestment.eai.portal.portlet.crud.config.visitor.ConfigurationVisitor;
import de.unioninvestment.eai.portal.portlet.crud.domain.form.SearchFormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.form.SearchFormActionValidator;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Form;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormAction;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.FormActions;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelBuilder;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;

public class CrudValidator {
	
	private ModelBuilder modelBuilder; 
	private Portlet portletDomain;
	private PortletConfig portletConfig;
	
	public CrudValidator(ModelBuilder modelBuilder, Portlet portletDomain,
			PortletConfig portletConfig) {
		super();
		this.modelBuilder = modelBuilder;
		this.portletDomain = portletDomain;
		this.portletConfig = portletConfig;
	}

	public void validate(){
		//possibly todo
		//validateDateColumnsHaveDateType();
		checkSearchFilterColumnsDefinedInQueries();
		validateSearchFormFilters();
	}

	void checkSearchFilterColumnsDefinedInQueries(){

		List<Form> forms = modelBuilder.getForms();
				
		for(Form aForm:forms){
			FormActions actions = aForm.getActions();
			FormAction searchActionWrapper = actions.getSearchAction();
			if(searchActionWrapper!=null){
				final List<String> searchColumnNames = new ArrayList<String>();
				final Set<String> columnNames = new LinkedHashSet<String>();

				FormActionConfig actionConfig = (FormActionConfig) modelBuilder.getModelToConfigMapping().get(searchActionWrapper);
				
				// ermittle Ziel-Tabellen über SearchFormAction
				// traversiere rekursiv über filterconfigs
				//   wenn explizit Tabelle angegeben:, prüfe auf Existenz in dessen Container
				//   wenn keine Tabelle angegeben: prüfe auf Existenz in mindestens einem der Container der Tabellen 
				if(actionConfig.getSearch() != null){
					SearchConfig searchConfig = actionConfig.getSearch();
					List<FilterConfig> filters = searchConfig.getApplyFilters().getFilters();
					gatherSearchColumnNames(filters, searchColumnNames);
				}
				
				
				SearchFormAction searchAction = (SearchFormAction)searchActionWrapper.getActionHandler();
				List<Table> searchableTables = searchAction.findSearchableTables(aForm);
				for(Table aTable:searchableTables){
					columnNames.addAll(aTable.getContainer().getColumns());
				}
				
				if(searchColumnNames.size()>0){
					if(columnNames.size()>0){
						for(String name:searchColumnNames){
							if(!columnNames.contains(name)){
								throw new IllegalArgumentException("Die Spalte '"
			                            + name + "' ist nicht den durchsuchten Tabellen verfügbar");
							}
						}
					}else{
						throw new IllegalArgumentException("Config ist nicht richtig konfiguriert. Da gibt es Such Filtern, aber keine Tabellen für Suche");
					}
				}

			}
		}

	}

	static void gatherSearchColumnNames(List<FilterConfig> filters,
			final List<String> searchColumnNames) {
		for(FilterConfig filterConf:filters){
			if(filterConf instanceof ComparisonFilterConfig){
				ComparisonFilterConfig comparisonFilterConfig = (ComparisonFilterConfig) filterConf;
				String column = comparisonFilterConfig.getColumn();
				if(column!=null){
					searchColumnNames.add(column);
				}
			}else if(filterConf instanceof SQLFilterConfig){
				SQLFilterConfig sqlFilterConfig = (SQLFilterConfig)filterConf;
				String column = sqlFilterConfig.getColumn();
				if(column!=null){
					searchColumnNames.add(column);
				}
			} else if (filterConf instanceof AnyFilterConfig) {
				gatherSearchColumnNames(((AnyFilterConfig) filterConf).getFilters(), searchColumnNames);	
			} else if (filterConf instanceof AllFilterConfig) {
				gatherSearchColumnNames(((AllFilterConfig) filterConf).getFilters(), searchColumnNames);
			} else if (filterConf instanceof NotFilterConfig) {
				gatherSearchColumnNames(((NotFilterConfig) filterConf).getFilters(), searchColumnNames);
			} else if (filterConf instanceof CustomFilterConfig) {
				//Do Nothing - Groovy Script
			} else if (filterConf instanceof IncludeFilterConfig) {
				//Skip - will be checked separately
			}
			
		}
	}
	
	void validateSearchFormFilters() {
		List<Form> forms = modelBuilder.getForms();
		
		for(Form aForm:forms){
			FormActions actions = aForm.getActions();
			FormAction searchActionWrapper = actions.getSearchAction();
			if(searchActionWrapper!=null){
				SearchFormAction searchAction = (SearchFormAction)searchActionWrapper.getActionHandler();
				new SearchFormActionValidator(searchAction, aForm).validate();
			}
		}
	}

}
