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

package org.wiredwidgets.cow.server.completion;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;

//@Component
//@Scope("prototype")
public class SubProcessEvaluator extends AbstractEvaluator<SubProcess> {
    
    @Autowired
    ProcessInstanceService processInstanceService;
    
    // @Autowired
    //  ExecutionService executionService;

    @Override
    protected void evaluateInternal() {
        this.completionState = getCompletionState();
    }

    private CompletionState getCompletionState() {
        
        // First, check for open instances of the subprocess linked to its parent
        List<ProcessInstance> openProcessInstances = processInstanceService.findProcessInstancesByKey(activity.getSubProcessKey());
              
        for (ProcessInstance processInstance : openProcessInstances) {
        	if (processInstance.getParentId() != null 
        			&& processInstance.getParentId().equals(processInstanceId)) {
                return CompletionState.OPEN;
            }
        }        
        
        // Next, check the completion state variable
        // see JbpmSubProcessNodeBuilder for how this var is created
        // String completed = (String) executionService.getVariable(processInstanceId, "_" + activity.getSubProcessKey() + "_complete");
        
        // TODO: implement this!
        String completed = "xxx";
        
        if (completed != null && completed.equals("true")) {
            return CompletionState.COMPLETED;
        }
        
        // not completed, not open, must be not started
        return branchState;
    }

	@Override
	protected Class<SubProcess> getActivityClass() {
		return SubProcess.class;
	}
}
