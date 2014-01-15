Composable Operational Workflows (COW)
==================================================

The COW project is comprised of five sub-projects.  Each is open-sourced and was hosted on
BitBucket.org as a Mercurial repository.  Documentation is hosted via this
WiredWidgets.org website.

* [COW Documentation](http://www.wiredwidgets.org/cow)
* [Server API Javadocs](http://smart-cow.github.io/scow/site/apidocs/index.html)

* [COW Server](http://www.wiredwidgets.org/documentation/cow-server)
    (Summary | [Code](https://bitbucket.org/wjk5828/wiredwidgets-cow-server) |
    [Changelog](http://www.wiredwidgets.org/cow/cow-server-changelog))
* [COW Server API]() (Summary | [Code](http://www.wiredwidgets.org/documentation/cow-server-api) |
    [Changelog](http://www.wiredwidgets.org/cow/cow-server-api-changelog))
* COW Web Client ( [Summary](http://www.wiredwidgets.org/cow#TOC-COW-Web-Application) |
    [Code](https://bitbucket.org/wjk5828/wiredwidgets-cow-webapp)  |
    [Changelog](http://www.wiredwidgets.org/cow/cow-webapp-changelog) )
* COW AgileClient Plugins ([Summary](http://www.wiredwidgets.org/cow#TOC-COW-AgileClient-plugins) |
    [Code](https://bitbucket.org/wjk5828/wired-widgets-cow-agile-client) |
    [Changelog](http://www.wiredwidgets.org/cow/cow-agile-client-changelog))
* COW Openfire Plugin ([Summary](http://www.wiredwidgets.org/cow#TOC-COW-Openfire-plugin) |
    [Code](https://bitbucket.org/wjk5828/wiredwidgets-cow-openfire-plugin) |
    [Changelog](http://www.wiredwidgets.org/cow/cow-openfire-plugin-changelog))

Project Overview
------------------------
This is the general COW overview...

Sub-project Descriptions
------------------------
### COW Server ###
The COW Server is a web application that providing a REST service API for interacting with the COW BPMN engine.  The engine is currently implemented using JBoss JBPM 5.4, but by design the COW Server is intended to decouple the underlying BPMN engine implementation from the API it exposes.

### COW Server API ###
The COW Server API includes the XML schemas and generated java classes used by the COW Server to implement its REST API.  The API is managed as a separate project in order to create a clean separation between the COW Server implementation and the API that can be used by clients.

### COW Web Application ###
The COW webapp is a web GUI designed to be used in conjunction with the REST services procided by the COW server. The application is built using SmartGWT, a variant of GWT (Google Web Toolkit). GWT allows code to be written with a subset of Java and compiles into Javascript with multi-browser compatibility, and will compile into both a standard web page and an OpenSocial gadget with minimal code changes.

The COW webapp is designed to demonstrate as many features that the COW server provides as possible. This includes creating and editing workflows, executing workflows and tracking their progress, completing tasks created from workflows in execution, and more.

### COW AgileClient plugins ###
The COW Agile Client plugins are a representative prototype of a Netbeans-based client to manage workflows and complete assigned tasks.  It connects to a COW server via REST calls defined by the COW Server API. The plugins are designed to run in the AgileClient platform (a customized Netbeans 6.9.1 platform) but could be tweaked to work with any "vanilla" Netbeans platform.

There are three Netbeans modules (NBMs) that are contained within a single module suite.  All three are needed to function.
* cow-ac-client: the core module which contains all the source code for the actual client
* cow-ac-libs: wraps dependency libraries needed for Spring, message handling, server notifications, logging,   and so forth
* cow-server-api: wraps the cow-server-api jar into a NBM which provides access to java and the REST calls in use by the server


### COW Openfire plugin ###
The COW Openfire plugin is a plugin that we have created to manage the users and groups on the COW server.  When a user or group is added, changed, or deleted on the Openfire server, the changes are replicated to the COW server via REST calls.  This also includes adding and removing users from groups.

After the plugin has been deployed to the Openfire server, there will be a COW Admin Page.  This is located under Users/Groups->Users->COW Admin.  From this page, you can set the location of the COW Server.  This means you do not have to be running the Openfire server on the same machine as the COW server.  You can also "Push Users/Groups to COW" and "Pull Users/Groups from COW" if for some reason the two become out of sync.

Troubleshooting and Frequently Asked Questions
------------------------------------------------------
All sub-projects are combined into a single page available here.

Subpages (6):
[COW AgileClient Changelog](http://www.wiredwidgets.org/cow/cow-agile-client-changelog)
[COW Openfire Plugin Changelog](http://www.wiredwidgets.org/cow/cow-openfire-plugin-changelog)
[COW Server API ChangeLog](http://www.wiredwidgets.org/cow/cow-server-api-changelog)
[COW Server Changelog](http://www.wiredwidgets.org/cow/cow-server-changelog)
[COW - Troubleshooting and Frequently Asked Questions](http://www.wiredwidgets.org/cow/faq)
[cow-webapp-changelog](http://www.wiredwidgets.org/cow/cow-webapp-changelog)
