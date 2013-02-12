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
 * Options are possible choices for a Decision.
 * 
 * Options contain an Activities by default, which
 * can be filled up with any combination of Activity
 * objects just like a normal Activities list.
 * 
 * Options are not technically Activity objects, but
 * share many of the same features as them. For instance,
 * they are still represented in the GUI by TreeNodes.
 * Ideally, there would have been an interface for both
 * Activitys and Options to implement, but as of right now
 * there is none. So, there may be some instanceof chunks
 * of code to check whether or not a TreeNode is an Activity
 * or an Option.
 * 
 * @author JSTASIK
 *
 */
public class Option {
	// The Decision this Option belongs to
	protected Decision parent;
	// List of Activity objects
	protected Activities activities;
	// Name of the Option
	protected String name;
	
	/**
	 * Default constructor, with parent specified
	 * @param d
	 */
	public Option(Decision d) {
		this(d, new Activities(), "");
	}
	
	/**
	 * Default constructor, with parent, default activities, and name
	 * @param p
	 * @param a
	 * @param n
	 */
	public Option(Decision p, Activities a, String n) {
		parent = p;
		activities = (Activities)a;
		name = n;
		activities.setParent(parent);
	}
	
	public Decision getParent() {
		return parent;
	}
	
	public Activities getActivities() {
		activities.setName(getName());
		activities.setKey(getName());
		return activities;
	}
	
	public void setActivities(Activities a) {
		activities = a;
		activities.setParent(parent);
	}
	
	/**
	 * Adds an Activity
	 * @param a
	 */
	public void addActivity(Activity a) {
		activities.addActivity(a);
	}
	
	/**
	 * Inserts an Activity at the specified location
	 * @param a
	 * @param index
	 */
	public void insertActivity(Activity a, int index) {
		activities.insertActivity(a, index);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void setSequential(boolean s) {
		activities.setSequential(s);
	}
	
	public boolean isSequential() {
		return activities.isSequential();
	}
	
	public boolean getBypass() {
		return activities.getBypass();
	}
	
	public void setBypass(boolean b) {
		activities.setBypass(b);
	}
	
	/**
	 * Gets a TreeNode representation of an Option
	 */
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		TreeNode[] children = new TreeNode[activities.getActivities().size()];
		for(int i = 0; i < activities.getActivities().size(); i++) {
			children[i] = activities.getActivities().get(i).getTreeNode(edit);
		}
		node.setAttribute("completion", activities.getCompletion());
		node.setAttribute("children", children);
		node.setAttribute("activity", this);
		node.setCanDrag(false);
		node.setAttribute("icon", "Icon_Decision_Arrow.png");
		node.setAttribute("showDropIcon", "Icon_Decision_Arrow_drop.png");
		return node;
	}
	
	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (OPTION) has no name");
		}
		if(activities == null || activities.getActivities() == null) {
			hasError = true;
			errors.add(name + " (OPTION) has no children");
		} else {
			for(Activity a : activities.getActivities()) {
				if(a.hasErrors(errors))
					hasError = true;
			}
		}
		return hasError;
	}
	
	/**
	 * Outputs XML description of Option
	 */
	public String toString() {
		if(activities.getActivities().size() == 0) return "";
		activities.setName(getName() + "option");
		activities.setKey(getName() + "option");
		String out = "<option name=\"" + name + "\">";
		out += activities.toString();
		out += "</option>";
		return out;
	}
}
