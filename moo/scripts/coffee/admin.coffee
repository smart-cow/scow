
$ -> ko.applyBindings(new AdminViewModel())

class AdminViewModel
    constructor: ->
        @runningWorkflows = ko.observableArray()
        @runningTypes = ko.computed =>
            names = (id.leftOf(".") for id in @runningWorkflows())
            return names.unique()
        @selectedRunningWorkflow = ko.observable()
        @selectedType = ko.observable()

        @update()

    update: =>
        @runningWorkflows.removeAll()
        COW.activeWorkflows (ids) =>
            @runningWorkflows.push(id) for id in ids
        @selectedRunningWorkflow()
        @selectedType()

    deleteWorkflow: =>
        idNum = @selectedRunningWorkflow().rightOf(".")
        COW.cowRequest("processInstances/#{idNum}", "delete").done =>
            alert("#{@selectedRunningWorkflow()} deleted")
            @update()

    deleteWorkflowType: =>
        COW.deleteRunningInstances(@selectedType()).done =>
            alert("All instances of #{@selectedType()} deleted")
            @update()

    deleteAll: =>
        $.when(COW.deleteRunningInstances(t) for t in @runningTypes()).done =>
            @update()
            alert("All Workflows Deleted")
