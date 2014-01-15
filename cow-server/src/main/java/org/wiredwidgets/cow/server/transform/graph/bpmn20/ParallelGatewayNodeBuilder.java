package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.TComplexGateway;
import org.omg.spec.bpmn._20100524.model.TGatewayDirection;
import org.omg.spec.bpmn._20100524.model.TParallelGateway;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.ComplexGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.ParallelGatewayActivity;

@Component
public class ParallelGatewayNodeBuilder extends AbstractFlowNodeBuilder<ParallelGatewayActivity, TParallelGateway> {

	@Override
	public TParallelGateway newNode() {
		return new TParallelGateway();
	}

	@Override
	public JAXBElement<TParallelGateway> createElement(TParallelGateway node) {
		return factory.createParallelGateway(node);
	}

	@Override
	protected void buildInternal(TParallelGateway node,
			ParallelGatewayActivity activity, Bpmn20ProcessContext context) {
		
		node.setName("gateway");
		node.setGatewayDirection(TGatewayDirection.fromValue(activity.getDirection()));
	}

	@Override
	public Class<ParallelGatewayActivity> getType() {
		return ParallelGatewayActivity.class;
	}

}
