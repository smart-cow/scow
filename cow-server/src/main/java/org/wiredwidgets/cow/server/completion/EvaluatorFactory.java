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

package org.wiredwidgets.cow.server.completion;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Script;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;
import org.wiredwidgets.cow.server.api.model.v2.Task;

@Component
public class EvaluatorFactory implements ApplicationContextAware {
	
    private ApplicationContext applicationContext;

    public Evaluator<? extends Activity> getEvaluator(String processInstanceId, Activity activity, ProcessInstanceInfo history, 
    		CompletionState branchState, boolean inLoop) {
        Evaluator eval = null;
        if (activity.isBypassable() && !activity.isWrapped()) {
            eval = applicationContext.getBean(BypassableActivityEvaluator.class);
            activity = new BypassableActivity(activity);
        } else if (activity instanceof Activities) {
            // parallel and sequential use the same evaluator
            eval = applicationContext.getBean(ActivitiesEvaluator.class);
        } else if (activity instanceof Task) {
            eval = applicationContext.getBean(TaskEvaluator.class);
        } else if (activity instanceof Decision) {
            eval = applicationContext.getBean(DecisionEvaluator.class);
        } else if (activity instanceof Loop) {
            eval = applicationContext.getBean(LoopEvaluator.class);
        } else if (activity instanceof Exit) {
            eval = applicationContext.getBean(ExitEvaluator.class);
        } else if (activity instanceof Script) {
            eval = applicationContext.getBean(ScriptEvaluator.class);   
        } else if (activity instanceof ServiceTask) {
            eval = applicationContext.getBean(ServiceTaskEvaluator.class);            
        } else if (activity instanceof Signal) {
            eval = applicationContext.getBean(SignalEvaluator.class);
        } else if (activity instanceof SubProcess) {
            eval = applicationContext.getBean(SubProcessEvaluator.class);
        }
        eval.setProcessInstanceId(processInstanceId);
        eval.setActivity(activity);
        eval.setHistory(history);
        eval.setBranchState(branchState);
        eval.setInLoop(inLoop);

        return eval;
    }
    
    public ProcessEvaluator getProcessEvaluator(String processInstanceId, Process process, ProcessInstanceInfo history) {
        ProcessEvaluator eval = applicationContext.getBean(ProcessEvaluator.class);
        eval.setProcessInstanceId(processInstanceId);
        eval.setActivity(process.getActivity().getValue());
        eval.setHistory(history);
        eval.setBranchState(CompletionState.PLANNED);
        return eval;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
