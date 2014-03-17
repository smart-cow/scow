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
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.SignalNodeBuilder;

@Component
@Scope("prototype")
public class SignalEvaluator extends AbstractEvaluator<Signal> {
    
    @Override
    protected void evaluateInternal() {
    	
    	String signalVar = info.getVariables().get(SignalNodeBuilder.getVarName(activity));
    	if (signalVar != null) {	
    		this.completionState = CompletionState.COMPLETED;
    	}
    	else {
    		this.completionState = branchState;
    	}
    }

	@Override
	protected Class<Signal> getActivityClass() {
		return Signal.class;
	}
     
}
