/*global ko: false, COW: false, cowConfig: false*/

// Knockout JS to observable mapping options
var TASK_MAPPING = {
    key: function (item) {
        return ko.utils.unwrapObservable(item.id);
    },
    "variables": {
        create: function (options) {
            if (options.data == null) {
                return ko.observableArray();
            }
            //sometimes it is "variable", other times it is "variables"
            var variables = options.data.variable || options.data.variables;
            return ko.mapping.fromJS(variables);
        },
        update: function (options) {
            if (options.data == null) {
                return options.target;
            }
            //sometimes it is "variable", other times it is "variables"
            var variables = options.data.variable || options.data.variables;
            return ko.mapping.fromJS(variables);
        }
    }
};


// Class to represent a human task. The properties are converted to observables 
// so the ui will stay in sync with the data.
function Task(newTaskData) {
    var self = this;

    // Add the properties of newTaskData to self as observables   
    ko.mapping.fromJS(newTaskData, TASK_MAPPING, self);

    if (self.variables == null) {
        self.variables = ko.observableArray();
    }
    if (self.outcomes == null) {
        self.outcomes = ko.observableArray();
    }

    self.selectedOutcome = ko.observable();
    if (self.outcomes().length === 1) {
        self.selectedOutcome(self.outcomes()[0]);
    }


    /*** Computed Data ***/
    self.canAssignTask = ko.computed(function () {
        return self.assignee() == null;
    });


    self.canCompleteTask = ko.computed(function () {
        return self.state() === "Reserved";
    });


    /*** Behaviours ***/
    self.addVariable = function () {
        self.variables.push({ name: ko.observable(), value: ko.observable() });
    };

    self.removeVariable = function (variable) {
        self.variables.remove(variable);
    };


    // Find out if any variable names have been duplicated
    self.hasVariableConflicts = COW.hasVariableConflicts(self.variables);


    // Find out if a specific variable"s name has been duplicated
    self.variableHasConflict = COW.variableHasConflict(self.variables);
}





