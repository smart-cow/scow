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

public class SubProcess extends Activity {
	protected String workflowName;

	public SubProcess() {
		this("", "");
	}

	public SubProcess(String name, String key) {
		super(name, key);
		workflowName = "";
	}
	
	public String getWorkflow() {
		return workflowName;
	}
	
	public void setWorkflow(String s) {
		workflowName = s;
	}

	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("icon", "Icon_SubProcess.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}

	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (SUBPROCESS) has no name");
		}
		if(workflowName == null || workflowName.equals("")) {
			hasError = true;
			errors.add(name + " (SUBPROCESS) has no workflow defined ");
		}
		return hasError;
	}

	public String toString() {
		String out = "<subProcess name=\"" + name + "\" key=\"" + key + "\" bypassable=\"" + bypass + "\" sub-process-key=\"" + workflowName + "\">";
		out += "</subProcess>";
		return out;
	}

}
