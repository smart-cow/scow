#! /usr/bin/env python

from scow import *
import json


controllers = None
client = None

def prettyPrint(jsonObj):
    print json.dumps(jsonObj, sort_keys=True, indent=4, separators=(',', ': '))

def loadControllers(filePath):
    with open(filePath) as f:
        return pickle.load(f)


def sendScowCommand(controllerName, methodName, **params):
    controller = [c for c in controllers if c.name == controllerName][0]
    method = [m for m in controller.methods if m.name == methodName][0]
    cmd = client.getScowCommand(controller, method, params)
    return client.sendCommand(cmd)



def getInstancesIds(instancesDict):
    instances = instancesDict["processInstance"]
    return (e['id'] for e in instances)


def deleteProcInstances(ids):
    for id in ids:
        name, ext = id.split('.')
        sendScowCommand('ProcessInstancesController', 'deleteProcessInstance', 
                        id = name, ext = ext)
        break


def main():
    global controllers
    global client
    controllers = loadControllers('scowMethods.pickle')
    client = ScowClient('http://scout2:8080/cow-server/')
    #client = ScowClient('http://localhost:8080/cow-server/')

    processInstances = sendScowCommand('ProcessInstancesController', 'getAllProcessInstances')
    procInstanceIds = getInstancesIds(processInstances)
    deleteProcInstances(procInstanceIds)

    


if __name__ == "__main__":
    main()