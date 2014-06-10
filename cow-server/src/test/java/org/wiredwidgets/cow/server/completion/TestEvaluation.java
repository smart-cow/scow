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

import static org.junit.Assert.assertEquals;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.COMPLETED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.CONTINGENT;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.OPEN;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PLANNED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PRECLUDED;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.process.audit.NodeInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.CompletionState;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.completion.graph.GraphCompletionEvaluator;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.builder.BypassGraphBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.DecisionGraphBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.LoopGraphBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.ParallelActivitiesGraphBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/eval-test-context.xml")
public class TestEvaluation {
	
	private static final int ENTER = NodeInstanceLog.TYPE_ENTER;
	private static final int EXIT = NodeInstanceLog.TYPE_EXIT;
      
    @Autowired
    GraphCompletionEvaluator graphEvaluator;
      
    @Autowired
    EvaluatorFactory evaluatorFactory;

    @Test
    public void testSequenceEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.TRUE);
        activities.setName("sequential");
        Task task1 = new Task();
        task1.setName("task1");

        Task task2 = new Task();
        task2.setName("task2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));
       
        List<NodeInstanceLog> nodes = initNodes();
       
        // enter task1
        
        enter(task1.getName(), nodes);
		evaluate(process, nodes);
		
        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(PLANNED, task2.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());


        // exit task1, enter task1
        
        exit(task1.getName(), nodes);
        enter(task2.getName(), nodes);
		evaluate(process, nodes);
		
        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, task2.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());

        // task2 is COMPLETE

        exit(task2.getName(), nodes);
		evaluate(process, nodes);

        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(COMPLETED, task2.getCompletionState());
        assertEquals(COMPLETED, activities.getCompletionState());
    }

    @Test
    public void testRaceConditionEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.FALSE);
        activities.setMergeCondition("1");
        activities.setName("parallel");
        Task task1 = new Task();
        task1.setName("task1");

        Task task2 = new Task();
        task2.setName("task2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));

        List<NodeInstanceLog> nodes = initNodes();

        // Task1 is OPEN
        // enter and exit the diverging gateway
        enter(ParallelActivitiesGraphBuilder.getDivergingGatewayName(activities), nodes);
        exit(ParallelActivitiesGraphBuilder.getDivergingGatewayName(activities), nodes);
        
        // enter task1 and task2
        enter(task1.getName(), nodes);
        enter(task2.getName(), nodes);
		evaluate(process, nodes);

        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(OPEN, task2.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());

        // exit task1
        exit(task1.getName(), nodes);
        
        // this triggers the gateway
        enter(ParallelActivitiesGraphBuilder.getConvergingGatewayName(activities), nodes);
        exit(ParallelActivitiesGraphBuilder.getConvergingGatewayName(activities), nodes);
		evaluate(process, nodes);
		
        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, task2.getCompletionState());
        assertEquals(COMPLETED, activities.getCompletionState());

    }

    @Test
    public void testBypassEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.TRUE);
        activities.setBypassable(Boolean.TRUE);
        activities.setName("set");

        Task task1 = new Task();
        task1.setName("task1");

        Task task2 = new Task();
        task2.setName("task2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));


        List<NodeInstanceLog> nodes = initNodes();

        // Task1 and BypassTask are OPEN
        
        // enter and exit the diverging gateway
        enter(BypassGraphBuilder.getBypassDivergingGatewayName(activities), nodes);
        exit(BypassGraphBuilder.getBypassDivergingGatewayName(activities), nodes);
        
        // enter task1 and bypasstask at the same time
        enter(task1.getName(), nodes);
        enter(BypassGraphBuilder.getBypassTaskName(activities), nodes);

        evaluate(process, nodes);

        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(PLANNED, task2.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());

        // Task1 is complete
        
        exit(task1.getName(), nodes);

        // task2 is now OPEN
      
        enter(task2.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, task2.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());

        // invoke the bypass
        exit(BypassGraphBuilder.getBypassTaskName(activities), nodes);
        enter(BypassGraphBuilder.getBypassConvergingGatewayName(activities), nodes);
        exit(BypassGraphBuilder.getBypassConvergingGatewayName(activities), nodes);
        evaluate(process, nodes);
        
        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, task2.getCompletionState());
        assertEquals(COMPLETED, activities.getCompletionState());

    }

    @Test
    public void testDecisionEval() {

        Process process = new Process();
        Decision decision = new Decision();
        decision.setName("decision");
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createDecision(decision));
        Task decisionTask = new Task();
        decisionTask.setName("which option?");
        decision.setTask(decisionTask);

        Option option1 = new Option();
        option1.setName("option1");

        Task task1 = new Task();
        task1.setName("task1");
        
        Task task1a = new Task();
        task1a.setName("task1a");
        
        Activities activities = new Activities();
        activities.setName("activities");
        activities.setSequential(true);
        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task1a));
        
        option1.setActivity(factory.createActivities(activities));

        Option option2 = new Option();
        option2.setName("option2");

        Task task2 = new Task();
        task2.setName("task2");
        option2.setActivity(factory.createTask(task2));

        decision.getOptions().add(option1);
        decision.getOptions().add(option2);
        
        List<NodeInstanceLog> nodes = initNodes();

        // Decision task is OPEN
        
        enter(decisionTask.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(CONTINGENT, task1.getCompletionState());
        assertEquals(CONTINGENT, task1a.getCompletionState());
        assertEquals(CONTINGENT, task2.getCompletionState());
        assertEquals(CONTINGENT, activities.getCompletionState());
        assertEquals(OPEN, decision.getTask().getCompletionState());
        assertEquals(OPEN, decision.getCompletionState());

        // Decision is made, task1 is now OPEN
        
        exit(decisionTask.getName(), nodes);
        enter(DecisionGraphBuilder.getDivergingGatewayName(decision), nodes);
        exit(DecisionGraphBuilder.getDivergingGatewayName(decision), nodes);
        enter(task1.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(PLANNED, task1a.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());
        
        // task2 is PRECLUDED as this branch was not selected
        assertEquals(PRECLUDED, task2.getCompletionState());
        assertEquals(COMPLETED, decision.getTask().getCompletionState());
        assertEquals(OPEN, decision.getCompletionState());

        // task1 is COMPLETED, task 1a is OPEN

        exit(task1.getName(), nodes);
        enter(task1a.getName(), nodes);
        evaluate(process, nodes);
        
        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, task1a.getCompletionState());
        assertEquals(OPEN, activities.getCompletionState());
        assertEquals(PRECLUDED, task2.getCompletionState());
        assertEquals(COMPLETED, decision.getTask().getCompletionState());
        assertEquals(OPEN, decision.getCompletionState());
        
        // task 1a is complete
        
        exit(task1a.getName(), nodes);
        enter(DecisionGraphBuilder.getConvergingGatewayName(decision), nodes);
        exit(DecisionGraphBuilder.getConvergingGatewayName(decision), nodes);
        evaluate(process, nodes);

        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(COMPLETED, task1a.getCompletionState());
        assertEquals(COMPLETED, activities.getCompletionState());
        assertEquals(PRECLUDED, task2.getCompletionState());
        assertEquals(COMPLETED, decision.getTask().getCompletionState());
        assertEquals(COMPLETED, decision.getCompletionState());        
        
    }

    @Test
    public void testLoopEval() {

        Process process = new Process();
        Loop loop = new Loop();
        loop.setName("loop");
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createLoop(loop));
        Task loopTask = new Task();
        loopTask.setName("loopTask");
        loop.setLoopTask(loopTask);
        loop.setRepeatName("repeat");

        Task task1 = new Task();
        task1.setName("task1");
        loop.setActivity(factory.createTask(task1));

        List<NodeInstanceLog> nodes = initNodes();

        // task1 is OPEN
        enter(LoopGraphBuilder.getConvergingGatewayName(loop), nodes);
        exit(LoopGraphBuilder.getConvergingGatewayName(loop), nodes);
        enter(task1.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(PLANNED, loop.getLoopTask().getCompletionState());
        assertEquals(OPEN, loop.getCompletionState());

        exit(task1.getName(), nodes);

        // loop task is OPEN
        enter(loopTask.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, loop.getLoopTask().getCompletionState());
        assertEquals(OPEN, loop.getCompletionState());


        // loop task is completed, but we decided to repeat the loop, so we're back to task1

        exit(loopTask.getName(), nodes);
        enter(LoopGraphBuilder.getDivergingGatewayName(loop), nodes);
        exit(LoopGraphBuilder.getDivergingGatewayName(loop), nodes);
        enter(LoopGraphBuilder.getConvergingGatewayName(loop), nodes);
        exit(LoopGraphBuilder.getConvergingGatewayName(loop), nodes);
        enter(task1.getName(), nodes);
        evaluate(process, nodes);
      
        assertEquals(OPEN, task1.getCompletionState());
        assertEquals(COMPLETED, loop.getLoopTask().getCompletionState());
        assertEquals(OPEN, loop.getCompletionState());

        // complete the task, back on the loop
        
        exit(task1.getName(), nodes);
        enter(loopTask.getName(), nodes);
        evaluate(process, nodes);

        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(OPEN, loop.getLoopTask().getCompletionState());
        assertEquals(OPEN, loop.getCompletionState());

        // finally we complete the loop

        exit(loopTask.getName(), nodes);
        enter(LoopGraphBuilder.getDivergingGatewayName(loop), nodes);
        exit(LoopGraphBuilder.getDivergingGatewayName(loop), nodes);
        evaluate(process, nodes);
        
        assertEquals(COMPLETED, task1.getCompletionState());
        assertEquals(COMPLETED, loop.getLoopTask().getCompletionState());
        assertEquals(COMPLETED, loop.getCompletionState());

    }

    private XMLGregorianCalendar newXMLGregorianCalendar() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<NodeInstanceLog> initNodes() {
    	List<NodeInstanceLog> nodes = new ArrayList<NodeInstanceLog>();
    	enter("start", nodes);
    	exit("start", nodes);
    	return nodes;
    }
    
    private void evaluate(Process process, List<NodeInstanceLog> nodes) {
		ActivityGraph graph = graphEvaluator.evaluate(process, nodes);

		ProcessInstanceInfo history = new ProcessInstanceInfo(1, graph);
		
        evaluatorFactory.getProcessEvaluator(null, process, history).evaluate();
    }
    
    private void enter(String name, List<NodeInstanceLog> nodes) {
    	nodes.add(new NodeInstanceLog(ENTER, 1,"x","x", "x", name));
    }
    
    private void exit(String name, List<NodeInstanceLog> nodes) {
    	nodes.add(new NodeInstanceLog(EXIT, 1,"x","x", "x", name));
    }
}