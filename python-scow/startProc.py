from scowclient import ScowClient
import json


def startProc():
    sclient = ScowClient()
    jsonObj = sclient.post('processInstances/active?execute=1grp&name=myproc', '')
    prettyPrintJson(jsonObj)

def prettyPrintJson(obj):
    print json.dumps(obj, sort_keys=True, indent=4)

def main():
    listProcs()


if __name__ == "__main__":
    startProc()
