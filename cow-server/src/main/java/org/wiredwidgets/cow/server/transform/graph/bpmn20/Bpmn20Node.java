package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.TFlowNode;

public class Bpmn20Node {
	
	private JAXBElement<? extends TFlowNode> node;
	
	public Bpmn20Node(JAXBElement<? extends TFlowNode> node) {
		this.node = node;
	}
	
	public JAXBElement<? extends TFlowNode> getNode() {
		return node;
	}

}
