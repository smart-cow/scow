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

/**
 * Incomplete class. Originally intended to be part
 * of the User/Groups management package.
 * @author JSTASIK
 *
 */
public class Group {
	protected String name;
	protected String type;
	protected ArrayList<String> users;
	
	public Group() {
		this("", "");
	}
	
	public Group(String name) {
		this(name, "");
	}
	
	public Group(String name, String type) {
		this.name = name;
		this.type = type;
		this.users = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getUsers() {
		return users;
	}

	public void addUser(String s) {
		users.add(s);
	}
	
	public String removeUser(String user) {
		int i = 0;
		boolean found = false;
		for(String s : users) {
			if(s.equals(user)) {
				found = true;
				break;
			}
			i++;
		}		
		return found ? users.remove(i) : null;
	}
}
