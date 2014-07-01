
ALL_HIT_TYPES = ["over", "after", "before"]
NON_FOLDER_HIT_TYPES = ["before", "after"]
OVER_HIT_TYPE = ["over"]

###
usedTreeKeys = { }

getUniqKey = (preferredName) ->

    possibleName = preferredName
    suffix = 0
    while usedTreeKeys[possibleName]
        suffix += 1
        possibleName = preferredName + suffix

    usedTreeKeys[possibleName] = true
    return possibleName
###


usedTreeKeys = { }


getUniqKey = (preferredName) ->
    possibleName = preferredName
    suffix = 1
    while usedTreeKeys[possibleName]
        suffix += 1
        possibleName = preferredName + suffix

    usedTreeKeys[possibleName] = true
    return possibleName



# Base class for all activities
class Activity
    icon: "Icon_Task.png"
    displayName: "Error: this is abstract"
    isDecision: false
    isActivities: false
    isOption: false
    folder: false

    # Set default values
    constructor: (@data) ->
        @key = getUniqKey(@data?.name ? @.constructor.name)
        @setTitle(@key)

        @draggable = true
        @expanded = true
        # Add reference to self so it can be accessed from fancytree nodes
        @act = @
        @variables = ko.observableArray()
        @readVariables()

        @apiAttributes = ko.observableArray()
        nameAttr = @addAttr("name", "Name", true)
        unless nameAttr.value()
            nameAttr.value(@title)
        nameAttr.value.subscribe (newVal) =>
            @setTitle(newVal)

        @addAttr("description", "Description")
#        @addInvisibleAttr("key", true)
        @addAttr("bypassable", "Bypassable", true, "checkbox")


    dragEnter: (treeData) =>
        if @otherIsOption(treeData)
            # Options can only be dropped on decisions
            if @isDecision
                return OVER_HIT_TYPE
            else
                return false
        if @folder
            return OVER_HIT_TYPE
        else
            return NON_FOLDER_HIT_TYPES


    dragDrop: (treeData) =>
        if treeData.otherNode?
            @dragDropExistingNode(treeData)
        else
            @dragDropNewActivity(treeData)

    dragDropExistingNode: (treeData) =>
        target = treeData.node
        if treeData.hitMode isnt "over"
            # not dropped into another node
            treeData.otherNode.moveTo(target, treeData.hitMode)
            return

        # Can only drop over folder nodes
        return null unless @folder
        targetChild = target.getFirstChild()
        if targetChild?
            # when target has a child place otherNode before the first child
            treeData.otherNode.moveTo(targetChild, "before")
        else
            # When using move to with "child", the added node becomes the last child of target.
            # In this case target is empty so first and last are the same
            treeData.otherNode.moveTo(target, "child", -> target.setExpanded(true))

    dragDropNewActivity: (treeData) =>
        newActivity = ActivityFactory.createFromTreeData(treeData)
        if newActivity?
            treeData.node.addNode([newActivity], treeData.hitMode)

    findTreeNode: =>
        try
            $("#tree").fancytree("getTree")?.getNodeByKey(@key)


    setTitle: (newTitle) =>
        @title = newTitle
        @findTreeNode()?.setTitle(@title)


    otherIsActivities: (treeData) ->
        otherNode = treeData.otherNode
        if otherNode?
            return otherNode.data.act.isActivities
        droppedType = ActivityFactory.typeFromTreeData(treeData)
        return droppedType?::isActivities

    otherIsOption: (treeData) ->
        otherNode = treeData.otherNode
        if otherNode?
            return otherNode.data.act.isOption
        droppedType = ActivityFactory.typeFromTreeData(treeData)
        return droppedType?::isOption

    # Add to the set of attributes that can be edited and included in the xml
    addAttr: (key, label, isXmlAttribute = false, inputType = "text") =>
        newAttribute =
            key: key
            value: ko.observable(@data?[key])
            label: label
            isXmlAttribute: isXmlAttribute
            inputType: inputType

        @apiAttributes.push(newAttribute)
        return newAttribute

    addInvisibleAttr: (key, isXmlAttribute = false) => @addAttr(key, null, isXmlAttribute)

    readVariables: =>
        varList = @data?.variables?.variable
        return unless varList?
        @variables.push(@createObservableVar(v)) for v in varList

    addVariable: =>
        @variables.push(@createObservableVar())

    removeVariable:(variable) =>
        matchingVar = @variables().first (v) -> v.name() is variable.name()
        @variables.remove(matchingVar)

    createObservableVar: (variable) ->
        name: ko.observable(variable?.name)
        value: ko.observable(variable?.value)
        required: ko.observable(variable?.required ? false)
        output: ko.observable(variable?.output ? true)





