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

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TScriptTask;
import org.wiredwidgets.cow.server.api.model.v2.Script;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 * Node builder for TEndEvent
 * @author JKRANES
 */
public class Bpmn20ScriptTaskNodeBuilder extends Bpmn20ActivityNodeBuilder<TScriptTask, Script> {

    public Bpmn20ScriptTaskNodeBuilder(ProcessContext context, Script script) {
        super(context, new TScriptTask(), script);
    }

    @Override
    protected JAXBElement<TScriptTask> createNode() {
        return factory.createScriptTask(getNode());
    }

    @Override
    protected void buildInternal() {
         setId();
         
         getNode().setName(getActivity().getName());
         
         // add imports at the process level;
         for (String className : getActivity().getImports()) {
        	 getContext().addImport(className);
         }
         
         // create a process level property to indicate whether the script has been
         // run or not. 
         String completedPropertyName = getNode().getId() + "_completed";
         Property property = getContext().addProcessVariable(completedPropertyName, "Boolean");
         
         
         org.omg.spec.bpmn._20100524.model.Script script = new org.omg.spec.bpmn._20100524.model.Script();           
         script.getContent().add(getActivity().getContent());
         
         // XXX: kind of a hack
         // Tried to do this somehow via output variable assignment but was unable
         // to get it working.  Unclear whether it is possible to have an output variable
         // that is simply an expression value.  In any case this gets the job done.
         
         String scriptLine = "kcontext.setVariable(\"" + completedPropertyName + "\"" + ", true);";
         
         script.getContent().add(scriptLine);
         
         getNode().setScriptFormat(getActivity().getScriptFormat());
         getNode().setScript(script);
         
         
         
         
         // getNode().setIoSpecification(ioSpec);           
         // ioSpec.getOutputSets().add(outputSet);
         // assignOutputValue(addDataOutput("complete", "complete"), "true");
         
         
    }
    
   
}
