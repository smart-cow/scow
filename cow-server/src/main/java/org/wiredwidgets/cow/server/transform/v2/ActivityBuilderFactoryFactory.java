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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Holds references to all candidate ActivityBuilderFactories and selects the correct ActivityBuilderFactory
 * to be used for specified conditions.
 * @author JKRANES
 */
@Component
public class ActivityBuilderFactoryFactory {

    @Autowired
    NodeBuilderFactoryFactory nodeBuilderFactoryFactory;

    private static Logger log = Logger.getLogger(ActivityBuilderFactory.class);

    private List<ActivityBuilderFactory> factories = new ArrayList<ActivityBuilderFactory>();

    /**
     * The set of candidate ActivityBuilderFactories to consider
     * @return
     */
    public List<ActivityBuilderFactory> getFactories() {
        return factories;
    }

    public void setFactories(List<ActivityBuilderFactory> factories) {
        this.factories = factories;
    }
    
    public void addFactory(ActivityBuilderFactory factory) {
    	factories.add(factory);
    }

    /*
     * Selects the correct Factory instance by calling supports() on all potential candidate factories
     */
    private ActivityBuilderFactory getActivityBuilderFactory(Activity activity, Class<? extends ProcessContext> contextClass) {
        ActivityBuilderFactory factory = null;
        log.debug("Finding factory for " + activity.getClass().getSimpleName() + ", " + contextClass.getSimpleName());
        for (ActivityBuilderFactory candidate : factories) {
            if (candidate.supports(activity, contextClass)) {
                log.debug("Selected factory: " + candidate.getClass().getSimpleName());
                factory = candidate;
                break;
            }
        }
        assert (factory != null);
        return factory;
    }

    /**
     * Select the correct ActivityBuilderFactory instance and calls the createActivityBuilder method
     * on that instance.
     * @param context
     * @param activity
     * @return
     */
    public ActivityBuilder createActivityBuilder(ProcessContext context, Activity activity) {
    	ActivityBuilderFactory factory = getActivityBuilderFactory(activity, context.getClass());
    	return factory.createActivityBuilder(context, activity);
        // return getActivityBuilderFactory(activity, context.getClass()).createActivityBuilder(context, activity);
    }

}