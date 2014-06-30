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
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.OPEN;

import javax.xml.bind.JAXBElement;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.graph.builder.ParallelActivitiesGraphBuilder;

@Component
@Scope("prototype")
public class ActivitiesEvaluator extends AbstractEvaluator<Activities> {
    
    @Override
    public void evaluateInternal() {
    	
    	for (JAXBElement<? extends Activity> jbe : activity.getActivities()) {
            evaluate(jbe.getValue());
    	}
    	
    	if (activity.isSequential()) {
    		evaluateSequential();
    	}
    	else {
    		evaluateParallel();
    	}
    }

	@Override
	protected Class<Activities> getActivityClass() {
		return Activities.class;
	}
	
	private void evaluateSequential() {
		Activity first = activity.getActivities().get(0).getValue();
		Activity last = activity.getActivities().get(activity.getActivities().size() - 1).getValue();
		if (last.getCompletionState() == COMPLETED) {
			completionState = COMPLETED;
		}
		else if (first.getCompletionState() == COMPLETED) {
			completionState = OPEN;
		}
		else {
			completionState = first.getCompletionState();
		}
		
	}
	
	private void evaluateParallel() {
		Activity diverging = getGraphActivity(ParallelActivitiesGraphBuilder.getDivergingGatewayName(activity));
		Activity converging = getGraphActivity(ParallelActivitiesGraphBuilder.getConvergingGatewayName(activity));
		if (converging.getCompletionState() == COMPLETED) {
			completionState = COMPLETED;
		}
		else if (diverging.getCompletionState() == COMPLETED) {
			completionState = OPEN;
		}
		else {
			completionState = diverging.getCompletionState();
		}
		
	}
}
