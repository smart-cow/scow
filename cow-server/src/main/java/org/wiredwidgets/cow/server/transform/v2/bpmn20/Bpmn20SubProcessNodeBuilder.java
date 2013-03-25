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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TCallActivity;
import org.wiredwidgets.cow.server.api.model.v2.Parameter;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class Bpmn20SubProcessNodeBuilder extends Bpmn20ActivityNodeBuilder<TCallActivity, SubProcess> {

    public Bpmn20SubProcessNodeBuilder(ProcessContext context, SubProcess subProcess) {
        super(context, new TCallActivity(), subProcess);
    }

    @Override
    protected void buildInternal() {
    	
    	setId();

        SubProcess source = getActivity();
        TCallActivity t = getNode();
        t.setName(source.getName());

        // the process ID of the called process
        t.setCalledElement(new QName(source.getSubProcessKey()));
        
        t.setIoSpecification(ioSpec);     
        ioSpec.getInputSets().add(inputSet);       
        ioSpec.getOutputSets().add(outputSet);        
        
        // this means that if the parent process is terminated the subprocess will also be terminated.
        addOtherAttribute("independent","false");
        
        // input params
        for (Parameter param : source.getParameterIns()) {
        	if (param.getExpr() != null) {
        		// input is an expression
        		addDataInput(param.getSubvar(), param.getExpr());
        	}
        	else {
        		Property p = new Property();
        		p.setId(param.getVar());
        		p.setName(param.getVar());
        		p.setItemSubjectRef(new QName(param.getVar()));
        		addDataInput(param.getSubvar(), p);
        	}
        }
        
        // output params
        for (Parameter param : source.getParameterOuts()) {
        	if (param.getExpr() != null) {
        		addDataOutput(param.getSubvar(), param.getExpr());
        	}        	
        	else {
        		Property p = new Property();
        		p.setId(param.getVar());
        		p.setName(param.getVar());
        		p.setItemSubjectRef(new QName(param.getVar()));
        		addDataOutput(param.getSubvar(), p);
        	}
        }

    }
    
    @Override
    protected JAXBElement<TCallActivity> createNode() {
        return factory.createCallActivity(getNode());
    }

}
