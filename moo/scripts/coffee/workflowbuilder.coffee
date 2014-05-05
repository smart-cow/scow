
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
        dto = new WorkflowDto(@tree).getDto()
        console.log(dto)
        $("#debug").text(JSON.stringify(dto))
        COW.cowRequest("processes", "post", dto).done (d) ->
            console.log("done")
            console.log(d)

#        COW.cowRequest("processes/#{dto.key}", "put", dto).done (d) ->
#            console.log("done")
#            console.log(d)

#        COW.cowRequest("processes", "post", @data).done (d) ->
#            console.log("done")
#            console.log(d)


createDisplay = (label, value, inputType = "text") ->
    label: ko.observable(label + ":")
    value: ko.observable(value)
    inputType: ko.observable(inputType)





class WorkflowDto
    constructor: (tree) ->
        workflowRoot = tree.rootNode.children[0]
        @visit(workflowRoot)

    getDto: => @dto

    visit: (node) ->
        node.data.self.accept(@, node)

    visitWorkflow: (node) ->
        @dto = @attrsToDto(node)
        children = @visit(node.children[0])
        @dto.activity = children
        return @dto

    visitActivities: (node) ->
        valueDto = @attrsToDto(node)
        children = (@visit(child) for child in node.children)
        valueDto.activity = children
        return @activityDto(node, valueDto)


    visitHumanTask: (node) -> @visitNoChildren(node)

    visitServiceTask: (node) -> @visitNoChildren(node)

    visitScript: (node) -> @visitNoChildren(node)

    visitDecision: (node) -> console.log(node)

    visitExit: (node) -> @visitNoChildren(node)

    visitLoop: (node) -> console.log(node)

    visitSignal: (node) -> @visitNoChildren(node)

    visitSubprocess: (node) -> @visitNoChildren(node)


    visitNoChildren: (node) ->
        valueDto = @attrsToDto(node)
        return @activityDto(node, valueDto)


    attrsToDto: (node) ->
        attrs = ko.mapping.toJS(node.data.self.attributes())
        dto = { }
        for attr in attrs when attr.value?
            dto[attr.key] = attr.value
        return dto

    activityDto: (node, valueObj) ->
        declaredType: node.data.self.typeStr()
        value: valueObj





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
        @key = @data.key
        @icon = "Icon_Task.png"
        @draggable = true
        @expanded = true
        @self = @
        @isDecision = false
        @isActivities = false
        @folder = false

        @attributes = ko.observableArray()
        @setAttr("Name", "name")
        @setAttr("Description", "description")
        @setAttr("Bypassable", "bypassable", "checkbox")
        @setAttr("Key", "key")

    dragEnter: =>
        if @folder
            return ["over"]
        return ["before", "after"]

    setAttr: (label, key, inputType = "text") =>
        @attributes.push
            label: ko.observable(label)
            key: key
            value: ko.observable(@data[key])
            inputType: ko.observable(inputType)



class Workflow extends Activity
    constructor: (@data) ->
        @key = @data.key
        @title = "<span class='glyphicon glyphicon-list-alt'></span> #{@key}"
        @children = [ ActivityFactory.create(@data.activity) ]
        @draggable = false
        @folder = true
        @expanded = true
        @self = @
        @attributes = ko.observableArray()
        @setAttr("Name", "name")
        @setAttr("Key", "key")

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

        @setAttr("Sequential", "sequential", "checkbox")



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
        @setAttr("Assignee", "assignee")
        @setAttr("Candidate users", "candidateUsers")
        @setAttr("Candidate groups", "candidateGroups")

    accept: (visitor, node) ->
        visitor.visitHumanTask(node)

    typeStr: -> HumanTask.typeString



class ServiceTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.ServiceTask"

    constructor: (@data) ->
        super(@data)
        @icon = "Icon_ServiceTask.png"

        @setAttr("Method", "method")
        @setAttr("URL", "url")
        @setAttr("Content", "content")
        @setAttr("Content type", "contentType")
        @setAttr("Var", "var")
        @setAttr("Result selector XPath", "resultSelectorXPath")


    accept: (visitor, node) ->
        visitor.visitServiceTask(node)

    typeStr: -> ServiceTask.typeString



class ScriptTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Script"

    constructor: (data) ->
        super
        @icon = "Icon_Script.png"

        @setAttr("Content", "content")

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


