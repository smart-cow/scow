from xml.dom.minidom import *
from scowclient import ScowClient

def listUsers():
    url = 'users'
    sclient = ScowClient()
    document = parseString(sclient.get(url))
    users = document.getElementsByTagName('user')
    for u in users:
        idTag = u.getElementsByTagName('id')[0]
        username = idTag.childNodes[0].data
        print username


def main():
    listUsers()


if __name__ == "__main__":
    main()
