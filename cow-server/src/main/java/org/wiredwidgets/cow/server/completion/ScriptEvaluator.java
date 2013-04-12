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

package org.wiredwidgets.cow.server.completion;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Script;

@Component
@Scope("prototype")
public class ScriptEvaluator extends AbstractEvaluator<Script> {
    
    @Override
    protected void evaluateInternal() {
    	
    	// See BpmnScriptTaskNodeBuilder
    	// Assignment of this variable to "true" is appended to the script
    	// and thus executed as part of the script task
    	
    	// Note that this does not handle loops -- this will indicate that
    	// the script is completed as long as it has been executed at least once
    	
    	String completedVarName = activity.getKey() + "_completed";
    	String completed = info.getVariables().get(completedVarName);
    	if (completed != null && completed.equalsIgnoreCase("true")) {
    		this.completionState = CompletionState.COMPLETED;
    	}
    	else {
    		this.completionState = branchState;
    	}
    }
     
}
