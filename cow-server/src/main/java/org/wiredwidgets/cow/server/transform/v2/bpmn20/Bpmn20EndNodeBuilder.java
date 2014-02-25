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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;
import org.omg.spec.bpmn._20100524.model.TEndEvent;
import org.omg.spec.bpmn._20100524.model.TTerminateEventDefinition;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Node builder for TEndEvent
 * @author JKRANES
 */
public class Bpmn20EndNodeBuilder extends Bpmn20FlowNodeBuilder<TEndEvent, Activity> {

    public Bpmn20EndNodeBuilder(ProcessContext context) {
        super(context, new TEndEvent(), null);
    }

    @Override
    protected JAXBElement<TEndEvent> createNode() {
        return factory.createEndEvent(getNode());
    }

    @Override
    protected void buildInternal() {
        setId();
        getNode().setName("end");
        
        // NOTE: See JBPM documentation for the distinction between Terminating and
        // non terminating end events.  Terminating end events will also terminate
        // any parent process if this process is being called as a sub-process.  In order
        // to avoid this, we make the end event non terminating, and we precede it with
        // a converging exclusive gateway, so that all exit paths lead to the same point.
        
        getNode().getEventDefinitions().add(factory.createTerminateEventDefinition(new TTerminateEventDefinition()));
    }

}
