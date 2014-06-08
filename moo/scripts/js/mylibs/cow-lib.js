// Generated by CoffeeScript 1.7.1
(function() {
  var CowUtil, typeExtensions,
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  (typeExtensions = function() {
    String.prototype.rightOf = function(char) {
      return this.substr(this.lastIndexOf(char) + 1);
    };
    return Array.prototype.first = function(predicate) {
      var e, _i, _len;
      for (_i = 0, _len = this.length; _i < _len; _i++) {
        e = this[_i];
        if (predicate(e)) {
          return e;
        }
      }
      return null;
    };
  })();

  CowUtil = (function() {
    var AMQP, Amqp;

    function CowUtil() {
      this.amqpConnect = __bind(this.amqpConnect, this);
      this.amqpSubscribe = __bind(this.amqpSubscribe, this);
      this.variableHasConflict = __bind(this.variableHasConflict, this);
      this.hasVariableConflicts = __bind(this.hasVariableConflicts, this);
      this.activeWorkflowIds = __bind(this.activeWorkflowIds, this);
      this.deleteRunningInstances = __bind(this.deleteRunningInstances, this);
      this.cowRequest = __bind(this.cowRequest, this);
    }

    CowUtil.prototype.cowRequest = function(path, httpMethod, data) {
      if (httpMethod == null) {
        httpMethod = "get";
      }
      if (data == null) {
        data = void 0;
      }
      return $.ajax({
        url: cowConfig.cowServerHost + path,
        data: data != null ? JSON.stringify(data) : void 0,
        type: httpMethod,
        contentType: "application/json",
        dataType: "json",
        xhrFields: {
          withCredentials: true
        }
      });
    };

    CowUtil.prototype.xmlRequest = function(path, httpMethod, xml) {
      return $.ajax({
        url: cowConfig.cowServerHost + path,
        data: new XMLSerializer().serializeToString(xml),
        type: httpMethod,
        contentType: "application/xml",
        dataType: "json",
        xhrFields: {
          withCredentials: true
        }
      });
    };

    CowUtil.prototype.deleteRunningInstances = function(workflowName) {
      return this.cowRequest("processes/" + workflowName + "/processInstances", "delete");
    };

    CowUtil.prototype.activeWorkflowIds = function(callBack) {
      return COW.cowRequest("processInstances").done(function(data) {
        var pi;
        return callBack((function() {
          var _i, _len, _ref, _results;
          _ref = data.processInstance;
          _results = [];
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            pi = _ref[_i];
            _results.push(+pi.id.rightOf('.'));
          }
          return _results;
        })());
      });
    };

    CowUtil.prototype.hasVariableConflicts = function(variables) {
      return ko.computed((function(_this) {
        return function() {
          var variable, varsMap, _i, _len, _ref;
          varsMap = {};
          _ref = variables();
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            variable = _ref[_i];
            if (varsMap[variable.name()]) {
              return true;
            }
            varsMap[variable.name()] = true;
          }
          return false;
        };
      })(this));
    };

    CowUtil.prototype.variableHasConflict = function(variables) {
      return (function(_this) {
        return function(variable) {
          var matchingVars;
          matchingVars = variables().filter(function(v) {
            return v.name() === variable.name();
          });
          return matchingVars.length > 1;
        };
      })(this);
    };

    AMQP = null;

    CowUtil.prototype.amqpSubscribe = function(routingKey, onReceive) {
      if (AMQP == null) {
        AMQP = new Amqp();
      }
      return AMQP.subscribe(routingKey, onReceive);
    };

    CowUtil.prototype.amqpConnect = function() {
      return AMQP.connect();
    };

    Amqp = (function() {
      function Amqp() {
        this.connect = __bind(this.connect, this);
        this.stompConnect = __bind(this.stompConnect, this);
        this.startTimeout = __bind(this.startTimeout, this);
        this.onError = __bind(this.onError, this);
        this.onConnect = __bind(this.onConnect, this);
        this.subscribe = __bind(this.subscribe, this);
        this.addSubscription = __bind(this.addSubscription, this);
        this.stomp = Stomp.over(new SockJS(cowConfig.amqpUrl));
        this.subscriptions = [];
        this.isConnected = false;
        this.numFailedConnectionAttempts = 0;
        this.connectTimeoutId = 0;
      }

      Amqp.prototype.addSubscription = function(subscription) {
        var destination;
        destination = cowConfig.amqpExchange + subscription.routingKey;
        return this.stomp.subscribe(destination, (function(_this) {
          return function(message) {
            var parsedBody, routingKey;
            routingKey = message.headers.destination.rightOf("/");
            parsedBody = $.parseJSON(message.body);
            return subscription.onReceive(parsedBody, routingKey);
          };
        })(this));
      };

      Amqp.prototype.subscribe = function(routingKey, onReceive) {
        var subscription;
        subscription = {
          routingKey: routingKey,
          onReceive: onReceive
        };
        if (this.isConnected) {
          this.addSubscription(subscription);
        }
        return this.subscriptions.push(subscription);
      };

      Amqp.prototype.onConnect = function() {
        var s, _i, _len, _ref;
        clearTimeout(this.connectTimeoutId);
        _ref = this.subscriptions;
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          s = _ref[_i];
          this.addSubscription(s);
        }
        this.isConnected = true;
        return this.numFailedConnectionAttempts = 0;
      };

      Amqp.prototype.onError = function() {
        this.isConnected = false;
        this.numFailedConnectionAttempts += 1;
        if (this.numFailedConnectionAttempts < 6) {
          return this.stompConnect();
        } else {
          return setTimeout(this.stompConnect, 500);
        }
      };

      Amqp.prototype.startTimeout = function() {
        return this.connectTimeoutId = setTimeout(((function(_this) {
          return function() {
            console.log("Could not connect to amqp with in timeout limit!!");
            return _this.stomp.disconnect(function() {
              _this.stomp = Stomp.over(new SockJS(cowConfig.amqpUrl));
              return _this.connect();
            });
          };
        })(this)), cowConfig.amqpConnectTimeout);
      };

      Amqp.prototype.stompConnect = function() {
        this.startTimeout();
        return this.stomp.connect("guest", "guest", this.onConnect, this.onError);
      };

      Amqp.prototype.connect = function() {
        if (!this.isConnected) {
          return this.stompConnect();
        }
      };

      return Amqp;

    })();

    return CowUtil;

  })();

  window.COW = new CowUtil();

}).call(this);

//# sourceMappingURL=cow-lib.map
