/*global cowConfig: false, SockJS: false, Stomp: false, ko: false*/
/*
Module containing general cow related functions.
Following module pattern from http://www.adequatelygood.com/JavaScript-Module-Pattern-In-Depth.html
*/
var COW = (function ($) {

    var my = {};

    // Helper function to make CORS AJAX requests to cow-server.
    // Path is the section of the url after cow-server/. 
    // Ex. if url is http://scout2:8080/cow-server/tasks?candidate=brosenberg, 
    //  path will be tasks?candidate=brosenberg. The beginning of the url is defined in config.json
    // httpMethod is optional and defaults to get
    // data is optional and defaults to nothing. data is what will be in the body of the ajax request
    // returns the jqXHR object so the deferred methods like done, success, fail, etc can be called
    my.cowRequest = function (path, httpMethod, data) {

        var defaultAjaxParams = {
            url: cowConfig.cowServerHost + path,
            contentType: "application/json",
            dataType: "json",
            xhrFields: {
                withCredentials: true
            }
        };
        var optionalParams = {
            data: JSON.stringify(data),
            type: httpMethod
        };

        var ajaxParams = $.extend({}, defaultAjaxParams, optionalParams);

        return $.ajax(ajaxParams);
    };

    my.activeWorkflowIds = function (callBack) {
        my.cowRequest("processInstances").done(function (data) {
            var ids = $.map(data.processInstance, function (procInstance) {
                var nameId = procInstance.id;
                var dotPosition = nameId.lastIndexOf(".");
                return +nameId.substr(dotPosition + 1);
            });
            callBack(ids);
        });
    };


    /*
    Find out if any variable names have been duplicated
    */
    my.hasVariableConflicts = function (variables) {
        return ko.computed(function () {
            var varsMap = {};
            for (var i = 0; i < variables().length; i++) {
                var name = variables()[i].name();
                if (varsMap[name] != null) {
                    return true;
                }
                varsMap[name] = true;
            }
            return false;
        });
    };


    /*
    Find out if a specific variable"s name has been duplicated
    */
    my.variableHasConflict = function (variables) {
        return function (variable) {
            var foundMatch = false;
            for (var i = 0; i < variables().length; i++) {
                var name = variables()[i].name();
                if (name === variable.name()) {
                    if (foundMatch) {
                        return true;
                    }
                    foundMatch = true;
                }
            }
            return false;
        };
    };


    // Configure Stomp to use SockJs to connect to AMQP
    var sock = new SockJS(cowConfig.amqpUrl);
    var stomp = Stomp.over(sock);
    // Keep a list of subscriptions to resubscribe after disconnect and reconnect
    var amqpSubscriptions = [];
    var amqpIsConnected = false;
    // Keeps track of the number of consecutive failed connection attemps
    var amqpFailedConnectionAttempts = 0;

    var amqpConnectTimerId = 0;

    /*
    For an unknown reason stomp occasionally hangs while establishing the connection.
    This handles disconnecting and reconnecting after a timeout period
    */
    var startAmqpTimeout = function () {
        amqpConnectTimerId = setTimeout(function () {
            console.log("Could not connect to amqp with in timeout limit!!");
            stomp.disconnect(function () {
                sock = new SockJS(cowConfig.amqpUrl);
                stomp = Stomp.over(sock);
                my.amqpConnect();
            });
        }, cowConfig.amqpConnectTimeout);
    };

    var clearAmqpTimeout = function () {
        clearTimeout(amqpConnectTimerId);
    };

    /*
    Attaches a subscription to the Stomp client
    */
    var addSubscription = function (subscription) {
        // Stomp requires routing keys to be in the format /exchange/amq.topic/<routing key>
        var destination = cowConfig.amqpExchange + subscription.routingKey;
        stomp.subscribe(destination, function (message) {
            // Extract routing key from message headers
            var slashPosition = message.headers.destination.lastIndexOf("/");
            var routingKey = message.headers.destination.substr(slashPosition);
            // Message comes in as text
            var parsedBody = $.parseJSON(message.body);
            subscription.onReceive(parsedBody, routingKey);
        });
    };

    /*
     Called by external code inorder to add an AMQP subscription. Doesn"t actually connect.
     Need to call COW.amqpConnect() in order to connect.
     onReceive = function(data, routingKey). Data is the Javascript object from the body of the
     message, routingKey is the routing the message was sent with
    */
    my.amqpSubscribe = function (routingKey, onReceive) {
        var subscription = {
            routingKey: routingKey,
            onReceive: onReceive
        };
        if (amqpIsConnected) {
            addSubscription(subscription);
        }
        // If it"s not connect just add to list. When Stomp reconnects all amqpSubscriptions
        // in the list are set up.
        amqpSubscriptions.push(subscription);
    };


    /*
    Starts the AMQP connection
    */
    my.amqpConnect = function () {
        // Only allow one connection
        if (amqpIsConnected) {
            return;
        }

        var onConnect = function () {
            clearAmqpTimeout();
            $.each(amqpSubscriptions, function (i, e) {
                addSubscription(e);
            });
            amqpIsConnected = true;
            amqpFailedConnectionAttempts = 0;
        };
        var onError = function () {
            amqpIsConnected = false;
            amqpFailedConnectionAttempts++;

            if (amqpFailedConnectionAttempts < 6) {
                stomp.connect("guest", "guest", onConnect, onError);
                startAmqpTimeout();
            }
            else {
                setTimeout(function () {
                    stomp.connect("guest", "guest", onConnect, onError);
                    startAmqpTimeout();
                }, 500);
            }
        };
        startAmqpTimeout();
        stomp.connect("guest", "guest", onConnect, onError);
    };




    return my;
}(jQuery));