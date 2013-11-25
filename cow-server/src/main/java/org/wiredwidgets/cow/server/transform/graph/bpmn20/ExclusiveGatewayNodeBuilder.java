package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.TExclusiveGateway;
import org.omg.spec.bpmn._20100524.model.TGatewayDirection;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;

@Component
public class ExclusiveGatewayNodeBuilder extends AbstractFlowNodeBuilder<ExclusiveGatewayActivity, TExclusiveGateway> {

	@Override
	public TExclusiveGateway newNode() {
		return new TExclusiveGateway();
	}

	@Override
	public JAXBElement<TExclusiveGateway> createElement(TExclusiveGateway node) {
		return factory.createExclusiveGateway(node);
	}

	@Override
	protected void buildInternal(TExclusiveGateway node,
			ExclusiveGatewayActivity activity, Bpmn20ProcessContext context) {
		
		node.setName("gateway");
		node.setGatewayDirection(TGatewayDirection.fromValue(activity.getDirection()));
	}

	@Override
	public Class<ExclusiveGatewayActivity> getType() {
		return ExclusiveGatewayActivity.class;
	}
	
	

}
