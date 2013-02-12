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

import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.wiredwidgets.cow.server.api.model.v2.Task;

/**
 *
 * @author JKRANES
 */
public class BypassableActivityBuilder extends ActivityBuilderImpl<Activity> {

    private ObjectFactory factory = new ObjectFactory();

    public BypassableActivityBuilder(ProcessContext context, Activity activity, ActivityBuilderFactory factory) {
        super(context, activity, factory);
    }

    @Override
    protected void build() {

            // wrap the activity in a parallel Activities object, consisting of the
            // original activity and a Task that can be used to bypass the activity.
            Activities activities = new Activities();
            activities.setSequential(false);
            // a mergeCondition of 1 indicates that only one of the set must complete
            activities.setMergeCondition("1");
            activities.getActivities().add(factory.createActivity(getActivity()));
            
            // create a task that can be used to bypass the activity
            Task task = new Task();
            task.setName("Bypass " + getActivity().getName());
            task.setCandidateGroups(getContext().getSource().getBypassCandidateGroups());
            task.setCandidateUsers(getContext().getSource().getBypassCandidateUsers());
            task.setAssignee(getContext().getSource().getBypassAssignee());
            activities.getActivities().add(factory.createTask(task));

            ActivityBuilder builder = createActivityBuilder(activities);
            setLinkSource(builder);
            setLinkTarget(builder);

            // Set the wrapped flag so we don't wrap it again next time we see it
            getActivity().setWrapped(true);
            builder.build(this);
            getActivity().setWrapped(false);
    }
}
