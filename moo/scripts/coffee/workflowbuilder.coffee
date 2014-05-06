
$ ->
  ko.applyBindings(new WorkflowBuilderViewModel())

#getWorkflows = ->
#  $.getJSON("data/workflows.json")


dndOptions =
    autoExpandMS: 100
    preventVoidMoves: true
    preventRecursiveMoves: true

    dragStart: (node) ->
        node.data.draggable

    dragEnter: (target, data) ->
        target.data.act.dragEnter(data)

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
        @loadWorkflow("complicated")
        @selectedActivity = ko.observable()


    loadWorkflow: (workflowName) =>
        COW.cowRequest("processes/#{workflowName}").done (data) =>
            @data = data
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

    prettyPrint: =>
        new PrettyPrintVisitor(@tree)

    save: =>
        converter = new WorkflowXmlConverter(@tree)
        xml = converter.getXml()
        console.log(xml)
#        COW.xmlRequest("processes/#{converter.key}", "put", xml).done (d) ->
#            alert("xml saved")







class WorkflowXmlConverter
    constructor: (tree) ->
        @xml = $($.parseXML(
              '<process xmlns="http://www.wiredwidgets.org/cow/server/schema/model-v2"></process>'))
        @xmlPosition = @xml
        workflowRoot = tree.rootNode.children[0]
        console.log(@xml)
        @visit(workflowRoot)

    getXml: -> @xml[0]

    visit: (node) ->
        node.data.act.accept(@, node)

    visitWorkflow: (node) =>
        # Should have only one root node
        workflow = node.data.act

        process = $(@xmlPosition.find("process"))
        @key = workflow.key
        process.attr("name", workflow.key)
        process.attr("key", workflow.key)

        after = @moveXmlPosition(@, process)
        @visit(node.children[0])
        after();


    moveXmlPosition: (self, newXmlPosition) ->
        [oldPosition, self.xmlPosition] = [self.xmlPosition, newXmlPosition]
        return ->
            self.xmlPosition = oldPosition


    visitActivities: (node) =>
        activities = node.data.act
        xmlActivities = $("<activities />").appendTo(@xmlPosition)

        xmlActivities.attr("sequential", activities.isSequential)

        after = @moveXmlPosition(@, xmlActivities)
        @visit(child) for child in node.children
        after();




    visitHumanTask: (node) =>
        task = node.data.act
        xmlTask = $("<task />").appendTo(@xmlPosition)
        xmlTask.attr("name", task.name)
        @createTextNode(xmlTask, "description", task.description)
        @createTextNode(xmlTask, "assignee", task.assignee)
        @createTextNode(xmlTask, "candidateGroups", task.candidateGroups)


    visitServiceTask: (node) -> console.log(node)

    visitScript: (node) -> console.log(node)

    visitDecision: (node) -> console.log(node)

    visitExit: (node) -> console.log(node)

    visitLoop: (node) -> console.log(node)

    visitSignal: (node) -> console.log(node)

    visitSubprocess: (node) -> console.log(node)



    createTextNode: (target, tag, content) ->
        $("<#{tag}/>")
            .text(content)
            .appendTo(target)






# Base class for all activities
class Activity
    constructor: (@data) ->
        @title = @data.name ? @.constructor.name
        @key = @data.key
        @icon = "Icon_Task.png"
        @draggable = true
        @expanded = true
        @act = @
        @isDecision = false
        @isActivities = false
        @folder = false
        @description = @data.description

        @apiAttributes = ko.observableArray()
        @addAttr("name", "Name", true)
        @addAttr("description", "Description")
        @addInvisibleAttr("key", true)
        @addAttr("bypassable", "Bypassable", true, "checkbox")



    dragEnter: =>
        if @folder
            return ["over"]
        return ["before", "after"]

    addAttr: (key, label, isXmlAttribute = false, inputType = "text") =>
        @apiAttributes.push
            key: ko.observable(key)
            value: ko.observable(@data[key] ? "")
            label: label
            isXmlAttribute: isXmlAttribute
            inputType: inputType

    addInvisibleAttr: (key, isXmlAttribute = false) -> @addAttr(key, null, isXmlAttribute)




class Workflow extends Activity
    constructor: (@data) ->
        @key = @data.key
        @title = "<span class='glyphicon glyphicon-list-alt'></span> #{@key}"
        @children = [ ActivityFactory.create(@data.activity) ]
        @draggable = false
        @folder = true
        @expanded = true
        @act = @

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

    constructor: (@data) ->
        super(@data)
        @isSequential = @data.sequential
        if @data.name?
            @title = @data.name
        else
            @title = if @isSequential then "List" else "Parallel List"
        @children = (ActivityFactory.create(d) for d in @data.activity)
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
        super
        @icon = "Icon_Script.png"
        @addAttr("import", "Imports")
        @addAttr("content", "Content")


    accept: (visitor, node) ->
        visitor.visitScript(node)

    typeStr: -> ScriptTask.typeString



class Decision extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Decision"

    constructor: (data) ->
        super
        @children = (ActivityFactory.create(option.activity) for option in data.option)
        c.icon = "Icon_Decision_Arrow.png" for c in @children
        @icon = "Icon_Decision.png"
        @folder = true
        @isDecision = true

    dragEnter: (data) =>
        if data.otherNode.data.act.isActivities then ["over"] else false

    accept: (visitor, node) ->
        visitor.visitDecision(node)

    typeStr: -> Decision.typeString


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
        super
        @children = ActivityFactory.create(data.activity).children
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

    visitExit: (node) ->
        @display(node.title)

    visitLoop: (node) ->
        @display(node.title)
        @visitChildren(node)

    visitSignal: (node) ->
        @display(node.title)

    visitSubprocess: (node) ->
        @display(node.title)

