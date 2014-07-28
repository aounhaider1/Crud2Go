package de.unioninvestment.eai.portal.portlet.crud.domain.validation;

import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelBuilder;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet;

public class ModelValidator {
	public void validateModel(ModelBuilder modelBuilder, 
			Portlet portletDomain, Config portletConfig){
		new CrudValidator(modelBuilder, portletDomain, portletConfig).validate();
	}
}