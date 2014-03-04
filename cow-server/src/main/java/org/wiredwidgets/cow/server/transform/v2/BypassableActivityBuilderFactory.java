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

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 *
 * @author JKRANES
 */
@Component
public class BypassableActivityBuilderFactory extends ActivityBuilderFactory<Activity>{

    public BypassableActivityBuilderFactory() {
        super(Activity.class, null);
    }

    @Override
    public boolean supports(Activity activity) {
        log.debug("Bypassable: " + activity.isBypassable());
        return activity.isBypassable() && !activity.isWrapped();
    }
    
    @Override
    public BypassableActivityBuilder createActivityBuilder(ProcessContext context, Activity activity) {
        return new BypassableActivityBuilder(context, activity, this);
    }

}
