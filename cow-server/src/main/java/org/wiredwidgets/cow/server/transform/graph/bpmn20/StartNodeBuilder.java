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

import org.omg.spec.bpmn._20100524.model.TStartEvent;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.StartActivity;

@Component
public class StartNodeBuilder extends AbstractFlowNodeBuilder<StartActivity, TStartEvent> {

	@Override
	public TStartEvent newNode() {
		return new TStartEvent();
	}

	@Override
	public JAXBElement<TStartEvent> createElement(TStartEvent node) {
		return factory.createStartEvent(node);
	}

	@Override
	public Class<StartActivity> getType() {
		return StartActivity.class;
	}

	@Override
	protected void buildInternal(TStartEvent node, StartActivity activity,
			Bpmn20ProcessContext context) {
		node.setName("start");
	}
	

}
