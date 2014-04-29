__author__ = 'BROSENBERG'
__author__ = 'brosenberg'
#! /usr/bin/env python

import os

from urllib2 import *
import json
from pytest import raises

class ScowClient:
    def __init__(self, baseUrl='http://scout2:8080/cow-server/'):
        self.basicAuthVal = 'Basic YnJvc2VuYmVyZzpicmlhbg=='
        self.baseUrl = baseUrl
        self.opener = build_opener(ProxyHandler({}))
        #self.opener = build_opener(ProxyHandler({'http': 'http://127.0.0.1:8888'}))
        self.opener.addheaders = [('Authorization', self.basicAuthVal),
                                  ('Accept', 'application/json'), ('Content-Type', 'application/json')]


    def url(self, path):
        return self.baseUrl + path

    def get(self, path):
        url = self.url(path)
        print 'GET', url
        resp = self.opener.open(url)
        return self.parseResult(resp)

    def delete(self, path):
        url = self.url(path)
        print 'DELETE', url
        request = Request(url)
        request.get_method = lambda: 'DELETE'
        resp = self.opener.open(request)
        return self.parseResult(resp)

    def post(self, path, data='', contentType="application/json"):
        url = self.url(path)
        print 'POST', url
        req = Request(url, data=data, headers={'Content-type': contentType})
        resp = self.opener.open(req)
        return self.parseResult(resp)

    def parseResult(self, resp):
        result = resp.read()
        if result:
            return json.loads(result)

    def whoami(self):
        return self.get('whoami')

    def processDefinitions(self):
        return self.get('processDefinitions')['processDefinition']

    def processes(self, procId=None):
        url = 'processes'
        if procId:
            url = url + '/' + procId
        return self.get(url)

    def execute(self, procId):
        url = 'processInstances?execute=' + procId
        return self.post(url)['id']

    def processInstances(self, instanceId=None):
        url = 'processInstances'
        if instanceId:
            url = url + '/' + instanceId
        return self.get(url)

    def tasksByProcInstance(self, instanceId):
        url = 'tasks?processInstance=%s' % instanceId
        return self.get(url)['task']

    def takeTask(self, taskId, assignee):
        url = 'tasks/%s/take?assignee=%s' % (taskId, assignee)
        return self.post(url)

    def completeTask(self, taskId, **variables):
        url = 'tasks/%s' % taskId
        varStr = '&'.join('var=%s:%s' % v for v in variables.iteritems())
        if varStr:
            url = url + '?' + varStr
        return self.delete(url)

    def createProcess(self, processXml):
        return self.post('processes', processXml, contentType="application/xml")

    def deleteProcess(self, procName):
        return self.delete("processes/" + procName)

    def deleteAllInstancesOfProcess(self, procName):
        path = 'processes/%s/processInstances' % procName
        return self.delete(path)

    def deleteProcessInstance(self, instanceId):
        path = 'processInstances/%s' % instanceId
        return self.delete(path)

    def readyTasksByProcInstance(self, instanceId):
        tasks = self.tasksByProcInstance(instanceId)
        return [t for t in tasks if t['state'] == 'Ready']


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








