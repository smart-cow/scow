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
import java.util.LinkedHashMap;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Activities;
import org.wiredwidgets.cow.webapp.client.bpm.Activity;
import org.wiredwidgets.cow.webapp.client.bpm.BaseList;
import org.wiredwidgets.cow.webapp.client.bpm.Decision;
import org.wiredwidgets.cow.webapp.client.bpm.Exit;
import org.wiredwidgets.cow.webapp.client.bpm.Loop;
import org.wiredwidgets.cow.webapp.client.bpm.Option;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.bpm.ServiceTask;
import org.wiredwidgets.cow.webapp.client.bpm.SubProcess;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.bpm.Template;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.DragAppearance;
import com.smartgwt.client.types.ImageStyle;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.events.DragStartEvent;
import com.smartgwt.client.widgets.events.DragStartHandler;
import com.smartgwt.client.widgets.events.DragStopEvent;
import com.smartgwt.client.widgets.events.DragStopHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.PickerIcon.Picker;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.FocusEvent;
import com.smartgwt.client.widgets.form.fields.events.FocusHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderDropEvent;
import com.smartgwt.client.widgets.tree.events.FolderDropHandler;

/**
 * Page to edit a workflow
 * 
 * There are a lot of instanceof calls on this page, because
 * there is currently no Interface linking Activity and Option.
 * Because of this, the easy to tell the difference between
 * them is to use instanceof. It's not great but it gets the job
 * done. 
 * 
 * @author JSTASIK
 *
 */
public class EditWorkflow extends PageWidget {
	private boolean removeTreeNode = false; // used for dragging nodes onto decisions (and creating options)
	private int counter = 1; // used for naming new tasks
	private TreeNode copyParent = null; // the list you're copying a workflow to
	private Template template; // the current workflow
	
	// helper vars to tell where a node was dropped
	// smartgwt provides different index values depending on where you drop it
	// so a small adjustment is necessary in some cases
	private int initialPosition = -1;
	private TreeNode initialParent = null;
	
	// a way to keep track of the selected node before a blur event
	// because blurs happen after the new node is selected
	private TreeNode tempBlurNode = null;
	
	// maps for users and groups combo box
	private LinkedHashMap<String, String> usersMap;
	private LinkedHashMap<String, String> groupsMap;
	private LinkedHashMap<String, String> workflowsMap;
	
	// side bar for advanced icons
	private VLayout sidebar;
	private Button sidebarButton;
	
	// form which holds overall workflow name
	DynamicForm nameForm;
	
	// folder selection
	private Window folderSelector = null;
	private TreeGrid treeGrid = null;
	private ArrayList<String> tempFolders;

