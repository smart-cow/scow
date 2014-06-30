# CoffeeScript

$ -> ko.applyBindings new TasksViewModel()

# Knockout JS to observable mapping options
TASK_MAPPING =
    variables:
        create: (options) ->
            if options.data?
                # sometimes it is "variable", other times it is "variables"
                ko.mapping.fromJS(options.data.variable ? options.data.variables)
            else
                ko.observableArray()
        update: (options) ->
            if options.data?
                ko.mapping.fromJS(options.data.variable ? options.data.variables)
            else
                options.target



# Root object used by Knockout
class TasksViewModel
    constructor: ->
        # The tasks the user can take or are assigned to them
        @activeTasks = ko.observableArray()

        @showHistory = ko.observable(false)
        @historyTasks = ko.observableArray()

        # Holds the task that the user last clicked on, and will show up in the modal
        @selectedTask = ko.observable()

        # Currently logged in user's user name
        @username = ko.observable()
        @setComputed()

        # Get username first so we know which username to use in the ajax calls for tasks
        COW.cowRequest("whoami").done (data) =>
            @username(data.id)
            @refreshAllTasks()
            # Right now this subscribes to all task messages, we may want to filter on
            # username and group eventually
            COW.amqpSubscribe("#.tasks.#", @createOrUpdateTask)
            # Establish amqp connection when the page is loaded
            COW.amqpConnect()

    setComputed: =>
        # Tasks from @activeTasks that are assigned to the user
        @assignedTasks = ko.computed =>
            @activeTasks().filter (t) =>
                t.canCompleteTask() and t.assignee() is @username()

        #  Tasks from self.activeTasks that the user could take
        @availableTasks = ko.computed =>
            @activeTasks().filter (t) =>
                t.state() is "Ready"


    userCanCompleteOrTakeTask: (task) =>
        # Can't complete a task that is already completed
        return false if task.state() is "Completed"
        # User can complete task if it hasn't been assigned to someone
        return true unless task.assignee()?
        # If the task is assigned to the user
        return task.assignee() is @username()



    # Creates/updates task using task data from either the ajax calls or amqp notfications
    createOrUpdateTask: (newTaskData) =>
        # check if task exists
        task = @activeTasks().first (t) => t.id() is newTaskData.id

        # update existing task
        if task?
            @mapTaskToKoTask(newTaskData, task)
            # Remove tasks that are completed or have been assigned to someone else
            @activeTasks.remove (t) => not @userCanCompleteOrTakeTask(task)
        # create new task
        else
            task = @mapTaskToKoTask(newTaskData)
            # Add the new task if user can take or complete it
            @activeTasks.push(task) if @userCanCompleteOrTakeTask(task)


        # AMQP doesn"t send notifications about the task history, so if a task was created
        # get the task with ajax.
        @updateHistoryTasks() if task.state() is "Completed"


    # This function will convert the task data received from amqp or cow-server, to the observable
    # If updateTarget is provided, it will update the existing task, else create a new one
    mapTaskToKoTask: (newTaskData, updateTarget) =>
        # Sometimes "outcomes" is used and other times it is "outcome"
        if newTaskData.outcome?
            newTaskData.outcomes = newTaskData.outcome
            delete newTaskData.outcome

        if updateTarget?
            ko.mapping.fromJS(newTaskData, TASK_MAPPING, updateTarget)
        else
            new Task(newTaskData)


    # Use ajax to load tasks. This is mainly just used when the page first loads because
    # it will use amqp to stay in sync.
    # Can also be used to explicitly sync with the server
    updateAssignedTasks: =>
        @updateTaskList("assignee")

    updateAvailableTasks: =>
        @updateTaskList("candidate")

    # Use cowRequest method to make the CORS ajax request
    updateTaskList: (queryStringKey) =>
        url = "tasks?" + queryStringKey + "=" + @username()
        COW.cowRequest(url).done (data) =>
            @createOrUpdateTask(t) for t in data.task


    updateHistoryTasks: =>
        return unless @showHistory()

        year = new Date().getFullYear()
        queryString = "?" + $.param
            assignee: @username()
            start: (year - 1) + "-1-1"
            end: (year + 1) + "-1-1"
        COW.cowRequest("tasks/history#{queryString}").done (data) =>
            @historyTasks data.historyTask


    toggleHistory: =>
        @showHistory(not @showHistory())
        if @showHistory()
            @updateHistoryTasks()
        else
            @historyTasks.removeAll()

    #Explicitly sync all tasks with cow server.
    refreshAllTasks: =>
        @updateAssignedTasks()
        @updateAvailableTasks()
        @updateHistoryTasks()

    # Called when a user clicks on a task from one of the tables
    showTask: (task) =>
        @selectedTask(task)


    # Called when a user clicks Assign to me. Use ajax to assign task to user on cow-server.
    takeTask:(task) =>
        # url format: tasks/{id}/task?assignee={username}
        url = "tasks/#{ task.id() }/take?assignee=#{ @username() }"
        COW.cowRequest(url, "post").done (data) =>
            # cow-server returns the updated task
            @createOrUpdateTask(data)
            # Wait till after the ajax call completes to close the modal, so the user doesn"t
            # see the table until it has been updated.
            $("#taskInfoModal").modal("hide")

    # Called when a user clicks complete. Use ajax to set the task as completed.
    completeTask:(task) =>
        outcomeSelected = task.selectedOutcome()? or
                          task.outcomes().length is 0
        if not outcomeSelected
            # show error if user needs to select on outcome
            $("#outcomes-form").addClass("has-error")
            $("#outcomes-form .has-error").removeClass("hidden")
            return

        queryString = @buildCompleteTaskQueryString(task)
        url = "tasks/" + task.id() + queryString
        COW.cowRequest(url, "delete").done =>
            # Since the call was successful the task no longer belongs in activeTasks
            @activeTasks.remove(task)
            # Wait till after the ajax call completes to close the modal, so the user doesn"t
            # see the table until it has been updated.
            $("#taskInfoModal").modal("hide")


    # Called when a user clicks Assign to me. Use ajax to assign task to user on cow-server.
    takeSelectedTask: =>
        # url format: tasks/{id}/task?assignee={username}
        url = "tasks/#{ @selectedTask().id() }/take?assignee=#{ @username() }"
        COW.cowRequest(url, "post").done (data) =>
            # cow-server returns the updated task
            @createOrUpdateTask(data)
            # Wait till after the ajax call completes to close the modal, so the user doesn"t
            # see the table until it has been updated.
            $("#taskInfoModal").modal("hide")

    # Called when a user clicks complete. Use ajax to set the task as completed.
    completeSelectedTask: =>
        outcomeSelected = @selectedTask().selectedOutcome()? or
                          @selectedTask().outcomes().length is 0
        if not outcomeSelected
            # show error if user needs to select on outcome
            $("#outcomes-form").addClass("has-error")
            $("#outcomes-form .has-error").removeClass("hidden")
            return

        queryString = @buildCompleteTaskQueryString(@selectedTask())
        url = "tasks/" + @selectedTask().id() + queryString
        COW.cowRequest(url, "delete").done =>
            # Since the call was successful the task no longer belongs in activeTasks
            @activeTasks.remove(@selectedTask())
            # Wait till after the ajax call completes to close the modal, so the user doesn"t
            # see the table until it has been updated.
            $("#taskInfoModal").modal("hide")


    buildCompleteTaskQueryString: (task) =>
        qsBuilder = []
        for v in task.variables()
            encoded = $.param(var: "#{v.name()}:#{v.value()}")
            qsBuilder.push(encoded)

        if task.selectedOutcome()
            encoded = $.param(outcome: task.selectedOutcome())
            qsBuilder.push(encoded)

        if qsBuilder.length > 0
            return "?" + qsBuilder.join("&")
        else
            return ""


    encodeVariables: (variables) =>
        return null if variables.length is 0
        varPairs = ("var=#{v.name()}:#{v.value()}" for v in variables)
        varPairs.join("&")



# Class to represent a human task. The properties are converted to observables
# so the ui will stay in sync with the data.
class Task
    constructor: (newTaskData) ->
        # Add the properties of newTaskData to this as observables
        ko.mapping.fromJS(newTaskData, TASK_MAPPING, @)

        @variables ?= ko.observableArray()
        @outcomes ?= ko.observableArray()

        @selectedOutcome = ko.observable()
        @selectedOutcome(@outcomes()[0]) if @outcomes().length is 1

        @setComputed()


    setComputed: =>
        @canAssignTask = ko.computed => not @assignee()?

        @canCompleteTask = ko.computed => @state() is "Reserved"

        # Find out if any variable names have been duplicated
        @hasVariableConflicts = COW.hasVariableConflicts(@variables)
        # Find out if a specific variable"s name has been duplicated
        @variableHasConflict = COW.variableHasConflict(@variables)

    addVariable: () =>
        @variables.push
            name: ko.observable()
            value: ko.observable()

    removeVariable: (variable) =>
        @variables.remove(variable)
