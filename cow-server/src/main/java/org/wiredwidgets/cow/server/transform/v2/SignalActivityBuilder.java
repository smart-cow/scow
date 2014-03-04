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

package org.wiredwidgets.cow.server.transform.v2;

import org.omg.spec.bpmn._20100524.model.TGatewayDirection;
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ParallelGatewayNodeBuilder;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20SignalEventNodeBuilder;

/**
 *
 * @author JKRANES
 */
public class SignalActivityBuilder extends ActivityBuilderImpl<Signal> {
    
    public SignalActivityBuilder(ProcessContext context, Signal signal, SignalActivityBuilderFactory factory) {
        super(context, signal, factory);
    }

    @Override
    public void build() {
    	
        Builder signalBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.SIGNAL);
        signalBuilder.build(this);    	
    	
        // pass null as the Activity (it's not used anyway)
        // The constructor expects instance of Activities which we don't have here.
        Builder gatewayBuilder = this.createNodeBuilder(getContext(), null, NodeType.CONVERGING_PARALLEL_GATEWAY);
        gatewayBuilder.build(this);

        signalBuilder.link(gatewayBuilder);              	
    	
        setLinkTarget(gatewayBuilder); // previous node links to the gateway
        setLinkSource(gatewayBuilder); // gateway links to the next node
    }
}