	/**
	 * Constructor
	 * @param name The name of the workflow to exit, or "" if creating a new workflow
	 */
	public EditWorkflow(String name) {
		super();
		
		// create the combo box data maps
		loadInitialData();
		
		// if no name is supplied, create a new workflow
		// otherwise, load the old one for editing
		// name can technically never be null because of constructor overload
		if(name == null || name.equals("")) {
			generateBody("");
		} else {
			BpmServiceMain.sendGet("/processes/" + BpmServiceMain.urlEncode(name) + "?format=v2", new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
				}
				public void onSuccess(String result) {
					generateBody(result);
				}
			});
		}
	}
	
	public EditWorkflow(boolean load) {
		super();
		
		loadInitialData();
		if(load) {
			final VLayout layout = new VLayout();
			layout.setWidth100();
			layout.setHeight100();
			final DynamicForm form = new DynamicForm();
			form.setFields(new TextAreaItem("XML"));
			
			IButton submit = new IButton();
			submit.setLayoutAlign(VerticalAlignment.TOP);
			submit.setTitle("Submit");
			submit.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					removeChild(layout);
					generateBody(form.getValueAsString("XML"));
				}
			});
			layout.addMember(form);
			layout.addMember(submit);
			createPage(layout, PageWidget.PAGE_EDITWORKFLOW);
		} else {
			generateBody("");
		}
	}
	
	/**
	 * Constructor
	 * @param t The template to edit
	 */
	public EditWorkflow(Template t) {
		super();
		
		loadInitialData();
		
		// technically can never be null because of constructor overload
		Template template = t == null ? new Template() : t;
		template.clearTree();
		generateBody(template);
	}
	
	protected void loadInitialData() {
		usersMap = new LinkedHashMap<String, String>();
		groupsMap = new LinkedHashMap<String, String>();
		workflowsMap = new LinkedHashMap<String, String>();
		tempFolders = new ArrayList<String>();
		// get list of users
		BpmServiceMain.sendGet("/users", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<String> users = Parse.parseUsers(result);
				for(String s : users) {
					usersMap.put(s, s);
				}
			}
		});
		// get list of groups
		BpmServiceMain.sendGet("/groups", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<String> groups = Parse.parseGroups(result);
				for(String s : groups) {
					groupsMap.put(s, s);
				}
			}
		});
		// get list of existing workflows
		BpmServiceMain.sendGet("/processDefinitions", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<String> workflows = Parse.parseProcessDefinitions(result);
				for(String s : workflows) {
					workflowsMap.put(s, s);
				}
			}
		});
	}
	
	/**
	 * Parses XML and then calls method to generate body of page
	 * @param result The XML of the workflow
	 */
	protected void generateBody(String result) {
		try {
			Template template = (result == null || result.equals("") ? new Template() : Parse.parseTemplate(result));
			generateBody(template);
		} catch(Exception e) {
			createPage(new Label("Could not parse XML"), PageWidget.PAGE_EDITWORKFLOW);
		}
	}
	
	/**
	 * Generates all the widgets that make up the body of the page
	 * @param t The workflow to display
	 */
	protected void generateBody(Template t) {
		template = t;
		
		// ADD ACTIVITY ICON BAR
		Label addNew = new Label();
		addNew.setContents("To add a new item to your workflow, drag one of the icons to the right onto the tree.");
		addNew.setWidth(225);
		addNew.setMargin(10);
		
		HLayout topBar = new HLayout();
		topBar.setBorder("1px solid #A7ABB4");
		topBar.setWidth100();
		topBar.addMember(addNew);
		topBar.addMember(generateActivityImg("Task", 50, 50));
		LayoutSpacer spacer5 = new LayoutSpacer();
		spacer5.setWidth(15);
		topBar.addMember(spacer5);
		topBar.addMember(generateActivityImg("List", 50, 50));
		LayoutSpacer spacer2 = new LayoutSpacer();
		spacer2.setWidth(15);
		topBar.addMember(spacer2);
		topBar.addMember(generateActivityImg("Loop", 50, 50));
		LayoutSpacer spacer3 = new LayoutSpacer();
		spacer3.setWidth(15);
		topBar.addMember(spacer3);
		topBar.addMember(generateActivityImg("Decision", 50, 50));
		LayoutSpacer spacer6 = new LayoutSpacer();
		spacer6.setWidth(15);
		topBar.addMember(spacer6);
		sidebarButton = new Button("Advanced");
		sidebarButton.setActionType(SelectionType.CHECKBOX);
		sidebarButton.setShowRollOver(false);
		sidebarButton.setHeight(32);
		sidebarButton.setLayoutAlign(VerticalAlignment.CENTER);
		sidebarButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
                toggleSidebar();
            }  
        });
        topBar.addMember(sidebarButton);
        
        // ADVANCED SIDE BAR
        sidebar = new VLayout();
        sidebar.setWidth(50);
        sidebar.setHeight100();
        sidebar.setCanDragResize(true);
        sidebar.setBorder("1px solid #A7ABB4");
        sidebar.setResizeFrom("L", "R");
        sidebar.addMember(generateActivityImg("ServiceTask", 50, 50));
        LayoutSpacer sidebarSpacer1 = new LayoutSpacer();
        sidebarSpacer1.setHeight(15);
		sidebar.addMember(sidebarSpacer1);
		sidebar.addMember(generateActivityImg("Copy", 50, 50));
		LayoutSpacer sidebarSpacer2 = new LayoutSpacer();
		sidebarSpacer2.setHeight(15);
		sidebar.addMember(sidebarSpacer2);
		sidebar.addMember(generateActivityImg("Exit", 50, 50));
        LayoutSpacer sidebarSpacer3 = new LayoutSpacer();
        sidebarSpacer3.setHeight(15);
		sidebar.addMember(sidebarSpacer3);
		sidebar.addMember(generateActivityImg("SubProcess", 50, 50));
        LayoutSpacer sidebarSpacer4 = new LayoutSpacer();
		sidebar.addMember(sidebarSpacer4);
		sidebar.setVisible(false);
		
		
		// UPDATE FORM CONTAINER
		final VLayout formContainer = new VLayout();
		formContainer.setCanDragResize(true);
		formContainer.setResizeFrom("L", "R");
		formContainer.setWidth100();
		formContainer.setHeight100();
		formContainer.setBorder("1px solid #A7ABB4");
		Label formHelp = new Label();
		formHelp.setWidth100();
		formHelp.setMargin(10);
		formHelp.setDefaultHeight(20);
		formHelp.setContents("Click on an item in the tree to the left, and a form will appear that allows you to edit your selected item.");
		formContainer.addMember(formHelp);
		
		// TREEGRID
		final TreeGrid grid = new TreeGrid() {
			// allow Img objects in addition to defaults
			public Boolean willAcceptDrop() {
				if(EventHandler.getDragTarget() instanceof Img) return new Boolean(true);
				return super.willAcceptDrop();
			}
		};
		grid.setOverflow(Overflow.AUTO);
		grid.setCanDragResize(true);
		grid.setResizeFrom("L", "R");
		grid.setWidth("50%");
		grid.setHeight100();
		grid.setSelectionType(SelectionStyle.SINGLE);
		grid.setData(template.getTree(true));
		grid.setShowConnectors(true);
		grid.setCanAcceptDrop(true);
		grid.setCanReorderRecords(true);
		grid.setCanReparentNodes(true);
		grid.setCanSort(false);
		grid.setCanAutoFitFields(false);
		grid.getTree().openAll();
		ListGridField field = new ListGridField("name", "Name");
		grid.setFields(field);
		grid.addDragStartHandler(new DragStartHandler() {
			// on a drag, remove the selected node from the underlying workflow
			// when it's dropped, it'll be readded (even if the drop is cancelled
			// and it goes back to the same location)
			public void onDragStart(DragStartEvent event) {
				Object child = grid.getSelectedRecord().getAttributeAsObject("activity");
				Activity parent = null;
				if(child instanceof Activity) {
					parent = ((Activity)child).getParent();
				} else if(child instanceof Option){ 
					parent = ((Option)child).getParent();
				}
				
				if(parent instanceof Activities) {
					((Activities)parent).removeActivity((Activity)child);
				} else if(parent instanceof BaseList) {
					((BaseList)parent).removeActivity((Activity)child);
				} else if(parent instanceof Decision) {
					((Decision)parent).removeOption((Option)child);
				}
				
				// store parent/starting position for adjustments later on
				initialParent = grid.getTree().getParent((TreeNode)grid.getSelectedRecord());
				TreeNode[] children = grid.getTree().getChildren(initialParent);
				for(int i = 0; i < children.length; i++) {
					if(children[i].equals(grid.getSelectedRecord())) {
						initialPosition = i;
						break;
					}
				}
			}
		});
		grid.addFolderDropHandler(new FolderDropHandler() {
			// when a treenode is dropped on the grid, add it at the appropriate location
			// only called for reordering nodes
			public void onFolderDrop(FolderDropEvent event) {
				Object child = (event.getNodes()[0]).getAttributeAsObject("activity");
				Object parent = event.getFolder().getAttributeAsObject("activity");
				
				// smartgwt index adjustment
				int index = event.getIndex();
				if(index > initialPosition && event.getFolder().equals(initialParent)) {
					index--;
				}
				
				// if dragging onto a collapsed node, expand it first
				grid.getTree().openFolder(event.getFolder());
				
				// if dragging an option, convert it to a list unless the parent
				// is a decision
				if(child instanceof Option) {
					if(parent instanceof Decision) {
						((Decision)parent).insertOption((Option)child, index);
						return;
					} else {
						child = ((Option)child).getActivities();
						event.getNodes()[0].setAttribute("icon", "Icon_List.png");
					}
				}
				
				if(parent instanceof Activities) {
					((Activities)parent).insertActivity((Activity)child, index);
				} else if(parent instanceof BaseList) {
					((BaseList)parent).insertActivity((Activity)child, index);
				} else if(parent instanceof Loop) {
					((Loop)parent).insertActivity((Activity)child, index);
				} else if(parent instanceof Decision) {
					// if dragging an activity onto a decision, first create a new option
					((Decision)parent).insertOption((Activity)child, index, "Option " + counter++);
					grid.getTree().add(((Decision)parent).getOptions().get(
							index).getTreeNode(true), event.getFolder(), index);
					removeTreeNode = true; // make removeTreeNode true to remove duplicate child
				} else if(parent instanceof Option) {
					((Option)parent).insertActivity((Activity)child, index);
				}
			}
		});
		grid.addDragStopHandler(new DragStopHandler() {
			// when dragging ends, check if a new option was created
			// and if so, remove the duplicate child
			public void onDragStop(DragStopEvent event) {
				if(removeTreeNode)
					grid.getTree().remove((TreeNode)grid.getSelectedRecord());
				removeTreeNode = false;
			}
		});
		grid.addKeyPressHandler(new KeyPressHandler() {
			// remove an activity from a workflow on 'delete' keypresses
			public void onKeyPress(KeyPressEvent event) {
				if(event.getKeyName().equals("Delete")) {
					TreeNode node = (TreeNode)grid.getSelectedRecord();
					DynamicForm form = (DynamicForm)formContainer.getMember(0);
					saveRecord(node, form, grid);
					formContainer.removeMember(form);
					form.destroy();
					Object activity = node.getAttributeAsObject("activity");
					grid.getTree().remove(node);
					if(activity instanceof Activity) {
						Activity parent = ((Activity)activity).getParent();
						if(parent == null) {
							parent = template.getBase();
						}
						if(parent instanceof Activities) {
							((Activities) parent).removeActivity((Activity)activity);
						} else if(parent instanceof BaseList) {
							((BaseList) parent).removeActivity((Activity)activity);
						}
					} else {
						Decision parent = ((Option)activity).getParent();
						parent.removeOption((Option)activity);
					}
				}
			}
		});

		grid.addDropHandler(new DropHandler() {
			// when a non-treenode (in this case, Img) is dropped on the grid
			// only called when adding a new activity to the grid
			public void onDrop(DropEvent event) {
				if(EventHandler.getDragTarget() instanceof Img) {
					Img target = (Img)EventHandler.getDragTarget();
					Activity a = null;
					
					if(target.getTitle().equals("Task")) {
						a = new Task("Task " + counter, "Task " + counter++);
					} else if(target.getTitle().equals("Exit")) {
						a = new Exit("Exit", "Exit");
					} else if(target.getTitle().equals("ServiceTask")) {
						a = new ServiceTask("ServiceTask " + counter, "ServiceTask " + counter++);
					} else if(target.getTitle().equals("List")) {
						a = new Activities("List " + counter, "List " + counter++);
					} else if(target.getTitle().equals("Loop")) {
						a = new Loop("Loop " + counter, "Loop " + counter++);
						((Loop)a).setTask(new Task());
					} else if(target.getTitle().equals("Decision")) {
						a = new Decision("Decision " + counter, "Decision " + counter++);
						((Decision)a).setTask(new Task());
					} else if(target.getTitle().equals("SubProcess")) {
						a = new SubProcess("SubProcess " + counter, "SubProcess " + counter++);
					} else if(target.getTitle().equals("Copy")) {
						copyParent = grid.getDropFolder() == null ? grid.getTree().getRoot() : grid.getDropFolder();
						// if using the copy command, first grab a list of all existing workflow names
						BpmServiceMain.sendGet("/processDefinitions", new AsyncCallback<String>() {
							public void onFailure(Throwable arg0) {
							}
							public void onSuccess(String arg0) {
								ArrayList<String> processDefinitions = Parse.parseProcessDefinitions(arg0);
								// build a modal dialog window to allow the user to choose which workflow to copy
								final Window choose = new Window();
								choose.setHeight(300);
								choose.setWidth(250);
								choose.setIsModal(true);
								choose.setTitle("Choose a workflow to copy");
								choose.setShowModalMask(true);
								choose.centerInPage();
								choose.setShowMinimizeButton(false);
								choose.addCloseClickHandler(new CloseClickHandler() {
									public void onCloseClick(CloseClientEvent event) {
										choose.destroy();
									}
								});
								VLayout v = new VLayout();
								v.setWidth100();
								v.setHeight100();
								// output a blue string (to look like a hyperlink) for each
								// workflow name
								for(String s : processDefinitions) {
									Label l = new Label();
									l.setContents("<span style=\"color:blue;\">" + s + "</span>");
									l.setID(s);
									l.setEdgeSize(3);
									l.setWidth100();
									l.setCursor(Cursor.POINTER);
									// override mouseover and mouseout handlers to change background color
									// of rows so users know which workflow they're going to select
									l.addMouseOverHandler(new MouseOverHandler() {
										public void onMouseOver(MouseOverEvent event) {
											((Label)event.getSource()).setBackgroundColor("LightGray");
										}
									});
									l.addMouseOutHandler(new MouseOutHandler() {
										public void onMouseOut(MouseOutEvent event) {
											((Label)event.getSource()).setBackgroundColor("White");
										}
									});
									l.addClickHandler(new ClickHandler() {
										// when a user picks a name, grab the workflow and add it in
										public void onClick(ClickEvent event) {
											String title = ((Label)event.getSource()).getID();
											choose.destroy();
											BpmServiceMain.sendGet("/processes/" + BpmServiceMain.urlEncode(title) + "?format=v2", new AsyncCallback<String>() {
												public void onFailure(Throwable arg0) {
												}
												public void onSuccess(String arg0) {
													Activity a = Parse.parseCopyTemplate(arg0);
													addActivityToTree(a, copyParent, grid);
													copyParent = null;
												}
											});
										}
									});
									v.addMember(l);
								}
								choose.addItem(v);
								choose.show();
							}
						});
						event.cancel();
						return;
					}
					addActivityToTree(a, grid.getDropFolder(), grid);
					event.cancel();
				}
			}
		});
		
		// UPDATE FORM
		grid.addSelectionChangedHandler(new SelectionChangedHandler() {
			// when a new row is selected, save the old record, destroy the old form,
			// and create a new form for the newly selected record
			// 2 selection events get generated when selecting a new row -
			// one for deselection of the existing selection, and one for the newly
			// selected node
			// the deselection event can be detected by checking if
			// event.getSelectedRecord() == null
			public void onSelectionChanged(SelectionEvent event) {
				Canvas oldForm = formContainer.getMember(0);
				Object activity = event.getRecord().getAttributeAsObject("activity");
				if(event.getSelectedRecord() == null) {
					saveRecord((TreeNode)event.getRecord(), (DynamicForm)oldForm, grid);
				} else {
					if(oldForm != null) {
						formContainer.removeMember(oldForm);
						oldForm.destroy();
					}
					DynamicForm form = new DynamicForm();
					form.setMargin(10);
					form.setWidth100();
					form.setHeight100();
					
					HeaderItem basic = new HeaderItem("Basic");
					basic.setValue("Basic Options");
					
					HeaderItem advanced = new HeaderItem("Advanced");
					advanced.setValue("Advanced Options");
					
					TextItem name = new TextItem("Name");
					name.setRequired(true);
					name.setTitle("<nobr>Name</nobr>");
					name.addFocusHandler(new FocusHandler() {
						public void onFocus(FocusEvent event) {
							tempBlurNode = (TreeNode)grid.getSelectedRecord();
						}
					});
					name.addBlurHandler(new BlurHandler() {
						public void onBlur(BlurEvent event) {
							if(tempBlurNode != null) {
								tempBlurNode.setName(event.getForm().getValueAsString("Name"));
								grid.redraw();
								tempBlurNode = null;
							}
						}
					});
					name.addKeyPressHandler(new com.smartgwt.client.widgets.form.fields.events.KeyPressHandler() {
						public void onKeyPress(com.smartgwt.client.widgets.form.fields.events.KeyPressEvent event) {
							if(event.getKeyName().equals("Enter")) {
								TreeNode node = (TreeNode)grid.getSelectedRecord();
								node.setName(event.getForm().getValueAsString("Name"));
								grid.redraw();
							}
						}
					});
					name.setWidth(300);
					
					CheckboxItem bypass = new CheckboxItem("Bypass");
					bypass.setTitle("<nobr>Allow admin to bypass</nobr>");

					TextAreaItem description = new TextAreaItem("Description");
					description.setTitle("<nobr>Description</nobr>");
					description.setWidth(300);
					
					if(activity instanceof Task) {
						Task t = (Task)activity;
						name.setValue(getSavedStringValue(t.getName()));
						bypass.setValue(t.getBypass());
						
						RadioGroupItem assigneeType = new RadioGroupItem("AssigneeType");
						assigneeType.setTitle("<nobr>Assign To</nobr>");
						assigneeType.setVertical(false);
						assigneeType.setWidth(146);
						assigneeType.setValueMap("User", "Group");
						assigneeType.setValue(t.get("assigneeType") == null ? "User" : t.get("assigneeType"));
						assigneeType.setRequired(true);
						
						TextItem addInfo1 = new TextItem("AddInfo1");
						addInfo1.setWidth(300);
						addInfo1.setValue(t.getVariable("Additional Info 1"));
						addInfo1.setTitle("Additional Info 1");
						
						TextItem addInfo2 = new TextItem("AddInfo2");
						addInfo2.setWidth(300);
						addInfo2.setValue(t.getVariable("Additional Info 2"));
						addInfo2.setTitle("Additional Info 2");
						
						assigneeType.addChangedHandler(new ChangedHandler() {
							public void onChanged(ChangedEvent event) {
								ComboBoxItem assignee = (ComboBoxItem)event.getForm().getItem("Assignee");
								RadioGroupItem assigneeType = (RadioGroupItem)event.getForm().getItem("AssigneeType");
								assignee.setTitle((String)event.getValue());
								assignee.setValueMap(assigneeType.getValue().equals("Group") ? groupsMap : usersMap);
								assignee.setValue("");
								assignee.redraw();
							}
						});

						ComboBoxItem assignee = new ComboBoxItem("Assignee");
						assignee.setTitle("<nobr>" + t.get("assigneeType") + "</nobr>");
						assignee.setType("comboBox");
						assignee.setValueMap(t.get("assigneeType").equals("Group") ? groupsMap : usersMap);
						assignee.setValue(getSavedStringValue(t.get("assignee")));
						assignee.setRequired(true);
						
						description.setValue(getSavedStringValue(t.getDescription()));
						
						form.setFields(basic, name, assigneeType, assignee, description, advanced, addInfo1, addInfo2, bypass);
					} else if(activity instanceof Exit) {
						Exit e = (Exit)activity;
						name.setValue(e.getName());
						name.setDisabled(true);
						
						TextItem reason = new TextItem("Reason");
						reason.setTitle("<nobr>Reason for exit</nobr>");
						reason.setValue(getSavedStringValue(e.getReason()));
						reason.setWidth(300);
						
						description.setValue(getSavedStringValue(e.getDescription()));
						
						form.setFields(basic, name, reason, description);
					} else if(activity instanceof ServiceTask) {
						ServiceTask t = (ServiceTask)activity;
						name.setValue(getSavedStringValue(t.getName()));
						bypass.setValue(t.getBypass());

						ComboBoxItem method = new ComboBoxItem("Method");
						method.setTitle("<nobr>Method</nobr>");
						method.setType("comboBox");
						method.setValueMap("GET", "POST"/*, "DELETE"*/);
						method.setValue(getSavedStringValue(t.getMethod()));
						method.setRequired(true);
						
						method.addChangedHandler(new ChangedHandler() {
							public void onChanged(ChangedEvent event) {
								TextItem method = (TextItem)event.getForm().getItem("Method");
								TextItem content = (TextItem)event.getForm().getItem("Content");
								if(method.getValue().equals("POST")) {
									if(content.getDisabled()) {
										content.setDisabled(false);
										content.redraw();
									}
								} else {
									if(!content.getDisabled()) {
										content.setDisabled(true);
										content.redraw();
									}
								}
							}
						});
						
						TextItem serviceUrl = new TextItem("ServiceUrl");
						serviceUrl.setTitle("<nobr>Service Url</nobr>");
						serviceUrl.setRequired(true);
						serviceUrl.setValue(getSavedStringValue(t.getServiceUrl()));
						serviceUrl.setWidth(300);
						
						TextItem content = new TextItem("Content");
						content.setTitle("<nobr>POST Content</nobr>");
						content.setValue(getSavedStringValue(t.getContent()));
						content.setDisabled(!method.getValue().equals("POST"));
						content.setWidth(300);
						
						TextItem var = new TextItem("Var");
						var.setTitle("<nobr>Variable</nobr>");
						var.setValue(getSavedStringValue(t.getVar()));
						var.setHint("<nobr>The name of the note that should store all contents <br/>returned by the service task</nobr>");
						var.setWidth(300);
						
						description.setValue(getSavedStringValue(t.getDescription()));
						
						form.setFields(basic, name, serviceUrl, method, content, var, description, advanced, bypass);
					} else if(activity instanceof Activities) {
						Activities a = (Activities)activity;
						name.setValue(getSavedStringValue(a.getName()));
						bypass.setValue(a.getBypass());
						
						ComboBoxItem order = new ComboBoxItem("Order");
						order.setTitle("<nobr>Actions Order</nobr>");
						order.setHint("<nobr>The order the listed actions should execute in</nobr>");
						order.setValueMap("One at a time, in order", "All at the same time");
						order.setValue(a.isSequential() ? "One at a time, in order" : "All at the same time");
						
						description.setValue(getSavedStringValue(a.getDescription()));
						
						form.setFields(basic, name, order, description, advanced, bypass);
					} else if(activity instanceof Loop) {
						Loop l = (Loop)activity;
						Task t = l.getLoopTask();
						name.setValue(getSavedStringValue(l.getName()));
						bypass.setValue(l.getBypass());
						
						TextItem loopTask = new TextItem("Loop End Condition");
						loopTask.setTitle("<nobr>Loop End Condition</nobr>");
						loopTask.setRequired(true);
						loopTask.setHint("<nobr>A question with a \"yes\" answer exits the loop.<br/> \"no\" repeats. See: advanced options</nobr>");
						loopTask.setValue(getSavedStringValue(t.getName()));
						loopTask.setWidth(300);
						
						RadioGroupItem assigneeType = new RadioGroupItem("Loop Decision-Maker Type");
						assigneeType.setTitle("<nobr>Loop Decision-Maker</nobr>");
						assigneeType.setVertical(false);
						assigneeType.setWidth(146);
						assigneeType.setValueMap("User", "Group");
						assigneeType.setValue(t.get("assigneeType") == null ? "User" : t.get("assigneeType"));
						assigneeType.setRequired(true);
						
						assigneeType.addChangedHandler(new ChangedHandler() {
							public void onChanged(ChangedEvent event) {
								ComboBoxItem assignee = (ComboBoxItem)event.getForm().getItem("Loop Decision-Maker");
								RadioGroupItem assigneeType = (RadioGroupItem)event.getForm().getItem("Loop Decision-Maker Type");
								assignee.setTitle((String)event.getValue());
								assignee.setValueMap(assigneeType.getValue().equals("Group") ? groupsMap : usersMap);
								assignee.setValue("");
								assignee.redraw();
							}
						});

						ComboBoxItem assignee = new ComboBoxItem("Loop Decision-Maker");
						assignee.setTitle("<nobr>User</nobr>");
						assignee.setType("comboBox");
						assignee.setValueMap(t.get("assigneeType").equals("Group") ? groupsMap : usersMap);
						assignee.setValue(getSavedStringValue(t.get("assignee")));
						assignee.setRequired(true);
						
						description.setValue(getSavedStringValue(l.getDescription()));
						
						TextItem addInfo1 = new TextItem("AddInfo1");
						addInfo1.setWidth(300);
						addInfo1.setValue(l.getLoopTask().getVariable("Additional Info 1"));
						addInfo1.setTitle("Additional Info 1");
						
						TextItem addInfo2 = new TextItem("AddInfo2");
						addInfo2.setWidth(300);
						addInfo2.setValue(l.getLoopTask().getVariable("Additional Info 2"));
						addInfo2.setTitle("Additional Info 2");
						
						ComboBoxItem order = new ComboBoxItem("Order");
						order.setTitle("<nobr>Actions Order</nobr>");
						order.setHint("<nobr>The order the looped actions should execute in</nobr>");
						order.setValueMap("One at a time, in order", "All at the same time");
						order.setValue(l.isSequential() ? "One at a time, in order" : "All at the same time");
						
						TextItem doneName = new TextItem("Done Name");
						doneName.setTitle("<nobr>Option To Finish Loop</nobr>");
						doneName.setHint("<nobr>Option that appears to the user who decides whether<br/> to repeat or exit the loop. This option exits.</nobr>");
						doneName.setValue(getSavedStringValue(l.getDoneName()));
						doneName.setWidth(300);
						
						TextItem repeatName = new TextItem("Repeat Name");
						repeatName.setTitle("<nobr>Option To Repeat Loop</nobr>");
						repeatName.setHint("<nobr>Option that appears to the user who decides whether<br/> to repeat or exit the loop. This option repeats.</nobr>");
						repeatName.setValue(getSavedStringValue(l.getRepeatName()));
						repeatName.setWidth(300);
						
						form.setFields(basic, name, loopTask, assigneeType, assignee, description, order, advanced, doneName, repeatName, addInfo1, addInfo2, bypass);
					} else if(activity instanceof Decision) {
						Decision d = (Decision)activity;
						Task t = d.getTask();
						name.setValue(getSavedStringValue(d.getName()));
						bypass.setValue(d.getBypass());
						
						TextItem decisionTask = new TextItem("Decision Question");
						decisionTask.setTitle("<nobr>Decision Question</nobr>");
						decisionTask.setRequired(true);
						decisionTask.setValue(getSavedStringValue(t.getName()));
						decisionTask.setWidth(300);
						
						RadioGroupItem assigneeType = new RadioGroupItem("Decision-Maker Type");
						assigneeType.setTitle("<nobr>Decision-Maker</nobr>");
						assigneeType.setVertical(false);
						assigneeType.setWidth(146);
						assigneeType.setValueMap("User", "Group");
						assigneeType.setValue(t.get("assigneeType") == null ? "User" : t.get("assigneeType"));
						assigneeType.setRequired(true);
						
						assigneeType.addChangedHandler(new ChangedHandler() {
							public void onChanged(ChangedEvent event) {
								ComboBoxItem assignee = (ComboBoxItem)event.getForm().getItem("Decision-Maker");
								RadioGroupItem assigneeType = (RadioGroupItem)event.getForm().getItem("Decision-Maker Type");
								assignee.setTitle((String)event.getValue());
								assignee.setValueMap(assigneeType.getValue().equals("Group") ? groupsMap : usersMap);
								assignee.setValue("");
								assignee.redraw();
							}
						});

						ComboBoxItem assignee = new ComboBoxItem("Decision-Maker");
						assignee.setTitle("<nobr>User</nobr>");
						assignee.setType("comboBox");
						assignee.setValueMap(t.get("assigneeType").equals("Group") ? groupsMap : usersMap);
						assignee.setValue(getSavedStringValue(t.get("assignee")));
						assignee.setRequired(true);
						
						description.setValue(getSavedStringValue(d.getDescription()));
						TextItem addInfo1 = new TextItem("AddInfo1");
						addInfo1.setWidth(300);
						addInfo1.setValue(d.getTask().getVariable("Additional Info 1"));
						addInfo1.setTitle("Additional Info 1");
						
						TextItem addInfo2 = new TextItem("AddInfo2");
						addInfo2.setWidth(300);
						addInfo2.setValue(d.getTask().getVariable("Additional Info 2"));
						addInfo2.setTitle("Additional Info 2");
						
						form.setFields(basic, name, decisionTask, assigneeType, assignee, description, advanced, bypass);
					} else if(activity instanceof Option) {
						Option o = (Option)activity;
						name.setValue(getSavedStringValue(o.getName()));
						bypass.setValue(o.getBypass());
						
						ComboBoxItem order = new ComboBoxItem("Order");
						order.setTitle("<nobr>Actions Order</nobr>");
						order.setHint("<nobr>The order the option's actions should execute in</nobr>");
						order.setValueMap("One at a time, in order", "All at the same time");
						order.setValue(o.isSequential() ? "One at a time, in order" : "All at the same time");
						
						description.setValue(getSavedStringValue(o.getActivities().getDescription()));
						
						form.setFields(basic, name, order, description, advanced, bypass);
					} else if(activity instanceof SubProcess) {
						SubProcess s = (SubProcess)activity;
						name.setValue(s.getName());
						bypass.setValue(s.getBypass());
						
						ComboBoxItem workflow = new ComboBoxItem("Workflow");
						workflow.setTitle("<nobr>Workflow</nobr>");
						workflow.setType("comboBox");
						workflow.setValueMap(workflowsMap);
						workflow.setValue(getSavedStringValue(s.getWorkflow()));
						workflow.setRequired(true);
						
						description.setValue(getSavedStringValue(s.getDescription()));
						
						form.setFields(basic, name, workflow, description, advanced, bypass);
					}
					
					formContainer.addMember(form);
				}
			}
		});
		
		// HEADER
		HLayout head = new HLayout();
		head.setWidth100();
		
		// TEMPLATE NAME AND ORDER		
		nameForm = new DynamicForm();
		nameForm.setNumCols(4);
		TextItem templateName = new TextItem("templateName");
		templateName.setTitle("Workflow Name");
		templateName.setValue(template.getName().equals("null") ? "" : template.getName());
		// FOLDER
		PickerIcon folderPicker = new PickerIcon(new Picker("folder.png"), new FormItemClickHandler() {
			public void onFormItemClick(FormItemIconClickEvent event) {
				if(folderSelector == null) {
					folderSelector = new Window();
					folderSelector.setWidth(300);
					folderSelector.setHeight(500);
					folderSelector.setAutoCenter(true);
					folderSelector.setTitle("Select Folder");
					
					VLayout mainLayout = new VLayout();
					mainLayout.setWidth100();
					mainLayout.setHeight100();
					
					treeGrid = new TreeGrid();
					treeGrid.setWidth100();
					treeGrid.setHeight100();
					treeGrid.setSelectionType(SelectionStyle.SINGLE);
					treeGrid.setShowConnectors(false);
					treeGrid.setCanSort(false);
					treeGrid.setCanAutoFitFields(false);
					treeGrid.setFields(new ListGridField("name", "Folder"));
					treeGrid.addDoubleClickHandler(new DoubleClickHandler() {
						public void onDoubleClick(DoubleClickEvent event) {
							handleFolderSelect();
						}
					});
					
					for (Map.Entry<String,String> entry : workflowsMap.entrySet()) {
						if(entry.getKey().indexOf('/') != -1)
							tempFolders.add(entry.getKey().substring(0, entry.getKey().lastIndexOf('/')+1));
					}
					
					HLayout buttons = new HLayout();
					buttons.setWidth100();
					Button newFolder = new Button();
					newFolder.setTitle("");
					newFolder.setIcon("folder_add.png");
					newFolder.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							SC.askforValue("Folder Name", new ValueCallback() {
								public void execute(String value) {
									if(value != null && !value.equals("")) {
										value = value.replaceAll(" ", "_");
										Tree tree = treeGrid.getTree();
										TreeNode selected = (TreeNode)treeGrid.getSelectedRecord();
										TreeNode node = new TreeNode();
										node.setAttribute("name", value);
										String realName = "";
										if(selected != null && !selected.getAttribute("realName").equals("No Selection"))
											realName = selected.getAttribute("realName") + value + "/";
										else
											realName = value + "/";
										node.setAttribute("realName", realName);
										node.setIsFolder(true);
										if(realName.equals(value + "/"))
											tree.add(node, tree.getRoot());
										else
											tree.add(node, selected);
										tempFolders.add(realName);
										tree.openFolder(node);
										treeGrid.deselectAllRecords();
										treeGrid.selectRecord(node);
									}
								}
							});
						}
					});
					Button ok = new Button();
					ok.setTitle("OK");
					ok.setWidth100();
					ok.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							handleFolderSelect();
						}
					});
					Button cancel = new Button();
					cancel.setTitle("Cancel");
					cancel.setWidth100();
					cancel.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							folderSelector.hide();
						}
					});
					buttons.addMember(newFolder);
					buttons.addMember(ok);
					buttons.addMember(cancel);
					
					mainLayout.addMember(treeGrid);
					mainLayout.addMember(buttons);
					folderSelector.addChild(mainLayout);
				}
				String currentName = nameForm.getValueAsString("templateName").replaceAll("/+", "/");
				String currentFolderName = "";
				if(currentName.indexOf('/') != -1) {
					boolean currentNameExists = false;
					currentFolderName = currentName.substring(0, currentName.lastIndexOf('/')+1).replaceAll(" ", "_");
					for(String s : tempFolders) {
						if(s.equals(currentFolderName)) {
							currentNameExists = true;
							break;
						}
					}
					if(!currentNameExists)
						tempFolders.add(currentFolderName);
				}
				
				Tree tree = new Tree();
				tree.setModelType(TreeModelType.CHILDREN);
				TreeNode root = new TreeNode();
				root.setAttribute("name", "root");
				tree.setRoot(root);
				TreeNode none = new TreeNode();
				none.setAttribute("name", "No Selection");
				none.setAttribute("realName", "No Selection");
				none.setIsFolder(true);
				none.setIcon("delete.png");
				tree.add(none, root);
				for(int i = 0; i < tempFolders.size(); i++) {
					String[] definition = tempFolders.get(i).split("/");
					if(definition.length > 0 && !definition[0].equals("")) {
						TreeNode parent = null;
						for(int j = 0; j < definition.length; j++) {
							String realName = "";
							for(int k = 0; k <= j; k++)
								realName += definition[k] + "/";
							TreeNode node = tree.find("realName", realName);
							if(node == null) { 
								node = new TreeNode();
								node.setAttribute("name", definition[j]);
								node.setAttribute("realName", realName);
								node.setIsFolder(true);
								if(parent == null)
									parent = root;
								tree.add(node, parent);
							}
							parent = node;
						}
					}
				}
				treeGrid.setData(tree);
				if(!currentFolderName.equals(""))
					treeGrid.selectRecord(treeGrid.getTree().find("realName", currentFolderName));
				tree.openAll();
				folderSelector.show();
			}
		});
		templateName.setIcons(folderPicker);
		ComboBoxItem order = new ComboBoxItem("order");
		order.setTitle("<nobr>Base Actions Order</nobr>");
		order.setValueMap("One at a time, in order", "All at the same time");
		order.setValue(template.getBase().isSequential() ? "One at a time, in order" : "All at the same time");
		nameForm.setFields(templateName, order);
		
		// SAVE BUTTON
		Button save = new Button();
		save.setTitle("Save");
		save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(formContainer.getMember(0) instanceof DynamicForm)
					saveRecord((TreeNode)grid.getSelectedRecord(), (DynamicForm)formContainer.getMember(0), grid);
				template.setName(nameForm.getValueAsString("templateName").replace(" ", "_"));
				template.getBase().setSequential(nameForm.getValueAsString("order").equals("One at a time, in order") ? true : false);
				if(!template.hasErrors()) {
					BpmServiceMain.sendPost("/deployments/v2?name=" + BpmServiceMain.urlEncode(template.getName()), template.toString(), new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							String xml = BpmServiceMain.xmlEncode(template.toString());
							SC.say("Failed to save template:<br />" + xml);
						}
	
						public void onSuccess(String result) {
							String[] args= {template.getName()};
							PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
						}
					});
				}
			}
		});
		Button tempSave = new Button();
		tempSave.setTitle("Save Locally");
		tempSave.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(formContainer.getMember(0) instanceof DynamicForm)
					saveRecord((TreeNode)grid.getSelectedRecord(), (DynamicForm)formContainer.getMember(0), grid);
				template.setName(nameForm.getValueAsString("templateName").replace(" ", "_"));
				template.getBase().setSequential(nameForm.getValueAsString("order").equals("One at a time, in order") ? true : false);
				String xml = template.toString();
				xml = xml.replaceAll("<", "&lt;");
				xml = xml.replaceAll(">", "&gt;");
			}
		});

		head.addMember(save);
		head.addMember(tempSave);
		head.addMember(nameForm);
		
		HLayout center = new HLayout();
		center.setWidth100();
		center.setHeight100();
		center.addMember(grid);
		center.addMember(sidebar);
		center.addMember(formContainer);
		
		createPage(head, PageWidget.PAGE_EDITWORKFLOW);
		addMember(topBar);
		addMember(center);
		
		if(!template.getName().equals("")) {
			MenuItem back = new MenuItem("Back to Workflow");
			back.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
				public void onClick(MenuItemClickEvent event) {
					String[] args = {template.getName()};
					PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
				}
			});
			header.addMenuItem(back);
		}
	}
	
	protected Img generateActivityImg(String type, int width, int height) {
		Img img = new Img("Icon_" + type + ".png", width, height) {
			protected boolean setDragTracker() {
				EventHandler.setDragTracker(Canvas.imgHTML("Icon_" + getTitle() + ".png", 16, 16));
				return false;
			}
		};
		img.setTooltip(type);
		img.setTitle(type);
		img.setMargin(5);
		img.setImageType(ImageStyle.STRETCH);
		img.setCursor(Cursor.MOVE);
		img.setCanDrag(true);
		img.setCanDrop(true);
		img.setShowEdges(true);
		img.setDragAppearance(DragAppearance.TRACKER);
		
		return img;
	}
	
	protected void addActivityToTree(Activity toAdd, TreeNode parent, TreeGrid grid) {
		//int index = grid.getEventRow() - grid.getTree().getLevel(grid.getDropFolder());
		Object parentActivity = parent.getAttributeAsObject("activity");
		if(parentActivity instanceof Activities) {
			//((Activities)parent).insertActivity(a, index);
			((Activities)parentActivity).addActivity(toAdd);
		} else if(parentActivity instanceof BaseList) {
			//((BaseList)parent).insertActivity(a, index);
			((BaseList)parentActivity).addActivity(toAdd);
		} else if(parentActivity instanceof Loop) {
			//((Loop)parent).insertActivity(a, index);
			((Loop)parentActivity).addActivity(toAdd);
		} else if(parentActivity instanceof Decision) {
			//((Decision)parent).insertOption(a, index, "Option " + counter++);
			//TreeNode node = ((Option)((Decision)parent).getOptions().get(index)).getTreeNode();
			//grid.getTree().add(node, grid.getDropFolder(), index);
			((Decision)parentActivity).addOption(toAdd, "Option " + counter++);
			TreeNode node = ((Option)((Decision)parentActivity).getOptions().get(((Decision)parentActivity).getOptions().size()-1)).getTreeNode(true);
			grid.getTree().add(node, grid.getDropFolder());
			grid.getTree().openFolder(parent);
			grid.getTree().openFolder(node);
			return;
		} else if(parentActivity instanceof Option) {
			//((Option)parent).insertActivity(a, index);
			((Option)parentActivity).addActivity(toAdd);
		}
		//grid.getTree().add(a.getTreeNode(), grid.getDropFolder(), index);
		TreeNode newNode = grid.getTree().add(toAdd.getTreeNode(true), grid.getDropFolder() == null ? grid.getTree().getRoot() : grid.getDropFolder());
		grid.deselectAllRecords();
		grid.getTree().openFolder(parent);
		grid.selectRecord(newNode);
	}
	
	protected void saveRecord(TreeNode node, DynamicForm form, TreeGrid grid) {
		if(node == null || form == null) return;
		Object activity = node.getAttributeAsObject("activity");
		node.setName(form.getValueAsString("Name"));
		grid.redraw();
		if(activity instanceof Task) {
			Task t = (Task)activity;
			t.setName(form.getValueAsString("Name"));
			t.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			t.set("assignee", form.getValueAsString("Assignee"));
			t.set("assigneeType", form.getValueAsString("AssigneeType"));
			t.setDescription(form.getValueAsString("Description"));
			t.setVariable(1, form.getValueAsString("AddInfo1"));
			t.setVariable(2, form.getValueAsString("AddInfo2"));
		} else if(activity instanceof Exit) {
			Exit e = (Exit)activity;
			e.setReason(form.getValueAsString("Reason"));
			e.setDescription(form.getValueAsString("Description"));
		} else if(activity instanceof ServiceTask) {
			ServiceTask t = (ServiceTask)activity;
			t.setName(form.getValueAsString("Name"));
			t.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			t.setMethod(form.getValueAsString("Method"));
			if(t.getMethod().equals("POST"))
				t.setContent(form.getValueAsString("Content"));
			else
				t.setContent("");
			t.setServiceUrl(form.getValueAsString("ServiceUrl"));
			t.setVar(form.getValueAsString("Var"));
			t.setDescription(form.getValueAsString("Description"));
		} else if(activity instanceof Activities) {
			Activities a = (Activities)activity;
			a.setName(form.getValueAsString("Name"));
			a.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			a.setSequential(form.getValueAsString("Order").equals("One at a time, in order") ? true : false);
			a.setDescription(form.getValueAsString("Description"));
		} else if(activity instanceof Loop) {
			Loop l = (Loop)activity;
			l.setName(form.getValueAsString("Name"));
			l.getLoopTask().setName(form.getValueAsString("Loop End Condition"));
			l.getLoopTask().set("assignee", form.getValueAsString("Loop Decision-Maker"));
			l.getLoopTask().set("assigneeType", form.getValueAsString("Loop Decision-Maker Type"));
			l.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			l.setSequential(form.getValueAsString("Order").equals("One at a time, in order") ? true : false);
			l.setDoneName(form.getValueAsString("Done Name"));
			l.setRepeatName(form.getValueAsString("Repeat Name"));
			l.getLoopTask().set("description", form.getValueAsString("Loop Description"));
			l.setDescription(form.getValueAsString("Description"));
			l.getLoopTask().setVariable(1, form.getValueAsString("AddInfo1"));
			l.getLoopTask().setVariable(2, form.getValueAsString("AddInfo2"));
		} else if(activity instanceof Decision) {
			Decision d = (Decision)activity;
			d.setName(form.getValueAsString("Name"));
			d.getTask().setName(form.getValueAsString("Decision Question"));
			d.getTask().set("assignee", form.getValueAsString("Decision-Maker"));
			d.getTask().set("assigneeType", form.getValueAsString("Decision-Maker Type"));
			d.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			d.getTask().set("description", form.getValueAsString("Decision Description"));
			d.getTask().setVariable(1, form.getValueAsString("AddInfo1"));
			d.getTask().setVariable(2, form.getValueAsString("AddInfo2"));
			d.setDescription(form.getValueAsString("Description"));
		} else if(activity instanceof Option) {
			Option o = (Option)activity;
			o.setName(form.getValueAsString("Name"));
			o.setSequential(form.getValueAsString("Order").equals("One at a time, in order") ? true : false);
			o.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
			o.getActivities().setDescription(form.getValueAsString("Description"));
		} else if(activity instanceof SubProcess) {
			SubProcess s = (SubProcess)activity;
			s.setWorkflow(form.getValueAsString("Workflow"));
			s.setDescription(form.getValueAsString("Description"));
			s.setBypass(Boolean.parseBoolean(form.getValueAsString("Bypass")));
		} 
	}
	
	protected void handleFolderSelect() {
		String name = nameForm.getValueAsString("templateName");
		if(name.indexOf('/') != -1) {
			if(name.charAt(name.length()-1) == '/')
				name = "";
			else
				name = name.substring(name.lastIndexOf('/')+1);
		}
		name = name.replaceAll(" ", "_");
		if(treeGrid.getSelectedRecord() == null || treeGrid.getSelectedRecord().getAttribute("realName").equals("No Selection"))
			((TextItem)nameForm.getItem("templateName")).setValue(name);
		else
			((TextItem)nameForm.getItem("templateName")).setValue(treeGrid.getSelectedRecord().getAttribute("realName") + name);
		folderSelector.hide();
	}
	
	protected String getSavedStringValue(String input) {
		if(input == null || input.equals("null"))
			return "";
		return input;
	}
	
	protected void toggleSidebar() {
		sidebar.setVisible(!sidebar.isVisible());
	}
	
	public void refresh() {
		String[] args= {template.getName()};
		PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
	}
}
