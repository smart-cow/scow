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

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 *
 * @author JKRANES
 */
public abstract class AbstractNodeBuilderFactory<T extends NodeBuilder, S extends Activity> implements NodeBuilderFactory<T, S>, InitializingBean {

	@Autowired
	NodeBuilderFactoryFactory ff;
	
	
    private Set<NodeType> nodeTypes = EnumSet.noneOf(NodeType.class);
    private Class<? extends ProcessContext> contextType;

    public AbstractNodeBuilderFactory(NodeType nodeType, Class<? extends ProcessContext> contextType) {
        this.nodeTypes.add(nodeType);
        this.contextType = contextType;
    }
    
    @Override
    public void afterPropertiesSet() {
    	ff.addFactory(this);
    }

    protected void addNodeType(NodeType nodeType) {
        this.nodeTypes.add(nodeType);
    }

    @Override
    public boolean supports(Class<? extends ProcessContext> contextType, NodeType nodeType) {
        return (this.supportsContext(contextType) && this.supportsNodeType(nodeType));
    }

    public boolean supportsContext(Class<? extends ProcessContext> contextType) {
        return (contextType.equals(this.contextType));
    }

    public boolean supportsNodeType(NodeType nodeType) {
        return  this.nodeTypes.contains(nodeType);
    }

}
