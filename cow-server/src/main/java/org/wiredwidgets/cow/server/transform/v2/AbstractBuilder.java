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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Exit;


/**
 * Abstract implementation to handle some common functions used by Builder subclasses
 * @author JKRANES
 */
public abstract class AbstractBuilder implements Builder {

    private Builder parent = null;
    private int childNumber;
    private int nextChild = 1;
    private ProcessContext context;
    private List<LinkSource> sources = new ArrayList<LinkSource>();
    private Map<String, Object> buildProperties = new HashMap<String, Object>();

    public AbstractBuilder(ProcessContext context) {
        this.context = context;
    }

    protected ProcessContext getContext() {
        return context;
    }

    @Override
    public void build(Builder parent) {
        this.parent = parent;
        if (parent != null) {
            childNumber = parent.addChild(this);
        } else {
            childNumber = 1;
        }
        build();
    }

    @Override
    public void setParent(Builder parent) {
        this.parent = parent;
    }

    @Override
    public Builder getParent() {
        return parent;
    }

    /**
     * Template method to be implemented by concrete classes.
     * The implementation of this method is expected to do the following:
     * (1) Create one or more child builders
     * (2) If the builder creates multiple child builders, it must create links between sibling builders,
     *     by invoking link()
     * (3) Invoke setFirstBuilder() passing a reference to the child builder to which links to this
     *     builder should be directed (i.e. the first child in the workflow sequence)
     * (4) Invoke getLinkSources().add() or .addAll() as appropriate (see doc for getLinkSources)
     */
    protected abstract void build();

    /**
     * Convenience method, calls link(target, null)
     * @param target
     */
    @Override
    public void link(Builder target) {
        for (LinkSource source : sources) {
            if (source.getName() == null) {
                source.getBuilder().link(target);
            }
            else {
                source.getBuilder().link(target, source.getName());
            }
        }
    }

    /**
     * Link this builder to the specified target
     * @param target the target node to link to
     */
    @Override
    public void link(Builder target, String transitionName) {
        for (LinkSource source : sources) {
            source.getBuilder().link(target, transitionName);
        }
    }

    @Override
    public String getPath() {
        if (parent != null) {
            return parent.getPath() + "." + childNumber;
        } else {
            return String.valueOf(childNumber);
        }
    }

    @Override
    public int addChild(Builder builder) {
        builder.setParent(this);
        return nextChild++;
    }

    @Override
    public List<LinkSource> getLinkSources() {
        return this.sources;
    }

    @Override
    public void setLinkSource(Builder builder) {
        sources.add(new LinkSource(builder,null));
        assert(sources.size() == 1);
    }
    
    @Override
    public void setLinkSource(Builder builder, String name) {
        sources.add(new LinkSource(builder,name));
        assert(sources.size() == 1);
    }

    @Override
    public Object getBuildProperty(String key) {
        return buildProperties.get(key);
    }

    @Override
    public void setBuildProperty(String key, Object value) {
        buildProperties.put(key, value);
    }
    
    protected boolean containsExit(Activity activity) {
    	if (activity instanceof Exit) {
    		return true;
    	}
    	else if (activity instanceof Activities) {
			for (JAXBElement<? extends Activity> element : ((Activities)activity).getActivities()) {
				if (containsExit(element.getValue())) {
					return true;
				}
			}
			return false;
    	}
    	return false;
    }  
    
    protected boolean containsNonExit(Activity activity) {
    	if (activity instanceof Exit) {
    		return false;
    	}
    	else if (activity instanceof Activities && ((Activities)activity).getActivities().size() > 0) {
    		// at least one must be a non exit
    		
    		Activities activities = (Activities)activity;
    		
    		if (activities.isSequential()) {
    			// NONE can be an Exit
				for (JAXBElement<? extends Activity> element : ((Activities)activity).getActivities()) {
					if (!containsNonExit(element.getValue())) {
						return false;
					}
				}  
				return true;
    		}
    		else {
	    		// AT LEAST ONE must be a non exit
				for (JAXBElement<? extends Activity> element : ((Activities)activity).getActivities()) {
					if (containsNonExit(element.getValue())) {
						return true;
					}
				}
				return false;
    		}
    	}
    	return true;    	
    }
    
    protected int countNonExitPaths(List<JAXBElement<? extends Activity>> activities) {
    	int count = 0;
        for (JAXBElement<? extends Activity> element : activities) {
            if (containsNonExit(element.getValue())) {
            	count++;
            }
        }
        return count;
    }
    
    
}
