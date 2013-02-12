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
 * Activities are lists of Activity objects. They can be executed
 * sequentially or in parallel.
 * 
 * An Activities currently must have at least one Activity or
 * it will not save.
 * 
 * @author JSTASIK
 *
 */
public class Activities extends Activity {
	// A list of the Activity objects
	protected ArrayList<Activity> activities;
	// Whether or not this list is sequential
	protected boolean sequential;

	/**
	 * Default constructor
	 */
	public Activities() {
		this("", "");
	}
	
	/**
	 * Default constructor, with a name
	 * @param name name
	 * @param key key
	 */
	public Activities(String name, String key) {
		this(name, key, true);
	}
	
	/**
	 * Constructor, with sequential and a name specified
	 * @param name name
	 * @param key key
	 * @param sequential Whether or not the list should be executed sequentially
	 */
	public Activities(String name, String key, String sequential) {
		this(name, key, (Boolean.parseBoolean(sequential)));
	}
	
	/**
	 * Constructor, with sequential and a name specified
	 * @param name name
	 * @param key key
	 * @param sequential Whether or not the list should be executed sequentially
	 */
	public Activities(String name, String key, boolean sequential) {
		super(name, key);
		this.sequential = sequential;
		activities = new ArrayList<Activity>();
	}
	
	/**
	 * Whether or not the list should be executed sequentially
	 * @return true if it should be executed sequentially, otherwise false
	 */
	public boolean isSequential() {
		return sequential;
	}
	
	public void setSequential(boolean b) {
		sequential = b;
	}
	
	/**
	 * Adds an Activity to the list
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		activity.setParent(this);
		activities.add(activity);
	}
	
	/**
	 * Inserts an Activity to the list at a specified index
	 * @param activity
	 * @param index
	 */
	public void insertActivity(Activity activity, int index) {
		activity.setParent(this);
		activities.add(index, activity);
	}
	
	/**
	 * Removes an Activity from the list
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		int loc = activities.indexOf(activity);
		activities.remove(loc);
	}
	
	public ArrayList<Activity> getActivities() {
		return activities;
	}
	
	/**
	 * Gets a TreeNode representation of an Activities
	 */
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		TreeNode[] children = new TreeNode[activities.size()];
		for(int i = 0; i < activities.size(); i++) {
			children[i] = activities.get(i).getTreeNode(edit);
		}
		node.setAttribute("children", children);
		node.setAttribute("activity", this);
		node.setAttribute("completion", getCompletion());
		node.setAttribute("icon", "Icon_List.png");
		//TODO Adding this to allow for List to have a drop (why is list implemented here?) - MH
		node.setCanAcceptDrop(true);
		node.setAttribute("showDropIcon", "Icon_List_drop.png");
		if(edit) {
			//node.setCanAcceptDrop(true);
			node.setCanDrag(true);
		}
		return node;
	}
	
	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (LIST) has no name");
		}
		if(activities == null || activities.size() == 0) {
			hasError = true;
			errors.add(name + " (LIST) has no children");
		} else {
			for(Activity a : activities) {
				if(a.hasErrors(errors))
					hasError = true;
			}
		}
		return hasError;
	}
	
	/**
	 * Outputs XML description of Activities
	 */
	public String toString() {
		String out = "<activities sequential=\"" + sequential + "\" name=\"" + name + "\" key=\"" + key + "\" bypassable=\"" + bypass + "\">";
		for(Activity a : activities) {
			out += a.toString();
		}
		out += "<description>" + description + "</description>";
		out += "</activities>";
		return out;
	}
}
