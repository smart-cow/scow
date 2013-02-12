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

package org.wiredwidgets.cow.server.convert;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.service.Variables;

/**
 *
 * @author JKRANES
 */
@Component
public class JbpmToSc2ProcessInstance extends AbstractConverter<org.drools.runtime.process.ProcessInstance, ProcessInstance> {

    /*@Autowired
    ProcessEngine engine;*/

    private Logger log = Logger.getLogger(JbpmToSc2ProcessInstance.class);

    @Override
    public ProcessInstance convert(org.drools.runtime.process.ProcessInstance source) {
              
        // note that Execution.getParent returns null for a sub-process
        // so we are forced to use the implementation class to get to the parent
        // log.debug("id: " + source.getId());
        // TODO: this seems to work but needs more testing -- esp. for deeper nesting levels
        /*String parentId = null;
        if (source instanceof ExecutionImpl) {
            // set parent to source just to satisfy the loop condition to get started
            ExecutionImpl parent = (ExecutionImpl)source;
            while (parent != null) {
                log.debug("parent id: " + parent.getId());
                parentId = parent.getId();
                parent = parent.getSuperProcessExecution();
            }           
        }
        else {
            // not sure when this would happen -- log it
            log.warn("Not an instance of ExecutionImpl!");
            log.warn("Process instance class: " + source.getClass().getName());
        }*/
        
        ProcessInstance target = new ProcessInstance();
        target.setId(Long.toString(source.getId()));
        target.setName(source.getProcessName());
        target.setState(Integer.toString(source.getState()));
        target.setProcessDefinitionId(source.getProcessId());
        /*target.setPriority(source);
        target.setParentId(parentId);
        */

        /*Map<String, Object> vars = this.engine.getExecutionService().getVariables(source.getId(), this.engine.getExecutionService().getVariableNames(source.getId()));
        for (String key : vars.keySet()) {
            // special case for "_name" variable
            if (key.equals("_name")) {
                target.setName(vars.get(key).toString());        
            }
            else {
                // exclude "_" variables, as these are for internal system use
                if (!key.startsWith("_")) {
                    addVariable(target, key, (vars.get(key) == null ? null : vars.get(key).toString()) );
                }
            }
        }    */   
        return target;
    }
    
    private void addVariable(ProcessInstance pi, String name, String value) {
        if (pi.getVariables() == null) {
            pi.setVariables(new Variables());
        }
        Variable v = new Variable();
        v.setName(name);
        v.setValue(value);
        pi.getVariables().getVariables().add(v);
    }
}
