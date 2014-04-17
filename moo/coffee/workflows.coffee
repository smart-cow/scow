﻿# CoffeeScript

class WorkflowsViewModel
    constructor: ->
        @workflows = ko.observableArray()
        # When @lastLoadedWorkflow has a value,the green alert box shows up
        @lastLoadedWorkflow = ko.observable()
        @selectedWorkflow = ko.observable()
        @selectedWorkflowVariables = ko.observableArray()

        @hasVariableConflicts = COW.hasVariableConflicts @selectedWorkflowVariables
        @variableHasConflict = COW.variableHasConflict @selectedWorkflowVariables

        @loadWorkflows()


    # Build and send ajax request to start the workflow
    startWorkflow: =>
        requestBody = processDefinitionKey: @selectedWorkflow()
        @insertVariables requestBody
        COW.cowRequest("/processInstances", "post", requestBody).done (data) =>
            @lastLoadedWorkflow data.key
            $("#variables-modal").modal "hide"

    # Encode variables in the weird way required by the server's Json serializer
    insertVariables: (data) =>
        return if @selectedWorkflowVariables().length < 1
        varList = (name: v.name(), value: v.value() for v in @selectedWorkflowVariables())
        data.variables = variable: varList

    # Convert process level variables to observables to allow the user to edit them before
    # starting the process
    loadWorkflowVars:  (variables) =>
        for v in variables
            @selectedWorkflowVariables.push
                name: ko.observable v.name
                value: ko.observable v.value

    # Called when a user clicks on workflow from the table
    workflowSelected: (workflow) =>
        @selectedWorkflow workflow
        # Show modal to allow user to enter values for process variables
        $("#variables-modal").modal "show"
        # Get process level variables for the selected workflow
        COW.cowRequest("processes/#{ workflow }").done (data) =>
            @selectedWorkflowVariables.removeAll()
            # Null checks to account for serializer
            vars = data.variables?.variable
            @loadWorkflowVars vars if vars?.length > 0

    loadWorkflows: =>
        COW.cowRequest("processDefinitions").done (data) =>
            @workflows.push pd.key for pd in data.processDefinition
            @workflows.sort @caseInsensitiveSort

    removeVariable: (variable) =>
        @selectedWorkflowVariables.remove variable

    addVariable: =>
        @selectedWorkflowVariables.push
            name: ko.observable()
            value: ko.observable()


    caseInsensitiveSort: (left, right) =>
        leftLower = left.toLowerCase()
        rightLower = right.toLowerCase()
        if leftLower < rightLower
            -1
        else if leftLower > rightLower
            1
        else
            0


$ -> ko.applyBindings new WorkflowsViewModel()