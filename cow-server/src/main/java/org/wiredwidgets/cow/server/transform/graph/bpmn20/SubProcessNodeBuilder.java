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

import org.omg.spec.bpmn._20100524.model.TCallActivity;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;

@Component
public class SubProcessNodeBuilder extends AbstractFlowNodeBuilder<SubProcess, TCallActivity> {

	@Override
	public Class<SubProcess> getType() {
		return SubProcess.class;
	}

	@Override
	public TCallActivity newNode() {
		return new TCallActivity();
	}

	@Override
	public JAXBElement<TCallActivity> createElement(TCallActivity node) {
		return factory.createCallActivity(node);
	}

	@Override
	protected void buildInternal(TCallActivity t, SubProcess source,
			Bpmn20ProcessContext context) {

        t.setName(source.getName());

        // the process ID of the called process
        t.setCalledElement(new QName(source.getSubProcessKey()));       
        
        // this means that if the parent process is terminated the subprocess will also be terminated.
        addOtherAttribute("independent","false", t);
        
        // inputs and outputs 
        // other variable inputs
        if (source.getVariables() != null) {
        	addInputOutputVariables(source.getVariables().getVariables(), t, context);
        }		
		
		
	}
	
	

}
