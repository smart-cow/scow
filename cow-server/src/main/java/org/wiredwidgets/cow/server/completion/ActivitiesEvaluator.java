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

import javax.xml.bind.JAXBElement;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import static org.wiredwidgets.cow.server.completion.CompletionState.*;

@Component
@Scope("prototype")
public class ActivitiesEvaluator extends AbstractEvaluator<Activities> {
    
    @Override
    public void evaluateInternal() {

        int completedCount = 0;
        int openCount = 0;
        
        for (JAXBElement<? extends Activity> jbe : activity.getActivities()) {
            Activity act = jbe.getValue();
            
            evaluate(act);
            CompletionState state = CompletionState.forName(act.getCompletionState());
            if (state == COMPLETED) {
                completedCount++;
            } else if (state == OPEN) {
                openCount++;    
            }
        }
        
        if (completedCount == 0 && openCount == 0) {
        	// have not yet reached this activity
            completionState = branchState;
        }
        else if (activity.getMergeCondition() != null && activity.getMergeCondition().equals("1") && completedCount > 0 ) {
            // special case for 'race condition' sets where completion of of at least one
            // task will complete the set
            completionState = CompletionState.COMPLETED;
        }
        else if (completedCount < activity.getActivities().size()) {
            completionState = CompletionState.OPEN;
        }
        else {
            completionState = CompletionState.COMPLETED;    
        }
    }

	@Override
	protected Class<Activities> getActivityClass() {
		return Activities.class;
	}
}
