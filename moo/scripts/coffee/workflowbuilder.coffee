
$ ->
    ko.applyBindings(new WorkflowBuilderViewModel())
    $(".draggable").draggable
        revert: true
        cursorAt: {top: -5, left: -5}
        connectToFancytree: true


#getWorkflows = ->
#  $.getJSON("data/workflows.json")


dndOptions =
    autoExpandMS: 100
    preventVoidMoves: true
    preventRecursiveMoves: true

    dragStart: (node) -> node.data.draggable

    dragEnter: (target, data) -> target.data.act.dragEnter(data)

    dragDrop: (target, data) ->
        if data.hitMode is "over"
            data.otherNode.moveTo(target.getFirstChild(), "before")
        else
            data.otherNode.moveTo(target, data.hitMode)


class WorkflowBuilderViewModel
    constructor: ->
        @workflow = ko.observable()
        #@loadWorkflow("BrianTempSvc")
#        @loadWorkflow("v2-simple")
#        @loadWorkflow("complicated")
#        @loadWorkflow("Denim_Decision")
#        @loadWorkflow("LoopTest")
        @loadWorkflow("vars-test")
        @selectedActivity = ko.observable()

        @workflowComponents = ActivityFactory.all()



    loadWorkflow: (workflowName) =>
        COW.cowRequest("processes/#{workflowName}").done (data) =>
            @workflow(new Workflow(data))
            @configTree(@workflow())


    configTree: (workflow) =>
        $("#tree").fancytree
            extensions: ["dnd"]
            debugLevel: 2
            source: [workflow]
            imagePath: "images/"
            icons: false
            dnd: dndOptions
            click: (event, data) => @selectedActivity(data.node.data.act)
        @tree = $("#tree").fancytree("getTree")

    # Used to test tree visitor
    prettyPrint: => new PrettyPrintVisitor(@tree)

    save: =>
        converter = new WorkflowXmlConverter(@tree)
        xml = converter.getXml()
        window.xml = xml
        console.log(xml)
        COW.xmlRequest("processes/#{converter.key}", "put", xml).done ->
            alert("xml saved")






# cow-server doesn't accept json workflows, so we need to use xml
# The visitor pattern is used to build up the xml document
class WorkflowXmlConverter
    constructor: (tree) ->
        # create a new xml document
        @xml = $($.parseXML(
              '<process xmlns="http://www.wiredwidgets.org/cow/server/schema/model-v2"></process>'))
        @parentXml = @xml
        # tree root node only has one child, the workflow
        workflowRoot = tree.rootNode.children[0]
        console.log(@xml)
        @visit(workflowRoot)

    # Get non-jquery xml document
    getXml: -> @xml[0]

    # Delegate to correct visit* method
    visit: (node) ->
        node.data.act.accept(@, node)


    # Visits children of a folder element
    visitChildren: (nodeXml, nodeChildren) =>
        # Since node is a folder set the parentXml to them self
        [oldXmlPosition, @parentXml] = [@parentXml, nodeXml]
        @visit(child) for child in nodeChildren
        # Reset the parentXml since we have already visited the child elements
        @parentXml = oldXmlPosition


    visitWorkflow: (node) =>
        # Should have only one root node
        @key = node.key

        process = $(@parentXml.find("process"))
        @addAttributesToNode(process, node.data.act.apiAttributes)
        @createVariablesElement(process, node.data.act.variables)

        # workflow should have only one child, activities
        @visitChildren(process, [node.children[0]])


    visitActivities: (node) =>
        xmlActivities = @createActivityElement("activities", node)
        @visitChildren(xmlActivities, node.children)


    visitDecision: (node) =>
        xmlDecision = @createActivityElement("decision", node)
        xmlTask = @createTag("task", xmlDecision)
        @addAttributesToNode(xmlTask, node.data.act.task.apiAttributes)
        @visitChildren(xmlDecision, node.children)


    visitOption: (node) =>
        xmlOption = @createActivityElement("option", node)
        @visitChildren(xmlOption, node.children)


    visitLoop: (node) =>
        xmlLoop = @createActivityElement("loop", node)
        xmlLoopTask = @createTag("loopTask", xmlLoop)
        @addAttributesToNode(xmlLoopTask, node.data.act.loopTask.apiAttributes)
        @visitChildren(xmlLoop, node.children)


    visitHumanTask: (node) => @createActivityElement("task", node)

    visitServiceTask: (node) => @createActivityElement("serviceTask", node)

    visitScript: (node) => @createActivityElement("script", node)

    visitExit: (node) => @createActivityElement("exit", node)

    visitSignal: (node) => @createActivityElement("signal", node)

    visitSubprocess: (node) => @createActivityElement("subProcess", node)


    createActivityElement: (tag, treeNode) =>
        xml = @createTag(tag, @parentXml)
        @addAttributesToNode(xml, treeNode.data.act.apiAttributes)
        @createVariablesElement(xml, treeNode.data.act.variables)
        return xml

    createTextElement: (parent, tag, content) ->
        xml = @createTag(tag, parent)
        xml.text(content)
        return xml

    # If I do $("<tag />") jquery parses it as html and lowercases all the tags,
    # if I do $.parseXML("<tag />"), then the new tag is in a separate xml document
    createTag: (name, parent) ->
        parent.append("<#{name} class='hack'/>")
        newTag = parent.find(".hack")
        newTag.removeAttr("class")
        return newTag

    addAttributesToNode: (xmlElement, attributes) =>
        unwrappedAttributes = ko.mapping.toJS(attributes)
        for attr in unwrappedAttributes when attr.value
            if attr.isXmlAttribute
                xmlElement.attr(attr.key, attr.value)
            else
                @createTextElement(xmlElement, attr.key, attr.value)


    createVariablesElement: (xmlElement, observableVars) ->
        variablesXml = @createTag("variables", xmlElement)
        unwrappedVars = ko.mapping.toJS(observableVars)
        for variable in unwrappedVars
            varXml = @createTag("variable", variablesXml)
            for own attrName, attrValue of variable
                varXml.attr(attrName, attrValue)






