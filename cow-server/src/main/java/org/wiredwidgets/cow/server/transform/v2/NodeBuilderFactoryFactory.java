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

package org.wiredwidgets.cow.server.transform.v2;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Singleton class used to obtain NodeBuilderFactory instances.
 * Calls the supports() method of each factory in turn
 * @author JKRANES
 */
@Component
public class NodeBuilderFactoryFactory {

    private static Logger log = Logger.getLogger(NodeBuilderFactoryFactory.class);

    private Set<NodeBuilderFactory> factories = new HashSet<NodeBuilderFactory>();

    private NodeBuilderFactory getNodeBuilderFactory(Class<? extends ProcessContext> contextClass, NodeType nodeType) {
        log.debug("Finding factory for context " + contextClass.getSimpleName() + ", nodeType " + nodeType);
        NodeBuilderFactory factory = null;
        for (NodeBuilderFactory candidate : factories) {
            if (candidate.supports(contextClass, nodeType)) {
                factory = candidate;
            }
        }
        assert factory != null : "Factory not found: " + nodeType;
        log.debug("Selected: " + factory.getClass().getSimpleName());
        return factory;
    }

    /**
     * Creates a NodeBuilder using the ProcessContext, NodeType and Activity as inputs.
     * Does this by first finding the correct Factory and then calling the createNodeBuilder method
     * on the factory.
     * @param context
     * @param nodeType
     * @param activity
     * @return the NodeBuilder.
     */
    public NodeBuilder createNodeBuilder(ProcessContext context, NodeType nodeType, Activity activity) {
        return getNodeBuilderFactory(context.getClass(), nodeType).createNodeBuilder(context, activity);
    }

    /**
     * Returns the set of all registered factories
     * @return 
     */
    public Set<NodeBuilderFactory> getFactories() {
        return factories;
    }
    
    public void addFactory(NodeBuilderFactory factory) {
    	factories.add(factory);
    }

    /**
     * Setter for the set of Factories
     * @param factories 
     */
    public void setFactories(Set<NodeBuilderFactory> factories) {
        this.factories = factories;
    }



}
