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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.transform.graph.builder.BypassGraphBuilder;

public abstract class AbstractEvaluator<T extends Activity> implements Evaluator<T> {
	
	private final Logger log = LoggerFactory.getLogger(AbstractEvaluator.class);

    @Autowired
    private EvaluatorFactory factory;
    
    protected T activity;
    protected ProcessInstanceInfo info;
    protected CompletionState completionState = null;
    protected String processInstanceId;
    // protected CompletionState branchState;
    // protected boolean inLoop = false;
    
    protected abstract Class<T> getActivityClass();   

    @Override
    public final void evaluate() {
    	
    		evaluateInternal();
    		
    		if (activity.isBypassable()) {
    			Activity converging = getGraphActivity(BypassGraphBuilder.getBypassConvergingGatewayName(activity));
    			// if the bypass task is completed then the converging gateway will have been completed
    			if (converging.getCompletionState() != null && converging.getCompletionState() == CompletionState.COMPLETED) {
    				// either the activity itself was completed, or the bypass was invoked
    				// either way it's COMPLETED
    				completionState = CompletionState.COMPLETED;
    			}
    		}
    	
	        if (completionState != null) {
	        	activity.setCompletionState(completionState);
	        }
    }

    /*
     * To be implemented by subclasses.  This method should set the values of protected
     * variables.
     */
    protected abstract void evaluateInternal();

    protected final void evaluate(Activity activity) {
        factory.getEvaluator(processInstanceId, activity, info).evaluate();
    }

    protected final void setCompletionState(CompletionState state) {
        completionState = state;
    }

    @Override
    public void setActivity(T activity) {
        this.activity = activity;
    }

    @Override
    public void setHistory(ProcessInstanceInfo history) {
        this.info = history;
    }

    @Override
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
//    @Override
//    public void setBranchState(CompletionState branchState) {
//    	this.branchState = branchState;
//    }
    
//    public void setInLoop(boolean inLoop) {
//    	this.inLoop = inLoop;
//    }
//    
//    public boolean isInLoop() {
//    	return inLoop;
//    }
    
    protected Activity getGraphActivity(String name) {
    	for (Activity activity : info.getGraph().vertexSet()) {
    		if (activity.getName().equals(name)) {
    			return activity;
    		}
    	}
    	log.error("Activity not found in graph: " + name);
    	return null;
    }
    
    
}
