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

package org.wiredwidgets.cow.server.listener;

import java.util.List;

import org.wiredwidgets.cow.server.api.service.Task;

public interface TasksEventListener {
	    
	    public void onCreateTask(EventParameters evtParams);
	    
	    public void onCompleteTask(EventParameters evtParams);
	    
	    public void onTakeTask(EventParameters evtParams);
	    
	    /*   
	    public void onUpdateTask(Task task);
	    
	    public void onAddGroupParticipation(Task task, Group group);
	    
	    public void onDeleteGroupParticipation(Task task, Group group);
	    
	    public void onAddUserParticipation(Task task, User user);
	    
	    public void onDeleteUserParticipation(Task task, User user);
	    */
	    
	    public class EventParameters {
			private Task task_;
	    	private List<String> groups_;
	    	private List<String> users_;
	    	
	    	public EventParameters(Task task, List<String> groups, List<String> users) {
	    		task_ = task;
	    		groups_ = groups;
	    		users_ = users;
	    	}
	    	
	    	public Task getTask() {
				return task_;
			}
			public List<String> getGroups() {
				return groups_;
			}
			public List<String> getUsers() {
				return users_;
			}

	    }
}
