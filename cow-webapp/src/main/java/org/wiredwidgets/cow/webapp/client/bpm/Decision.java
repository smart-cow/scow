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
 * A Decision is special type of task where a
 * user has to choose a specific outcome. Outcomes
 * are listed as Option objects. Users can specify
 * what the decision question is, who makes the
 * decision, what the potential choices are, and
 * what happens depending on which choice is made. 
 * 
 * I think a Decision has to have at least one Option.
 * 
 * @author JSTASIK
 *
 */
public class Decision extends Activity {
	// The Task used to control what the decision question is
	protected Task task;
	// A list of potential outcomes for the decision
	protected ArrayList<Option> options;

	/**
	 * Default constructor
	 */
	public Decision() {
		this("", "");
	}

	/**
	 * Default constructor, with name
	 * @param name
	 * @param key
	 */
	public Decision(String name, String key) {
		super(name, key);
		options = new ArrayList<Option>();
	}

	public ArrayList<Option> getOptions() {
		return options;
	}

	/**
	 * Sets the decision's Task
	 * @param t
	 */
	public void setTask(Task t) {
		task = t;
		t.setParent(this);
	}
	
	public Task getTask() {
		return task;
	}

	/**
	 * Removes an Option from the list of choices
	 * @param o
	 */
	public void removeOption(Option o) {
		options.remove(o);
	}
	
	/**
	 * Adds an Option to the list of choices
	 * @param o
	 */
	public void addOption(Option o) {
		options.add(o);
	}
	
	/**
	 * Adds an Option with a default Activity
	 * @param a The default Activity
	 * @param name The name of the Option
	 */
	public void addOption(Activity a, String name) {
		Option o = new Option(this);
		o.setName(name);
		o.addActivity(a);
		options.add(o);
	}
	
	/**
	 * Inserts an Option at the specified index
	 * @param o
	 * @param index
	 */
	public void insertOption(Option o, int index) {
		options.add(index, o);
	}
	
	/**
	 * Inserts an Option at the specified index with a default Activity
	 * @param a The default Activity
	 * @param index The index to insert the Option at
	 * @param name The name of the Option
	 */
	public void insertOption(Activity a, int index, String name) {
		Option o = new Option(this);
		o.setName(name);
		o.addActivity(a);
		options.add(index, o);
	}
	
	/**
	 * Gets a TreeNode representation of a Decision
	 */
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		TreeNode[] children = new TreeNode[options.size()];
		for(int i = 0; i < options.size(); i++) {
			children[i] = options.get(i).getTreeNode(edit);
		}
		node.setAttribute("children", children);
		node.setAttribute("icon", "Icon_Decision.png");
		node.setAttribute("showDropIcon", "Icon_Decision_drop.png");
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
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
			errors.add(name + " (DECISION) has no name");
		}
		if(task.getName() == null || task.getName().equals("")) {
			hasError = true;
			errors.add(name + " (DECISION) has no question");
		}
		if(task.get("assignee") == null || task.get("assignee").equals("")) {
			hasError = true;
			errors.add(name + " (DECISION) has no assigned user");
		}
		if(options == null || options.size() == 0) {
			hasError = true;
			errors.add(name + " (DECISION) has no options");
		} else {
			for(Option o : options) {
				if(o.hasErrors(errors))
					hasError = true;
			}
		}
		return hasError;
	}

	/**
	 * Outputs XML description of Decision
	 */
	public String toString() {
		String out = "<decision name=\"" + name + "\" key=\"" + key + "\" bypassable=\"" + bypass + "\">";
		out += task.toString();
		for(Option o : options) {
			out += o.toString();
		}
		out += "<description>" + description + "</description>";
		out += "</decision>";
		return out;
	}
}
