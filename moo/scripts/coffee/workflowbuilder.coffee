
$ ->
    ko.applyBindings(new WorkflowBuilderViewModel())
    # TODO: Set proper dragable behavior
    $(".draggable").draggable
        helper: "clone"
        cursorAt: {top: -5, left: -5}
        connectToFancytree: true


#getWorkflows = ->
#  $.getJSON("data/workflows.json")


# Fancytree drag and drop configuration
dndOptions =
    autoExpandMS: 100
    preventVoidMoves: true
    preventRecursiveMoves: true

    # Fires when you start dragging something already in the tree
    # If true allow the item to be dragged
    dragStart: (target) -> target.data.draggable

    # Return valid hit modes
    dragEnter: (target, data) ->
        target.data.act.dragEnter(data)

    # Called when the drop occurs
    dragDrop: (target, data) ->
        target.data.act.dragDrop(data)







class WorkflowBuilderViewModel
    constructor: ->
        @workflow = ko.observable() # Current workflow being displayed
        @conflictingInstances = ko.observableArray() # Holds list of running work flows preventing update
        @selectedActivity = ko.observable() # The last clicked on activity
        @workflowComponents = ACT_FACTORY.draggableActivities() # The list of draggables to display
        if window.location.hash is ""
            @createNewWorkflow()
        else
            @loadWorkflow(window.location.hash.substring(1))
#        @createNewWorkflow()

#        @loadWorkflow("BrianTempSvc")
#        @loadWorkflow("v2-simple")
#        @loadWorkflow("complicated")
#        @loadWorkflow("Denim_Decision")
#        @loadWorkflow("LoopTest")
#        @loadWorkflow("vars-test")


    loadWorkflow: (workflowName) =>
        COW.cowRequest("processes/#{workflowName}").done (data) =>
            @workflow(ACT_FACTORY.createWorkflow(data))
            @configTree(@workflow())

    createNewWorkflow: =>
        @workflow(ACT_FACTORY.createWorkflow())
        @configTree(@workflow())

    deleteRunningInstances: =>
        COW.deleteRunningInstances(@workflow().name()).done ->
            $("#conflicts-modal").modal("hide")
            $("#confirm-save-modal").modal("show")



    # Initialize fancy tree
    configTree: (workflow) =>
        $("#tree").fancytree
            extensions: ["dnd"] # Enable drag and drop
            debugLevel: 2
            source: [workflow] # Tree nodes to show
            imagePath: "images/" # Icon directory
            icons: false # Disable default node icons
            dnd: dndOptions
            click: (event, data) => @selectedActivity(data.node.data.act)
        @tree = $("#tree").fancytree("getTree")

    # Used to test tree visitor
    prettyPrint: => new PrettyPrintVisitor(@tree)


    save: =>
        converter = new WorkflowXmlConverter(@tree)
        xml = converter.getXml()
        console.log(xml)
        unless converter.hasAtLeaskOneTask
            alert("Workflow must have at least one task to save it")
            return
        COW.xmlRequest("processes/#{converter.name}", "put", xml)
            .always ->
                $("#confirm-save-modal").modal("hide")
            .done ->
                alert("Workflow saved")
            .fail (resp, ..., errorType) =>
                unless errorType is "Conflict"
                    alert("Error: #{errorType}")
                    return
                @conflictingInstances.removeAll()
                @conflictingInstances.push(pi.id) for pi in resp.responseJSON.processInstance
                $('#conflicts-modal').modal('show')







# cow-server doesn't accept json workflows, so we need to use xml
# The visitor pattern is used to build up the xml document
class WorkflowXmlConverter
    constructor: (tree) ->
        @hasAtLeaskOneTask = false
        # create a new xml document
        @xml = $($.parseXML(
              '<process xmlns="http://www.wiredwidgets.org/cow/server/schema/model-v2"></process>'))
        # Keeps track of the xml node that we are currently added children to
        @parentXml = @xml
        # tree root node only has one child, the workflow
        workflowRoot = tree.rootNode.children[0]
        @visit(workflowRoot)

    # Get non-jquery xml document
    getXml: -> @xml[0]

    # Delegate to correct visit* method
    visit: (node) ->
        node.data.act.accept(@, node)


    # Visits children of a folder element
    visitChildren: (nodeXml, nodeChildren) =>
        # Since node is a folder, set it to parentXml
        [oldXmlPosition, @parentXml] = [@parentXml, nodeXml]
        if nodeChildren?
            @visit(child) for child in nodeChildren
        # Reset the parentXml since we have already visited the child elements
        @parentXml = oldXmlPosition


    visitWorkflow: (node) =>
        @name = node.data.act.name()

        # Find process element at the root of the document
        process = $(@parentXml.find("process"))
        @addAttributesToNode(process, node.data.act.apiAttributes)
        @createVariablesElement(process, node.data.act.variables)

        # workflow should have only one child, activities
        @visitChildren(process, [node.children[0]])


    visitActivities: (node) =>
        xmlActivities = @createActivityElement("activities", node)
        @hasAtLeaskOneTask = node.children?.length > 0
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
        for attr in unwrappedAttributes when attr.value?
            # Handle properties stored as attributes
            if attr.isXmlAttribute
                xmlElement.attr(attr.key, attr.value)
            # Handle properties stored in the text of a tag
            else
                @createTextElement(xmlElement, attr.key, attr.value)


    createVariablesElement: (xmlElement, observableVars) ->
        variablesXml = @createTag("variables", xmlElement)
        unwrappedVars = ko.mapping.toJS(observableVars)
        for variable in unwrappedVars
            varXml = @createTag("variable", variablesXml)
            for own attrName, attrValue of variable
                varXml.attr(attrName, attrValue)






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
        @display(node.data.act.name())
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

