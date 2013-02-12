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

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;

/**
 * Simple login page; does not check any credentials, but
 * the user name is used to get Tasks for that user.
 * 
 * @author JSTASIK
 *
 */
public class Login extends PageWidget {
	protected DynamicForm form;
	
	public Login() {
		super();
		setDefaultLayoutAlign(Alignment.CENTER);
		
		form = new DynamicForm();
		form.setWidth(250);
		
		TextItem username = new TextItem("Username");
		username.setTitle("Username");
		username.setRequired(true);
		username.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if(event.getKeyName().equals("Enter"))
					submit();
			}
		});
		
		PasswordItem password = new PasswordItem("Password");
		password.setTitle("Password");
		password.setRequired(false);
		password.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if(event.getKeyName().equals("Enter"))
					submit();
			}
		});
		
		form.setFields(username, password);
		
		IButton submit = new IButton();
		submit.setTitle("Submit");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		createPage(form, "login");
		addMember(submit);
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.LOGIN ,null);
	}
	
	protected void submit() {
		String username = form.getValueAsString("Username");
		if(username == null || username.equals("")) return;
		BpmServiceMain.setUser(username);
		PageManager.getInstance().setPageHistory(Pages.TASK ,null);
	}
}
