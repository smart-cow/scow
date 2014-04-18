String::rightOf = (char) ->
    @substr(@lastIndexOf(char) + 1)



class CowUtil
    # Helper function to make CORS AJAX requests to cow-server.
    # Path is the section of the url after cow-server/.
    # Ex. if url is http://scout2:8080/cow-server/tasks?candidate=brosenberg,
    #  path will be tasks?candidate=brosenberg. The beginning of the url is defined in config.json
    # httpMethod is optional and defaults to get
    # data is optional and defaults to nothing. data is what will be in the body of the ajax request
    # returns the jqXHR object so the deferred methods like done, success, fail, etc can be called
    cowRequest: (path, httpMethod = "get", data = undefined) =>
        $.ajax
            url: cowConfig.cowServerHost + path
            data: if data? then JSON.stringify(data)
            type: httpMethod
            contentType: "application/json"
            dataType: "json"
            xhrFields:
                withCredentials: true


    activeWorkflowIds: (callBack) =>
        COW.cowRequest("processInstances").done (data) ->
            callBack (+pi.id.rightOf('.') for pi in data.processInstance)

    # Find out if any variable names have been duplicated
    hasVariableConflicts: (variables) => ko.computed =>
            varsMap = {}
            for variable in variables()
                return true if varsMap[variable.name()]
                varsMap[variable.name()] = true
            return false

    # Find out if a specific variable"s name has been duplicated
    variableHasConflict: (variables) =>
        (variable) =>
            matchingVars = variables().filter (v) => v.name() is variable.name()
            matchingVars > 1

    AMQP = null
    # Called by external code inorder to add an AMQP subscription. Doesn"t actually connect.
    # Need to call COW.amqpConnect() in order to connect.
    # onReceive = function(data, routingKey). Data is the Javascript object from the body of the
    # message, routingKey is the routing the message was sent with
    amqpSubscribe: (routingKey, onReceive) =>
        AMQP ?= new Amqp()
        AMQP.subscribe(routingKey, onReceive)

    # Starts the AMQP connection
    amqpConnect: =>
        AMQP.connect()


    class Amqp
        constructor: ->
            @stomp = Stomp.over(new SockJS cowConfig.amqpUrl)
            @subscriptions = []
            @isConnected = false
            @numFailedConnectionAttempts = 0
            @connectTimeoutId = 0

        addSubscription: (subscription) =>
            destination = cowConfig.amqpExchange + subscription.routingKey
            @stomp.subscribe destination, (message) =>
                routingKey = message.headers.destination.rightOf("/")
                parsedBody = $.parseJSON(message.body)
                subscription.onReceive(parsedBody, routingKey)


        subscribe: (routingKey, onReceive) =>
            subscription = routingKey: routingKey, onReceive: onReceive
            @addSubscription(subscription) if @isConnected
            @subscriptions.push(subscription)


        onConnect: =>
            clearTimeout(@connectTimeoutId)
            @addSubscription(s) for s in @subscriptions
            @isConnected = true
            @numFailedConnectionAttempts = 0

        onError: =>
            @isConnected = false
            @numFailedConnectionAttempts += 1
            if @numFailedConnectionAttempts < 6
                @stompConnect()
            else
                setTimeout(@stompConnect, 500)

        startTimeout: =>
            @connectTimeoutId = setTimeout(( =>
                console.log "Could not connect to amqp with in timeout limit!!"
                @stomp.disconnect =>
                    @stomp = Stomp.over(new SockJS(cowConfig.amqpUrl))
                    @connect()
                ), cowConfig.amqpConnectTimeout)

        stompConnect: =>
            @startTimeout()
            @stomp.connect("guest", "guest", @onConnect, @onError)

        connect: =>
            @stompConnect() unless @isConnected




window.COW = new CowUtil()
