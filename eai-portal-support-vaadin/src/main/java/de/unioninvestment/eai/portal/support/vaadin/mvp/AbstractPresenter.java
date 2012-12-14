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
package de.unioninvestment.eai.portal.support.vaadin.mvp;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Convenient base implementation of <code>Presenter</code>.
 * 
 * @author Jan Malcomess (codecentric AG)
 * @since 1.45
 */
public abstract class AbstractPresenter<V extends View> implements Presenter {

	/**
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The view controlled by this presenter.
	 */
	private final V view;

	/**
	 * @param view
	 *            The view controlled by this presenter.
	 */
	public AbstractPresenter(V view) {
		Assert.notNull(view);
		this.view = view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V getView() {
		return this.view;
	}
}
