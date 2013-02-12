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

/**
 * The CompletionState values are used to indicate the status of a workflow activity
 * within the context of a specified running ProcessInstance.
 */

package org.wiredwidgets.cow.server.completion;

public enum CompletionState {
          
    // indicates a task or activity that is currently open
	OPEN("open"), 
        
    // indicates a task or activity that has been completed
	COMPLETED("completed"),
	
	PLANNED("planned"),
	
	CONTINGENT("contingent"),
	
	PRECLUDED("precluded");
	
	

	private String name;

	private CompletionState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public static CompletionState forName(String name) {
		for (CompletionState status : CompletionState.values()) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}
}
