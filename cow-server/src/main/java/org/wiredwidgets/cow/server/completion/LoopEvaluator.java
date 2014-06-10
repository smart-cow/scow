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

package org.wiredwidgets.cow.server.completion;

import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.COMPLETED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.OPEN;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.transform.graph.builder.LoopGraphBuilder;

/**
 *
 * @author JKRANES
 */
@Component
@Scope("prototype")
public class LoopEvaluator extends AbstractEvaluator<Loop> {

    @Override
    protected void evaluateInternal() {
        // evaluate(this.activity.getLoopTask());
        evaluate(this.activity.getActivity().getValue());
        
        CompletionState loopTaskCompletionState = this.activity.getLoopTask().getCompletionState();
        
        Activity diverging = getGraphActivity(LoopGraphBuilder.getDivergingGatewayName(activity));
        Activity converging = getGraphActivity(LoopGraphBuilder.getConvergingGatewayName(activity));
        
        if (converging.getCompletionState() != COMPLETED) {
        	// we have not yet reached the start of the loop
        	completionState = converging.getCompletionState();
        }
        else if (diverging.getCompletionState() != COMPLETED) {
        	// we are in the loop for the first time but have not finished
        	// we could be in the middle of the loop activity or we could be
        	// waiting for a decision.  Either way the loop is OPEN
        	completionState = OPEN;
        }
        else {
        	// we have completed at least one full pass; we may have exited
        	// or we may be back in the loop for another pass.
        	
        	if (loopTaskCompletionState == OPEN) {
        		// we are waiting for a decision
        		completionState = OPEN;
        	}
        	else {
        		// Set the completion state to that of the loop activity
        		// this should be either OPEN or COMPLETE
        		completionState = this.activity.getActivity().getValue().getCompletionState();
        	}
        }
        
    }
    
	@Override
	protected Class<Loop> getActivityClass() {
		return Loop.class;
	}
    
}
