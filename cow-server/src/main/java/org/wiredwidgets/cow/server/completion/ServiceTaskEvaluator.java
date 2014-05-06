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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;

@Component
@Scope("prototype")
public class ServiceTaskEvaluator extends AbstractEvaluator<ServiceTask> {

    private List<HistoryActivity> historyActivities;

    @Override
    protected void evaluateInternal() {
        this.historyActivities = info.getActivities(activity.getKey());
        this.completionState = getCompletionState();
    }

    private CompletionState getCompletionState() {

        // Because of possible looping structures, there may be more than once instance of a task activity
        if (historyActivities.isEmpty()) {
            return branchState;
        }
        else {
            boolean isCompleted =  true;
            for (HistoryActivity historyActivity : historyActivities) {
                if (historyActivity.getEndTime() == null) {
                    isCompleted = false;
                }
            }
            return (isCompleted ? CompletionState.COMPLETED : CompletionState.OPEN);
        }
    }

	@Override
	protected Class<ServiceTask> getActivityClass() {
		return ServiceTask.class;
	}
}
