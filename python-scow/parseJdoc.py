#! /usr/bin/env python

#xpath samples
#
#/html/body/div[4]/div[1]/ul/li/pre/text()[1] controller annotation
#
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[1]/li  = first method
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[2]/li = second method
#
#
#
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[1]/li/h4 = first method name
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[2]/li/h4 = second method name
#
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[1]/li/pre/text()[1] = first method request mapping
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[2]/li/pre/text()[1] = second method request mapping
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[1]/li/div = first method description
#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[14]/li/div = 14 method description

#/html/body/div[4]/div[3]/ul/li/ul[2]/li/ul[3]/li/dl = param section


from xml.etree.ElementTree import ElementTree
import re
import sys
import pickle
from scow import *



def main():
    #controller = ScowController(sys.argv[1])
    controllers = [ScowController(i) for i in sys.argv[1:]]
    toString = ''.join(str(c) for c in controllers)
    print len(toString)
    pickle.dump(controllers, open('scowMethods.pickle', 'w'))

if __name__ == "__main__":
    main()
