from scowclient import ScowClient
import json


def listProcs():
    sclient = ScowClient()
    jsonObj = sclient.get('processDefinitions')
    prettyPrintJson(jsonObj['processDefinitions'][0:4])

def prettyPrintJson(obj):
    print json.dumps(obj, sort_keys=True, indent=4)

def main():
    listProcs()


if __name__ == "__main__":
    main()
