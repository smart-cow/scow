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
