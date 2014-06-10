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


import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.COMPLETED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.CONTINGENT;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.OPEN;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PLANNED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PRECLUDED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.graph.builder.DecisionGraphBuilder;

@Component
@Scope("prototype")
public class DecisionEvaluator extends AbstractEvaluator<Decision> {
	
	private final Logger log = LoggerFactory.getLogger(DecisionEvaluator.class);

    @Override
    protected void evaluateInternal() {
    	
    	Task decisionTask = activity.getTask();
    	   	
        // first, evaluate the decision task
    	// already done during graph evaluation
        // evaluate(this.activity.getTask());      
        CompletionState decisionTaskCompletionState = decisionTask.getCompletionState();
        
        if (decisionTaskCompletionState != COMPLETED) {
        	completionState = decisionTaskCompletionState;
        }
        else {	
        	// get the converging gateway that closes the decision structure
        	Activity converging = getGraphActivity(DecisionGraphBuilder.getConvergingGatewayName(activity));
        	if (activity == null) {
        		log.error("No converging gateway found for decision!!!");
        		return;
        	}
        	
        	if (converging.getCompletionState() == COMPLETED) {
        		// we have moved through the entire decision
        		completionState = COMPLETED;
        	}
        	else {
        		// we are somewhere in between the task and final completion
        		completionState = OPEN;
        	}
        }
        evaluateBranches();
    }
    
    private void evaluateBranches() {
        for (Option option : activity.getOptions()) {
            evaluate(option.getActivity().getValue());
        }
    }
    
	@Override
	protected Class<Decision> getActivityClass() {
		return Decision.class;
	}
}
