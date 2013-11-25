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
