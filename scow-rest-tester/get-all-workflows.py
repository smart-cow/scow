#! /usr/bin/env python
"""
Usage: python get-all-workflows.py [host] [output workflow directory]
"""
from scowclient import ScowClient
import sys
import os

def getBaseUrl():
    host = "scout3" if len(sys.argv) == 1 else sys.argv[1]
    return "http://%s:8080/cow-server/" % host

def getOutputDir():
    workflowDir = "workflows/" if len(sys.argv) < 3 else sys.argv[2] + '/'
    if not os.path.exists(workflowDir):
        os.makedirs(workflowDir)
    return workflowDir

def getProcKeys(scow):
    procDefs = scow.processDefinitions()
    return (pd["key"] for pd in procDefs)

def getProcs(scow):
    procKeys = getProcKeys(scow)
    return ( (key, scow.processes(key)) for key in procKeys)


def saveProc(key, proc, wflowDir):
    fileName = wflowDir + key + ".xml"
    with open(fileName, "w") as f:
        f.write(proc)


def main():
    scow = ScowClient(getBaseUrl())
    workflowDir = getOutputDir()
    for key, proc in getProcs(scow):
        saveProc(key, proc, workflowDir)


if __name__ == '__main__':
    main()
