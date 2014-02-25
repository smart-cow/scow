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


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import static org.wiredwidgets.cow.server.completion.CompletionState.*;

@Component
@Scope("prototype")
public class DecisionEvaluator extends AbstractEvaluator<Decision> {

    @Override
    protected void evaluateInternal() {
    	
    	Task decisionTask = activity.getTask();
    	   	
        // first, evaluate the decision task
        evaluate(this.activity.getTask());      
        CompletionState decisionTaskCompletionState = CompletionState.forName(decisionTask.getCompletionState());
          
        switch(decisionTaskCompletionState) {
        	case PLANNED :
        		completionState = PLANNED;
        		branchState = CONTINGENT;
        		evaluateBranches();
        		break;
        	case PRECLUDED :
        		completionState = PRECLUDED;
        		evaluateBranches();
        		break;
        	case OPEN :
        		completionState = OPEN;
        		branchState = CONTINGENT;
        		evaluateBranches();
        		break;
        	case CONTINGENT :
        		completionState = CONTINGENT;
        		branchState = CONTINGENT;
        		evaluateBranches();
        		break;
        	case COMPLETED :
        		String choice = getChosenOption();
        		boolean isCompleted = true; // until we discover otherwise
                for (Option option : activity.getOptions()) {
                	if (option.getName().equals(choice)) {
                		branchState = PLANNED;
                	}
                	else {
                		branchState = PRECLUDED;
                	}
                    evaluate(option.getActivity().getValue());
                    
                    if (CompletionState.forName(option.getActivity().getValue().getCompletionState()) == CompletionState.OPEN) {
                        isCompleted = false;
                    }

                }
                completionState = (isCompleted ? CompletionState.COMPLETED : OPEN);
                break;
        }
	
    }
    
    private void evaluateBranches() {
        for (Option option : activity.getOptions()) {
            evaluate(option.getActivity().getValue());
        }
    }
    
    private String getChosenOption() {
    	String varName = activity.getTask().getKey() + "_decision";
    	return info.getVariables().get(varName);
    }

	@Override
	protected Class<Decision> getActivityClass() {
		return Decision.class;
	}
}
