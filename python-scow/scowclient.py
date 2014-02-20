#! /usr/bin/env python

import pickle
from scow import *
import json
from sys import exit

controllers = None


def getInput(items, itemType, prompt = 'Select a %s: \n'):
    prompt = prompt % itemType
    selection = ''
    while not selection.isdigit():
        print prompt,
        for index, element in enumerate(items):
            print '%s: %s' % (index, element.name)
        selection = raw_input(prompt)
        if selection[0] == 'd':
            describe(selection, items)
        elif selection[0] == 'q':
            exit()
    return items[int(selection)]




def getDigt(selection):
    st = None
    end = None
    for i, ch in enumerate(selection):
        if st is None and ch.isdigit():
            st = i
        elif st is not None and not ch.isdigit():
            end = i
            break
    number = selection[st:end]
    return int(number)


def describe(selection, items):
    try:
        number = getDigt(selection)
        item = items[number]
        print item
    except Exception, e:
        print 'invalid entry "%s"' % selection



def chooseParameters(method):
    params = {}
    while True:
        for p in method.params.itervalues():
            print 'Enter value for ', p.name
            print p
            pValue = raw_input('Enter value for %s: ' % p.name)
            if (pValue == 'reset'):
                params = {}
                break;
            params[p.name] = pValue
        else:
            return params


def verifyCommand(controller, method, parameters):
    cmd = client.getScowCommand(controller, method, parameters)
    print cmd
    resp = raw_input('Send command?(y/n): ')
    if resp[0].lower() == 'n' :
        return None
    return cmd


def loadControllers(filePath):
    with open(filePath) as f:
        return pickle.load(f)

def prettyPrint(jsonObj):
    print json.dumps(jsonObj, sort_keys=True, indent=4,
            separators=(',', ': '))


def main():
    global controllers
    global client
    controllers = loadControllers('scowMethods.pickle')
    #client = ScowClient('http://localhost:8080/cow-server/')
    client = ScowClient('http://scout2:8080/cow-server/')
    while True:
        try:
            controller = getInput(controllers, 'controller')
            method = getInput(controller.methods, 'method')
            parameters = chooseParameters(method)
            command = verifyCommand(controller, method, parameters)
        except Exception as e:
            raw_input('Input error %s\npress any key...' % e)
            continue
        if command is None:
            continue
        resp = client.sendCommand(command)
        if resp :
            prettyPrint(resp)
        else :
            print 'No response'





if __name__ == "__main__":
    main()
