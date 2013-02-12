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

import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.components.Header;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.BaseWidget;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Base class for all pages. Pages must implement the refresh() method,
 * which is called when the user selects Refresh from the Page menu.
 * 
 * When adding a new PageWidget, a new enum value must be added so it 
 * can be passed to the Header on creation.
 * 
 * @author JSTASIK
 *
 */
public abstract class PageWidget extends VLayout {
	public static String PAGE_CREATENEWTASK = "create_new_task";
	public static String PAGE_TASKS = "tasks";
	public static String PAGE_LOGIN = "login";
	public static String PAGE_MANAGEWORKFLOWS = "manage_workflows";
	public static String PAGE_EDITWORKFLOW = "edit_workflow";
	public static String PAGE_VIEWWORKFLOW = "view_workflow";
	public static String PAGE_ADMIN = "admin";
	public static String PAGE_VIEWACTIVEWORKFLOWS = "view_active_workflows";
	
	protected Header header;
	protected boolean headerAdded;
	
	public PageWidget() {
		setWidth100();
		setHeight100();
		headerAdded = false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createPage(BaseWidget w, String name) {
		if(!name.equals(PAGE_LOGIN) && !headerAdded) {
			header = new Header(name, this);
			addMember(header);
			headerAdded = true;
		}
		if(w != null)
			addMember(w);
		/*/MATT
		History.addValueChangeHandler(new ValueChangeHandler() {
			
			public void onValueChange(ValueChangeEvent arg0) {

				String[] args = arg0.getValue().toString().split("-ARG-");
				Pages p = Pages.valueOf(args[0]);
				
				
				BaseWidget b = null;
				
				switch (p){
					case TEMPLATE:
						b = new EditWorkflow(Window.Location.getParameter("template"));
						break;
					case TASK:
						b = new Tasks();
						break;
					case LOGIN:
						b = new Login();
						break;
					case WORKFLOW:
							boolean a = Boolean.valueOf(args[2]);
							b = new ViewWorkflow(args[1].toString(), a);

						break;
					case EDITWORKFLOWSTRING:
						b = new EditWorkflow(args[1].toString());
						break;	
					case VIEWACTIVEWORKFLOWS:
						b = new ViewActiveWorkflows();
						break;	
						
					case ADMIN:
						b = new Admin();
						break;
					case CREATENEWTASK:
						b = new CreateNewTask();
						break;
					case EDITWORKFLOWBOOLEAN:
						boolean a1 = Boolean.valueOf(args[2]);
						b = new EditWorkflow(a1);
						break;
					case MANAGEWORKFLOWS2:
						b = new ManageWorkflows2();
						break;
					case MANAGEWORKFLOWS:
						b = new ManageWorkflows();
						break;
						
					case VIEWWORKFLOW:
						
						if ((args.length > 2)){
							boolean a11 = Boolean.valueOf(args[2]);					
							b = new ViewWorkflow(args[1].toString(), a11);
						}
						else 
							b = new ViewWorkflow(args[1].toString());
						break;
					default:
						b = new Login();
						break;
						
				}
				
				PageManager.getInstance().setPage(b);
				

				
			}
		});
		*/
	}
	
	public abstract void refresh();
}
