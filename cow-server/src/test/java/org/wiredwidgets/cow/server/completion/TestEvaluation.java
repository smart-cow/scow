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
import static org.wiredwidgets.cow.server.completion.CompletionState.COMPLETED;
import static org.wiredwidgets.cow.server.completion.CompletionState.CONTINGENT;
import static org.wiredwidgets.cow.server.completion.CompletionState.OPEN;
import static org.wiredwidgets.cow.server.completion.CompletionState.PLANNED;
import static org.wiredwidgets.cow.server.completion.CompletionState.PRECLUDED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.api.model.v2.ObjectFactory;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/eval-test-context.xml")
public class TestEvaluation {
    
    @Autowired
    EvaluatorFactory evalFactory;

    @Test
    public void testSequenceEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.TRUE);
        Task task1 = new Task();
        task1.setName("task1");
        task1.setKey("_1");

        Task task2 = new Task();
        task2.setName("task2");
        task2.setKey("_2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));

        List<HistoryActivity> historyList = new ArrayList<HistoryActivity>();
        ProcessInstanceInfo history = new ProcessInstanceInfo(historyList, 1, null);

        ProcessEvaluator evaluator = evalFactory.getProcessEvaluator(null, process, history);

        evaluator.evaluate();

        // no tasks have started
        // not a realistic standalone workflow but valid for an embedded sequence

        assertEquals(PLANNED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(PLANNED.getName(), activities.getCompletionState());
        assertEquals(PLANNED, evaluator.completionState);

        // Task1 is OPEN

        HistoryActivity activity1 = new HistoryActivity();
        activity1.setActivityName("_1");
        historyList.add(activity1);

        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(OPEN.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());
        assertEquals(OPEN, evaluator.completionState);

        // Task1 is complete, task2 is not started
        // not a realistic work flow but we will test anyway

        activity1.setEndTime(newXMLGregorianCalendar());
        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());
        assertEquals(OPEN, evaluator.completionState);

        // task2 is now OPEN

        HistoryActivity activity2 = new HistoryActivity();
        activity2.setActivityName("_2");
        historyList.add(activity2);

        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(OPEN.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());
        assertEquals(OPEN, evaluator.completionState);

        // task2 is COMPLETE

        activity2.setEndTime(newXMLGregorianCalendar());
        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(COMPLETED.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());
        assertEquals(COMPLETED, evaluator.completionState);
    }

    @Test
    public void testRaceConditionEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.FALSE);
        activities.setMergeCondition("1");
        Task task1 = new Task();
        task1.setKey("_1");

        Task task2 = new Task();
        task2.setKey("_2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));

        List<HistoryActivity> historyList = new ArrayList<HistoryActivity>();
        ProcessInstanceInfo history = new ProcessInstanceInfo(historyList, 1, null);

        ProcessEvaluator evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        // no tasks have started
        // not a realistic standalone workflow but valid for an embedded sequence

        assertEquals(PLANNED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(PLANNED.getName(), activities.getCompletionState());
        assertEquals(PLANNED, evaluator.completionState);

        // Task1 is OPEN

        HistoryActivity activity1 = new HistoryActivity();
        activity1.setActivityName("_1");
        historyList.add(activity1);

        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(OPEN.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());
        assertEquals(OPEN, evaluator.completionState);

        // Task1 is complete, task2 is not started
        // not a realistic work flow but we will test anyway

        activity1.setEndTime(newXMLGregorianCalendar());
        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());
        assertEquals(COMPLETED, evaluator.completionState);

        // task2 is now OPEN
        // not realistic, as Task1 now becomes obsolete.

        HistoryActivity activity2 = new HistoryActivity();
        activity2.setActivityName("_2");
        historyList.add(activity2);

        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(OPEN.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());
        assertEquals(COMPLETED, evaluator.completionState);

        // task2 is COMPLETE
        // again, not realistic

        activity2.setEndTime(newXMLGregorianCalendar());
        history = new ProcessInstanceInfo(historyList, 1, null);
        evaluator = evalFactory.getProcessEvaluator(null, process, history);
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(COMPLETED.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());
        assertEquals(COMPLETED, evaluator.completionState);
    }

    @Test
    public void testBypassEval() {

        Process process = new Process();
        Activities activities = new Activities();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createActivities(activities));

        activities.setSequential(Boolean.TRUE);
        activities.setBypassable(true);
        activities.setKey("set");

        Task task1 = new Task();
        task1.setKey("_1");

        Task task2 = new Task();
        task2.setKey("_2");

        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task2));

        List<HistoryActivity> historyList = new ArrayList<HistoryActivity>();

        ProcessEvaluator evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        // no tasks have started
        // not a realistic standalone workflow but valid for an embedded sequence

        assertEquals(PLANNED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(PLANNED.getName(), activities.getCompletionState());

        // Task1 and BypassTask are OPEN

        HistoryActivity activity1 = new HistoryActivity();
        activity1.setActivityName("_1");
        historyList.add(activity1);

        HistoryActivity activity3 = new HistoryActivity();
        activity3.setActivityName("Bypass set");
        historyList.add(activity3);

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(OPEN.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());

        // Task1 is complete, task2 is not started
        // not a realistic work flow but we will test anyway

        activity1.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(PLANNED.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());

        // task2 is now OPEN

        HistoryActivity activity2 = new HistoryActivity();
        activity2.setActivityName("_2");
        historyList.add(activity2);

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(OPEN.getName(), task2.getCompletionState());
        assertEquals(OPEN.getName(), activities.getCompletionState());

        // invoke the bypass
        activity3.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(OPEN.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());

        // reverse the bypass
        activity3.setEndTime(null);

        // task2 is COMPLETE

        activity2.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(COMPLETED.getName(), task1.getCompletionState());
        assertEquals(COMPLETED.getName(), task2.getCompletionState());
        assertEquals(COMPLETED.getName(), activities.getCompletionState());

    }

    @Test
    public void testDecisionEval() {

        Process process = new Process();
        Decision decision = new Decision();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createDecision(decision));
        Task decisionTask = new Task();
        decisionTask.setKey("decisionTask");
        decision.setTask(decisionTask);

        Option option1 = new Option();
        option1.setName("option1");

        Task task1 = new Task();
        task1.setKey("_1");
        
        Task task1a = new Task();
        task1a.setKey("_1a");
        
        Activities activities = new Activities();
        activities.setSequential(true);
        activities.getActivities().add(factory.createTask(task1));
        activities.getActivities().add(factory.createTask(task1a));
        
        option1.setActivity(factory.createActivities(activities));

        Option option2 = new Option();
        option2.setName("option2");

        Task task2 = new Task();
        task2.setKey("_2");
        option2.setActivity(factory.createTask(task2));

        decision.getOptions().add(option1);
        decision.getOptions().add(option2);

        List<HistoryActivity> historyList = new ArrayList<HistoryActivity>();
        ProcessEvaluator evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        // no tasks have started
        // not a realistic standalone workflow but valid for an embedded sequence
        // both tasks are CONTINGENT as no decision has been made

        assertEquals(CONTINGENT, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(task1a.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(task2.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(activities.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(decisionTask.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(decision.getCompletionState()));

        // Decision task is OPEN

        HistoryActivity activity1 = new HistoryActivity();
        activity1.setActivityName("decisionTask");
        historyList.add(activity1);

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CONTINGENT, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(task1a.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(task2.getCompletionState()));
        assertEquals(CONTINGENT, CompletionState.forName(activities.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(decisionTask.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(decision.getCompletionState()));

        // Decision is made, task1 is now OPEN

        activity1.setEndTime(newXMLGregorianCalendar());
        HistoryActivity activity2 = new HistoryActivity();
        activity2.setActivityName("_1");
        historyList.add(activity2);
        
        // map to hold the decision outcome
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("decisionTask_decision", "option1");

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, vars));
        evaluator.evaluate();
        
        assertEquals(CompletionState.OPEN, CompletionState.forName(task1.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(task1a.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(activities.getCompletionState()));
        
        // task2 is PRECLUDED as this branch was not selected
        assertEquals(PRECLUDED, CompletionState.forName(task2.getCompletionState()));
        assertEquals(CompletionState.COMPLETED, CompletionState.forName(decisionTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(decision.getCompletionState()));

        // task1 is COMPLETED, task 1a is OPEN

        activity2.setEndTime(newXMLGregorianCalendar());
        
        HistoryActivity activity3 = new HistoryActivity();
        activity3.setActivityName("_1a");
        historyList.add(activity3);        
        
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, vars));
        evaluator.evaluate();

        assertEquals(COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(task1a.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(activities.getCompletionState()));
        assertEquals(PRECLUDED, CompletionState.forName(task2.getCompletionState()));
        assertEquals(COMPLETED, CompletionState.forName(decisionTask.getCompletionState()));
        assertEquals(OPEN, CompletionState.forName(decision.getCompletionState()));
        
        // task 1a is complete
        activity3.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, vars));
        evaluator.evaluate();
        
        assertEquals(COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(COMPLETED, CompletionState.forName(task1a.getCompletionState()));
        assertEquals(COMPLETED, CompletionState.forName(activities.getCompletionState()));
        assertEquals(PRECLUDED, CompletionState.forName(task2.getCompletionState()));
        assertEquals(COMPLETED, CompletionState.forName(decisionTask.getCompletionState()));
        assertEquals(COMPLETED, CompletionState.forName(decision.getCompletionState()));        
        
        

    }

    @Test
    public void testLoopEval() {

        Process process = new Process();
        Loop loop = new Loop();
        ObjectFactory factory = new ObjectFactory();
        process.setActivity(factory.createLoop(loop));
        Task loopTask = new Task();
        loopTask.setKey("loopTask");
        loop.setLoopTask(loopTask);
        loop.setRepeatName("repeat");

        Task task1 = new Task();
        task1.setKey("_1");
        loop.setActivity(factory.createTask(task1));

        List<HistoryActivity> historyList = new ArrayList<HistoryActivity>();

        ProcessEvaluator evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        // no tasks have started
        // not a realistic standalone workflow but valid for an embedded sequence

        assertEquals(PLANNED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(loop.getCompletionState()));


        // task1 is OPEN

        HistoryActivity activity1 = new HistoryActivity();
        activity1.setActivityName("_1");
        historyList.add(activity1);

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CompletionState.OPEN, CompletionState.forName(task1.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loop.getCompletionState()));

        // task1 is complete, loop is NOT_STARTED
        // not realistic as the loop would be OPEN but test anyway

        activity1.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CompletionState.COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(PLANNED, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loop.getCompletionState()));

        // loop task is OPEN

        HistoryActivity activity2 = new HistoryActivity();
        activity2.setActivityName("loopTask");
        historyList.add(activity2);
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CompletionState.COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loop.getCompletionState()));


        // loop task is completed, but we decided to repeat the loop, so we're back to task1

        activity2.setEndTime(newXMLGregorianCalendar());
        HistoryActivity activity3 = new HistoryActivity();
        activity3.setActivityName("_1");
        historyList.add(activity3);
        
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("loopTask_decision", "repeat");

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, vars));
        evaluator.evaluate();

        assertEquals(CompletionState.OPEN, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CompletionState.COMPLETED, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loop.getCompletionState()));

        // complete the task, back on the loop

        activity3.setEndTime(newXMLGregorianCalendar());
        HistoryActivity activity4 = new HistoryActivity();
        activity4.setActivityName("loopTask");
        historyList.add(activity4);

        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CompletionState.COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.OPEN, CompletionState.forName(loop.getCompletionState()));

        // finally we complete the loop

        activity4.setEndTime(newXMLGregorianCalendar());
        evaluator = evalFactory.getProcessEvaluator(null, process, new ProcessInstanceInfo(historyList, 1, null));
        evaluator.evaluate();

        assertEquals(CompletionState.COMPLETED, CompletionState.forName(task1.getCompletionState()));
        assertEquals(CompletionState.COMPLETED, CompletionState.forName(loopTask.getCompletionState()));
        assertEquals(CompletionState.COMPLETED, CompletionState.forName(loop.getCompletionState()));

    }

    private XMLGregorianCalendar newXMLGregorianCalendar() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
            return null;
        }
    }
}