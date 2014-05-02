
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
        @loadWorkflow("SvcAndScript")
        @selectedActivity = ko.observable()


    loadWorkflow: (workflowName) =>
        COW.cowRequest("processes/#{workflowName}").done (data) =>
            @workflow(new Workflow(data))
            @configTree(@workflow())


    configTree: (workflow) =>
        $("#test-tree").fancytree
            extensions: ["dnd"]
            debugLevel: 2
            source: [workflow]
            imagePath: "images/"
            icons: false
            dnd: dndOptions
            click: (event, data) => @selectedActivity(data.node.data.self)


createDisplay = (label, value, inputType = "text") ->
    label: ko.observable(label + ":")
    value: ko.observable(value)
    inputType: ko.observable(inputType)


ACTIVITY_TYPE_MAP =
    "org.wiredwidgets.cow.server.api.model.v2.Activities" : (data) -> new Activities(data)
    "org.wiredwidgets.cow.server.api.model.v2.Task" : (data) -> new HumanTask(data)
    "org.wiredwidgets.cow.server.api.model.v2.ServiceTask" : (data) -> new ServiceTask(data)
    "org.wiredwidgets.cow.server.api.model.v2.Script" : (data) -> new ScriptTask(data)
    "org.wiredwidgets.cow.server.api.model.v2.Decision": (data) -> new Decision(data)
    "org.wiredwidgets.cow.server.api.model.v2.Exit": (data) -> new Exit(data)
    "org.wiredwidgets.cow.server.api.model.v2.Loop": (data) -> new Loop(data)
    "org.wiredwidgets.cow.server.api.model.v2.Signal": (data) -> new Signal(data)
    "org.wiredwidgets.cow.server.api.model.v2.SubProcess": (data) -> new Subprocess(data)

getActivity = (data) ->
    ACTIVITY_TYPE_MAP[data.declaredType](data.value)




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

        @attributes = ko.observableArray()
        @setAttr("Name", "name")
        @setAttr("Description", "description")
        @setAttr("Bypassable", "bypassable", "checkbox")

    dragEnter: =>
        if @folder
            return ["over"]
        return ["before", "after"]

    setAttr: (label, key, inputType = "text") =>
        @attributes.push
            label: ko.observable(label)
            value: ko.observable(@data[key])
            inputType: ko.observable(inputType)


class Workflow extends Activity
    constructor: (wflowData) ->
        @key = wflowData.key
        @title = "<span class='glyphicon glyphicon-list-alt'></span> #{@key}"
        @children = [ getActivity(wflowData.activity) ]
        @draggable = false
        @folder = true
        @expanded = true
        @self = @
        @attributes = ko.observableArray()

    dragEnter: -> false


class Activities extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Activities"

    constructor: (@data) ->
        super(@data)
        @isSequential = @data.sequential
        if @data.name?
            @title = @data.name
        else
            @title = if @isSequential then "List" else "Parallel List"
        @children = (getActivity(d) for d in @data.activity)
        @icon = "Icon_List.png"
        @folder = true
        @isActivities = true

        @setAttr("Sequential", "sequential", "checkbox")


    dragEnter: (data) =>
        if data.node.getParent()?.data.self.isDecision and data.otherNode.data.self.isActivities
            return ["over", "after", "before"]
        else
            return ["over"]


class HumanTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Task"

    constructor: (@data) ->
        super(@data)
        @setAttr("Assignee", "assignee")
        @setAttr("Candidate users", "candidateUsers")
        @setAttr("Candidate groups", "candidateGroups")


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


class ScriptTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Script"

    constructor: (data) ->
        super
        @icon = "Icon_Script.png"

        @setAttr("Content", "content")


class Decision extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Decision"

    constructor: (data) ->
        super
        @children = (getActivity(option.activity) for option in data.option)
        c.icon = "Icon_Decision_Arrow.png" for c in @children
        @icon = "Icon_Decision.png"
        @folder = true
        @isDecision = true

    dragEnter: (data) =>
        if data.otherNode.data.self.isActivities then ["over"] else false



class Exit extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Exit"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Exit.png"


class Signal extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Signal"

    constructor: (data) ->
        super(data)
        @icon = "Icon_Signal.png"


class Subprocess extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.SubProcess"

    constructor: (data) ->
        super(data)
        @icon = "Icon_SubProcess.png"


class Loop extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Loop"

    constructor: (data) ->
        super
        @children = getActivity(data.activity).children
        @icon = "Icon_Loop.png"
        @folder = true

