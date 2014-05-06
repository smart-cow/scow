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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.*;
import org.wiredwidgets.cow.server.api.model.v2.Loop;

/**
 *
 * @author JKRANES
 */
@Component
@Scope("prototype")
public class LoopEvaluator extends AbstractEvaluator<Loop> {

    @Override
    protected void evaluateInternal() {
        evaluate(this.activity.getLoopTask());
        evaluate(this.activity.getActivity().getValue());
        
        CompletionState loopTaskCompletionState = this.activity.getLoopTask().getCompletionState();
        
        switch (loopTaskCompletionState) {
        	case OPEN:
        		// waiting on the loop decision
        		completionState = OPEN;
        		break;
        	case COMPLETED:
        		// are we done or are we repeating the loop again?
        		completionState = isRepeat() ? OPEN : COMPLETED;
        		break;
        	case PLANNED:
        		completionState = this.activity.getActivity().getValue().getCompletionState();
        		
        		if (completionState.equals(COMPLETED)) {
        			// this is not realistic but code for it anyway
        			// the activity is COMPLETED but decision task not yet OPEN for some reason?
        			completionState = OPEN;
        		}
        		break;
        	default:
        		completionState = branchState;
        		break;
        				
        }


    }
    
    private boolean isRepeat() {
    	String varName = activity.getLoopTask().getKey() + "_decision";
    	String decision = info.getVariables().get(varName);
    	return decision == null ? false : decision.equals(activity.getRepeatName());
    }

	@Override
	protected Class<Loop> getActivityClass() {
		return Loop.class;
	}
    
}
