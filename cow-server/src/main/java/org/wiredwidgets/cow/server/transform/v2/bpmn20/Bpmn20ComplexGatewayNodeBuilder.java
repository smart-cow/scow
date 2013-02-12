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

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;
import org.omg.spec.bpmn._20100524.model.TComplexGateway;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TGatewayDirection;

/**
 * Node builder for TParallelGateway
 * @author JKRANES
 */
public class Bpmn20ComplexGatewayNodeBuilder extends Bpmn20GatewayNodeBuilder<TComplexGateway, Activities> {

    public Bpmn20ComplexGatewayNodeBuilder(ProcessContext context, TGatewayDirection direction, Activities activities) {
        super(context, direction, new TComplexGateway(), activities);
    }

    @Override
    protected JAXBElement<TComplexGateway> createNode() {
        return factory.createComplexGateway(getNode());
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();
        // For the converging side, apply optional merge conditions
        // merge condition "1" means only one path must be completed
        if (getNode().getGatewayDirection().equals(TGatewayDirection.CONVERGING) && getActivity().getMergeCondition() != null) {
            // for now this is just a placeholder for the expression
            // TODO: fix this with the correct expression syntax.
            TFormalExpression formalExpr = new TFormalExpression();
            formalExpr.getContent().add(getActivity().getMergeCondition());
            getNode().setActivationCondition(formalExpr);
        }
    }
    
}
