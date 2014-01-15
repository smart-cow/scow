from scowclient import ScowClient
import json



def listUsers():
    url = 'users'

    sclient = ScowClient()
    #jsonObj = json.loads(sclient.get(url))
    jsonObj = sclient.get(url)
    users = jsonObj['users']
    for u in users:
        print u['id']

def prettyPrintJson(obj):
    print json.dumps(obj, sort_keys=True, indent=4)

def main():
    listUsers()


if __name__ == "__main__":
    main()
