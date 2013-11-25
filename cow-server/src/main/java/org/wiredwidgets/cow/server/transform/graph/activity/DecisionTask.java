package org.wiredwidgets.cow.server.transform.graph.activity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.wiredwidgets.cow.server.api.model.v2.Task;

public class DecisionTask extends Task {
	
	List<String> options = new ArrayList<String>();
	
	public DecisionTask(Task task) {
		BeanUtils.copyProperties(task, this);
	}
	
	public void addOption(String option) {
		this.options.add(option);
	}
	
	public List<String> getOptions() {
		return options;
	}

}
