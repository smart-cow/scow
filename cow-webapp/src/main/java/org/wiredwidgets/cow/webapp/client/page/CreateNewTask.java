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

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextItem;

/**
 * Page that creates a new public task
 * 
 * @author JSTASIK
 *
 */
public class CreateNewTask extends PageWidget {
	public CreateNewTask() {
		super();
		
		final DynamicForm form = new DynamicForm();
		form.setHeight100();
		TextItem name = new TextItem();
		name.setName("Task Name");
		name.setRequired(true);
		TextItem assignee = new TextItem();
		assignee.setName("Task Assignee");
		assignee.setRequired(false);
		SubmitItem submit = new SubmitItem();
		
		form.addSubmitValuesHandler(new SubmitValuesHandler() {
			public void onSubmitValues(SubmitValuesEvent event) {
				String name = form.getValueAsString("Task Name").trim();
				String assignee = form.getValueAsString("Task Assignee").trim();
				if(name.equals("")) {
					SC.say("Please enter a task name before submitting.");
					return;
				}
				String out = "<task key=\"" + name + "\" name=\"" + name + "\" xmlns=\"" + BpmServiceMain.serviceNamespace + "\">";
				out += "<name>" + name + "</name>";
				if(!assignee.equals(""))
					out += "<assignee>"+ assignee + "</assignee>";
				out += "</task>";
				BpmServiceMain.sendPostLocation("/tasks", out, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
					}
					public void onSuccess(String result) {
						PageManager.getInstance().setPageHistory(Pages.TASK ,null);
					}
				});
			}
		});
		
		form.setFields(name, assignee, submit);
		
		createPage(form, PageWidget.PAGE_CREATENEWTASK);
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.CREATENEWTASK ,null);
	}
}
