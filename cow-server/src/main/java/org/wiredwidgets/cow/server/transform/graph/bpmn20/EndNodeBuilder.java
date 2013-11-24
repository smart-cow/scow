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

		node.setName("end");

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