//Root object used by Knockout
function TasksViewModel() {
    var self = this;

    /*** Data ***/
    // The tasks the user can take or are assigned to them
    self.activeTasks = ko.observableArray();

    // Tasks from self.activeTasks that are assigned to the user
    self.assignedTasks = ko.computed(function () {
        return ko.utils.arrayFilter(self.activeTasks(), function (item) {
            return item.canCompleteTask() && item.assignee() === self.username();
        });
    });

    // Tasks from self.activeTasks that the user could take
    self.availableTasks = ko.computed(function (item) {
        return ko.utils.arrayFilter(self.activeTasks(), function (item) {
            return item.state() === "Ready";
        });
    });

    self.showHistory = ko.observable(false);
    self.historyTasks = ko.observableArray();

    // Holds the task that the user last clicked on, and will show up in the modal
    self.selectedTask = ko.observable();
    // Currently logged in user"s user name
    self.username = ko.observable();
    // Currently logged in user"s groups
    self.groups = [];


    /*** Behaviours ***/

    // Creates/updates task using task data from either the ajax calls or amqp notfications
    self.createOrUpdateTask = function (newTaskData) {
        //check if task exists
        var task = ko.utils.arrayFirst(self.activeTasks(), function (item) {
            return item.id() === newTaskData.id;
        });
        //update existing task
        if (task != null) {
            self.mapTaskToKoTask(newTaskData, task);
            //Remove tasks that are completed or have been assined to someone else
            self.activeTasks.remove(function (item) {
                return item.state() === "Completed" ||
                    (item.assignee() != null && item.assignee() !== self.username());
            });
        }
            //create new task
        else {
            task = self.mapTaskToKoTask(newTaskData);
            // Add the new task if user can take or complete it
            if (task.state() !== "Completed" &&
                    (task.assignee() == null || task.assignee() === self.username())) {
                self.activeTasks.push(task);
            }
        }

        // AMQP doesn"t send notifications about the task history, so if a task was created 
        // get the task with ajax.     
        if (task.state() === "Completed") {
            self.updateHistoryTasks();
        }
    };

    // This function will convert the task data received from amqp or cow-server, to the observable
    // If updateTarget is provided, it will update the existing task, else create a new one
    self.mapTaskToKoTask = function (newTaskData, updateTarget) {
        // Sometimes "outcomes" is used and other times it is "outcome"
        if (newTaskData.outcome != null) {
            newTaskData.outcomes = newTaskData.outcome;
            delete newTaskData.outcome;
        }
        var koTask;
        if (updateTarget) {
            koTask = ko.mapping.fromJS(newTaskData, TASK_MAPPING, updateTarget);
        }
        else {
            koTask = new Task(newTaskData);
        }
        return koTask;
    };



    // Use ajax to load tasks. This is mainly just used when the page first loads because
    // it will use amqp to stay in sync.
    // Can also be used to explicitly sync with the server
    self.updateAssignedTasks = function () {
        var queryString = "?" + $.param({ assignee: self.username });
        return self.updateTaskList(queryString);
    };

    self.updateAvailableTasks = function () {
        var queryString = "?" + $.param({ candidate: self.username });
        return self.updateTaskList(queryString);
    };

    // Add the tasks at /tasks{ending} to self.activeTasks        
    self.updateTaskList = function (urlEnding) {
        var endpoint = "tasks";
        if (urlEnding) {
            endpoint = endpoint + urlEnding;
        }
        // Use cowRequest method to make the CORS ajax request
        return COW.cowRequest(endpoint).done(function (data) {
            $.each(data.task, function (i, item) {
                self.createOrUpdateTask(item);
            });
        });
    };


    self.updateHistoryTasks = function () {
        if (!self.showHistory()) {
            return;
        }
        var now = new Date();

        //Should probably use a smaller range here
        var queryString = "?" + $.param({
            assignee: self.username,
            start: now.getFullYear() - 1 + "-1-1",
            end: now.getFullYear() + 1 + "-1-1"
        });
        return COW.cowRequest("tasks/history" + queryString).done(function (data) {
            self.historyTasks(data.historyTask);
        });
    };


    self.toggleHistory = function () {
        var oldSetting = self.showHistory();
        self.showHistory(!oldSetting);
        if (self.showHistory()) {
            self.updateHistoryTasks();
        }
        else {
            self.historyTasks.removeAll();
        }
    };


    //Explicitly sync all tasks with cow server.
    self.refreshAllTasks = function (data, event) {
        return $.when(
            self.updateAssignedTasks(),
            self.updateAvailableTasks(),
            self.updateHistoryTasks()
        );
    };


    // Called when a user clicks on a task from one of the tables    
    self.showTask = function (task) {
        self.selectedTask(task);
    };


    // Called when a user clicks Assign to me. Use ajax to assign task to user on cow-server.
    self.takeSelectedTask = function () {
        // url format: tasks/{id}/task?assignee={username}
        var url = "tasks/" + self.selectedTask().id() + "/take?assignee=" + self.username();
        COW.cowRequest(url, "post").done(function (data) {
            // cow-server returns the updated task
            self.createOrUpdateTask(data);
            // Wait till after the ajax call completes to close the modal, so the user doesn"t
            // see the table until it has been updated.
            $("#taskInfoModal").modal("hide");
        });
    };


    // Called when a user clicks complete. Use ajax to set the task as completed.
    self.completeCurrentTask = function () {
        var hasSelectedOutcome = self.selectedTask().selectedOutcome() != null;
        // Make sure outcome is known.
        if (!hasSelectedOutcome && self.selectedTask().outcomes().length > 0) {
            $("#outcomes-form").addClass("has-error");
            $("#outcomes-form .has-error").removeClass("hidden");
            return;
        }
        // Build query string out of outcome and variables
        // Possible urls are:
        //    tasks/{id}
        //    tasks/{id}?var=name1:value1&var=name2:value&outcome={outcome}
        //    tasks/{id}?var=name1:value1&var=name2:value
        //    tasks/{id}?outcome={outcome}
        var queryString = "";
        var varsQueryString = self.encodeVariables(self.selectedTask().variables());
        if (varsQueryString !== "") {
            queryString = "?" + varsQueryString;
            // This makes sure the url doesn"t end with &
            if (hasSelectedOutcome) {
                queryString += "&";
            }
        }
        if (hasSelectedOutcome) {
            // Only need to add the "?" when there are no variables
            if (queryString === "") {
                queryString += "?";
            }
            queryString += $.param({ outcome: self.selectedTask().selectedOutcome() });
        }

        var url = "tasks/" + self.selectedTask().id() + queryString;

        COW.cowRequest(url, "delete").done(function () {
            // Since the call was successful the task no longer belongs in activeTasks
            self.activeTasks.remove(self.selectedTask());
            // Wait till after the ajax call completes to close the modal, so the user doesn"t
            // see the table until it has been updated.
            $("#taskInfoModal").modal("hide");
        });
    };


    // put variables in "var=val1:name1&var=val2:name2" format
    self.encodeVariables = function (variables) {
        if (variables.length === 0) {
            return "";
        }
        var varPairs = $.map(variables, function (item) {
            return "var=" + item.name() + ":" + item.value();
        });
        return varPairs.join("&");
    };





    /*** Initialization ***/



    // Called when page is first loaded. Sets initial state of the page.
    self.init = function () {
        //Get username first so we know which username to use in the ajax calls for tasks
        COW.cowRequest("whoami").done(function (data) {
            self.username(data.id);
            self.groups = $.map(data.membership, function (group) {
                return group.group;
            });

            self.refreshAllTasks();

            // Right now this subscribes to all task messages, we may want to filter on
            // username and group eventually
            COW.amqpSubscribe("#.tasks.#", self.createOrUpdateTask);
            // Establish amqp connection when the page is loaded
            COW.amqpConnect();
        });
    };
    self.init();

}



$(function () {
    ko.applyBindings(new TasksViewModel());
});


