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

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 *
 * @author JKRANES
 */
public class ComplexSetActivityBuilder extends ActivityBuilderImpl<Activities> {

    public ComplexSetActivityBuilder(ProcessContext context, Activities activities, ActivityBuilderFactory factory) {
        super(context, activities, factory);
    }

    @Override
    protected void build() {

        // NodeBuilder forkBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.DIVERGING_COMPLEX_GATEWAY);
    	NodeBuilder forkBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.DIVERGING_PARALLEL_GATEWAY);

        forkBuilder.build(this);
        setLinkTarget(forkBuilder);

        // NodeBuilder joinBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.CONVERGING_COMPLEX_GATEWAY);
        NodeBuilder joinBuilder = this.createNodeBuilder(getContext(), getActivity(), NodeType.CONVERGING_EXCLUSIVE_GATEWAY);
        joinBuilder.build(this);
        setLinkSource(joinBuilder);

        for (JAXBElement<? extends Activity> element : getActivity().getActivities()) {
            Activity act = element.getValue();
            ActivityBuilder builder = createActivityBuilder(act);
            builder.build(this);
            forkBuilder.link(builder);
            builder.link(joinBuilder);
        }
    }

}
