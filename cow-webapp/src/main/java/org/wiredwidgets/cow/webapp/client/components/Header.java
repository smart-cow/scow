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

package org.wiredwidgets.cow.webapp.client.components;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.page.PageWidget;


import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;

/**
 * A custom component that belongs at the top of every page
 * except the Login page. Simply contains a list of button
 * and dropdowns to let users navigate pages.
 * 
 * The header contains a special Page menu, where pages
 * can put anything they want in. Pages must manually add items
 * to it using the addMenuItem() method.
 * 
 * @author JSTASIK
 *
 */
public class Header extends HLayout {
	// The Page the Header is on
	PageWidget p;
	// The first menu item, which changes based on the Page
	Menu page;
	
	/**
	 * Default constructor
	 * @param page_type The type of page, see PageWidget enum
	 * @param p The PageWidget object this Header belongs to
	 */
	public Header(String page_type, PageWidget p) {
		this.p = p;
		setWidth100();
		ToolStrip t = new ToolStrip();
		t.setWidth100();
		
		page = new Menu();
		MenuItem refresh = new MenuItem("Refresh");
		refresh.addClickHandler(generateMenuClickHandler("refresh"));
		page.setItems(refresh);
		ToolStripMenuButton pageMenu = new ToolStripMenuButton("Page", page);
		t.addMenuButton(pageMenu);
		
		ToolStripButton tasks = new ToolStripButton("Tasks");
		tasks.addClickHandler(generateClickHandler(PageWidget.PAGE_TASKS));
		t.addButton(tasks);
		
		Menu create = new Menu();
		MenuItem createWorkflow = new MenuItem("New Workflow");
		createWorkflow.addClickHandler(generateMenuClickHandler("new_workflow"));
		MenuItem createTask = new MenuItem("New Task");
		createTask.addClickHandler(generateMenuClickHandler(PageWidget.PAGE_CREATENEWTASK));
		MenuItem loadWorkflow = new MenuItem("Load Workflow");
		loadWorkflow.addClickHandler(generateMenuClickHandler("load_workflow"));
		create.setItems(createWorkflow, createTask, loadWorkflow);
		ToolStripMenuButton createMenu = new ToolStripMenuButton("Create", create);
		t.addMenuButton(createMenu);
		
		Menu manage = new Menu();
		MenuItem list = new MenuItem("List");
		list.addClickHandler(generateMenuClickHandler("manage_list"));
		MenuItem tree = new MenuItem("Tree");
		tree.addClickHandler(generateMenuClickHandler("manage_tree"));
		manage.setItems(list, tree);
		ToolStripMenuButton manageMenu = new ToolStripMenuButton("Manage Workflows", manage);
		t.addMenuButton(manageMenu);
		
		/*ToolStripButton manage = new ToolStripButton("Manage");
		manage.addClickHandler(generateClickHandler("manage"));
		t.addButton(manage);*/
		
		/*ToolStripButton users = new ToolStripButton("Users");
		users.addClickHandler(generateClickHandler("users"));
		t.addButton(users);*/
		
		ToolStripButton active = new ToolStripButton("Active");
		active.addClickHandler(generateClickHandler("active"));
		t.addButton(active);
		
		ToolStripButton admin = new ToolStripButton("Admin");
		admin.addClickHandler(generateClickHandler("admin"));
		t.addButton(admin);
		
		t.addFill();
		
		ToolStripButton logout = new ToolStripButton(BpmServiceMain.getUser() + " - Logout");
		logout.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				PageManager.getInstance().setPageHistory(Pages.LOGIN ,null);
			}
		});
		t.addButton(logout);
		
		addMember(t);
	}
	
	/**
	 * Adds a menu item to the Page menu, PageWidgets must
	 * create the Page menu themselves
	 * @param m The MenuItem to add to Page
	 */
	public void addMenuItem(MenuItem m) {
		page.addItem(m);
	}
	
	/**
	 * Generates a ClickHandler that is attached to a menu button
	 * @param button The name of the button to attach
	 * @return The associated ClickHandler
	 */
	protected ClickHandler generateClickHandler(String button) {
		if(button.equals(PageWidget.PAGE_TASKS)) {
			return new ClickHandler() {
				public void onClick(ClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.TASK ,null);
				}
			};
		}/* else if(button.equals("manage")) {
			return new ClickHandler() {
				public void onClick(ClickEvent event) {
					PageManager.getInstance().setPage(new ManageWorkflows2());
				}
			};
		}*/ else if(button.equals("users")) {
			return new ClickHandler() {
				public void onClick(ClickEvent event) {
					//PageManager.getInstance().setPage();
				}
			};
		} else if(button.equals("active")) {
			return new ClickHandler() {
				public void onClick(ClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.VIEWACTIVEWORKFLOWS ,null);
				}
			};
		} else if(button.equals("admin")) {
			return new ClickHandler() {
				public void onClick(ClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.ADMIN ,null);
				}
			};
		}
		return null;
	}
	
	/**
	 * Generates a ClickHandler that is attached to a menu button
	 * @param menu The name of the button to attach
	 * @return The associated ClickHandler
	 */
	protected com.smartgwt.client.widgets.menu.events.ClickHandler generateMenuClickHandler(String menu) {
		if(menu.equals("new_workflow")) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					Object[] args = {""};
					PageManager.getInstance().setPageHistory(Pages.EDITWORKFLOWSTRING, args);
				}
			};
		} else if(menu.equals(PageWidget.PAGE_CREATENEWTASK)) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.CREATENEWTASK,null);
				}
			};
		} else if(menu.equals("load_workflow")) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					Object[] args = {true};
					PageManager.getInstance().setPageHistory(Pages.EDITWORKFLOWBOOLEAN, args);
				}
			};
		} else if(menu.equals("refresh")) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					p.refresh();
				}
			};
		} else if(menu.equals("manage_list")) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.MANAGEWORKFLOWS,null);
				}
			};
		} else if(menu.equals("manage_tree")) {
			return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					PageManager.getInstance().setPageHistory(Pages.MANAGEWORKFLOWS2,null);
				}
			};
		}
		return null;
	}
}
