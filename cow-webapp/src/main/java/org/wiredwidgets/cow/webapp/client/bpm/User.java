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
public class User {
	protected String username;
	protected String firstName;
	protected String email;
	protected ArrayList<String> groups;
	
	public User() {
		this("", "", "");
	}
	
	public User(String username, String firstName, String email) {
		this.username = username;
		this.firstName = firstName;
		this.email = email;
		groups = new ArrayList<String>();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<String> getGroups() {
		return groups;
	}
	
	public void addGroup(String s) {
		groups.add(s);
	}
	
	public String removeGroup(String group) {
		int i = 0;
		boolean found = false;
		for(String s : groups) {
			if(s.equals(group)) {
				found = true;
				break;
			}
			i++;
		}		
		return found ? groups.remove(i) : null;
	}
}
