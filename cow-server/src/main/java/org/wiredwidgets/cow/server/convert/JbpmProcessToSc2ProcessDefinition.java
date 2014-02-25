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

package org.wiredwidgets.cow.server.convert;

import org.drools.definition.process.Process;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;

@Component
public class JbpmProcessToSc2ProcessDefinition extends AbstractConverter<org.drools.definition.process.Process, ProcessDefinition> {

	@Override
	public ProcessDefinition convert(Process source) {
		ProcessDefinition pd = new ProcessDefinition();
		pd.setId(source.getId());
		pd.setKey(source.getId());
		pd.setName(source.getName());
		pd.setSuspended(false);
		if (source.getVersion() != null) {
			pd.setVersion(Integer.parseInt(source.getVersion()));
		}
		return pd;
	}

}
