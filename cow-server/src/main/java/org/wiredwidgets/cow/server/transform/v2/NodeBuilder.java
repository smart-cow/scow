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

import org.apache.log4j.Logger;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Abstract class that deals with building process nodes based on an input Activity
 * Concrete classes will be responsible for rendering the Activity in a specific BPMN notation scheme
 * such as BPMN2.0 or JPDL
 * Type T is the object type of the node
 * Type S is the type required for use by the surrounding process.  It may be the same as T (in JPDL)
 * or it may be a JAXBElement<T> (in BPMN2.0)
 * Type V is the type of the Activity being rendered
 * @author JKRANES
 */
public abstract class NodeBuilder<T extends Object, S extends Object, V extends Activity> extends AbstractBuilder {

    T node;
    V activity;
    
    Logger log = Logger.getLogger(NodeBuilder.class);
    String linkTransitionName = null;

    public NodeBuilder(ProcessContext context, T node, V activity) {
        super(context);
        this.node = node;
        this.activity = activity;
    }

    public T getNode() {
        return this.node;
    }
    
    protected V getActivity() {
        return this.activity;
    }

    @Override
    public void build() {
        log.debug("Building " + node.getClass().getName() + " at path " + getPath());
        getLinkSources().add(new LinkSource(this, null));
        buildInternal();
        getContext().addNode(createNode());
    }

    /**
     * Template method to be implemented by concrete node builder classes.
     * Handles all details for constructing the node and setting its attributes.
     */
    protected abstract void buildInternal();

    /*
     * Template method to be implemented by subclasses
     */
    protected abstract void addTransition(Builder target, String transitionName);

    /*
     * Optional transformation of the node, by default a null op
     */
    protected S createNode() {
        return (S)node;
    }

    protected void addTransition(Builder target) {
        addTransition(target, null);
    }

    @Override
    public void setLinkTransitionName(String name) {
        linkTransitionName = name;
    }

    @Override
    public void link(Builder builder) {
        if (linkTransitionName != null) {
            addTransition(builder, linkTransitionName);
        } else {
            addTransition(builder);
        }
    }

    @Override
    public void link(Builder builder, String transitionName) {
        if (transitionName == null) {
            link(builder);
        } else {
            addTransition(builder, transitionName);
        }
    }

    /**
     * Subclasses must implement this method
     * @return
     */
    @Override
    public abstract String getLinkTargetName();

    @Override
    public T getLinkTargetNode() {
        return node;
    }

    @Override
    public Builder getLinkTarget() {
        return this;
    }
}
