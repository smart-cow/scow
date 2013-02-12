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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Activities;
import org.wiredwidgets.cow.webapp.client.bpm.Decision;
import org.wiredwidgets.cow.webapp.client.bpm.Exit;
import org.wiredwidgets.cow.webapp.client.bpm.Loop;
import org.wiredwidgets.cow.webapp.client.bpm.Option;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.bpm.ServiceTask;
import org.wiredwidgets.cow.webapp.client.bpm.SubProcess;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.bpm.Template;


import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Page to view a workflow, without being able to edit it
 * 
 * @author JSTASIK
 *
 */
public class ViewWorkflow extends PageWidget {
	private Template template;
	private boolean instance;
	private String processInstanceId;
	private Window popup;
	private Timer timer;
	private TreeGrid grid;
	private Map<String, String> completionStates;
	HashMap<String, String> instanceMap;
	
	public ViewWorkflow(final String name) {
		super();
		
		if(name == null || name.equals("")) {
			generateBody(null, false);
		} else {
			BpmServiceMain.sendGet("/processes/" + BpmServiceMain.urlEncode(name) + "?format=v2", new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
				}
				public void onSuccess(String result) {
					generateBody(result, false);
					BpmServiceMain.sendGet("/processInstances/active/" + BpmServiceMain.urlEncode(name) + ".*", new AsyncCallback<String>() {
						public void onFailure(Throwable arg0) {
						}
						public void onSuccess(String arg0) {
							ArrayList<String> names = Parse.parseTemplateInstances(arg0);
							ArrayList<String> ids = Parse.parseTemplateInstancesIds(arg0);
							for(int i = 0; i < names.size(); i++) {
								instanceMap.put(names.get(i), ids.get(i));
							}
							MenuItem active = new MenuItem("Active Instances");
							Menu m = new Menu();
							for(String s : names) {
								MenuItem instance = new MenuItem(s);
								instance.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
									public void onClick(MenuItemClickEvent arg0) {
										Object[] args= {instanceMap.get(arg0.getItem().getTitle()), true};
										PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
									}
								});
								m.addItem(instance);
							}
							active.setSubmenu(m);
							header.addMenuItem(active);
						}
					});
				}
			});
		}
	}
	
	public ViewWorkflow(final String name, final boolean instance) {
		super();
		processInstanceId = name;
		BpmServiceMain.sendGet("/processInstances/active/" + BpmServiceMain.urlEncode(name) + "/status", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
			}
			public void onSuccess(String result) {
				generateBody(result, instance);
				timer = new Timer() {
					public void run() {
						BpmServiceMain.sendGet("/processInstances/active/" + BpmServiceMain.urlEncode(name) + "/status", new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								updateTree(false);
								timer.cancel();
							}
							public void onSuccess(String result) {
								completionStates = Parse.parseTemplateCompletion(result);
								updateTree(true);
							}
						});
					}
				};
				timer.scheduleRepeating(BpmServiceMain.getPollingRate());
			}
		});
	}
	
	protected void generateBody(String result, boolean instance) {
		this.instance = instance;
		
		template = (result == null || result.equals("") ? new Template() : Parse.parseTemplate(result));
		
		// TEMPLATE NAME TEXTFIELD
		Label templateName = new Label();
		templateName.addStyleName("bigLabel");
		templateName.setContents(template.getName());
		
		// TREEITEM VIEW CONTAINER
		final VLayout layoutContainer = new VLayout();
		layoutContainer.setCanDragResize(true);
		layoutContainer.setResizeFrom("L", "R");
		layoutContainer.setWidth100();
		layoutContainer.setHeight100();
		layoutContainer.setBorder("1px solid #A7ABB4");
		Label layoutHelp = new Label();
		layoutHelp.setWidth100();
		layoutHelp.setMargin(10);
		layoutHelp.setDefaultHeight(20);
		layoutHelp.setContents("Click on an item in the tree to the left to see more information about that item.");
		layoutContainer.addMember(layoutHelp);
		
		// TREEGRID
		grid = new TreeGrid() {
            protected String getBaseStyle(ListGridRecord record, int rowNum, int colNum) {
				TreeNode node = (TreeNode)record;
				if(node.getAttribute("completion") != null && !node.getAttribute("completion").equals("")) {
					return node.getAttribute("completion");
				}
                return super.getBaseStyle(record, rowNum, colNum);
            }
		};
		grid.setOverflow(Overflow.AUTO);
		grid.setCanDragResize(true);
		grid.setResizeFrom("L", "R");
		grid.setWidth("50%");
		grid.setHeight100();
		grid.setSelectionType(SelectionStyle.SINGLE);
		grid.setData(template.getTree(false));
		grid.setShowConnectors(true);
		grid.setCanSort(false);
		grid.setCanAutoFitFields(false);
		grid.getTree().openAll();
		ListGridField field = new ListGridField("name", "Name");
		grid.setFields(field);
		
		// UPDATE FORM
		grid.addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(SelectionEvent event) {
				Canvas oldLayout = layoutContainer.getMember(0);
				Object activity = event.getRecord().getAttributeAsObject("activity");
				if(oldLayout != null) {
					layoutContainer.removeMember(oldLayout);
					oldLayout.destroy();
				}
				DynamicForm form = new DynamicForm();
				form.setMargin(10);
				form.setWidth100();
				form.setHeight100();
				
				HeaderItem basic = new HeaderItem("Basic");
				basic.setValue("Basic Options");
				
				HeaderItem advanced = new HeaderItem("Advanced");
				advanced.setValue("Advanced Options");
				
				StaticTextItem name = new StaticTextItem("Name");
				name.setTitle("<nobr>Name</nobr>");
				
				StaticTextItem bypass = new StaticTextItem("Bypass");
				bypass.setTitle("<nobr>Bypassable?</nobr>");
				
				StaticTextItem description = new StaticTextItem("Description");
				description.setTitle("<nobr>Description</nobr>");
				
				if(activity instanceof Task) {
					Task t = (Task)activity;
					name.setValue(t.getName());
					bypass.setValue(t.getBypass() ? "Yes" : "No");
					
					StaticTextItem assignee = new StaticTextItem("Assignee");
					assignee.setTitle("<nobr>" + t.get("assigneeType") + "</nobr>");
					assignee.setValue(t.get("assignee"));
					
					description.setValue(t.getHtmlDescription());
					
					StaticTextItem addInfo1 = new StaticTextItem("AddInfo1");
					addInfo1.setTitle("<nobr>Additional Info 1</nobr>");
					addInfo1.setValue(BpmServiceMain.xmlEncode(t.getVariable("Additional Info 1")));
					
					StaticTextItem addInfo2 = new StaticTextItem("AddInfo2");
					addInfo2.setTitle("<nobr>Additional Info 2</nobr>");
					addInfo2.setValue(BpmServiceMain.xmlEncode(t.getVariable("Additional Info 2")));

					form.setFields(basic, name, assignee, description, advanced, addInfo1, addInfo2, bypass);
				} else if(activity instanceof Exit) {
					Exit e = (Exit)activity;
					name.setValue(e.getName());
					
					StaticTextItem reason = new StaticTextItem("Reason");
					reason.setTitle("<nobr>Reason for exit</nobr>");
					reason.setValue(e.getReason());
					
					description.setValue(e.getHtmlDescription());

					form.setFields(basic, name, reason, description);
				} else if(activity instanceof ServiceTask) {
					ServiceTask s = (ServiceTask)activity;
					name.setValue(s.getName());
					bypass.setValue(s.getBypass() ? "Yes" : "No");
					
					StaticTextItem method = new StaticTextItem("Method");
					method.setTitle("<nobr>Method</nobr>");
					method.setValue("<nobr>" + s.getMethod() + "</nobr>");
					
					StaticTextItem url = new StaticTextItem("Url");
					url.setTitle("<nobr>Url</nobr>");
					url.setValue("<nobr><a href=\"" + s.getServiceUrl() + "\">" + s.getServiceUrl() + "</a></nobr>");
					
					StaticTextItem content = new StaticTextItem("Content");
					content.setTitle("<nobr>Content</nobr>");
					content.setValue("<nobr>" + BpmServiceMain.xmlEncode(s.getContent()) + "</nobr>");
					
					StaticTextItem variable = new StaticTextItem("Var");
					variable.setTitle("<nobr>Variable</nobr>");
					variable.setValue("<nobr>" + BpmServiceMain.xmlEncode(s.getVar()) + "</nobr>");
					
					description.setValue(s.getHtmlDescription());
					
					form.setFields(basic, name, method, url, content, variable, description, advanced, bypass);
				} else if(activity instanceof Activities) {
					Activities a = (Activities)activity;
					name.setValue(a.getName());
					bypass.setValue(a.getBypass() ? "Yes" : "No");
					
					StaticTextItem order = new StaticTextItem("Order");
					order.setTitle("<nobr>Actions Order</nobr>");
					order.setValue(a.isSequential() ? "One at a time, in order" : "All at the same time");
					
					description.setValue(a.getHtmlDescription());
					
					form.setFields(basic, name, order, description, advanced, bypass);
				} else if(activity instanceof Loop) {
					Loop l = (Loop)activity;
					name.setValue(l.getName());
					bypass.setValue(l.getBypass() ? "Yes" : "No");
					
					StaticTextItem loopTask = new StaticTextItem("Loop End Condition");
					loopTask.setTitle("<nobr>Loop End Condition</nobr>");
					loopTask.setValue(l.getLoopTask().getName());
					
					StaticTextItem loopTaskAssignee = new StaticTextItem("Loop Decision-Maker");
					loopTaskAssignee.setTitle("<nobr>Loop Decision-Making " + l.getLoopTask().get("assigneeType") + "</nobr>");
					loopTaskAssignee.setValue(l.getLoopTask().get("assignee"));

					description.setValue(l.getHtmlDescription());
					
					StaticTextItem order = new StaticTextItem("Order");
					order.setTitle("<nobr>Actions Order</nobr>");
					order.setValue(l.isSequential() ? "One at a time, in order" : "All at the same time");
					
					StaticTextItem addInfo1 = new StaticTextItem("AddInfo1");
					addInfo1.setTitle("<nobr>Additional Info 1</nobr>");
					addInfo1.setValue(BpmServiceMain.xmlEncode(l.getLoopTask().getVariable("Additional Info 1")));
					
					StaticTextItem addInfo2 = new StaticTextItem("AddInfo2");
					addInfo2.setTitle("<nobr>Additional Info 2</nobr>");
					addInfo2.setValue(BpmServiceMain.xmlEncode(l.getLoopTask().getVariable("Additional Info 2")));
					
					StaticTextItem doneName = new StaticTextItem("Done Name");
					doneName.setTitle("<nobr>Option To Finish Loop</nobr>");
					doneName.setValue(BpmServiceMain.xmlEncode(l.getDoneName()));
					
					StaticTextItem repeatName = new StaticTextItem("Repeat Name");
					repeatName.setTitle("<nobr>Option To Repeat Loop</nobr>");
					repeatName.setValue(BpmServiceMain.xmlEncode(l.getRepeatName()));
					
					form.setFields(basic, name, loopTask, loopTaskAssignee, description, order, advanced, addInfo1, addInfo2, doneName, repeatName, bypass);
				} else if(activity instanceof Decision) {
					Decision d = (Decision)activity;
					name.setValue(d.getName());
					bypass.setValue(d.getBypass() ? "Yes" : "No");
					
					StaticTextItem decisionTask = new StaticTextItem("Decision Question");
					decisionTask.setTitle("<nobr>Decision Question</nobr>");
					decisionTask.setValue(d.getTask().getName());
					
					StaticTextItem decisionTaskAssignee = new StaticTextItem("Decision-Maker");
					decisionTaskAssignee.setTitle("<nobr>Decision-Making " + d.getTask().get("assigneeType") + "</nobr>");
					decisionTaskAssignee.setValue(d.getTask().get("assignee"));

					description.setValue(d.getHtmlDescription());
					
					StaticTextItem addInfo1 = new StaticTextItem("AddInfo1");
					addInfo1.setTitle("<nobr>Additional Info 1</nobr>");
					addInfo1.setValue(BpmServiceMain.xmlEncode(d.getTask().getVariable("Additional Info 1")));
					
					StaticTextItem addInfo2 = new StaticTextItem("AddInfo2");
					addInfo2.setTitle("<nobr>Additional Info 2</nobr>");
					addInfo2.setValue(BpmServiceMain.xmlEncode(d.getTask().getVariable("Additional Info 2")));
					
					form.setFields(basic, name, decisionTask, decisionTaskAssignee, description, advanced, addInfo1, addInfo2, bypass);
				} else if(activity instanceof Option) {
					Option o = (Option)activity;
					name.setValue(o.getName());
					bypass.setValue(o.getBypass() ? "Yes" : "No");
					
					StaticTextItem order = new StaticTextItem("Order");
					order.setTitle("<nobr>Actions Order</nobr>");
					order.setValue(o.isSequential() ? "One at a time, in order" : "All at the same time");
					
					description.setValue(o.getActivities().getHtmlDescription());
					
					form.setFields(basic, name, order, description, advanced, bypass);
				} else if(activity instanceof SubProcess) {
					SubProcess s = (SubProcess)activity;
					name.setValue(s.getName());
					bypass.setValue(s.getBypass() ? "Yes" : "No");
					
					StaticTextItem workflow = new StaticTextItem("Workflow");
					workflow.setTitle("<nobr>Workflow</nobr>");
					workflow.setValue(s.getWorkflow());
					
					description.setValue(s.getHtmlDescription());
					
					form.setFields(basic, name, workflow, description, advanced, bypass);
				}
				
				layoutContainer.addMember(form);
			}
		});
		
		HLayout center = new HLayout();
		center.setWidth100();
		center.setHeight100();
		center.addMember(grid);
		center.addMember(layoutContainer);
		
		createPage(templateName, PageWidget.PAGE_VIEWWORKFLOW);
		
		if(!instance) {
			// EDIT BUTTON
			MenuItem edit = new MenuItem("Edit");
			edit.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					String[] args= {template.getName()};
					PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
				}
			});
			header.addMenuItem(edit);
			
			// INITIATE BUTTON
			MenuItem initiate = new MenuItem("Initiate");
			initiate.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					popup = new Window();
					popup.setAlign(Alignment.CENTER);
					popup.setWidth(300);
					popup.setHeight(300);
					popup.setTitle("Start Workflow");
					popup.setIsModal(true);
					popup.setShowModalMask(true);
					popup.setShowMinimizeButton(false);
					popup.addCloseClickHandler(new CloseClickHandler() {
						public void onCloseClick(CloseClientEvent event) {
							popup.destroy();
						}
					});
					popup.centerInPage();
					
					DynamicForm form = new DynamicForm();
					form.setWidth(250);
					form.setLayoutAlign(Alignment.CENTER);
					
					FormItem[] fields = new FormItem[13];
					
					TextItem name = new TextItem("Name");
					name.setTitle("Name");
					name.setValue(template.getName() + "-" + (new Date()).toGMTString());
					name.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if(event.getKeyName().equals("Enter"))
								submit(event.getForm().getValues());
						}
					});
					fields[0] = name;
					
					ComboBoxItem order = new ComboBoxItem("Priority");
					order.setTitle("<nobr>Priority</nobr>");
					order.setValueMap("1", "2", "3", "4", "5");
					order.setValue("3");
					fields[1] = order;
					
					for(int i = 0; i < 5; i++) { 
						TextItem key = new TextItem();
						key.setName("key"+i);
						key.setShowTitle(false);
						key.setEmptyDisplayValue("Note Name");
						key.addKeyPressHandler(new KeyPressHandler() {
							public void onKeyPress(KeyPressEvent event) {
								if(event.getKeyName().equals("Enter"))
									submit(event.getForm().getValues());
							}
						});
						TextItem value = new TextItem();
						value.setEmptyDisplayValue("Note or URL");
						value.setShowTitle(false);
						value.setName("value"+i);
						value.addKeyPressHandler(new KeyPressHandler() {
							public void onKeyPress(KeyPressEvent event) {
								if(event.getKeyName().equals("Enter"))
									submit(event.getForm().getValues());
							}
						});
						key.setWidth(100);
						value.setWidth(100);
						fields[i*2+2] = key;
						fields[i*2+3] = value;
					}					
					
					SubmitItem submit = new SubmitItem();
					fields[12] = submit;
					form.addSubmitValuesHandler(new SubmitValuesHandler() {
						public void onSubmitValues(SubmitValuesEvent event) {
							submit(event.getValuesAsMap());
						}
					});
					
					form.setFields(fields);
					popup.addItem(form);
					popup.show();
				}
			});
			header.addMenuItem(initiate);
		} else {
			MenuItem back = new MenuItem("Back to Workflow");
			back.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					String[] args= {template.getName()};
					PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
				}
			});
			header.addMenuItem(back);
		}
		addMember(center);
	}
	
	protected void submit(Map<String, String> map) {
		String varString = "";
		for(int i = 0; i < 5; i++) {
			String key = map.get("key"+i), value = map.get("value"+i);
			if(key != null && !key.equals("Note Name")) {
				key = BpmServiceMain.xmlEncode(key);
				if(value == null || value == "Note or URL") {
					SC.say("The name for note #" + (i+1) + " is filled in, but there is no value");
					return;
				}
				value = BpmServiceMain.xmlEncode(value);
				varString += "<variable name=\""+key+"\" value=\""+value+"\" />";
			} else if((key == null || key.equals("Note Name")) && (value != null && !value.equals("Note or URL"))) {
				SC.say("The value for note #" + (i+1) + " is filled in, but there is no name");
				return;
			}
		}
		String out = "<processInstance xmlns=\"" + BpmServiceMain.serviceNamespace + "\">";
		out += "<processDefinitionKey>" + template.getName() + "</processDefinitionKey>";
		out += "<name>" + map.get("Name") + "</name>";
		out += "<priority>" + map.get("Priority") + "</priority>";
		out += "<variables>" + varString + "</variables>";
		out += "</processInstance>";
		
		popup.destroy();
		
		BpmServiceMain.sendPostLocation("/processInstances/active", out, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
			}

			public void onSuccess(String result) {
				refresh();
			}
		});
	}
	
	protected void updateTree(boolean success) {
		if(success) {
	    Iterator<Entry<String, String>> it = completionStates.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
		        TreeNode node = grid.getTree().find("name", pairs.getKey());
		        node.setAttribute("completion", pairs.getValue());
		    }
		} else {
			//TODO Doesn't this improperly mark all nodes as completed if any error occurs? MH
			TreeNode[] nodes = grid.getTree().getAllNodes();
			for(int i = 0; i < nodes.length; i++) {
				nodes[i].setAttribute("completion", "completed");
			}
		}
	    grid.redraw();
	}
	
	public void refresh() {
		if(instance){
			Object[] args= {processInstanceId, true};
			PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
		}
		else{
			String[] args= {template.getName()};
			PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
		}
	}
	
	public void destroy() {
		if(timer != null)
			timer.cancel();
		super.destroy();
	}
}
