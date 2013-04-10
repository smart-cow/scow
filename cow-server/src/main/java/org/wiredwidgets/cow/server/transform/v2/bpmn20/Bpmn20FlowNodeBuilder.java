/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
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

/*
 * Base class for BPMN2.0 nodes
 * Note that all BPMN 2.0 nodes require a JAXBElement to be added to the process
 */

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.wiredwidgets.cow.server.transform.v2.Builder;
import org.wiredwidgets.cow.server.transform.v2.NodeBuilder;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;
import org.omg.spec.bpmn._20100524.model.ObjectFactory;
import org.omg.spec.bpmn._20100524.model.TFlowNode;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Abstract base implementation for all BPMN20 node builders
 * @author JKRANES
 */
public abstract class Bpmn20FlowNodeBuilder<T extends TFlowNode, V extends Activity> extends NodeBuilder<T, JAXBElement<T>, V> {

    protected static ObjectFactory factory = new ObjectFactory();

    public Bpmn20FlowNodeBuilder(ProcessContext context, T node, V activity) {
        super(context, node, activity);
    }

    @Override
    public String getLinkTargetName() {
        return getNode().getName();
    }

    @Override
    protected void addTransition(Builder target, String transitionName) {
 
        if (transitionName != null) {
            // support for conditional expressions used in diverging gateways
            String expression = "return " + getBuildProperty("decisionVar") + " == \"" + transitionName + "\";";          
            addTransition(target, transitionName, expression);
        }
        else {
            addTransition(target, null, null);
        }
    }

    /**
     * Add a transition (sequenceFlow) with a FormalExpression
     * @param target
     * @param transitionName
     * @param expression
     */
    private void addTransition(Builder target, String transitionName, String expression) {
        TSequenceFlow sequenceFlow = new TSequenceFlow();
        TFlowNode targetNode = (TFlowNode)target.getLinkTargetNode();
        // follow convention of concatenating source and targetIds to form the sequence ID
        String sequenceId = getNode().getId() + targetNode.getId();
        sequenceFlow.setId(sequenceId);
        sequenceFlow.setName(transitionName);
        sequenceFlow.setSourceRef(getNode());
        sequenceFlow.setTargetRef(targetNode);

        if (expression != null) {
            TFormalExpression expr = new TFormalExpression();
            expr.getContent().add(expression);
            sequenceFlow.setConditionExpression(expr);
        }

        // need to use super here because downcast context does not work
        super.getContext().addNode(factory.createSequenceFlow(sequenceFlow));

        // incomings and outgoings are required by igrafx       
        getNode().getOutgoings().add(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", sequenceFlow.getId()));
        targetNode.getIncomings().add(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", sequenceFlow.getId()));
    }

    /**
     * Abstract method to be implemented by subclasses
     * Subclasses should use the Object factory to return a JAXBElement of the appropriate type
     * @return JAXBElement for the node
     */
    @Override
    protected abstract JAXBElement<T> createNode();
    
    /**
     * Convenience to avoid having to downcast
     * @return 
     */
    @Override
    protected Bpmn20ProcessContext getContext() {
        return (Bpmn20ProcessContext) super.getContext();
    }
    
    protected void addOtherAttribute(String name, String value) {
        getNode().getOtherAttributes().put(new QName("http://www.jboss.org/drools", name), value);
    }
    
    protected void setId() {
    	String id = null;
    	// Legacy workflows created before the new ID system will have keys but the
    	// maxID will not be set for the process.  So, we handle those as "new" workflows
    	// the first time they are saved, in order to get the maxID set correctly.
    	if (getContext().isRevised() && getActivity() != null && getActivity().getKey() != null  && !getActivity().getKey().isEmpty()) {
    		id = getActivity().getKey();
    	}
    	else {
    		id = getContext().generateId("_");
    	}

    	getNode().setId(id);
    	
    	if (getActivity() != null) {
    		getActivity().setKey(id);  	
    	}
    }
    

}
