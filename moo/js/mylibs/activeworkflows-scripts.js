/*global ko: false, COW: false, cowConfig: false*/



var statusPriority = ["open", "notStarted", "planned", "contingent", "completed", "precluded"];


/*
Holds the information for one row in the table.
Requires a reference to tableHeadings so it can return the values in the correct order.
*/
function Workflow(wflowData, tableHeadings) {
    var self = this;

    self.id = wflowData.id;
    self.statuses = ko.observableArray();


    self.getStatus = function (name) {
        return ko.utils.arrayFirst(self.statuses(), function (e) {
            return name === e.name;
        });
    };

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
            var status = self.getStatus(heading);
            if (status != null) {
                return status.status();
            }
            else {
                return "";
            }
        });
    });


    self.getHigherPriorityStatus = function (status1, status2) {
        if (status1 == null) {
            return status2;
        }
        if (status2 == null) {
            return status1;
        }
        var status1Index = statusPriority.indexOf(status1);
        var status2Index = statusPriority.indexOf(status2);

        return status1Index > status2Index ? status1 : status2;
    };



    self.updateStatuses = function (newStatuses) {
        //Build map of new higer priority statuses for each name
        var newStatusesMap = {};
        $.each(newStatuses, function () {
            var existingStatus = newStatusesMap[this.name];
            newStatusesMap[this.name] = self.getHigherPriorityStatus(this.status, existingStatus);
        });

        $.each(Object.keys(newStatusesMap), function (i, name) {
            var newStatus = newStatusesMap[name];
            var existingStatusObj = self.getStatus(name);

            if (existingStatusObj == null) {
                self.statuses.push({
                    name: name,
                    status: ko.observable(newStatus)
                });
            }
            else {
                existingStatusObj.status(newStatus);
            }
        });
    };

    self.updateStatuses(wflowData.statusSummary);




}


function ActiveWorkflowsViewModel() {
    var self = this;

    self.workflows = ko.observableArray();

    self.tableHeadings = ko.observableArray(["Workflow"]);

    /*
    Adds table headings that aren't already in self.tableHeadings.
    */
    self.updateTableHeadings = function (statuses) {
        $.each(statuses, function () {
            if (self.tableHeadings.indexOf(this.name) < 0) {
                self.tableHeadings.push(this.name);
            }
        });
    };


    self.createOrUpdateWorkflow = function (newWorkflowData) {
        var id = newWorkflowData.id;
        var workflow = ko.utils.arrayFirst(self.workflows(), function (e) {
            return e.id === id;
        });


        if (workflow != null) {
            workflow.updateStatuses(newWorkflowData.statusSummary);
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
            $.each(ids, function () {
                self.loadWorkflow(this);
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