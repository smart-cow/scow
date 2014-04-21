

$ ->
    ko.applyBindings(new WorkflowBuilderViewModel())



class WorkflowBuilderViewModel
    constructor: ->
        @loadWorkflow("SvcAndScript")
        @loadWorkflow("denim_test")
        @loadWorkflow("BrianTest")
        @loadWorkflow("exit-test")
        @loadWorkflow("LoopTest")
        @loadWorkflow("complicated")
        @loadWorkflow("signal")
        @loadWorkflow("subproctest")

        @workflows = ko.observableArray()


    loadWorkflow: (workflowName) =>
        COW.cowRequest("processes/#{workflowName}").done (data) =>
            @workflows.push(new Workflow(data))
            $(".wflowTree").dynatree
                imagePath: "images/"

    templateName: (activity) =>
        activity.templateName

    formSubmit: =>
        name = $("#newWorkflowName").val()
        @loadWorkflow(name)
        $("#newWorkflowName").val("")


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


class Workflow
    constructor: (wflowData) ->
        @key = wflowData.key
        @activities =  getActivity(wflowData.activity)



# Base class for all activities
class Activity
    constructor: (data) ->
        @name = data.name ? @.constructor.name
        @key = data.key
        @templateName = "task-template"
        @icon = "icon: 'Icon_Task.png'"



class Activities extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Activities"


    constructor: (activitiesData) ->
        super
        @isSequential = activitiesData.sequential
        @children = (getActivity(data) for data in activitiesData.activity)
        @templateName = "activities-template"
        @icon = "icon: 'Icon_List.png'"


class HumanTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Task"

    constructor: (data) ->
        super


class ServiceTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.ServiceTask"

    constructor: (data) ->
        super
        @icon = "icon: 'Icon_ServiceTask.png'"


class ScriptTask extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Script"

    constructor: (data) ->
        super
        @icon = "icon: 'Icon_Script.png'"


class Decision extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Decision"

    constructor: (data) ->
        super
        @options = (getActivity(option.activity) for option in data.option)
        o.icon = "icon: 'Icon_Decision_Arrow.png'" for o in @options
        @templateName = "option-template"
        @icon = "icon: 'Icon_Decision.png'"


class Exit extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Exit"

    constructor: (data) ->
        super
        @icon = "icon: 'Icon_Exit.png'"


class Signal extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Signal"

    constructor: (data) ->
        super
        @icon = "icon: 'Icon_Signal.png'"


class Subprocess extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.SubProcess"

    constructor: (data) ->
        super
        @icon = "icon: 'Icon_SubProcess.png'"


class Loop extends Activity
    @typeString = "org.wiredwidgets.cow.server.api.model.v2.Loop"

    constructor: (data) ->
        super
        @templateName = "activities-template"
        @children = getActivity(data.activity).children
        @icon = "icon: 'Icon_Loop.png'"

