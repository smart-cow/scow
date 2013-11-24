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
