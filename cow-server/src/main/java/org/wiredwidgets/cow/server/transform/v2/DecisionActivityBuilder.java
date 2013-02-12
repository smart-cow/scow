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

import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;

/**
 * A Decision is implemented as a task with transitions to multiple Activities
 * This requires multiple lastNode instances
 * @author JKRANES
 */
public class DecisionActivityBuilder extends ActivityBuilderImpl<Decision> {

    public DecisionActivityBuilder(ProcessContext context, Decision decision, ActivityBuilderFactory factory) {
        super(context, decision, factory);
    }

    @Override
    public void build() { 
        Decision source = getActivity();
        NodeBuilder taskBuilder = getFactory().createNodeBuilder(getContext(), NodeType.TASK, source.getTask());
        taskBuilder.build(this);
        setLinkTarget(taskBuilder);
        
        for (Option option : getActivity().getOptions()) {
            if (option.getActivity() != null) {
                ActivityBuilder builder = createActivityBuilder(option.getActivity().getValue());
                builder.build(this);
                // link the task to each option using the option name
                taskBuilder.link(builder, option.getName());
                // Each decision path is a link source
                getLinkSources().addAll(builder.getLinkSources());
            }
            else {
                // This is a null (do nothing) option
                // So we link directly from the task, using the option name
                getLinkSources().add(new LinkSource(taskBuilder, option.getName()));
            }
        }
    }
}
