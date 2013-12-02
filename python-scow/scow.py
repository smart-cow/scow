#! /usr/bin/env python

from xml.etree.ElementTree import ElementTree
import re
import sys
import pickle
from urllib2 import *
import urllib
import json

class ScowClient:
    def __init__(self, serverLoc = 'http://localhost:8080/cow-server/',
            accptJson = True,
            clrProxy = True):

        self.serverLoc = serverLoc

        if (clrProxy):
            proxy_handler = ProxyHandler({})
            self.opener = build_opener(proxy_handler)
        else:
            self.opener = build_opener()

        self.get = self.getstr
        if (accptJson):
            self.opener.addheaders = [('Accept', 'application/json')]
            self.get = self.getJson

    def getstr(self, path):
        return self.opener.open(self.serverLoc + path).read()

    def getJson(self, path):
        return json.loads(self.getstr(path))

    def post(self, path, data = "", timeout = 5):
        return self.opener.open(self.serverLoc + path, data, timeout).read()

    def delete(self, path, timeout = 5):
        request = Request(self.serverLoc + path)
        request.get_method = lambda: 'DELETE'
        doc = self.opener.open(request, timeout=timeout)
        return doc.read()

    def put(self, path, data="", timeout = 5):
        request = Request(self.serverLoc + path, data=data)
        request.get_method = lambda: 'PUT'
        doc = self.opener.open(request, timeout=timeout)
        return doc.read()

    def getScowCommand(self, controller, method, params):
        return ScowCommand(controller, method, params)

    def sendCommand(self, cmd):
        if cmd.httpMethod == "GET":
            return self.get(cmd.url)
        elif cmd.httpMethod == "POST":
            return self.post(cmd.url, cmd.bodyParams)
        elif cmd.httpMethod == "DELETE":
            return self.delete(cmd.url)
        elif cmd.httpMethod == "PUT":
            return self.put(cmd.url, cmd.bodyParams)
        return "unsupported http method: %s" % cmd.httpMethod




class ScowCommand:
    def __init__(self, controller, method, params):
        self.controller = controller
        self.method = method
        self.params = params
        self.httpMethod = method.httpMethod
        self.buildPath()
        self.queryString = self.encodeParams(ScowParam.QUERY_STRING)
        self.bodyParams = self.encodeParams(ScowParam.BODY)

        self.url = self.path
        if (self.queryString) :
            self.url += "?" + self.queryString

    def getParams(self, paramType):
        return [p.name for p in self.method.params.itervalues()
                    if p.paramType == paramType]

    def buildPath(self):
        urlPathParams = self.getParams(ScowParam.URL)
        url = self.controller.baseUrl + self.method.urlPath
        for param in urlPathParams:
            url = url.replace("{%s}" % param, self.params[param])
        self.path = quote(url)

    def encodeParams(self, paramType):
        params = self.getParams(paramType)
        paramVals = [(k,v) for k, v in self.params.iteritems() if k in params]
        return urllib.urlencode(paramVals)

    def __str__(self) :
        return '%s %s' % (self.httpMethod, self.url)



class ScowController:
    methodSectionPath = './body/div[4]/div[3]/ul/li/ul[2]/li/ul/li'
    controllerAnnotation = './body/div[4]/div[1]/ul/li/pre'
    controllerNamePath = './body/div[3]/h2'

    def __init__(self, filePath = None):
        if filePath is None:
            return
        self.doc = ElementTree()
        self.doc.parse(filePath)
        self.name = self.doc.find(ScowController.controllerNamePath).text.split()[1]
        self.getBaseUrl()
        self.getAllMethods()

    def getBaseUrl(self):
        annotations = self.doc.find(ScowController.controllerAnnotation).text
        match = re.findall(r'value="(.*?)"', annotations)
        if match:
            self.baseUrl = match[0]
        else:
            self.baseUrl = ''

    def getAllMethods(self):
        methodSections = self.doc.findall(ScowController.methodSectionPath)
        self.methods = [ScowMethod(sec) for sec in methodSections]

    def __str__(self):
        methodsStr = '\n'.join(str(m) for m in self.methods)
        return '''
Name: %s
BaseUrl: %s
Methods:
%s
        ''' % (self.name, self.baseUrl, methodsStr)


