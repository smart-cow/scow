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

import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * A special list of Activity objects where, when
 * finishing, a user can choose whether or not to 
 * repeat the actions or to continue in the workflow.
 * 
 * Loops have a slightly altered XML format, where the
 * Task representing whether or not to continue looping
 * is called a loopTask. The loopTask, however, is identical
 * to the XML format of a normal Task.
 * 
 * @author JSTASIK
 *
 */
public class Loop extends Activity {
	// The Task that lets a user pick whether or not to loop
	protected Task task;
	// A list of looped Activity objects
	protected Activities activities;
	// The choice presented to the user when they want to finish a loop
	protected String doneName;
	// The choice presented to the user when they want to repeat a loop
	protected String repeatName;

	/**
	 * Default constructor
	 */
	public Loop() {
		this("", "");
	}

	/**
	 * Default constructor, with name
	 * @param name
	 * @param key
	 */
	public Loop(String name, String key) {
		this(name, key, null);
	}

	/**
	 * Constructor, with name and loopTask set
	 * @param name
	 * @param key
	 * @param t
	 */
	public Loop(String name, String key, Task t) {
		super(name, key);
		task = t;
		activities = new Activities();
		doneName = "yes";
		repeatName = "no";
	}

	/**
	 * Sets the loopTask
	 * @param t
	 */
	public void setTask(Task t) {
		task = t;
		t.setParent(this);
	}
	
	public Task getLoopTask() {
		return task;
	}

	/**
	 * Adds an Activity to the looped items
	 * @param a
	 */
	public void addActivity(Activity a) {
		activities.addActivity(a);
	}
	
	/**
	 * Inserts an Activity to the looped items at the specified index
	 * @param a
	 * @param index
	 */
	public void insertActivity(Activity a, int index) {
		activities.insertActivity(a, index);
	}
	
	public void setActivities(Activities a) {
		activities = a;
	}
	public Activities getActivities() {
		return activities;
	}
	
	public void removeActivity(Activity a) {
		activities.removeActivity(a);
	}
	
	public void setSequential(boolean s) {
		activities.setSequential(s);
	}
	
	public boolean isSequential() {
		return activities.isSequential();
	}
	
	public String getDoneName() {
		return doneName;
	}
	
	public void setDoneName(String s) {
		doneName = s;
	}
	
	public String getRepeatName() {
		return repeatName;
	}
	
	public void setRepeatName(String s) {
		repeatName = s;
	}
	
	/**
	 * Gets a TreeNode representation of a Loop
	 */
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		TreeNode[] children = new TreeNode[activities.getActivities().size()];
		for(int i = 0; i < activities.getActivities().size(); i++) {
			children[i] = activities.getActivities().get(i).getTreeNode(edit);
		}
		node.setAttribute("children", children);
		node.setAttribute("activity", this);
		node.setAttribute("completion", getCompletion());
		node.setAttribute("doneName", doneName);
		node.setAttribute("repeatName", repeatName);
		node.setAttribute("icon", "Icon_Loop.png");
		node.setAttribute("showDropIcon", "Icon_Loop_drop.png");
		if(edit) {
			node.setCanAcceptDrop(true);
			node.setCanDrag(true);
		}
		return node;
	}
	
	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (LOOP) has no name");
		}
		if(task.getName() == null || task.getName().equals("")) {
			hasError = true;
			errors.add(name + " (LOOP) has no question");
		}
		if(task.get("assignee") == null || task.get("assignee").equals("")) {
			hasError = true;
			errors.add(name + " (LOOP) has no assigned user");
		}
		if(doneName == null || doneName.equals("")) {
			hasError = true;
			errors.add(name + " (LOOP) has no doneName");
		}
		if(repeatName == null || repeatName.equals("")) {
			hasError = true;
			errors.add(name + " (LOOP) has no repeatName");
		}
		if(activities == null || activities.getActivities() == null || activities.getActivities().size() == 0) {
			hasError = true;
			errors.add(name + " (LOOP) has no children");
		} else {
			for(Activity a : activities.getActivities()) {
				if(a.hasErrors(errors))
					hasError = true;
			}
		}
		return hasError;
	}

	/**
	 * Outputs XML description of Loop
	 */
	public String toString() {
		activities.setName(getName() + "looped");
		activities.setKey(getName() + "looped");
		String out = "<loop name=\"" + name + "\" key=\"" + key + "\" doneName=\"" + doneName + "\" repeatName=\"" + repeatName + "\" bypassable=\"" + bypass + "\">";
		String loopTask = task.toString();
		loopTask = loopTask.replaceFirst("task", "loopTask");
		loopTask = loopTask.replace("</task>", "</loopTask>");
		out += loopTask;
		out += activities.toString();
		out += "<description>" + description + "</description>";
		out += "</loop>";
		return out;
	}
}
