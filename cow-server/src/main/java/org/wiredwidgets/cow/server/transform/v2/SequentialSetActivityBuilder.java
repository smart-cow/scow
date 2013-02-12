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

import javax.xml.bind.JAXBElement;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

/**
 *
 * @author JKRANES
 */
public class SequentialSetActivityBuilder extends ActivityBuilderImpl<Activities> {

    public SequentialSetActivityBuilder(ProcessContext context, Activities activities, ActivityBuilderFactory factory) {
        super(context, activities, factory);
    }

    @Override
    public void build() {
        ActivityBuilder prevBuilder = null;
        for (JAXBElement<? extends Activity> element : getActivity().getActivities()) {
            ActivityBuilder builder = createActivityBuilder(element.getValue());
            builder.build(this);
            if (prevBuilder == null) {
                setLinkTarget(builder);
            } else {
                prevBuilder.link(builder);
            }
            prevBuilder = builder;
        }
        // despite the name, prevBuilder here is the final builder from the loop
        setLinkSource(prevBuilder);
    }
}
