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

package org.wiredwidgets.cow.server.transform.graph.builder;

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

/**
 * A default graph builder will be created for each type of Activity
 * that does not require any graph expansion.
 * @author JKRANES
 *
 * @param <T>
 */
public class DefaultGraphBuilder<T extends Activity> extends AbstractGraphBuilder<T> {
	
	private Class<T> type;
	
	public DefaultGraphBuilder(Class<T> type) {
		this.type = type;
	}

	@Override
	protected void buildInternal(T activity, ActivityGraph graph, Process process) {
		// do nothing
	}

	@Override
	public Class<T> getType() {
		return type;
	}

}