# Base class for all activities
class Activity
    # Set default values
    constructor: (@data) ->
#        @key = @data.key
        @title = @data?.name ? @.constructor.name
        @icon = "Icon_Task.png"
        @draggable = true
        @expanded = true
        # Add reference to self so it can be accessed from fancytree nodes
        @act = @
        @isDecision = false
        @isActivities = false
        @folder = false
        @variables = ko.observableArray()
        @readVariables()

        @apiAttributes = ko.observableArray()
        @addAttr("name", "Name", true)
        @addAttr("description", "Description")
        @addInvisibleAttr("key", true)
        @addAttr("bypassable", "Bypassable", true, "checkbox")

    dragEnter: =>
        if @folder
            return ["over"]
        return ["before", "after"]

    # Add to the set of attributes that can be edited and included in the xml
    addAttr: (key, label, isXmlAttribute = false, inputType = "text") =>
        @apiAttributes.push
            key: key
            value: ko.observable(@data?[key] ? "")
            label: label
            isXmlAttribute: isXmlAttribute
            inputType: inputType


    addInvisibleAttr: (key, isXmlAttribute = false) => @addAttr(key, null, isXmlAttribute)


    readVariables: =>
        varList = @data?.variables?.variable
        return unless varList?
        @variables.push(@createObservableVar(v)) for v in varList


    createObservableVar: (variable) ->
        name: ko.observable(variable.name)
        value: ko.observable(variable.value)
        type: ko.observable(variable.type)
        required: ko.observable(variable.required ? false)
        output: ko.observable(variable.output ? false)




class Workflow extends Activity
    constructor: (@data) ->
        @key = @data.key
        @title = "<span class='glyphicon glyphicon-list-alt'></span> #{@key}"
        @children = [ ActivityFactory.create(@data.activity) ]
        @draggable = false
        @folder = true
        @expanded = true
        @act = @
        @variables = ko.observableArray()
        @readVariables()

        @apiAttributes = ko.observableArray()
        @addAttr("name", "Name", true)
        @addAttr("bypassAssignee", "Bypass Assignee")
        @addAttr("bypassCandidateUsers", "Bypass Candidate Users")
        @addAttr("bypassCandidateGroups", "Bypass Candidate Groups")
        @addInvisibleAttr("key", true)


    dragEnter: -> false

    accept: (visitor, node) ->
        visitor.visitWorkflow(node)





class Activities extends Activity
    @typeString: "org.wiredwidgets.cow.server.api.model.v2.Activities"

    constructor: (data) ->
        super(data)
        @isSequential = @data?.sequential ? true
        if @data?.name?
            @title = @data.name
        else
            @title = if @isSequential then "List" else "Parallel List"

        childActivitiesData = @data?.activity
        if childActivitiesData
            @children = (ActivityFactory.create(d) for d in childActivitiesData)
        else
            @children = []

        @icon = "Icon_List.png"
        @folder = true
        @isActivities = true

        @addAttr("sequential", "Is Sequential", true,  "checkbox")
        @addAttr("mergeCondition", "Merge Condition", true)


    dragEnter: (data) =>
        acitivity = data.node.getParent()?.data.act
        if acitivity?.isDecision and data.otherNode.data.act.isActivities
            return ["over", "after", "before"]
        else
            return ["over"]

    accept: (visitor, node) ->
        visitor.visitActivities(node)

    typeStr: -> Activities.typeString




class HumanTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Task"

    constructor: (@data) ->
        super(@data)
        if @data?
            @assignee = data.assignee
            @candidateGroups = data.candidateGroups

        @addAttr("assignee", "Assignee")
        @addAttr("candidateUsers", "Candidate users")
        @addAttr("candidateGroups", "Candidate groups")
        @addAttr("createTime", "Create time")
        @addAttr("endTime", "End time")

    accept: (visitor, node) -> visitor.visitHumanTask(node)

    typeStr: -> HumanTask.typeString



class ServiceTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.ServiceTask"

    constructor: (@data) ->
        super(@data)
        @icon = "Icon_ServiceTask.png"

        @addAttr("url", "URL")
        @addAttr("content", "Content")
        @addAttr("contentType", "Content type")
        @addAttr("var", "Result variable")


    accept: (visitor, node) -> visitor.visitServiceTask(node)

    typeStr: -> ServiceTask.typeString



class ScriptTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Script"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Script.png"
        @addAttr("import", "Imports")
        @addAttr("content", "Content")


    accept: (visitor, node) ->
        visitor.visitScript(node)

    typeStr: -> ScriptTask.typeString



class Decision extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Decision"

    constructor: (data) ->
        super(data)
        optionsData = data?.option
        if optionsData
            @children = (new Option(opt) for opt in optionsData)
        else
            @children = []

        @icon = "Icon_Decision.png"
        @folder = true
        @isDecision = true
        @task = new HumanTask(data?.task)


    dragEnter: (data) =>
        if data.otherNode.data.act.isActivities then ["over"] else false

    accept: (visitor, node) ->
        visitor.visitDecision(node)

    typeStr: -> Decision.typeString



class Option extends Activity
    constructor: (@data) ->
        super(@data)
        @icon = "Icon_Decision_Arrow.png"

        childActivitiesData = @data?.activity;
        if (childActivitiesData)
            @children = [ ActivityFactory.create(childActivitiesData) ]
        else
            @children = []

        @folder = true

    accept: (visitor, node) ->
        visitor.visitOption(node)


class Exit extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Exit"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Exit.png"
        @addAttr("state", "State", true)

    accept: (visitor, node) ->
        visitor.visitExit(node)


    typeStr: -> Exit.typeString



class Signal extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Signal"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Signal.png"
        @addAttr("signalId", "Signal Id", true)

    accept: (visitor, node) ->
        visitor.visitSignal(node)

    typeStr: -> Signal.typeString



class Subprocess extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.SubProcess"

    constructor: (data) ->
        super(data)
        @icon = "Icon_SubProcess.png"

    accept: (visitor, node) ->
        visitor.visitSubprocess(node)

    typeStr: -> Subprocess.typeString


class Loop extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Loop"

    constructor: (data) ->
        super(data)
        childData = @data?.activity
        if childData
            @children = [ ActivityFactory.create(childData) ]
        else
            @children = []
        @loopTask = new HumanTask(data?.loopTask)
        @icon = "Icon_Loop.png"
        @folder = true
        @addAttr("doneName", "Done name", true)
        @addAttr("repeatName", "Repeat name", true)
        @addAttr("executionCount", "Execution count", true)

    accept: (visitor, node) ->
        visitor.visitLoop(node)

    typeStr: -> Loop.typeString





class ActivityFactory
    @typeMap: {}
    @typeMap[Activities.typeString] = Activities
    @typeMap[HumanTask.typeString] = HumanTask
    @typeMap[ServiceTask.typeString] = ServiceTask
    @typeMap[ScriptTask.typeString] = ScriptTask
    @typeMap[Decision.typeString] = Decision
    @typeMap[Exit.typeString] = Exit
    @typeMap[Loop.typeString] = Loop
    @typeMap[Signal.typeString] = Signal

    @create: (data) ->
        new @typeMap[data.declaredType](data.value)

    @all: ->
        (new act() for key, act of @typeMap)







# Test visitor pattern
class PrettyPrintVisitor
    constructor: (@tree) ->
        @tabs = ""
        workflowRoot = @tree.rootNode.children[0]
        @visit(workflowRoot)


    display: (title) ->
        console.log(@tabs + title)

    indent: => @tabs += "\t"

    dedent: => @tabs = @tabs.substr(1)

    visit: (node) ->
        node.data.act.accept(@, node)

    visitChildren: (node) ->
        @indent()
        @visit(child) for child in node.children
        @dedent()

    visitWorkflow: (node) ->
        @display(node.key)
        @visitChildren(node)

    visitActivities: (node) ->
        @display(node.title)
        @visitChildren(node)

    visitHumanTask: (node) ->
        @display(node.title)

    visitServiceTask: (node) ->
        @display(node.title)

    visitScript: (node) ->
        @display(node.title)

    visitDecision: (node) ->
        @display(node.title)
        @visitChildren(node)

    visitOption: (node) ->
        @display(node.title)
        @visitChildren(node)

    visitExit: (node) ->
        @display(node.title)

    visitLoop: (node) ->
        @display(node.title)
        @visitChildren(node)

    visitSignal: (node) ->
        @display(node.title)

    visitSubprocess: (node) ->
        @display(node.title)

