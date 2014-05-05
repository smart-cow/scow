
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
        target.data.self.dragEnter(data)

    dragDrop: (target, data) ->
        if data.hitMode is "over"
            data.otherNode.moveTo(target.getFirstChild(), "before")
        else
            data.otherNode.moveTo(target, data.hitMode)


class WorkflowBuilderViewModel
    constructor: ->
        @workflow = ko.observable()
        #@loadWorkflow("BrianTempSvc")
        @loadWorkflow("v2-simple")
        #@loadWorkflow("complicated")
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
            click: (event, data) => @selectedActivity(data.node.data.self)
        @tree = $("#tree").fancytree("getTree")

    prettyPrint: =>
        new PrettyPrintVisitor(@tree)

    save: =>
        converter = new WorkflowXmlConverter(@tree)
        xml = converter.getXml()
        COW.xmlRequest("processes/#{converter.key}", "put", xml).done (d) ->
            alert("xml saved")



createDisplay = (label, value, inputType = "text") ->
    label: ko.observable(label + ":")
    value: ko.observable(value)
    inputType: ko.observable(inputType)





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
        node.data.self.accept(@, node)

    visitWorkflow: (node) =>
        # Should have only one root node
        workflow = node.data.self

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
        activities = node.data.self
        xmlActivities = $("<activities />").appendTo(@xmlPosition)

        xmlActivities.attr("sequential", activities.isSequential)

        after = @moveXmlPosition(@, xmlActivities)
        @visit(child) for child in node.children
        after();



    visitHumanTask: (node) =>
        task = node.data.self
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
        $("<#{tag}>#{content}</#{tag}>").appendTo(target)



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
        node.data.self.accept(@, node)

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



# Base class for all activities
class Activity
    constructor: (@data) ->
        @title = @data.name ? @.constructor.name
        @name = @title
        @key = @data.key
        @icon = "Icon_Task.png"
        @draggable = true
        @expanded = true
        @self = @
        @isDecision = false
        @isActivities = false
        @folder = false
        @description = @data.description


    dragEnter: =>
        if @folder
            return ["over"]
        return ["before", "after"]





class Workflow extends Activity
    constructor: (@data) ->
        @key = @data.key
        @title = "<span class='glyphicon glyphicon-list-alt'></span> #{@key}"
        @children = [ ActivityFactory.create(@data.activity) ]
        @draggable = false
        @folder = true
        @expanded = true
        @self = @


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


    dragEnter: (data) =>
        if data.node.getParent()?.data.self.isDecision and data.otherNode.data.self.isActivities
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


    accept: (visitor, node) ->
        visitor.visitHumanTask(node)

    typeStr: -> HumanTask.typeString



class ServiceTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.ServiceTask"

    constructor: (@data) ->
        super(@data)
        @icon = "Icon_ServiceTask.png"



    accept: (visitor, node) ->
        visitor.visitServiceTask(node)

    typeStr: -> ServiceTask.typeString



class ScriptTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Script"

    constructor: (data) ->
        super
        @icon = "Icon_Script.png"


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
        if data.otherNode.data.self.isActivities then ["over"] else false

    accept: (visitor, node) ->
        visitor.visitDecision(node)

    typeStr: -> Decision.typeString


class Exit extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Exit"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Exit.png"

    accept: (visitor, node) ->
        visitor.visitExit(node)


    typeStr: -> Exit.typeString



class Signal extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Signal"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Signal.png"

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


