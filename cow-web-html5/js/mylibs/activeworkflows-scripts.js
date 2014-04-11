/*global ko: false, COW: false, cowConfig: false*/


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

function Workflow(wflowData, tableHeadings) {
    var self = this;

    self.id = wflowData.id;
    self.tasks = ko.observableArray();


    self.columnValues = ko.computed(function () {
        var colValues = $.map(tableHeadings(), function (heading, index) {
            if (index === 0 && heading === "Workflow") {
                return self.id;
            }
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
        return colValues;
    });


    self.handleMultipleTasksForUser = function (tasks) {
        var tasksMap = {};
        var tasksToRemove = [];
        $.each(tasks, function (i, task) {
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
        return ko.utils.arrayFilter(tasks, function (task) {
            return tasksToRemove.indexOf(task) < 0;
        });
    };

    self.updateTasks = function (newTasks) {
        newTasks = self.handleMultipleTasksForUser(newTasks);
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

        self.tasks.remove(function (existingTask) {
            var newTaskData = ko.utils.arrayFirst(newTasks, function (e) {
                return e.key === existingTask.key;
            });
            return newTaskData == null;
        });
    };

    self.updateTasks(wflowData.statusSummary);
}

function ActiveWorkflows() {
    var self = this;

    self.workflows = ko.observableArray();

    self.tableHeadings = ko.observableArray(["Workflow"]);

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


    self.onAmqpReceive = function (task) {
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
    ko.applyBindings(new ActiveWorkflows());
});