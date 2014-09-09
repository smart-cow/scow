__author__ = 'brosenberg'
#! /usr/bin/env python

import os


from pytest import raises
from scowclient import ScowClient




def getVariables(task):
    return task['variables']['variable']



BASE_URL = 'http://scout2:8080/cow-server/'
cwd = os.path.dirname(os.path.realpath(__file__)) + '\\'

def test_SvcAndScript():
    scow = ScowClient(BASE_URL)
    assert scow.whoami()['id'] == 'brosenberg'

    assert any(pd['key'] == 'SvcAndScript' for pd in scow.processDefinitions())

    scow.processes('SvcAndScript')

    pid = scow.execute('SvcAndScript')
    scow.processInstances(pid)

    taskId = scow.readyTasksByProcInstance(pid)[0]['id']
    scow.takeTask(taskId, 'brosenberg')
    scow.completeTask(taskId, tempInput=100)

    task = scow.readyTasksByProcInstance(pid)[0]
    assert any(v['name'] == 'message' and v['value'] == '100 degrees Celsius equals 212 degrees Farenheight'
               for v in getVariables(task))

    taskId = task['id']
    scow.takeTask(taskId, 'brosenberg')
    scow.completeTask(taskId)

    assert scow.processInstances(pid)['state'] == 'completed'





def test_createDeleteWorkflow():
    TEST_WORKFLOW = 'UnitTestWorkflow'

    scow = ScowClient(BASE_URL)
    try:
        scow.deleteAllInstancesOfProcess(TEST_WORKFLOW)
        scow.deleteProcess(TEST_WORKFLOW)
    except: pass

    with raises(Exception):
        scow.processes(TEST_WORKFLOW)

    workflowXml = open(cwd + 'UnitTestWorkflow.xml').read()
    scow.createProcess(workflowXml)
    pid1 = scow.execute(TEST_WORKFLOW)
    pid2 = scow.execute(TEST_WORKFLOW)

    with raises(Exception):
        scow.deleteProcess(TEST_WORKFLOW)

    scow.deleteProcessInstance(pid1)

    with raises(Exception):
        scow.deleteProcess(TEST_WORKFLOW)

    scow.deleteProcessInstance(pid2)
    scow.deleteProcess(TEST_WORKFLOW)

    with raises(Exception):
        scow.processes(TEST_WORKFLOW)








