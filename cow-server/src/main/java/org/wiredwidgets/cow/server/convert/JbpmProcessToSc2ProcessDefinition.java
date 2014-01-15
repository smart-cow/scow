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