class Workflow extends Activity
    displayName: "Workflow"
    folder: true

    constructor: (@data, requestedName = null) ->
        super(@data)
        @syncNameDisplays(requestedName)
        if @data?
            @children = [ ActivityFactory.create(@data.activity) ]
        else
            @children = [ActivityFactory.createEmpty(Activities::typeString)]
        @draggable = false

        @addAttr("bypassAssignee", "Bypass Assignee")
        @addAttr("bypassCandidateUsers", "Bypass Candidate Users")
        @addAttr("bypassCandidateGroups", "Bypass Candidate Groups")

    setTitle: (newTitle) =>
        super("<span class='glyphicon glyphicon-list-alt'></span> #{newTitle}")

    syncNameDisplays: (requestedName) =>
        nameAttr = @apiAttributes().first (e) -> e.key == "name"
        nameAttr.value(@data?.name ? requestedName ? @.constructor.name)
        @name = ko.computed =>
            nameAttr.value()

    dragEnter: -> false

    accept: (visitor, node) ->
        visitor.visitWorkflow(node)






class Activities extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Activities"
    icon: "Icon_List.png"
    displayName: "List"
    isActivities: true
    folder: true

    constructor: (data) ->
        super(data)
        isSequential = if data? then data.sequential else true
        if not data?.name
            newTitle = if isSequential then "List" else "Parallel List"
            uniqTitle = getUniqKey(newTitle)
            nameAttr = @apiAttributes().first (e) -> e.key == "name"
            nameAttr.value(uniqTitle)


        childActivitiesData = @data?.activity
        if childActivitiesData
            @children = (ActivityFactory.create(d) for d in childActivitiesData)
        else
            @children = []

        @addAttr("sequential", "Is Sequential", true,  "checkbox").value(isSequential)
        @addAttr("mergeCondition", "Merge Condition", true)


    dragEnter: (treeData) =>
        if @otherIsOption(treeData)
            return false
        parent = treeData.node.getParent()?.data.act?
        if parent.isActivities or (parent?.isDecision and @otherIsActivities(treeData))
            return ALL_HIT_TYPES
        else
            return OVER_HIT_TYPE

    accept: (visitor, node) ->
        visitor.visitActivities(node)

    toOption: =>
        new Option(@data)




class HumanTask extends Activity
    typeString:  "org.wiredwidgets.cow.server.api.model.v2.Task"
    displayName: "Human Task"

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




class ServiceTask extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.ServiceTask"
    icon: "Icon_ServiceTask.png"
    displayName: "Service Task"


    constructor: (@data) ->
        super(@data)

        @addAttr("url", "URL")
        @addAttr("content", "Content")
        @addAttr("contentType", "Content type")
        @addAttr("var", "Result variable")


    accept: (visitor, node) -> visitor.visitServiceTask(node)




class ScriptTask extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Script"
    icon: "Icon_Script.png"
    displayName: "Script Task"

    constructor: (data) ->
        super(data)
        @addAttr("import", "Imports")
        @addAttr("content", "Content")


    accept: (visitor, node) ->
        visitor.visitScript(node)




class Decision extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Decision"
    icon: "Icon_Decision.png"
    displayName: "Decision"
    isDecision: true
    folder: true

    constructor: (data) ->
        super(data)
        optionsData = data?.option
        if optionsData
            @children = (new Option(opt) for opt in optionsData)
        else
            @children = []

        @task = new HumanTask(data?.task)



