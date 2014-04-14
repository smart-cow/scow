/*global ko: false, COW: false, cowConfig: false*/

function WorkflowsViewModel() {
    var self = this;

    self.workflows = ko.observableArray();

    // When self.loadingWorkflow has a value the yellow warning alert show up
    self.loadingWorkflow = ko.observable(null);
    // When self.lastLoadedWorkflow has a value the green warning alert show up
    self.lastLoadedWorkflow = ko.observable();
    // Holds information about the selected workflow
    self.selectedWorkflow = ko.observable();
    self.selectedWorkflowVariables = ko.observableArray();



    /*
    Does ajax call to start the workflow
    */
    self.startWorkflow = function () {
        var body = {};
        body.processDefinitionKey = self.selectedWorkflow();
        self.addVariables(body);

        COW.cowRequest("/processInstances", "post", body).done(function (data) {
            // Hide yellow alert since workflow has been started
            self.loadingWorkflow(null);
            // Set lastLoadedWorkflow to make green alert show up
            self.lastLoadedWorkflow(data.key);
            $("#variables-modal").modal("hide");
        });
    };


    /*
    Add variables to the body of the ajax request that starts a workflow
    */
    self.addVariables = function (data) {
        if (self.selectedWorkflowVariables().length < 1) {
            return;
        }
        var varList = $.map(self.selectedWorkflowVariables(), function (v) {
            return { name: v.name(), value: v.value() };
        });
        data.variables = { variable: varList };
    };



    /*
    Convert process level variables to observables to allow the user to edit them before 
    starting the process
    */
    self.handleWorkflowVars = function (variables) {
        $.each(variables, function (i, variable) {
            self.selectedWorkflowVariables.push({
                name: ko.observable(variable.name),
                value: ko.observable(variable.value)
            });
        });
    };



    /*
    Called when a user selects a workflow from the table
    */
    self.workflowSelected = function (workflow) {
        // Set loadingWorkflow to workflow to make the yellow alert showup
        self.loadingWorkflow(workflow);
        self.selectedWorkflow(workflow);
        // Show modal to allow user to enter values for process variables
        $("#variables-modal").modal("show");

        // Get process level variables for the selected workflow
        COW.cowRequest("processes/" + workflow).done(function (data) {
            self.selectedWorkflowVariables.removeAll();
            if (data.variables != null && data.variables.variable != null &&
                    data.variables.variable.length > 0) {
                self.handleWorkflowVars(data.variables.variable);
            }
        });
    };


    self.loadWorkflows = function () {
        COW.cowRequest("processDefinitions").done(function (data) {
            $.each(data.processDefinition, function (i, pd) {
                self.workflows.push(pd.key);
            });
        });
    };


    self.init = function () {
        self.loadWorkflows();
    };
    self.init();
}


$(function () {
    ko.applyBindings(new WorkflowsViewModel());
});