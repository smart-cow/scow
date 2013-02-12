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
import java.util.Date;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;


import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tree.Tree;

/**
 * Stores a tree of Activity objects for a saved workflow.
 * 
 * Workflows always start with an Activities, but the Template
 * uses a special type of Activities called BaseList. This is
 * not required by the XML schema, but it is enforced in the GUI.
 * 
 * 
 * @author JSTASIK
 *
 */
public class Template {
	// The root node
	protected BaseList base;
	// The name of the Template
	protected String name;
	// The key of the Template
	protected String key;
	
	/**
	 * Default constructor
	 */
	public Template() {
		base = new BaseList();
		name = "";
		key = String.valueOf((new Date()).getTime());
	}
	
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}
	
	public BaseList getBase() {
		return base;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void setKey(String s) {
		key = s;
	}
	
	public void setBase(BaseList base) {
		this.base = base;
	}
	
	public Tree getTree(boolean edit) {
		return base.getTree(edit);
	}
	
	public void clearTree() {
		base.clearTree();
	}
	
	/**
	 * Checks a workflow for errors
	 * @return true if it has errors, otherwise false
	 */
	public boolean hasErrors() {
		ArrayList<String> errors = new ArrayList<String>();
		boolean hasError = base.hasErrors(errors);
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add("Workflow has no name");
		}
		if(hasError) {
			String output = "";
			for(String s : errors) {
				output += "- " + s + "<br />";
			}
			SC.say(output);
		}
		return hasError;
	}
	
	/**
	 * Gets the XML description of this workflow
	 */
	public String toString() {
		String out = "<process name=\"" + name + "\" key=\"" + name + "\" xmlns=\"" + BpmServiceMain.modelNamespace + "\">";
		out += "<bypassAssignee>admin</bypassAssignee>";
		out += base.toString();
		out += "</process>";
		return out;
	}
}
