/**
 * Approved for Public Release: 10-4800. Distribution Unlimited. Copyright 2011
 * The MITRE Corporation, Licensed under the Apache License, Version 2.0 (the
 * "License");
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import java.util.ArrayList;
import java.util.List;

import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class Bpmn20DecisionUserTaskNodeBuilder extends Bpmn20UserTaskNodeBuilder {
    
    private List<Option> options = new ArrayList<Option>();
    
    public static final String DECISION_VAR_NAME = "DecisionVarName";


    public Bpmn20DecisionUserTaskNodeBuilder(ProcessContext context, Decision decision) {
        super(context, decision.getTask());
        addOptions(decision);
    }
    
    public Bpmn20DecisionUserTaskNodeBuilder(ProcessContext context, Loop loop) {
        super(context, loop.getLoopTask());
        addOptions(loop);
    }    
    
    @Override
    protected void buildInternal() {

        super.buildInternal();
                   
        addDataInputFromExpression("Options", getOptionsString());
        String decisionVar = getNode().getId() + "_decision";
        addDataOutputFromProperty(decisionVar, decisionVar);
        
        // We need to be able to know the name of the decision variable
        addDataInputFromExpression(DECISION_VAR_NAME, decisionVar);
        
        // this is picked up in GatewayDecisionActivityBuilder
        setBuildProperty("decisionVar", decisionVar);
  
    }
    
    private void addOptions(Decision decision) {
    	for (Option option : decision.getOptions()) {
    		options.add(option);
    	}
    }
    
    private void addOptions(Loop loop) {
    	Option done = new Option();
    	done.setName(loop.getDoneName());
    	Option repeat = new Option();
    	repeat.setName(loop.getRepeatName());
    	options.add(done);
    	options.add(repeat);
    }

    private String getOptionsString() {
        String optionString = null;
        for (Option option : options) {
            if (optionString == null) {
                optionString = option.getName();
            } else {
                optionString += ("," + option.getName());
            }
        }
        return optionString;
    }
    
   
}