#    dragEnter: (treeData) =>
#        console.log("drag enter %o", treeData)
#        if @otherIsOption(treeData) or @otherIsActivities(treeData)
#            OVER_HIT_TYPE
#        else
#            false

    dragDrop: (treeData) =>
        otherActivity = treeData.otherNode?.data.act
        if otherActivity?
            if otherActivity.isOption
                @dragDropExistingNode(treeData)
            else if otherActivity.isActivities
                option = ActivityFactory.createEmpty(Option::typeString)
                option.children[0] = otherActivity
            return

        # Drop from a draggable
        droppedType = ActivityFactory.typeFromTreeData(treeData)
        if droppedType::typeString is Option::typeString
            @dragDropNewActivity(treeData)
            return
        # Wrap dropped in an option
        option = ActivityFactory.createEmpty(Option::typeString)
        if droppedType::typeString isnt Activities::typeString
            activities = option.children[0]
            droppedActivity = ActivityFactory.createEmpty(droppedType::typeString)
            activities.children.push(droppedActivity)
        treeData.node.addNode([option], treeData.hitMode)

    accept: (visitor, node) ->
        visitor.visitDecision(node)




class Option extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Option"
    icon: "Icon_Decision_Arrow.png"
    displayName: "Option"
    folder: true
    isOption: true

    constructor: (@data) ->
        super(@data)

        childActivitiesData = @data?.activity;
        if (childActivitiesData)
            @children = [ ActivityFactory.create(childActivitiesData) ]
        else
            @children = [ActivityFactory.createEmpty(Activities::typeString)]

    dragEnter: (treeData) =>
        if @otherIsOption(treeData) or @otherIsActivities(treeData)
            NON_FOLDER_HIT_TYPES

    accept: (visitor, node) ->
        visitor.visitOption(node)


class Exit extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Exit"
    icon: "Icon_Exit.png"
    displayName: "Exit"

    constructor: (data) ->
        super(data)
        @addAttr("state", "State", true)

    accept: (visitor, node) ->
        visitor.visitExit(node)





class Signal extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Signal"
    icon: "Icon_Signal.png"
    displayName: "Signal"

    constructor: (data) ->
        super(data)
        @addAttr("signalId", "Signal Id", true)

    accept: (visitor, node) ->
        visitor.visitSignal(node)




class Subprocess extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.SubProcess"
    icon: "Icon_SubProcess.png"
    displayName: "Subprocess"

    constructor: (data) ->
        super(data)

    accept: (visitor, node) ->
        visitor.visitSubprocess(node)



class Loop extends Activity
    typeString: "org.wiredwidgets.cow.server.api.model.v2.Loop"
    icon: "Icon_Loop.png"
    displayName: "Loop"
    folder: true

    constructor: (data) ->
        super(data)
        childData = @data?.activity
        if childData
            @children = [ ActivityFactory.create(childData) ]
        else
            @children = []
        @loopTask = new HumanTask(data?.loopTask)
        @addAttr("doneName", "Done name", true)
        @addAttr("repeatName", "Repeat name", true)
        @addAttr("executionCount", "Execution count", true)

    accept: (visitor, node) ->
        visitor.visitLoop(node)






class ActivityFactory
    @typeMap: {}
    @typeMap[HumanTask::typeString] = HumanTask
    @typeMap[Activities::typeString] = Activities 
    @typeMap[Loop::typeString] = Loop  
    @typeMap[Decision::typeString] = Decision    
    @typeMap[ServiceTask::typeString] = ServiceTask
    @typeMap[ScriptTask::typeString] = ScriptTask
    @typeMap[Signal::typeString] = Signal
    @typeMap[Exit::typeString] = Exit   
    
    @typeMap[Option::typeString] = Option

    @create: (cowData) ->
        new @typeMap[cowData.declaredType](cowData.value)

    @createEmpty: (typeName) ->
        new @typeMap[typeName]()

    @createFromTreeData: (treeData) ->
        type = @typeFromTreeData(treeData)
        if type
            new type()
        else
            null

    @draggableActivities: ->
        for key, val of @typeMap
            if val::displayName isnt "Option" #An Option Should not be explicitely added
                type: key
                name: val::displayName
                icon: "images/" + val::icon
            else #Needed to not push a null object

    @getType: (typeName) ->
        @typeMap[typeName]

    @typeNameFromTreeData: (treeData) ->
        treeData.draggable?.element?.data("component-type")

    @typeFromTreeData: (treeData) ->
        return @getType(@typeNameFromTreeData(treeData))

    @createWorkflow: (cowData = null) ->
        new Workflow(cowData)

    @createEmptyWorkflow: (name = "Workflow") ->
        new Workflow(null, name)


window.ACT_FACTORY = ActivityFactory
