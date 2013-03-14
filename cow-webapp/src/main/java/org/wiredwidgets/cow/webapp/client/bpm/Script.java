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
import java.util.Collection;
import java.util.List;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;



import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tree.TreeNode;

public class Script extends Activity {


	protected String format;
	protected ArrayList<String> imports;
	protected String content;

	public Script() {
		this("", "");
	}

	public Script(String name, String key) {
		super(name, key);
		format = "";
		imports =  new ArrayList<String>();
		content = "";
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public ArrayList<String> getImports() {
		return imports;
	}
	
	
	public String getImportsCSV(){
		StringBuilder sb = new StringBuilder();
		if (imports == null || imports.size() < 1 )
			return "";
	    for(String str:imports){
	    	if(sb.length() != 0){
	    		sb.append(",");
	        }
	        sb.append(str);
	    }
        return sb.toString();
    }
		

	public void setImports(ArrayList<String> imports) {
		this.imports = imports;
	}
	

	
	public void setContent(String c) {
		content = c;
	}
	
	public String getContent() {
		return content;
	}
	

	public TreeNode getTreeNode(boolean edit) {
		TreeNode node = new TreeNode();
		node.setAttribute("name", getName());
		node.setAttribute("completion", getCompletion());
		node.setAttribute("activity", this);
		node.setAttribute("icon", "Icon_Script.png");
		if(edit)
			node.setCanDrag(true);
		return node;
	}

	public boolean hasErrors(ArrayList<String> errors) {
		boolean hasError = false;
		if(name == null || name.equals("")) {
			hasError = true;
			errors.add(name + " (Script) has no name");
		}
		if(content == null || content.equals("")) {
			hasError = true;
			errors.add(name + " (Script) has no content");
		}
		return hasError;
	}
	
	public boolean addImport(String arg0) {
		return imports.add(arg0);
	}

	public String toString() {
		String out = "";
		if (format == "MVEL"){
			out = "<script name=\"" + name + "\" key=\"" + key + "\"  bypassable=\"" + bypass + "\">";
		}
		else {
			out = "<script name=\"" + name + "\" key=\"" + key + "\" scriptFormat=\"" + format + "\" bypassable=\"" + bypass + "\">";
		}
		for (String imp: imports){
			out += "<import>" + imp + "</import>";
		}
		out += "<content>" + content + "</content>";
		out += "<description>" + description + "</description>";
		out += "</script>";
		return out;
	}

	public boolean addAllImports(Collection<? extends String> c) {
		return imports.addAll(c);
	}
	
	public boolean setImports(Collection<? extends String> c) {
		imports = new ArrayList<String>();
		return imports.addAll(c);
	}



}
