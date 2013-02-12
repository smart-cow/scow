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

/**
 * BaseList is essentially a clone of Activities, but
 * for the special case of the root of the workflow. All
 * workflows start with an Activities list by default, so
 * this is just a special version of that. It creates a
 * root TreeNode instead of a generic node leaf, and has
 * a fixed name/key of 'base'.
 */
import java.util.ArrayList;

import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

public class BaseList extends Activity {
	// Whether or not the list is sequential
	protected boolean sequential;
	// The list of Activity objects
	protected ArrayList<Activity> activities;
	// The tree for the workflow
	protected Tree tree;
	
	/**
	 * Default constructor
	 */
	public BaseList() {
		super("base", "base");
		activities = new ArrayList<Activity>();
		sequential = true;
	}
	
	/**
	 * Adds an activity
	 * @param a 
	 */
	public void addActivity(Activity a) {
		activities.add(a);
		a.setParent(this);
	}
	/**
	 * Inserts an activity at the specified index
	 * @param a
	 * @param index
	 */
	public void insertActivity(Activity a, int index) {
		activities.add(index, a);
		a.setParent(this);
	}
	
	/**
	 * Removes an activity
	 * @param a
	 */
	public void removeActivity(Activity a) {
		activities.remove(a);
	}
	
	public ArrayList<Activity> getActivities() {
		return activities;
	}
	
	public boolean isSequential() {
		return sequential;
	}
	
	public void setSequential(boolean b) {
		sequential = b;
	}
	
	/**
	 * Gets the Tree for this workflow, calls getTreeNode() for
	 * all Activity objects listed beneath itself
	 * @param edit
	 * @return
	 */
	public Tree getTree(boolean edit) {
		if(tree != null) return tree;
		tree = new Tree();
		tree.setNameProperty("name");
		tree.setModelType(TreeModelType.CHILDREN);
		TreeNode root = new TreeNode();
		TreeNode[] children = new TreeNode[activities.size()];
		for(int i = 0; i < activities.size(); i++) {
			children[i] = activities.get(i).getTreeNode(edit);
		}
		root.setAttribute("activity", this);
		root.setAttribute("children", children);
		tree.setRoot(root);
		return tree;
	}
	
	/**
	 * Destroys the Tree object
	 */
	public void clearTree() {
		tree.destroy();
	}
	
	/**
	 * Never call this. Use BaseList.getTree() instead
	 */
	public TreeNode getTreeNode(boolean edit) {
		return null;
	}
	
	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(activities == null || activities.size() == 0) {
			hasError = true;
			errors.add("Workflow has no tasks");
		} else {
			for(Activity a : activities) {
				if(a.hasErrors(errors))
					hasError = true;
			}
		}
		return hasError;
	}
	
	/**
	 * Outputs XML description of BaseList
	 */
	public String toString() {
		String out = "<activities sequential=\"" + isSequential() + "\" name=\"base\" key=\"base\">";
		for(Activity a : activities) {
			out += a.toString();
		}
		out += "</activities>";
		return out;
	}
}
