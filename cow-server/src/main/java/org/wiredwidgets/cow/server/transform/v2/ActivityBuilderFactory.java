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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 * Factory class for obtaining an ActivityBuilderFactory instance based on the activity and type of notation (ProcessContext)
 * @author JKRANES
 */
public abstract class ActivityBuilderFactory<T extends Activity> implements InitializingBean {

    protected static Logger log = Logger.getLogger(ActivityBuilderFactory.class);
    @Autowired
    private NodeBuilderFactoryFactory nodeBuilderFactoryFactory;
    @Autowired
    private ActivityBuilderFactoryFactory activityBuilderFactoryFactory;
    private Class<T> activityClass;
    private Class<? extends ProcessContext> contextClass;

    /**
     * Constructor
     * @param activityClass the class of Activity that this factory supports.
     * @param contextClass The ProcessContext class that this factory supports.  A null indicates
     * that this factory supports all types
     */
    public ActivityBuilderFactory(Class<T> activityClass, Class<? extends ProcessContext> contextClass) {
        this.activityClass = activityClass;
        this.contextClass = contextClass;
    }
    
    public void afterPropertiesSet() {
    	activityBuilderFactoryFactory.addFactory(this);
    }

    private boolean supportsActivityClass(Class<? extends Activity> activityClass) {
        return this.activityClass.isAssignableFrom(activityClass);
    }

    private boolean supportsProcessContextClass(Class<? extends ProcessContext> contextClass) {
        return (this.contextClass == null ? true : this.contextClass.equals(contextClass));
    }

    /**
     * Indicates whether this factory supports (i.e. should be used to create) the specified
     * activity instance and ProcessContext class.  The FactoryFactory selects a factory  by calling this method
     * for all registered factories.  The first supporting factory is returned.
     * @param activity
     * @param contextClass
     * @return
     */
    public boolean supports(Activity activity, Class<? extends ProcessContext> contextClass) {
        // log.debug("Factory supports: " + this.activityClass.getSimpleName());
        // log.debug("Candidate activity: " + activity.getClass().getSimpleName());
        
        // note that we rely on shortcutting of evaluation here -- casting of activity to (T) will not occur 
        // unless supportsActivityClass returns true
        return (supportsActivityClass(activity.getClass()) && supportsProcessContextClass(contextClass) && supports((T)activity));
    }

    /**
     * Determine whether this factory supports the activity based on the activity instance
     * in addition to the activity type.
     * Default implementation returns true.  Override if additional logic is required by the factory.
     * @param activity
     * @return
     */
    public boolean supports(T activity) {
        return true;
    }

    /**
     * Creates the ActivityBuilder of the desired type and activity.  Implemented by subclasses.
     * Note that this method assumes the correct Factory instance has been selected by the FactoryFactory
     * @param context
     * @param activity
     * @return
     */
    public abstract ActivityBuilder createActivityBuilder(ProcessContext context, T activity);

    /**
     * Obtains the appropriate NodeBuilderFactory instance from the NodeBuilderFactoryFactory based on the parameters, 
     * and invokes the createNodeBuilder method on the Factory instance to create a NodeBuilder instance
     * @param context
     * @param nodeType
     * @param activity
     * @return
     */
    protected NodeBuilder createNodeBuilder(ProcessContext context, NodeType nodeType, Activity activity) {
        return nodeBuilderFactoryFactory.createNodeBuilder(context, nodeType, activity);
    }

    /**
     * Obtains the appropriate factory from the ActivityBuilderFactoryFactory and then calls createActivityBuilder
     * Note name difference to distinguish this from the createActivityBuilder method
     * that is implemented by the factory instance
     * @param context
     * @param activity
     * @return
     */
    protected ActivityBuilder createActivityBuilderFromFactory(ProcessContext context, Activity activity) {
        return activityBuilderFactoryFactory.createActivityBuilder(context, activity);
    }
}
