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
package de.unioninvestment.eai.portal.support.vaadin.groovy;

import com.vaadin.ui.Component
import com.vaadin.ui.Upload
import com.vaadin.ui.Upload.FailedListener
import com.vaadin.ui.Upload.Receiver
import com.vaadin.ui.Upload.SucceededListener

/**
 * Provides an <code>{@link Upload}</code>-Component. At minimum, a <code>Receiver</code> has to be provided (see <code>{@link #handleAttributeReceiver(FactoryBuilderSupport, Upload, Closure)}</code>).
 * 
 * 
 * @author Jan Malcomess (codecentric AG)
 * @since 1.46
 */
class UploadFactory extends AbstractComponentFactory {

	@Override
	Component createComponent() {
		return new Upload()
	}

	void handleAttributeReceiver(FactoryBuilderSupport builder, Upload component, Closure listener) {
		component.setReceiver(listener as Receiver)
	}

	void handleAttributeOnsuccess(FactoryBuilderSupport builder, Upload component, Closure listener) {
		component.addListener(listener as SucceededListener)
	}

	void handleAttributeOnfailure(FactoryBuilderSupport builder, Upload component, Closure listener) {
		component.addListener(listener as FailedListener)
	}
}
