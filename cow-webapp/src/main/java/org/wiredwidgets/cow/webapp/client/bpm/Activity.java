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

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;

import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Abstract base class for all Activity objects
 * Classes that inherit:
 * Activities
 * BaseList
 * Decision
 * Loop
 * Task
 * 
 * All Activity objects have a name and a key. These
 * are sometimes the same, but not always. A key is a
 * unique identifier and after initial creation the server
 * sometimes creates its own key for a certain Activity. 
 * Because the name of an activity can change, as long
 * as the key is the same, the server will know to update
 * an existing Activity rather than deleting and recreating it.
 * That said, this is not really implemented in the client
 * very well, and most of the time the key just copies whatever
 * is in the name field.
 * 
 * An Activity also has a completion field, which is used to
 * show the stoplight chart for a workflow in progress. This
 * field should only be set when parsing a ProcessInstance.
 * 
 * The note and url fields are experimental fields, which are
 * not currently implemented (the code is there, but commented).
 * 
 * @author JSTASIK
 *
 */
public abstract class Activity {
	protected String name;
	protected String key;
	protected Activity parent;
	protected String completion;
	protected String description;
	protected boolean bypass;
	
	/**
	 * Default constructor
	 */
	public Activity() {
		this("", "");
	}
	
	/**
	 * Default constructor, with a name
	 * @param name
	 * @param key
	 */
	public Activity(String name, String key) {
		this.name = name;
		this.key = key;
		this.parent = null;
		this.completion = "";
		this.bypass = false;
		this.description = "";
	}
	
	public void setParent(Activity a) {
		parent = a;
	}
	
	public Activity getParent() {
		return parent;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getCompletion() {
		return completion;
	}
	
	public void setCompletion(String s) {
		completion = s;
	}
	
	public boolean getBypass() {
		return bypass;
	}
	
	public void setBypass(boolean b) {
		bypass = b;
	}
	
	public String getDescription() {
		return getDescription(false);		
	}
	
	public String getDescription(boolean encode) {
		if(encode && description != null)
			return BpmServiceMain.xmlEncode(description);
		return description;
	}
	
	public String getHtmlDescription() {
		if(description == null) return "";
		return BpmServiceMain.xmlEncode(description).replaceAll("&#10;", "<br />");
	}
	
	public void setDescription(String s, boolean decode) {
		description = decode ? BpmServiceMain.xmlDecode(s) : s;
	}
	
	public void setDescription(String s) {
		setDescription(s, false);
	}
	
	/**
	 * Gets a TreeNode representation of an Activity
	 * @param edit If true, gets the TreeNode in 'edit' mode, otherwise in 'non-edit' mode
	 * @return The TreeNode created
	 */
	public abstract TreeNode getTreeNode(boolean edit);
	
	/**
	 * Checks an activity for errors
	 * @param errors A list of Strings where error messages are placed if found
	 * @return true if there is an error, otherwise false
	 */
	public abstract boolean hasErrors(ArrayList<String> errors);
}