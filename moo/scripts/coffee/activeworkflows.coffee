
$ -> ko.applyBindings new ActiveWorkflowsViewModel()


class ActiveWorkflowsViewModel
    constructor: ->
        @workflows = ko.observableArray()
        @tableHeadings = ko.observableArray(["Workflow"])
        @loadAllWorkflows()
        COW.amqpSubscribe("#.tasks.#", @onAmqpReceive)
        COW.amqpConnect()


    createOrUpdateWorkflow: (newWflowData) =>
        workflow = @workflows().first (w) -> newWflowData is w.id

        if workflow?
            workflow.updateStatuses(newWflowData.statusSummary)
        else
            workflow = new Workflow(newWflowData, @tableHeadings)
            @workflows.push(workflow)
        @updateTableHeadings(newWflowData.statusSummary)


    loadWorkflow: (id) =>
        COW.cowRequest("processInstances/#{id}/status").done (data) =>
            @createOrUpdateWorkflow(data)

    loadAllWorkflows: =>
        COW.activeWorkflowIds (ids) =>
            @loadWorkflow(id) for id in ids

    # Reloads whichever workflow the task is associated with
    onAmqpReceive: (task) =>
        @loadWorkflow(task.processInstanceId.rightOf("."))

    updateTableHeadings: (statuses) =>
        @tableHeadings.push(s.name) for s in statuses when s.name not in @tableHeadings()

    selectRow: (item) =>
        item.isSelected(not item.isSelected())


# Holds the information for one row in the table.
# Requires a reference to tableHeadings so it can return the values in the correct order.
class Workflow
    constructor: (wflowData, @tableHeadings) ->
        @id = wflowData.id
        @isSelected = ko.observable(false)
        @statuses = ko.observableArray([ name: "Workflow", status: ko.observable(@id) ])
        @updateStatuses(wflowData.statusSummary)

        @setComputed()

    setComputed: =>
        @columnValues = ko.computed =>
            @getStatus(heading)?.status for heading in @tableHeadings()

    getStatus: (name) =>
        @statuses().first (s) => name is s.name

    updateStatuses: (newStatuses) =>
        # Build map of new higer priority statuses for each name
        newStatusesMap = {}
        for s in newStatuses
            existingStatus = newStatusesMap[s.name]
            newStatusesMap[s.name] = @getHigherPriorityStatus(s.status, existingStatus)

        for name, status of newStatusesMap
            existingStatus = @getStatus(name)
            if existingStatus?
                existingStatus.status(status)
            else
                @statuses.push
                    name: name,
                    status: ko.observable(status)


    statusPriority = [
        "precluded"
        "completed"
        "contingent"
        "planned"
        "notStarted"
        "open"
    ]
    getHigherPriorityStatus: (status1, status2) =>
        index1 = statusPriority.indexOf(status1)
        index2 = statusPriority.indexOf(status2)
        if index1 > index2 then status1 else status2









