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

package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.omg.spec.bpmn._20100524.model.TUserTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Variable;
import org.wiredwidgets.cow.server.transform.graph.activity.DecisionTask;

@Component
public class DecisionTaskNodeBuilder extends AbstractUserTaskNodeBuilder<DecisionTask> {
	
	public static final String DECISION_VAR_NAME = "DecisionVarName";
	public static final String DECISION_QUESTION = "DecisionQuestion";
	public static final String OPTIONS = "Options";
	
	@Override
	public void buildInternal(TUserTask t, DecisionTask source,
			Bpmn20ProcessContext context) {
		
		super.buildInternal(t, source, context);
		
        String questionText = null;
        if (source.getQuestion() == null) {
        	questionText = source.getName();
        }
        else {
        	questionText = source.getQuestion();
        }
        
        addDataInputFromExpression(DECISION_QUESTION, questionText, t);
        addDataInputFromExpression(OPTIONS, getOptionsString(source), t);
	
        // We need to be able to know the name of the decision variable
		String decisionVarName = getDecisionVarName(t.getId());
		addDataInputFromExpression(DECISION_VAR_NAME, decisionVarName, t);
        addDataOutputFromProperty(decisionVarName, decisionVarName, t, context);
         		
	}	

	@Override
	public Class<DecisionTask> getType() {
		return DecisionTask.class;
	}
	
	/**
	 * Builds the decison variable name based on the node ID
	 * @param id
	 * @return
	 */
	public static String getDecisionVarName(String id) {
		// return id + "_decision";
		
		// strip out "-" characters as they are not valid in java variables
		String name = "";
		for (String s : id.split("-")) {
			name += s;
		}
		return "var_" + name + "_decision";
	}
	
    private String getOptionsString(DecisionTask dt) {
        String optionString = null;
        for (String option : dt.getOptions()) {
            if (optionString == null) {
                optionString = option;
            } else {
                optionString += ("," + option);
            }
        }
        return optionString;
    }	

}
