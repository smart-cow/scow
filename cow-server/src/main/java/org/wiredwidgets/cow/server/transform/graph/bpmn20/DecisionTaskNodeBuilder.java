package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.omg.spec.bpmn._20100524.model.TUserTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.transform.graph.activity.DecisionTask;

@Component
public class DecisionTaskNodeBuilder extends AbstractUserTaskNodeBuilder<DecisionTask> {
	
	public static final String DECISION_VAR_NAME = "DecisionVarName";
	
	@Override
	public void buildInternal(TUserTask t, DecisionTask source,
			Bpmn20ProcessContext context) {
		
		super.buildInternal(t, source, context);
		
        addDataInputFromExpression("Options", getOptionsString(source), t);
        String decisionVar = getDecisionVarName(t.getId());
        addDataOutputFromProperty(decisionVar, decisionVar, t, context);
        
        // We need to be able to know the name of the decision variable
        addDataInputFromExpression(DECISION_VAR_NAME, decisionVar, t);
        
        // this is picked up in GatewayDecisionActivityBuilder
        // setBuildProperty("decisionVar", decisionVar);		

	}	

	@Override
	public Class<DecisionTask> getType() {
		return DecisionTask.class;
	}
	
	/**
	 * Builds the decison variable name based on the node ID
	 * @param id
	 * @return
	 */
	public static String getDecisionVarName(String id) {
		return id + "_decision";
	}
	
    private String getOptionsString(DecisionTask dt) {
        String optionString = null;
        for (String option : dt.getOptions()) {
            if (optionString == null) {
                optionString = option;
            } else {
                optionString += ("," + option);
            }
        }
        return optionString;
    }	

}
