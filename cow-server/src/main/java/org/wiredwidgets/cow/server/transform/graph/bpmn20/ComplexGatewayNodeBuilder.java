/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.TComplexGateway;
import org.omg.spec.bpmn._20100524.model.TGatewayDirection;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.ComplexGatewayActivity;

@Component
public class ComplexGatewayNodeBuilder extends AbstractFlowNodeBuilder<ComplexGatewayActivity, TComplexGateway> {

	@Override
	public TComplexGateway newNode() {
		return new TComplexGateway();
	}

	@Override
	public JAXBElement<TComplexGateway> createElement(TComplexGateway node) {
		return factory.createComplexGateway(node);
	}

	@Override
	protected void buildInternal(TComplexGateway node,
			ComplexGatewayActivity activity, Bpmn20ProcessContext context) {
		
		node.setName("gateway");
		node.setGatewayDirection(TGatewayDirection.fromValue(activity.getDirection()));
	}

	@Override
	public Class<ComplexGatewayActivity> getType() {
		return ComplexGatewayActivity.class;
	}

}
