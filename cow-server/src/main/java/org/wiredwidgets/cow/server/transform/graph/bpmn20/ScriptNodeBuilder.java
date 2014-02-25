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

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TScriptTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Script;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20UserTaskNodeBuilder;

@Component
public class ScriptNodeBuilder extends AbstractFlowNodeBuilder<Script, TScriptTask> {

	@Override
	public Class<Script> getType() {
		return Script.class;
	}

	@Override
	public TScriptTask newNode() {
		return new TScriptTask();
	}

	@Override
	public JAXBElement<TScriptTask> createElement(TScriptTask node) {
		return factory.createScriptTask(node);
	}

	@Override
	protected void buildInternal(TScriptTask node, Script script,
			Bpmn20ProcessContext context) {
		
        node.setName(script.getName());
        
        // add imports at the process level;
        for (String className : script.getImports()) {
       	 context.addImport(className);
        }
        
        // create a process level property to indicate whether the script has been
        // run or not. 
        String completedPropertyName = node.getId() + "_completed";
        Property property = context.addProcessVariable(completedPropertyName, "Boolean");
        
        org.omg.spec.bpmn._20100524.model.Script bpmn20Script = new org.omg.spec.bpmn._20100524.model.Script();           
        bpmn20Script.getContent().add(script.getContent());
        
        // XXX: kind of a hack
        // Tried to do this somehow via output variable assignment but was unable
        // to get it working.  Unclear whether it is possible to have an output variable
        // that is simply an expression value.  In any case this gets the job done.
        
        String scriptLine = "kcontext.setVariable(\"" + completedPropertyName + "\"" + ", true);";
        
        bpmn20Script.getContent().add(scriptLine);
        
        // Brian modification to support input/output variables
//        Property varsProperty = context.getProcessVariable(Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
//        addDataInputFromProperty(Bpmn20UserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME, varsProperty, node);
        // end Brian modification
        
        node.setScriptFormat(script.getScriptFormat());
        node.setScript(bpmn20Script);	
		
        
	}
	
}
