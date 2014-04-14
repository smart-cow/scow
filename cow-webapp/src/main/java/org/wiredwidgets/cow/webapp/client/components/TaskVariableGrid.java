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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.page.PageWidget;
import org.wiredwidgets.cow.webapp.client.page.Tasks;
import org.wiredwidgets.cow.webapp.client.page.ViewWorkflow;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwt.components.client.xml.Document;
import com.gwt.components.client.xml.Node;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.RowEndEditAction;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.BaseWidget;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailFormatter;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

/**
 * A custom ListGrid that displays a variable list of fields
 * for each item, rather than a fixed set for all items. Also
 * formats some of the fields that have special identifiers
 * before them. Auto-detects URLs and makes them links.
 * 
 * 
 *
 */
public class TaskVariableGrid extends ListGrid {

	public TaskVariableGrid() {
		super();
	}

	public TaskVariableGrid(JavaScriptObject jsObj) {
		super(jsObj);
	}
	
	@Override
	/**
	 * Used to display Task details on the Tasks page
	 */
	public Canvas getExpansionComponent(final ListGridRecord record) {
        final ListGrid grid = this;  
        final ArrayList<String> outcomeString = new ArrayList<String>();
        outcomeString.add("");
        
        VLayout layout = new VLayout(5);  
        layout.setPadding(5); 
        HLayout hlayoutUpper = new HLayout();
        

        final ListGrid taskGrid = new ListGrid(){
           //Doesn't allow the varible name or required flag to be changed unless a user has just added it
        	@Override
            protected boolean canEditCell (int rowNum, int colNum){
            	ListGridRecord existingRecord = getRecord(rowNum);
            	if (existingRecord != null && 
            			existingRecord.getAttribute("varname") != null && 
            			//Used to verify GWT hasn't added a non-breaking space for the table
            			existingRecord.getAttribute("varname").replace("\u00a0","").replace("&nbsp;","").length() >= 0) {
            		//Ensures this is the value
            		return (colNum ==1) && super.canEditCell(rowNum, colNum) ;            	    	
            	    	
            	    }
            	return true;
            	    
            	    
        	
        }; 
  
        };
        taskGrid.setWidth(750);  
        taskGrid.setHeight(130);  
        taskGrid.setCellHeight(22);  
         
       
        

        taskGrid.setCanEdit(true);  
        //taskGrid.setModalEditing(true);
        taskGrid.setEditByCell(true);
        taskGrid.setEditEvent(ListGridEditEvent.CLICK);  
        taskGrid.setListEndEditAction(RowEndEditAction.NEXT);  
        taskGrid.setAutoSaveEdits(false);  
        ListGridField variable = new ListGridField("varname", "Variable Name");
        //variable.setCanEdit(false);
        CellFormatter formatter = new CellFormatter() {
        	public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
				if(value == null) return "";
				String s = value.toString();
				return s;
			}

	
		};
		
		ListGridField required = new ListGridField("required", "Required");
		required.setType(ListGridFieldType.BOOLEAN);
        taskGrid.setFields(variable, new ListGridField("value", "Value"),required);
        taskGrid.setCellFormatter(formatter);

        
        
        layout.addMember(taskGrid);  

        HLayout hLayout = new HLayout(10);  
        hLayout.setAlign(Alignment.LEFT);  

        IButton saveButton = new IButton("Save");  
        saveButton.setTop(250);  
        saveButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	
            	
            	
 
            }  
        });  
        hLayout.addMember(saveButton);  

        IButton discardButton = new IButton("Discard");  
        discardButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	taskGrid.discardAllEdits();  
            }  
        });  
        hLayout.addMember(discardButton);  

        IButton closeButton = new IButton("Close");  
        closeButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
                grid.collapseRecord(record);  
            }  
        });  
        hLayout.addMember(closeButton);  
        
        
        IButton addRowButton = new IButton("Add Variable");  
        addRowButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	taskGrid.startEditingNew();
                
            }  
        });  
        hLayout.addMember(addRowButton);  
        
                                         
        layout.addMember(hLayout);  
        
        hlayoutUpper.addMember(layout);
        Task t = (Task)record.getAttributeAsObject("task");
		ArrayList<String> outcomes = t.getOutcomes();
		   
        
        
        
        return hlayoutUpper;  
    } 

	
	
	/**
	 * Checks whether or not a String is a URL
	 * @param url The String to check
	 * @return true if it is a URL, otherwise false
	 */
	public static native boolean isURL(String url) /*-{
		if(url.match(/[-a-zA-Z0-9@:%_\+.~#?&\/\/=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&\/\/=]*)?/gi))
			return true;
		return false;
	}-*/;
	

	

}
