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

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.components.CustomListGrid;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * A page that lets users view all saved workflows and select
 * one to view or edit.
 * 
 * @author JSTASIK
 *
 */
public class ManageWorkflows2 extends PageWidget {
	private ListGridRecord recordToRemove;

	public ManageWorkflows2() {
		super();
		createPage(null, PageWidget.PAGE_MANAGEWORKFLOWS);		
		BpmServiceMain.sendGet("/processDefinitions", new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
				SC.say("Failed to retrieve process definitions.");
			}
			public void onSuccess(String arg0) {
				generateBody(Parse.parseProcessDefinitions(arg0));
			}
		});
	}
	
	protected void generateBody(ArrayList<String> definitions) {
		Tree tree = new Tree();
		tree.setNameProperty("id");
		tree.setModelType(TreeModelType.CHILDREN);
		TreeNode root = new TreeNode();
		root.setAttribute("name", "root");
		tree.setRoot(root);
		for(int i = 0; i < definitions.size(); i++) {
			String[] definition = (definitions.get(i)).split("/");
			TreeNode parent = null;
			for(int j = 0; j < definition.length-1; j++) {
				TreeNode node = tree.find("id", "." + definition[j]);
				if(node == null) { 
					node = new TreeNode();
					node.setAttribute("name", definition[j]);
					node.setID("." + definition[j]);
					node.setIsFolder(true);
					if(parent == null)
						parent = root;
					tree.add(node, parent);
				}
				parent = node;
			}
			TreeNode node = new TreeNode();
			node.setAttribute("name", definition[definition.length-1]);
			node.setAttribute("id", definitions.get(i));
			tree.add(node, parent == null ? root : parent);
		}
		
		final TreeGrid grid = new TreeGrid();
		grid.setOverflow(Overflow.AUTO);
		grid.setWidth100();
		grid.setHeight100();
		grid.setSelectionType(SelectionStyle.SINGLE);
		grid.setShowConnectors(true);
		grid.setSortField("id");
		grid.setCanSort(false);
		grid.setData(tree);
		grid.setCanAutoFitFields(false);
		grid.getTree().openAll();
		ListGridField field = new ListGridField("name", "Name");
		ListGridField field2 = new ListGridField("id", "ID");
		grid.setFields(field2, field);
		grid.hideFields(field2);
		grid.addDoubleClickHandler(new DoubleClickHandler() {
			public void onDoubleClick(DoubleClickEvent event) {
				if(!grid.getSelectedRecord().getAttribute("id").startsWith("."))
					;
				String[] args= {grid.getSelectedRecord().getAttribute("id")};
				PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
			}
		});
		grid.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if(event.getKeyName().equals("Delete")) {
					recordToRemove = grid.getSelectedRecord();
					if(!recordToRemove.getAttribute("id").startsWith(".")) {
						SC.ask("Are you sure you want to delete " + recordToRemove.getAttribute("id"), new BooleanCallback() {
							public void execute(Boolean value) {
								if(value) {
									String name = recordToRemove.getAttribute("id");
									BpmServiceMain.sendDelete("/processDefinitions?key=" + id, new AsyncCallback<Void>() {
										public void onFailure(Throwable arg0) {
											recordToRemove = null;
											SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
										}

										public void onSuccess(Void arg0) {
											if(recordToRemove != null)
												grid.removeData(recordToRemove);
											recordToRemove = null;
										}
									});
								} else {
									recordToRemove = null;
								}
							}
						});
					}
				}
			}
		});
		addMember(grid);
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.MANAGEWORKFLOWS2, null);
	}
}
