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

package org.wiredwidgets.cow.webapp.client.bpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;

import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * A single Task for a user to complete. This
 * is the most basic Activity, and all Activity
 * tree branches must eventually end with a Task.
 * 
 * Tasks have a name and assignee by default, but
 * also can store more data.
 * 
 * This Task object is used by stored workflows
 * and active workflows. When stored, it will be
 * a part of a Template object and the parent
 * field will not be null. When active, there
 * will be no parent and a number of new fields
 * will be set. Active Task creation is handled
 * through the Parse class. 
 * 
 * 
 * @author JSTASIK
 *
 */
public class Task extends Activity {
	// A map of key/values for data related to the Task
	protected HashMap<String, String> fields;
	// A list of possible outcomes for the task, only used
	// for active Decisions or LoopTasks
	protected ArrayList<String> outcomes;
	protected HashMap<String, String> variables;

	/**
	 * Default constructor
	 */
	public Task() {
		this("", "");
	}
	
	/**
	 * Default constructor, with name
	 * @param name
	 * @param key
	 */
	public Task(String name, String key) {
		super(name, key);
		fields = new HashMap<String, String>();
		outcomes = new ArrayList<String>();
		fields.put("assigneeType", "User");
		this.variables = new HashMap<String, String>();
	}
	
	public String get(String key) {
		return fields.get(key);
	}
	
	public void set(String key, String value) {
		fields.put(key, value);
	}
	
	/**
	 * Adds an outcome to the Task, only for active Tasks
	 * @param o
	 */
	public void addOutcome(String o) {
		outcomes.add(o);
	}
	
	public ArrayList<String> getOutcomes() {
		return outcomes;
	}
	
	public HashMap<String, String> getFields() {
		return fields;
	}
	
	public void setVariable(String key, String value) {
		if(value == null)
			value = "";
		variables.put(key, value);
	}
	
	public void setVariable(int i, String value) {
		variables.put("Additional Info " + i, value);
	}
	
	public String getVariable(String key) {
		return variables.get(key);
	}
	
	/**
	 * Gets a TreeNode representation of an Task
	 */
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("assignee", fields.get("assignee"));
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("icon", "Icon_Task.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}
	
	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (TASK) has no name");
		}
		if(get("assignee") == null || get("assignee").equals("")) {
			hasError = true;
			errors.add(name + " (TASK) has no assigned user");
		}
		return hasError;
	}
	
	/**
	 * Outputs XML description of Task
	 */
	public String toString() {
		String out = "<task name=\"" + name + "\" key=\"" + key + "\" bypassable=\"" + bypass + "\">";
		if(fields.get("assigneeType").equals("Group")) {
			out += "<candidateGroups>" + fields.get("assignee") + "</candidateGroups>";
		} else {
			out += "<assignee>" + fields.get("assignee") + "</assignee>";
		}
		out += "<description>" + getDescription(true) + "</description>";
		out += getVariables();
		out += "</task>";
		return out;
	}
	
	/**
	 * Output the variables associated with this Task in XML
	 * @return The <variables /> XML tag
	 */
	public String getVariables() {
		String out = "<variables>";
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String value = entry.getValue();
			if(value != null && !value.equals(""))
				out += "<variable name=\"" + entry.getKey() + "\" value=\"" + BpmServiceMain.xmlEncode(value) + "\" />";
		}
		out += "</variables>";
		return out;
	}

}
