__author__ = 'brosenberg'

from urllib2 import *
import json


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

    def getXml(self, path):
        url = self.url(path)
        print 'GET (xml)', url
        request = Request(url, headers = {"Accept": "application/xml"} )
        resp = self.opener.open(request)
        return resp.read()

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

    def processes(self, procId = None, asXml = True):
        url = 'processes'
        if procId:
            url = url + '/' + procId
        if asXml:
            return self.getXml(url)
        else:
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
