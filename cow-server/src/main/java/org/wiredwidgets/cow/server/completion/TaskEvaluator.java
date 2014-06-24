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

import javax.xml.datatype.DatatypeConstants;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;

@Component
@Scope("prototype")
public class TaskEvaluator extends AbstractEvaluator<Task> {
	
	private static Logger log = Logger.getLogger(TaskEvaluator.class);

    private List<HistoryActivity> historyActivities;

    @Override
    protected void evaluateInternal() {
        // this.historyActivities = info.getActivities(activity.getKey());
    	// should be handled by graph evaluator
        /// this.completionState = getCompletionState();
        
        // needed for the summary
        // activity.setCompletionState(this.completionState);
        
        if  (activity.getAssignee() != null) {
        	info.updateUserSummary(activity.getAssignee(), activity);
        }
        else {
        	info.updateGroupSummary(activity.getCandidateGroups(), activity);
        }
        
    }

//    private CompletionState getCompletionState() {
//
//        // Because of possible looping structures, there may be more than once instance of a task activity
//        
//        if (historyActivities.isEmpty()) {
//        	log.debug("No history for task, returning branch state: " + branchState);
//            return branchState;
//        } else {
//            boolean isCompleted = true;
//            for (HistoryActivity historyActivity : historyActivities) {
//                if (historyActivity.getEndTime() == null) {
//                    activity.setCreateTime(historyActivity.getStartTime());
//                    activity.setEndTime(null);
//                    isCompleted = false;
//                } else {
//                    if (activity.getCreateTime() == null) {
//                        // this is the first history instance -- set activity properties to match
//                        activity.setCreateTime(historyActivity.getStartTime());
//                        activity.setEndTime(historyActivity.getEndTime());
//                    } else {
//                        // not the first. Determine whether this new history instance is later than
//                        // the current values set for the activity.
//                        int result = activity.getCreateTime().compare(historyActivity.getStartTime());
//                        if (result == DatatypeConstants.LESSER) {
//                            activity.setCreateTime(historyActivity.getStartTime());
//                            activity.setEndTime(historyActivity.getEndTime());
//                        }
//                    }
//                }
//            }
//            
//            return (isCompleted ? CompletionState.COMPLETED : CompletionState.OPEN);
//        }
//    }

	@Override
	protected Class<Task> getActivityClass() {
		return Task.class;
	}
}
