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
package de.unioninvestment.eai.portal.portlet.crud.domain.model.filter;

import java.io.Serializable;

/**
 * Filterinterface für ein Datenfilter.
 * 
 * @author markus.bonsch
 * 
 */
public abstract class Filter implements Serializable {
	private static final long serialVersionUID = 42L;
	protected final boolean durable;

	/**
	 * Konstruktor mit Parameter.
	 * 
	 * @param durable
	 *            Permanenter Filter
	 */
	public Filter(boolean durable) {
		this.durable = durable;

	}

	public boolean isDurable() {
		return durable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (durable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Filter other = (Filter) obj;
		if (durable != other.durable)
			return false;
		return true;
	}
}
