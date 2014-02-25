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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TTask;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;


/**
 *
 * @author JKRANES
 */
public class Bpmn20ServiceTaskNodeBuilder extends Bpmn20ActivityNodeBuilder<TTask, ServiceTask> {

    public Bpmn20ServiceTaskNodeBuilder(ProcessContext context, ServiceTask task) {
        super(context, new TTask(), task);
    }

    @Override
    protected void buildInternal() {
    	setId();

        ServiceTask source = getActivity();
        TTask t = getNode();

        t.setName(source.getName());
   
        // this is the name JBPM uses to assign a work item handler
        addOtherAttribute("taskName", "RestService");
        
        t.setIoSpecification(ioSpec);     
        ioSpec.getInputSets().add(inputSet);       
        ioSpec.getOutputSets().add(outputSet);
        
        ServiceTask st = getActivity();
        
        Property varsProperty = getContext().getProcessVariable(Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
        addDataInputFromProperty(Bpmn20UserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME, varsProperty);
        addDataOutputFromProperty(Bpmn20UserTaskNodeBuilder.TASK_OUTPUT_VARIABLES_NAME, varsProperty);
        
        addDataInputFromExpression("method", st.getMethod());
        addDataInputFromExpression("url", st.getUrl());
        addDataInputFromExpression("content", st.getContent());
        addDataInputFromExpression("var", st.getVar());
        addDataInputFromExpression("resultSelectorXPath", st.getResultSelectorXPath());

    }
    
    @Override
    protected JAXBElement<TTask> createNode() {
        return factory.createTask(getNode());
    }

}
