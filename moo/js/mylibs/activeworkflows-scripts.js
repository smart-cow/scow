/*global ko: false, COW: false, cowConfig: false*/

/*
Holds the information for one cell in the table.
*/
function Task(newTaskData) {
    var self = this;

    self.key = newTaskData.key;
    self.name = ko.observable(newTaskData.name);
    self.status = ko.observable(newTaskData.status);

    self.update = function (updatedTaskInfo) {
        self.name(updatedTaskInfo.name)
            .status(updatedTaskInfo.status);
    };
}


/*
Holds the information for one row in the table.
Requires a reference to tableHeadings so it can return the values in the correct order.
*/
function Workflow(wflowData, tableHeadings) {
    var self = this;

    self.id = wflowData.id;
    self.tasks = ko.observableArray();


    /*
    Returns the information for each cell in the row.
    It is a computed so it automatically updates when tableHeadings changes.
    */
    self.columnValues = ko.computed(function () {
        return $.map(tableHeadings(), function (heading, index) {
            // First column is the workflow name
            if (index === 0 && heading === "Workflow") {
                return self.id;
            }
            // Search for task that has a matching name
            var task = ko.utils.arrayFirst(self.tasks(), function (e) {
                return e.name() === heading;
            });
            if (task != null) {
                return task.status();
            }
            else {
                return "";
            }
        });
    });


    /*
    Not sure what to do if there is more than one entry for a user in a single row.
    For now it just chooses one that isn't completed
    */
    self.handleMultipleTasksForUser = function (tasks) {
        var tasksMap = {};
        var tasksToRemove = [];
        $.each(tasks, function (i, task) {
            // If we already saw this name, that means it is a duplicate
            if (tasksMap[task.name] != null) {
                if (task.status === "completed") {
                    tasksToRemove.push(task);
                }
                else {
                    tasksToRemove.push(tasksMap[task.name]);
                }
            }
            tasksMap[task.name] = task;
        });
        // Return tasks that weren't in tasksToRemove
        return ko.utils.arrayFilter(tasks, function (task) {
            return tasksToRemove.indexOf(task) < 0;
        });
    };


    /*
    Called when new task info comes in
    */
    self.updateTasks = function (newTasks) {
        newTasks = self.handleMultipleTasksForUser(newTasks);
        // Update or create tasks
        $.each(newTasks, function (i, newTask) {
            var existingTask = ko.utils.arrayFirst(self.tasks(), function (e) {
                return newTask.key === e.key;
            });
            if (existingTask != null) {
                existingTask.update(newTask);
            }
            else {
                self.tasks.push(new Task(newTask));
            }
        });

        // Remove tasks that aren't in newTasks
        self.tasks.remove(function (existingTask) {
            var newTaskData = ko.utils.arrayFirst(newTasks, function (e) {
                return e.key === existingTask.key;
            });
            return newTaskData == null;
        });
    };

    self.updateTasks(wflowData.statusSummary);
}


function ActiveWorkflowsViewModel() {
    var self = this;

    self.workflows = ko.observableArray();

    self.tableHeadings = ko.observableArray(["Workflow"]);

    /*
    Adds table headings that aren't already in self.tableHeadings.
    */
    self.updateTableHeadings = function (tasks) {
        $.each(tasks, function (i, task) {
            if (self.tableHeadings.indexOf(task.name) < 0) {
                self.tableHeadings.push(task.name);
            }
        });
    };


    self.createOrUpdateWorkflow = function (newWorkflowData) {
        var id = newWorkflowData.id;
        var workflow = ko.utils.arrayFirst(self.workflows(), function (e) {
            return e.id === id;
        });


        if (workflow != null) {
            workflow.updateTasks(newWorkflowData.statusSummary);
        }
        else {
            workflow = new Workflow(newWorkflowData, self.tableHeadings);
            self.workflows.push(workflow);
        }
        self.updateTableHeadings(newWorkflowData.statusSummary);
    };


    self.loadWorkflow = function (id) {
        var url = "processInstances/" + id + "/status";
        COW.cowRequest(url).done(function (data) {
            self.createOrUpdateWorkflow(data);
        });
    };


    /*
    Reloads whichever workflow the task is associated with
    */
    self.onAmqpReceive = function (task) {
        // Convert from wflowName.id to just id
        var dotPosition = task.processInstanceId.lastIndexOf(".");
        var idNumber = +task.processInstanceId.substr(dotPosition + 1);
        self.loadWorkflow(idNumber);
    };



    self.loadAllWorkflows = function () {
        COW.activeWorkflowIds(function (ids) {
            $.each(ids, function (index, id) {
                self.loadWorkflow(id);
            });
        });
    };

    self.init = function () {
        self.loadAllWorkflows();
        COW.amqpSubscribe("#.tasks.#", self.onAmqpReceive);
        COW.amqpConnect();
    };

    self.init();
}


$(function() {
    ko.applyBindings(new ActiveWorkflowsViewModel());
});