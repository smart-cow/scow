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

package org.wiredwidgets.cow.server.completion;

import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.wiredwidgets.cow.server.api.model.v2.Task;

/**
 *
 * @author JKRANES
 */
public class BypassableActivity extends Activity {

    Activity wrappedActivity;
    Activities activities;
    private ObjectFactory factory = new ObjectFactory();

    public BypassableActivity(Activity activity) {
        wrappedActivity = activity;
        wrappedActivity.setWrapped(true); // avoid infininte loop
        activities = new Activities();
        activities.setSequential(false);
        activities.setMergeCondition("1");
        activities.getActivities().add(factory.createActivity(wrappedActivity));
        
        Task bypassTask = new Task();
        bypassTask.setKey("Bypass " + activity.getKey());
        activities.getActivities().add(factory.createActivity(bypassTask));
    }

    public Activity getActivities() {
        return activities;
    }

    public Activity getWrappedActivity() {
        return wrappedActivity;
    }

}
