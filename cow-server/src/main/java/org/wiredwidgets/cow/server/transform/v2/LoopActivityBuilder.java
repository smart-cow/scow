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
import org.wiredwidgets.cow.server.api.model.v2.Loop;

/**
 * A Loop is implemented as an Activity followed by a Task in which a decision is made
 * This requires multiple lastNode instances
 * @author JKRANES
 */
public class LoopActivityBuilder extends ActivityBuilderImpl<Loop> {

    private static Logger log = Logger.getLogger(LoopActivityBuilder.class);

    public LoopActivityBuilder(ProcessContext context, Loop loop, ActivityBuilderFactory factory) {
        super(context, loop, factory);
    }

    @Override
    public void build() {
        Loop source = getActivity();
        log.debug("Loop type: " + source.getClass().getName());
        log.debug("Activity type: " + source.getActivity().getValue().getClass().getName());
        log.debug("task name: " + source.getLoopTask().getName());
        ActivityBuilder builder = createActivityBuilder(source.getActivity().getValue());

        builder.build(this);

        setLinkTarget(builder);

        // Following the Activity we insert a Task for making a decision
        // whether to repeat the Activity or continue
        ActivityBuilder taskBuilder = createActivityBuilder(source.getLoopTask());
        taskBuilder.build(this);

        // Use a named transition for the continue path
        // This name should be used in the UI
        // NOTE: this MUST go after the build() call
        taskBuilder.setLinkTransitionName(source.getDoneName());
   
        builder.link(taskBuilder);

        // Link the transition back to the activity.  This creates a loop path
        // Use a named transition for the loop repeat.  The transition name
        // should be used in the UI
        taskBuilder.link(builder, source.getRepeatName());

        setLinkSource(taskBuilder);
    }
}
