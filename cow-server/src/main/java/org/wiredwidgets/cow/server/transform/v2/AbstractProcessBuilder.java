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
 * Abstract base class for ProcessBuilder subclasses
 */

package org.wiredwidgets.cow.server.transform.v2;

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author JKRANES
 */
public abstract class AbstractProcessBuilder<T extends Object> implements ProcessBuilder<T> {

    @Autowired
    private ActivityBuilderFactoryFactory activityBuilderFactoryFactory;

    /**
     * Create an ActivityBuilder instance based on the ProcessContext and Activity
     * @param context
     * @param activity
     * @return 
     */
    protected ActivityBuilder createActivityBuilder(ProcessContext context, Activity activity) {
        return activityBuilderFactoryFactory.createActivityBuilder(context, activity);
    }
   
}
