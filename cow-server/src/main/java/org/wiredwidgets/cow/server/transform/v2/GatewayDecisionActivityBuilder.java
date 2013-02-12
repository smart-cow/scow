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

package org.wiredwidgets.cow.server.transform.v2;

import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20DecisionUserTaskNodeBuilder;

/**
 * For BPMN2 we need to use gateway nodes on either side of the options
 * @author JKRANES
 */
public class GatewayDecisionActivityBuilder extends ActivityBuilderImpl<Decision> {

    public GatewayDecisionActivityBuilder(ProcessContext context, Decision decision, ActivityBuilderFactory factory) {
        super(context, decision, factory);
    }

    @Override
    public void build() { 
        Decision source = getActivity();
        // TaskActivityBuilder decisionTaskBuilder = (TaskActivityBuilder)createActivityBuilder(source.getTask());
        
        NodeBuilder decisionTaskBuilder = createNodeBuilder(getContext(), source, NodeType.DECISION_TASK);

        // for BPMN20, the decision task needs to be configured with the set of options
        // as an input variable.   This way, the UI can present the user with a choice
        // of options by using this variable value.  This was not necessary in JPDL as the options were implemented
        // as outgoing transitions from the task itself, whereas in BPMN20 there is a gateway
        // in between the decision task and the option nodes.
//        for (Option option : source.getOptions()) {
//            decisionTaskBuilder.addOption(option.getName());           
//        }
            
        decisionTaskBuilder.build(this);
        setLinkTarget(decisionTaskBuilder);

        NodeBuilder divergingGatewayBuilder = createNodeBuilder(getContext(), source, NodeType.DIVERGING_EXCLUSIVE_GATEWAY);
        
        // a bit of a hack to inject the name of the decision variable into the gateway builder
        divergingGatewayBuilder.setBuildProperty("decisionVar", decisionTaskBuilder.getBuildProperty("decisionVar"));
           
        divergingGatewayBuilder.build(this);

        decisionTaskBuilder.link(divergingGatewayBuilder);

        NodeBuilder convergingGatewayBuilder = createNodeBuilder(getContext(), source, NodeType.CONVERGING_EXCLUSIVE_GATEWAY);

        convergingGatewayBuilder.build(this);

        // note that BPMN2.0 requires at least two paths to or from a gateway.
        // So the xml schema has minoccurs=2 for Option.
        for (Option option : source.getOptions()) {
            // The option may have no activity -- this expresses a 'do nothing' workflow path
            if (option.getActivity() != null) {
                ActivityBuilder optionBuilder = createActivityBuilder(option.getActivity().getValue());
                optionBuilder.build(this);
                divergingGatewayBuilder.link(optionBuilder, option.getName());
                optionBuilder.link(convergingGatewayBuilder);  
            }
            else {
                // link directly to the converging gateway
                divergingGatewayBuilder.link(convergingGatewayBuilder, option.getName());
            }
        }
        // the converging gateway will act as the link source
        setLinkSource(convergingGatewayBuilder);
    }
}
