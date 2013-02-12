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

public class Exit extends Activity {
	protected String reason;

	public Exit() {
		this("exit", "exit");
	}
	
	public Exit(String name, String key){
		super(name, key);
		reason = "";
	}

	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("reason", getReason());
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("icon", "Icon_Exit.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}
	
	public void setReason(String r) {
		reason = r;
	}
	
	public String getReason() {
		return reason;
	}

	public boolean hasErrors(ArrayList<String> errors) {
		return false;
	}
	
	public String toString() {
		String out = "<exit name=\"" + name + "\" key=\"" + key + "\" bypassable=\"false\" state=\"" + reason + "\">";
		out += "<description>" + description + "</description>";
		out += "</exit>";
		return out;
	}

}
