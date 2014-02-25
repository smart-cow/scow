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

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Activities;

/**
 * Builds a set of parallel activities.  This corresponds to the "sequential=false" option in the
 * Activities class.
 * @author JKRANES
 */
public class ParallelSetActivityBuilder extends ActivityBuilderImpl<Activities> {

    public ParallelSetActivityBuilder(ProcessContext context, Activities activities, ActivityBuilderFactory factory) {
        super(context, activities, factory);
    }

    /**
     * A parallel set consists of a Fork node, a set of activities, and a Join node.
     * by default, a JPDL Join node requires that all parallel executions must be
     * completed before the process continues.
     */
    @Override
    public void build() {
    	
    	int activityCount = getActivity().getActivities().size();
    	
    	NodeBuilder forkBuilder = null;
    	NodeBuilder joinBuilder = null;

    	if (activityCount > 1) {   	
	        forkBuilder = this.createNodeBuilder(getContext(), null, NodeType.DIVERGING_PARALLEL_GATEWAY);
	        forkBuilder.build(this);
	        setLinkTarget(forkBuilder);
	        
	        joinBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.CONVERGING_PARALLEL_GATEWAY);
	        joinBuilder.build(this);
	        setLinkSource(joinBuilder);	                
    	}
    	
        for (JAXBElement<? extends Activity> element : getActivity().getActivities()) {
            Activity act = element.getValue();
            ActivityBuilder builder = createActivityBuilder(act);
            builder.build(this);
            
            if (activityCount > 1) {    
            	forkBuilder.link(builder);
            	builder.link(joinBuilder);
            }
            else {
            	// there is only one activity.  Link directly to it without a gateway.
            	setLinkTarget(builder);
            	setLinkSource(builder);
            }
            
        }
    }
}
