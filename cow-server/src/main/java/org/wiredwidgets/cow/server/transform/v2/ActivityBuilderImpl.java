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

import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Abstract base implementation for ActivityBuilder subclasses
 * @author JKRANES
 */
public abstract class ActivityBuilderImpl<T extends Activity> extends AbstractBuilder implements ActivityBuilder<T> {

    private T activity;
    private Builder linkTarget = null;
    private ActivityBuilderFactory<T> factory;

    public ActivityBuilderImpl(ProcessContext context, T activity, ActivityBuilderFactory<T> factory) {
        super(context);
        this.activity = activity;
        this.factory = factory;
    }

    public ActivityBuilderFactory<T> getFactory() {
        return factory;
    }
    
    protected T getActivity() {
        return activity;
    }

    protected NodeBuilder createNodeBuilder(ProcessContext context, Activity activity, NodeType nodeType) {
        return factory.createNodeBuilder(context, nodeType, activity);
    }

    protected ActivityBuilder createActivityBuilder(Activity activity) {  
        return factory.createActivityBuilderFromFactory(getContext(), activity);
    }



    /**
     * Implementation delegates to the link target
     * @return
     */
    @Override
    public String getLinkTargetName() {
        return getLinkTarget().getLinkTargetName();
    }

    /**
     * Implementation delegates to the link target
     * @return
     */
    @Override
    public Object getLinkTargetNode() {
        return getLinkTarget().getLinkTargetNode();
    }

    /**
     * Propagate to the link target
     * @param name
     */
    @Override
    public void setLinkTransitionName(String name) {
        getLinkTarget().setLinkTransitionName(name);
    }

    /**
     * Implementation delegates to the link target
     * @return
     */
    @Override
    public Builder getLinkTarget() {
        return linkTarget.getLinkTarget();
    }

    /**
     * Sets the target to which the preceding builder should link.  Each builder will create one or more
     * child builders, either node or activity builder (or both).  The link target should be set to the first of the child builders
     * (node or activity) in the execution sequence.  
     * @param builder
     */
    protected void setLinkTarget(Builder builder) {
        linkTarget = builder;
    }
}
