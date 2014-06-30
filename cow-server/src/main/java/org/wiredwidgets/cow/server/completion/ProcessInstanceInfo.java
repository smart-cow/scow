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

package org.wiredwidgets.cow.server.completion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.process.audit.NodeInstanceLog;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;
import org.wiredwidgets.cow.server.api.service.StatusSummary;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

public class ProcessInstanceInfo {
	
	private Map<String, List<HistoryActivity>> activitiesMap = new HashMap<String, List<HistoryActivity>>();
	
	private int processIntanceState;
	
	private Map<String, String> variables;
	
	private Map<String, Map<CompletionState, List<Task>>> userSummary = new HashMap<String, Map<CompletionState, List<Task>>>();
	
	private Map<String, Map<CompletionState, List<Task>>> groupSummary = new HashMap<String, Map<CompletionState, List<Task>>>();
	
	private ActivityGraph graph;
	
        
    /**
     * Constructor
     * @param activities
     * @param processState 
     */
	public ProcessInstanceInfo(int processState, ActivityGraph graph) {
		//setActivities(activities);
        this.processIntanceState = processState;
        this.graph = graph;
	}
	
	/**
	 * Returns the list of HistoryActivity instances for the specified key
	 * In most cases there will be zero or one item in the list.  The exception to this 
	 * is the Loop structure, as the same activity may be repeated multiple times in a Loop.
	 * @param key
	 * @return list of HistoryActivity objects
	 */
//	public List<HistoryActivity> getActivities(String key) {
//            if (activitiesMap.get(key) == null) {
//                // return empty list
//                return new ArrayList<HistoryActivity>();
//            }
//            else {
//		return activitiesMap.get(key);
//            }
//	}
	
//	private void setActivities(List<HistoryActivity> activities) {
//		for (HistoryActivity hi : activities) {
//			String key = hi.getActivityName();
//			if (activitiesMap.get(key) == null) {
//				activitiesMap.put(key, new ArrayList<HistoryActivity>());
//			}
//			activitiesMap.get(key).add(hi);
//		}
//	}

    public int getProcessInstanceState() {
        return processIntanceState;
    }

//	public Map<String, String> getVariables() {
//		return variables;
//	}
	
	private void updateSummary(String name, Task task, Map<String, Map<CompletionState, List<Task>>> map) {
		Map<CompletionState, List<Task>> summary = map.get(name);
		if (summary == null) {
			summary = new HashMap<CompletionState, List<Task>>();
					map.put(name, summary);
		}
		
		CompletionState status = task.getCompletionState();
		List<Task> tasks = summary.get(status);
		if (tasks == null) {
			tasks = new ArrayList<Task>();
			summary.put(status, tasks);
		}
		
		tasks.add(task);
	}
	
	public void updateUserSummary(String user, Task task) {
		updateSummary(user, task, userSummary);
	}
	
	public void updateGroupSummary(String group, Task task) {
		updateSummary(group, task, groupSummary);
	}
	
	public List<StatusSummary> getStatusSummary() {
		List<StatusSummary> summaryList = new ArrayList<StatusSummary>();
		summaryList.addAll(getStatusSummary("user", userSummary));
		summaryList.addAll(getStatusSummary("group", groupSummary));
		return summaryList;
	}
	
	public ActivityGraph getGraph() {
		return graph;
	}

	public void setGraph(ActivityGraph graph) {
		this.graph = graph;
	}

	private List<StatusSummary> getStatusSummary(String type, Map<String, Map<CompletionState, List<Task>>> map) {
		List<StatusSummary> summaryList = new ArrayList<StatusSummary>();
		
		for (Entry<String, Map<CompletionState, List<Task>>> userEntry : map.entrySet()) {
			for (Entry<CompletionState, List<Task>> statusEntry : userEntry.getValue().entrySet()) {
				StatusSummary summary = new StatusSummary();
				summary.setType(type);
				summary.setName(userEntry.getKey());
				summary.setStatus(statusEntry.getKey());
				int count = 0;
				for (Task task : statusEntry.getValue()) {
					count++;
					summary.getTasks().add(task);	
				}
				summary.setCount(count);
				summaryList.add(summary);
			}
			
			// Add any unused statuses with a zero count??
			// for now leave this out -- see if the client really needs it
			/*
			for (CompletionState state : CompletionState.values()) {
				if (userEntry.getValue().get(state.getName()) == null) {
					StatusSummary summary = new StatusSummary();
					summary.setType("user");
					summary.setName(state.getName());
					summary.setCount(0);
					summaryList.add(summary);
				}
			}
			*/
			
		}
		return summaryList;	
	}
      

}
