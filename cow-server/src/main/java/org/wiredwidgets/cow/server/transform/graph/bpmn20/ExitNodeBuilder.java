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

import org.omg.spec.bpmn._20100524.model.Script;
import org.omg.spec.bpmn._20100524.model.TScriptTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Exit;

@Component
public class ExitNodeBuilder extends AbstractFlowNodeBuilder<Exit, TScriptTask> {

	@Override
	public Class<Exit> getType() {
		return Exit.class;
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
	protected void buildInternal(TScriptTask node, Exit exit,
			Bpmn20ProcessContext context) {
		
        node.setName(getExitScriptName(exit));
        
        Script script = new Script();         
        String scriptText = "kcontext.getKnowledgeRuntime().signalEvent(\"exit\", \"" 
       		 + exit.getState() + "\", kcontext.getProcessInstance().getId());";   
        script.getContent().add(scriptText);
        node.setScript(script);		
		
	}
	
	public static String getExitScriptName(Exit exit) {
		return "exit:" + exit.getState();
	}
	
	
	

}
