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
package de.unioninvestment.eai.portal.support.vaadin.context;

import java.util.concurrent.Callable;

/**
 * Wrapper für Runnable, der einen Kontext setzt. Dies ist für asynchrone Verarbeitung notwendig.
 *
 * @author carsten.mjartan
 */
public abstract class ContextualCallable<T> implements Callable<T> {

	private ContextProvider provider;

	public ContextualCallable(ContextProvider provider) {
		this.provider = provider;
	}

	@Override
	public T call() throws Exception {
		ContextProvider oldProvider = Context.getProvider();
		Context.setProvider(provider);
		try {
			return callWithContext();
		} finally {
			Context.setProvider(oldProvider);
		}
	}

	abstract protected T callWithContext();
}
