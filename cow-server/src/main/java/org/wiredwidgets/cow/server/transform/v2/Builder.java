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

package org.wiredwidgets.cow.server.transform.v2;

import java.util.List;

/**
 * Root interface for builder hierarchy
 * @author JKRANES
 */
public interface Builder {
 
    /**
     * Triggers building of this builder and cascades to include building of all its children
     * @param parent the parent of this builder, used to keep track of nesting levels
     */
    public void build(Builder parent);

    /**
     * Returns the full path of this builder in xx.yy.zz format
     * @return
     */
    public String getPath();

    public void setParent(Builder parent);

    public int addChild(Builder child);

    /**
     * The link method is used express a connection between workflow nodes.  Ultimately, links must be implemented by
     * NodeBuilders, as only these can create the actual connections.  Calling this method on an Activity builder will
     * cause it to delegate to the appropriate NodeBuilder.
     * @param target
     */
    public void link(Builder target);

    /**
     * Allows for specification of a named transition.
     * NOTE: this takes priority over the name set in setLinkTransitionName
     * @param target
     * @param transitionName
     */
    public void link(Builder target, String transitionName);
    
    /**
     * The name to use when linking  to this Builder instance
     * Should be the name of the first process node managed by this builder
     * Each builder must have exactly one entry point to which the transition from
     * the previous node will be directed.
     * This is needed as a workaround for JDPL nodes which have no common ancestor and thus
     * no way to generically get the node name from an object node reference, as
     * we can do with BPMN20
     * @return
     */
    public String getLinkTargetName();

    /**
     * Object reference to the node itself
     * @return
     */
    public Object getLinkTargetNode();

    /**
     * Optional value to use if a named transition is desired.
     * If specified, will be used for transitions FROM this builder
     * This should be called AFTER the call to build()
     */
    public void setLinkTransitionName(String name);

    /**
     * Returns the actual target of any connections made to this Builder.  For example, in the case of a Sequential List, 
     * the target will be the first item in the list.
     * @return
     */
    public Builder getLinkTarget();

    /**
     * This value specifies all child builders for this builder that will serve as link sources
     * from this builder to the builder to which this builder is linked.  If this builder creates a sequence
     * of child builders, then the linkSources would include only the last builder in the sequence.
     * If this builder creates a parallel set of child builders, then the linkSources would include all
     * of the child builders.  
     * @return
     */
    public List<LinkSource> getLinkSources();

    /**
     * Convenience syntax for a single linkSource
     */

    public void setLinkSource(Builder builder);

    public void setLinkSource(Builder builder, String name);

    Builder getParent();
    
    /**
     * Generic method for getting properties set during the build
     * @param key
     * @return 
     */
    public Object getBuildProperty(String key);
    
    /**
     * Generic method for setting properties during the build
     * for later use
     * @param key
     * @param object 
     */
    public void setBuildProperty(String key, Object object);

}
