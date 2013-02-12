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

public class ServiceTask extends Activity {
	protected String method;
	protected String serviceUrl;
	protected String content;
	protected String var;

	public ServiceTask() {
		this("", "");
	}

	public ServiceTask(String name, String key) {
		super(name, key);
		method = "";
		serviceUrl = "";
		content = "";
		var = "";
	}
	
	public void setMethod(String m) {
		method = m;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setServiceUrl(String u) {
		serviceUrl = u;
	}
	
	public String getServiceUrl() {
		return serviceUrl;
	}
	
	public void setContent(String c) {
		content = c;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setVar(String v) {
		var = v;
	}
	
	public String getVar() {
		return var;
	}

	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("icon", "Icon_ServiceTask.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}

	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (SERVICETASK) has no name");
		}
		if(method == null || method.equals("")) {
			hasError = true;
			errors.add(name + " (SERVICETASK) has no method");
		}
		if(serviceUrl == null || serviceUrl.equals("")) {
			hasError = true;
			errors.add(name + " (SERVICETASK) has no url");
		}
		return hasError;
	}
	
	public String toString() {
		String out = "<serviceTask name=\"" + name + "\" key=\"" + key + "\" bypassable=\"" + bypass + "\">";
		out += "<method>" + method + "</method>";
		out += "<url>" + serviceUrl + "</url>";
		if(method.equals("POST"))
			out += "<content>" + BpmServiceMain.xmlEncode(content) + "</content>";
		if(var != null && !var.equals(""))
			out += "<var>" + var + "</var>";
		out += "<description>" + description + "</description>";
		out += "</serviceTask>";
		return out;
	}

}
