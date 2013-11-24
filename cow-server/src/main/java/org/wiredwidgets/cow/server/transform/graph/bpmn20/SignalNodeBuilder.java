package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TIntermediateCatchEvent;
import org.omg.spec.bpmn._20100524.model.TSignalEventDefinition;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;

@Component
public class SignalNodeBuilder extends AbstractFlowNodeBuilder<Signal, TIntermediateCatchEvent> {

	@Override
	public Class<Signal> getType() {
		return Signal.class;
	}

	@Override
	public TIntermediateCatchEvent newNode() {
		return new TIntermediateCatchEvent();
	}

	@Override
	public JAXBElement<TIntermediateCatchEvent> createElement(
			TIntermediateCatchEvent node) {
		return factory.createIntermediateCatchEvent(node);
	}

	@Override
	protected void buildInternal(TIntermediateCatchEvent node, Signal signal,
			Bpmn20ProcessContext context) {
		
		String eventType;
        Property outputVariableProperty = null;
        if (signal.getSignalId().equals("_exit")) {
        	// special case for the signal prior to the process End
        	eventType = "exit";
        	outputVariableProperty = context.getProcessVariable(Bpmn20ProcessBuilder.PROCESS_EXIT_PROPERTY);
        }
        else {
        	eventType = signal.getSignalId();
        	// adds a new process variable to hold the output of the signal event
        	outputVariableProperty = context.addProcessVariable(getVarName(signal), "String");
        }        
        
        node.setName("Signal:" + eventType);
        
        addDataOutputFromProperty("event", outputVariableProperty, node);
        
        TSignalEventDefinition def = factory.createTSignalEventDefinition();
        def.setSignalRef(new QName(eventType));
        node.getEventDefinitions().add(factory.createSignalEventDefinition(def));		
	}
	
    public static String getVarName(Signal signal) {
    	return ("signal_" + signal.getSignalId());
    }	
	
	

}
