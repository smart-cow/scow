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

import org.omg.spec.bpmn._20100524.model.TEndEvent;
import org.omg.spec.bpmn._20100524.model.TTerminateEventDefinition;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.EndActivity;

@Component
public class EndNodeBuilder extends
		AbstractFlowNodeBuilder<EndActivity, TEndEvent> {

	@Override
	public TEndEvent newNode() {
		return new TEndEvent();
	}

	@Override
	public JAXBElement<TEndEvent> createElement(TEndEvent node) {
		return factory.createEndEvent(node);
	}

	@Override
	public Class<EndActivity> getType() {
		return EndActivity.class;
	}

	@Override
	protected void buildInternal(TEndEvent node, EndActivity activity,
			Bpmn20ProcessContext context) {

		node.setName(activity.getName());

		// NOTE: See JBPM documentation for the distinction between Terminating
		// and
		// non terminating end events. Terminating end events will also
		// terminate
		// any parent process if this process is being called as a sub-process.
		// In order
		// to avoid this, we make the end event non terminating, and we precede
		// it with
		// a converging exclusive gateway, so that all exit paths lead to the
		// same point.

		node.getEventDefinitions()
				.add(factory
						.createTerminateEventDefinition(new TTerminateEventDefinition()));
	}

}
