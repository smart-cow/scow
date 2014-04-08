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

package org.wiredwidgets.cow.webapp.client.bpm;

import java.util.ArrayList;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;



import com.smartgwt.client.widgets.tree.TreeNode;

public class Signal extends Activity {

	protected String signalId;

	public Signal() {
		this("", "");
	}

	public Signal(String name, String key) {
		super(name, key);
		signalId = "";

	}
	



	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (SIGNAL) has no name");
		}
		return hasError;
	}
	
	public String toString() {
		String out = "<signal name=\"" + name + "\" key=\"" + key + "\" signalId=\"" + signalId + "\" bypassable=\"" + bypass + "\">";
		out += "<description>" + description + "</description>";
		out += "</signal>";
		return out;
	}

	
	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("signalId", getSignalId());
		node.setAttribute("icon", "Icon_Signal.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}

	public String getSignalId() {
		return signalId;
	}

	public void setSignalId(String signalId) {
		this.signalId = signalId;
	}

}
