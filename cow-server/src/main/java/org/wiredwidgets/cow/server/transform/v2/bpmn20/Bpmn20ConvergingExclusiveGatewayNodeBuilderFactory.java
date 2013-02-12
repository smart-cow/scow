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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import org.omg.spec.bpmn._20100524.model.TGatewayDirection;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.v2.NodeType;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
@Component
public class Bpmn20ConvergingExclusiveGatewayNodeBuilderFactory extends Bpmn20NodeBuilderFactory<Bpmn20ExclusiveGatewayNodeBuilder, Activity> {

    public Bpmn20ConvergingExclusiveGatewayNodeBuilderFactory() {
        super(NodeType.CONVERGING_EXCLUSIVE_GATEWAY);
    }
    
    @Override
    public Bpmn20ExclusiveGatewayNodeBuilder createNodeBuilder(ProcessContext context, Activity activity) {
        return new Bpmn20ExclusiveGatewayNodeBuilder(context, TGatewayDirection.CONVERGING);
    }

}
