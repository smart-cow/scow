/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
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

package org.wiredwidgets.cow.server.transform.graph.activity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.wiredwidgets.cow.server.api.model.v2.Task;

public class DecisionTask extends Task {
	
	private String question;
	
	private List<String> options = new ArrayList<String>();
	
	public DecisionTask(Task task) {
		BeanUtils.copyProperties(task, this);
	}
	
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void addOption(String option) {
		this.options.add(option);
	}
	
	public List<String> getOptions() {
		return options;
	}

}