class ScowMethod:

    def __init__(self, mSection = None):
        if mSection is None:
            return
        self.mSection = mSection
        self.urlPath = ""
        self.returnDescription = "<none>"
        self.params = {}
        self.findMethodName()
        self.findMethodPath()
        #self.printDebug()


    def printDebug(self):
        print self
        print
        print '-' * 10

    def __str__(self):
        paramStr = '\t'.join(str(p) for p in self.params.itervalues())
        return '''
Method name: %s
Description: %s
Return description: %s
URL path: %s
HTTP method: %s
Parameters:
        %s
        ''' % (self.name,
               self.description,
               self.returnDescription,
               self.urlPath,
               self.httpMethod,
               paramStr)


    def findMethodName(self):
        self.name = self.mSection.find('./h4').text
        div = self.mSection.find('./div')
        self.description = div.text if div is not None else '<no description>'


    def findMethodPath(self):
        #@RequestMapping(value="/participations/{taskId}",
        #                        method=POST,
        #                        params="user")
        methodHeader = ''.join(self.mSection.find('./pre').itertext())
        pubPos = methodHeader.find('public')
        reqMapping = methodHeader[:pubPos]
        sig = methodHeader[pubPos:]

        self.parseReqMapping(reqMapping)
        self.parseMethodSignature(sig)
        self.parseParamDescriptions()


    def parseReqMapping(self, reqMapping):
        match = re.search(r'value="(.*?)"', reqMapping)
        if match :
            self.urlPath = match.group(1)
            matches = re.findall(r'\{(.+?)\}', self.urlPath)
            newParams = ((name.strip(),
                          ScowParam(name.strip(), ScowParam.URL, True))
                          for name in matches)
            self.params.update(newParams)

        match = re.search(r'method=(\w*)', reqMapping)
        self.httpMethod = match.group(1).upper if match else "GET"



    def parseMethodSignature(self, sig):
        matches = re.findall(r'RequestParam\((.*?)\)', sig)
        for m in matches:
            valueStr = 'value="'
            startVarName = m.find(valueStr) + len(valueStr)
            endVarName = m.find('"', startVarName)
            varName = m[startVarName : endVarName]
            isRequired = m.find('required=false') == -1
            self.params[varName] = ScowParam(varName, ScowParam.QUERY_STRING,
                    isRequired)
        matches = re.findall(r'RequestBody\s+\w+\s+(\w+)', sig)
        matches = (m.strip() for m in matches)
        newParams = ((name, ScowParam(name, ScowParam.BODY, True))
            for name in matches)
        self.params.update(newParams)


    def parseParamDescriptions(self):
        paramsDl = self.mSection.find('./dl')
        if paramsDl is None:
            return
        currentDt = None
        for e in paramsDl:
            if (e.tag == 'dt'):
                currentDt = getInnerText(e)
            elif(e.tag == 'dd'):
                if currentDt == 'Returns:':
                    self.returnDescription = getInnerText(e)
                elif currentDt == 'Parameters:' :
                    self.parseParamDd(e)


    def parseParamDd(self, dd):
        name, desc = getInnerText(dd).split('-', 1)
        if name in ('request', 'response'):
            return
        name = name.strip()
        desc = desc.strip()
        scowparam = self.params.get(name)
        if (scowparam == None):
            return
        scowparam.description = desc




class ScowParam:
    URL = "url"
    QUERY_STRING = "query string"
    BODY = "body"

    def __init__(self, name, paramType, isRequired, description = ""):
        self.name = name
        self.paramType = paramType
        self.isRequired = isRequired
        self.description = description

    def __str__(self):
        return '''
        name: %s
        ParamType: %s
        isRequired: %s
        description: %s
        ''' % (self.name,
               self.paramType,
               self.isRequired,
               self.description)


def getInnerText(el):
    return ''.join(s.strip() for s in el.itertext())
