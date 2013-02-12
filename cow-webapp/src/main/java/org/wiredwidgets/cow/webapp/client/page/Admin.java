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

package org.wiredwidgets.cow.webapp.client.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Page with special admin buttons
 * 
 * Currently, the only admin control deletes
 * all active workflows
 * 
 * @author JSTASIK
 *
 */
public class Admin extends PageWidget {
	public Admin() {
		super();
		
		VLayout layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		Button delete = new Button();
		delete.setAutoFit(true);
		delete.setTitle("Delete all active workflows");
		delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				BpmServiceMain.sendGet("/processInstances/active", new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
						SC.say("Failed to retrieve workflow list");
					}
					public void onSuccess(String arg0) {
						ArrayList<String> instances = Parse.parseTemplateInstancesIds(arg0);
						Set<String> instancesToClear = new HashSet<String>();
						for(String s : instances) {
							String add = s.split("\\.")[0];
							instancesToClear.add(add);
						}
						for(String s : instancesToClear) {
							BpmServiceMain.sendDelete("/processInstances/active/" + BpmServiceMain.urlEncode(s) + ".*", new AsyncCallback<Void>() {
								public void onFailure(Throwable arg0) {}
								public void onSuccess(Void arg0) {}
							});
						}
						SC.say("All Executing Workflows Have Ended");
					}
				});
			}
		});
		
		Button groups = new Button();
		groups.setAutoFit(true);
		groups.setTitle("Create default groups (local server only)");
		groups.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				BpmServiceMain.sendPostLocation("/groups?name=alpha", "", new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
					}
					public void onSuccess(String arg0) {
					}
				});
				BpmServiceMain.sendPostLocation("/groups?name=beta", "", new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
					}
					public void onSuccess(String arg0) {
					}
				});
				BpmServiceMain.sendPostLocation("/groups?name=gamma", "", new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
					}
					public void onSuccess(String arg0) {
					}
				});
			}
		});
		
		Button users = new Button();
		users.setAutoFit(true);
		users.setTitle("Create default users (local server only)");
		users.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String user = "<user xmlns=\"" + BpmServiceMain.serviceNamespace + "\">";
				user += "<id>jdoe</id>";
				user += "<lastName>doe</lastName>";
				user += "<firstName>john</firstName>";
				user += "<email>jdoe@mitre.org</email>";
				user += "<membership group=\"alpha\" />";
				user += "<membership group=\"gamma\" />";
				user += "</user>";
				
				BpmServiceMain.sendPostLocation("/users", user, new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
					}
					public void onSuccess(String arg0) {
					}
				});
				
				user = "<user xmlns=\"" + BpmServiceMain.serviceNamespace + "\">";
				user += "<id>mpower</id>";
				user += "<lastName>power</lastName>";
				user += "<firstName>max</firstName>";
				user += "<email>mpower@mitre.org</email>";
				user += "<membership group=\"beta\" />";
				user += "<membership group=\"gamma\" />";
				user += "</user>";
				
				BpmServiceMain.sendPostLocation("/users", user, new AsyncCallback<String>() {
					public void onFailure(Throwable arg0) {
					}
					public void onSuccess(String arg0) {
					}
				});
			}
		});
		
		layout.addMember(delete);
		layout.addMember(groups);
		layout.addMember(users);
		
		createPage(layout, PageWidget.PAGE_ADMIN);
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.ADMIN ,null);
	}
}
