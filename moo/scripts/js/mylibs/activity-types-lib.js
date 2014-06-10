// Generated by CoffeeScript 1.7.1
(function() {
  var ALL_HIT_TYPES, Activities, Activity, ActivityFactory, Decision, Exit, HumanTask, Loop, NON_FOLDER_HIT_TYPES, OVER_HIT_TYPE, Option, ScriptTask, ServiceTask, Signal, Subprocess, Workflow, getUniqKey, usedTreeKeys,
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; },
    __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

  ALL_HIT_TYPES = ["over", "after", "before"];

  NON_FOLDER_HIT_TYPES = ["before", "after"];

  OVER_HIT_TYPE = ["over"];


  /*
  usedTreeKeys = { }
  
  getUniqKey = (preferredName) ->
  
      possibleName = preferredName
      suffix = 0
      while usedTreeKeys[possibleName]
          suffix += 1
          possibleName = preferredName + suffix
  
      usedTreeKeys[possibleName] = true
      return possibleName
   */

  usedTreeKeys = {};

  getUniqKey = function(preferredName) {
    var possibleName, suffix;
    possibleName = preferredName;
    suffix = 1;
    while (usedTreeKeys[possibleName]) {
      suffix += 1;
      possibleName = preferredName + suffix;
    }
    usedTreeKeys[possibleName] = true;
    return possibleName;
  };

  Activity = (function() {
    Activity.prototype.icon = "Icon_Task.png";

    Activity.prototype.displayName = "Error: this is abstract";

    Activity.prototype.isDecision = false;

    Activity.prototype.isActivities = false;

    Activity.prototype.isOption = false;

    Activity.prototype.folder = false;

    function Activity(data) {
      var nameAttr, _ref, _ref1;
      this.data = data;
      this.removeVariable = __bind(this.removeVariable, this);
      this.addVariable = __bind(this.addVariable, this);
      this.readVariables = __bind(this.readVariables, this);
      this.addInvisibleAttr = __bind(this.addInvisibleAttr, this);
      this.addAttr = __bind(this.addAttr, this);
      this.setTitle = __bind(this.setTitle, this);
      this.findTreeNode = __bind(this.findTreeNode, this);
      this.dragDropNewActivity = __bind(this.dragDropNewActivity, this);
      this.dragDropExistingNode = __bind(this.dragDropExistingNode, this);
      this.dragDrop = __bind(this.dragDrop, this);
      this.dragEnter = __bind(this.dragEnter, this);
      this.key = getUniqKey((_ref = (_ref1 = this.data) != null ? _ref1.name : void 0) != null ? _ref : this.constructor.name);
      this.setTitle(this.key);
      this.draggable = true;
      this.expanded = true;
      this.act = this;
      this.variables = ko.observableArray();
      this.readVariables();
      this.apiAttributes = ko.observableArray();
      nameAttr = this.addAttr("name", "Name", true);
      if (!nameAttr.value()) {
        nameAttr.value(this.title);
      }
      nameAttr.value.subscribe((function(_this) {
        return function(newVal) {
          return _this.setTitle(newVal);
        };
      })(this));
      this.addAttr("description", "Description");
      this.addAttr("bypassable", "Bypassable", true, "checkbox");
    }

    Activity.prototype.dragEnter = function(treeData) {
      if (this.otherIsOption(treeData)) {
        if (this.isDecision) {
          return OVER_HIT_TYPE;
        } else {
          return false;
        }
      }
      if (this.folder) {
        return OVER_HIT_TYPE;
      } else {
        return NON_FOLDER_HIT_TYPES;
      }
    };

    Activity.prototype.dragDrop = function(treeData) {
      if (treeData.otherNode != null) {
        return this.dragDropExistingNode(treeData);
      } else {
        return this.dragDropNewActivity(treeData);
      }
    };

    Activity.prototype.dragDropExistingNode = function(treeData) {
      var target, targetChild;
      target = treeData.node;
      if (treeData.hitMode !== "over") {
        treeData.otherNode.moveTo(target, treeData.hitMode);
        return;
      }
      if (!this.folder) {
        return null;
      }
      targetChild = target.getFirstChild();
      if (targetChild != null) {
        return treeData.otherNode.moveTo(targetChild, "before");
      } else {
        return treeData.otherNode.moveTo(target, "child", function() {
          return target.setExpanded(true);
        });
      }
    };

    Activity.prototype.dragDropNewActivity = function(treeData) {
      var newActivity;
      newActivity = ActivityFactory.createFromTreeData(treeData);
      if (newActivity != null) {
        return treeData.node.addNode([newActivity], treeData.hitMode);
      }
    };

    Activity.prototype.findTreeNode = function() {
      var _ref;
      try {
        return (_ref = $("#tree").fancytree("getTree")) != null ? _ref.getNodeByKey(this.key) : void 0;
      } catch (_error) {}
    };

    Activity.prototype.setTitle = function(newTitle) {
      var _ref;
      this.title = newTitle;
      return (_ref = this.findTreeNode()) != null ? _ref.setTitle(this.title) : void 0;
    };

    Activity.prototype.otherIsActivities = function(treeData) {
      var droppedType, otherNode;
      otherNode = treeData.otherNode;
      if (otherNode != null) {
        return otherNode.data.act.isActivities;
      }
      droppedType = ActivityFactory.typeFromTreeData(treeData);
      return droppedType != null ? droppedType.prototype.isActivities : void 0;
    };

    Activity.prototype.otherIsOption = function(treeData) {
      var droppedType, otherNode;
      otherNode = treeData.otherNode;
      if (otherNode != null) {
        return otherNode.data.act.isOption;
      }
      droppedType = ActivityFactory.typeFromTreeData(treeData);
      return droppedType != null ? droppedType.prototype.isOption : void 0;
    };

    Activity.prototype.addAttr = function(key, label, isXmlAttribute, inputType) {
      var newAttribute, _ref;
      if (isXmlAttribute == null) {
        isXmlAttribute = false;
      }
      if (inputType == null) {
        inputType = "text";
      }
      newAttribute = {
        key: key,
        value: ko.observable((_ref = this.data) != null ? _ref[key] : void 0),
        label: label,
        isXmlAttribute: isXmlAttribute,
        inputType: inputType
      };
      this.apiAttributes.push(newAttribute);
      return newAttribute;
    };

    Activity.prototype.addInvisibleAttr = function(key, isXmlAttribute) {
      if (isXmlAttribute == null) {
        isXmlAttribute = false;
      }
      return this.addAttr(key, null, isXmlAttribute);
    };

    Activity.prototype.readVariables = function() {
      var v, varList, _i, _len, _ref, _ref1, _results;
      varList = (_ref = this.data) != null ? (_ref1 = _ref.variables) != null ? _ref1.variable : void 0 : void 0;
      if (varList == null) {
        return;
      }
      _results = [];
      for (_i = 0, _len = varList.length; _i < _len; _i++) {
        v = varList[_i];
        _results.push(this.variables.push(this.createObservableVar(v)));
      }
      return _results;
    };

    Activity.prototype.addVariable = function() {
      return this.variables.push(this.createObservableVar());
    };

    Activity.prototype.removeVariable = function(variable) {
      var matchingVar;
      matchingVar = this.variables().first(function(v) {
        return v.name() === variable.name();
      });
      return this.variables.remove(matchingVar);
    };

    Activity.prototype.createObservableVar = function(variable) {
      var _ref, _ref1;
      return {
        name: ko.observable(variable != null ? variable.name : void 0),
        value: ko.observable(variable != null ? variable.value : void 0),
        required: ko.observable((_ref = variable != null ? variable.required : void 0) != null ? _ref : false),
        output: ko.observable((_ref1 = variable != null ? variable.output : void 0) != null ? _ref1 : true)
      };
    };

    return Activity;

  })();

  Workflow = (function(_super) {
    __extends(Workflow, _super);

    Workflow.prototype.displayName = "Workflow";

    Workflow.prototype.folder = true;

    function Workflow(data, requestedName) {
      this.data = data;
      if (requestedName == null) {
        requestedName = null;
      }
      this.syncNameDisplays = __bind(this.syncNameDisplays, this);
      this.setTitle = __bind(this.setTitle, this);
      Workflow.__super__.constructor.call(this, this.data);
      this.syncNameDisplays(requestedName);
      if (this.data != null) {
        this.children = [ActivityFactory.create(this.data.activity)];
      } else {
        this.children = [ActivityFactory.createEmpty(Activities.prototype.typeString)];
      }
      this.draggable = false;
      this.addAttr("bypassAssignee", "Bypass Assignee");
      this.addAttr("bypassCandidateUsers", "Bypass Candidate Users");
      this.addAttr("bypassCandidateGroups", "Bypass Candidate Groups");
    }

    Workflow.prototype.setTitle = function(newTitle) {
      return Workflow.__super__.setTitle.call(this, "<span class='glyphicon glyphicon-list-alt'></span> " + newTitle);
    };

    Workflow.prototype.syncNameDisplays = function(requestedName) {
      var nameAttr, _ref, _ref1, _ref2;
      nameAttr = this.apiAttributes().first(function(e) {
        return e.key === "name";
      });
      nameAttr.value((_ref = (_ref1 = (_ref2 = this.data) != null ? _ref2.name : void 0) != null ? _ref1 : requestedName) != null ? _ref : this.constructor.name);
      return this.name = ko.computed((function(_this) {
        return function() {
          return nameAttr.value();
        };
      })(this));
    };

    Workflow.prototype.dragEnter = function() {
      return false;
    };

    Workflow.prototype.accept = function(visitor, node) {
      return visitor.visitWorkflow(node);
    };

    return Workflow;

  })(Activity);

  Activities = (function(_super) {
    __extends(Activities, _super);

    Activities.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Activities";

    Activities.prototype.icon = "Icon_List.png";

    Activities.prototype.displayName = "List";

    Activities.prototype.isActivities = true;

    Activities.prototype.folder = true;

    function Activities(data) {
      this.toOption = __bind(this.toOption, this);
      this.dragEnter = __bind(this.dragEnter, this);
      var childActivitiesData, d, isSequential, nameAttr, newTitle, uniqTitle, _ref;
      Activities.__super__.constructor.call(this, data);
      isSequential = data != null ? data.sequential : true;
      if (!(data != null ? data.name : void 0)) {
        newTitle = isSequential ? "List" : "Parallel List";
        uniqTitle = getUniqKey(newTitle);
        nameAttr = this.apiAttributes().first(function(e) {
          return e.key === "name";
        });
        nameAttr.value(uniqTitle);
      }
      childActivitiesData = (_ref = this.data) != null ? _ref.activity : void 0;
      if (childActivitiesData) {
        this.children = (function() {
          var _i, _len, _results;
          _results = [];
          for (_i = 0, _len = childActivitiesData.length; _i < _len; _i++) {
            d = childActivitiesData[_i];
            _results.push(ActivityFactory.create(d));
          }
          return _results;
        })();
      } else {
        this.children = [];
      }
      this.addAttr("sequential", "Is Sequential", true, "checkbox").value(isSequential);
      this.addAttr("mergeCondition", "Merge Condition", true);
    }

    Activities.prototype.dragEnter = function(treeData) {
      var parent, _ref;
      if (this.otherIsOption(treeData)) {
        return false;
      }
      parent = ((_ref = treeData.node.getParent()) != null ? _ref.data.act : void 0) != null;
      if (parent.isActivities || ((parent != null ? parent.isDecision : void 0) && this.otherIsActivities(treeData))) {
        return ALL_HIT_TYPES;
      } else {
        return OVER_HIT_TYPE;
      }
    };

    Activities.prototype.accept = function(visitor, node) {
      return visitor.visitActivities(node);
    };

    Activities.prototype.toOption = function() {
      return new Option(this.data);
    };

    return Activities;

  })(Activity);

  HumanTask = (function(_super) {
    __extends(HumanTask, _super);

    HumanTask.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Task";

    HumanTask.prototype.displayName = "Human Task";

    function HumanTask(data) {
      this.data = data;
      HumanTask.__super__.constructor.call(this, this.data);
      if (this.data != null) {
        this.assignee = data.assignee;
        this.candidateGroups = data.candidateGroups;
      }
      this.addAttr("assignee", "Assignee");
      this.addAttr("candidateUsers", "Candidate users");
      this.addAttr("candidateGroups", "Candidate groups");
      this.addAttr("createTime", "Create time");
      this.addAttr("endTime", "End time");
    }

    HumanTask.prototype.accept = function(visitor, node) {
      return visitor.visitHumanTask(node);
    };

    return HumanTask;

  })(Activity);

  ServiceTask = (function(_super) {
    __extends(ServiceTask, _super);

    ServiceTask.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.ServiceTask";

    ServiceTask.prototype.icon = "Icon_ServiceTask.png";

    ServiceTask.prototype.displayName = "Service Task";

    function ServiceTask(data) {
      this.data = data;
      ServiceTask.__super__.constructor.call(this, this.data);
      this.addAttr("url", "URL");
      this.addAttr("content", "Content");
      this.addAttr("contentType", "Content type");
      this.addAttr("var", "Result variable");
    }

    ServiceTask.prototype.accept = function(visitor, node) {
      return visitor.visitServiceTask(node);
    };

    return ServiceTask;

  })(Activity);

  ScriptTask = (function(_super) {
    __extends(ScriptTask, _super);

    ScriptTask.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Script";

    ScriptTask.prototype.icon = "Icon_Script.png";

    ScriptTask.prototype.displayName = "Script Task";

    function ScriptTask(data) {
      ScriptTask.__super__.constructor.call(this, data);
      this.addAttr("import", "Imports");
      this.addAttr("content", "Content");
    }

    ScriptTask.prototype.accept = function(visitor, node) {
      return visitor.visitScript(node);
    };

    return ScriptTask;

  })(Activity);

  Decision = (function(_super) {
    __extends(Decision, _super);

    Decision.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Decision";

    Decision.prototype.icon = "Icon_Decision.png";

    Decision.prototype.displayName = "Decision";

    Decision.prototype.isDecision = true;

    Decision.prototype.folder = true;

    function Decision(data) {
      this.dragDrop = __bind(this.dragDrop, this);
      this.dragEnter = __bind(this.dragEnter, this);
      var opt, optionsData;
      Decision.__super__.constructor.call(this, data);
      optionsData = data != null ? data.option : void 0;
      if (optionsData) {
        this.children = (function() {
          var _i, _len, _results;
          _results = [];
          for (_i = 0, _len = optionsData.length; _i < _len; _i++) {
            opt = optionsData[_i];
            _results.push(new Option(opt));
          }
          return _results;
        })();
      } else {
        this.children = [];
      }
      this.task = new HumanTask(data != null ? data.task : void 0);
    }

    Decision.prototype.dragEnter = function(treeData) {
      if (this.otherIsOption(treeData) || this.otherIsActivities(treeData)) {
        return OVER_HIT_TYPE;
      } else {
        return false;
      }
    };

    Decision.prototype.dragDrop = function(treeData) {
      var droppedType, otherActivity, _ref;
      otherActivity = (_ref = treeData.otherNode) != null ? _ref.data.act : void 0;
      if (otherActivity != null) {
        if (otherActivity.isOption) {
          this.dragDropExistingNode(treeData);
        } else if (otherActivity.isActivities) {
          console.log("list dropped on option");
          console.log(treeData);
        }
        return;
      }
      droppedType = ActivityFactory.typeFromTreeData(treeData);
      if (droppedType.prototype.typeString === Option.prototype.typeString) {
        return console.log("new option");
      } else if (droppedType.prototype.typeString === Activities.prototype.typeString) {
        return console.log("new option");
      }
    };

    Decision.prototype.accept = function(visitor, node) {
      return visitor.visitDecision(node);
    };

    return Decision;

  })(Activity);

  Option = (function(_super) {
    __extends(Option, _super);

    Option.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Option";

    Option.prototype.icon = "Icon_Decision_Arrow.png";

    Option.prototype.displayName = "Option";

    Option.prototype.folder = true;

    Option.prototype.isOption = true;

    function Option(data) {
      var childActivitiesData, _ref;
      this.data = data;
      this.dragEnter = __bind(this.dragEnter, this);
      Option.__super__.constructor.call(this, this.data);
      childActivitiesData = (_ref = this.data) != null ? _ref.activity : void 0;
      if (childActivitiesData) {
        this.children = [ActivityFactory.create(childActivitiesData)];
      } else {
        this.children = [];
      }
    }

    Option.prototype.dragEnter = function(treeData) {
      if (this.otherIsOption(treeData) || this.otherIsActivities(treeData)) {
        return NON_FOLDER_HIT_TYPES;
      }
    };

    Option.prototype.accept = function(visitor, node) {
      return visitor.visitOption(node);
    };

    return Option;

  })(Activity);

  Exit = (function(_super) {
    __extends(Exit, _super);

    Exit.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Exit";

    Exit.prototype.icon = "Icon_Exit.png";

    Exit.prototype.displayName = "Exit";

    function Exit(data) {
      Exit.__super__.constructor.call(this, data);
      this.addAttr("state", "State", true);
    }

    Exit.prototype.accept = function(visitor, node) {
      return visitor.visitExit(node);
    };

    return Exit;

  })(Activity);

  Signal = (function(_super) {
    __extends(Signal, _super);

    Signal.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Signal";

    Signal.prototype.icon = "Icon_Signal.png";

    Signal.prototype.displayName = "Signal";

    function Signal(data) {
      Signal.__super__.constructor.call(this, data);
      this.addAttr("signalId", "Signal Id", true);
    }

    Signal.prototype.accept = function(visitor, node) {
      return visitor.visitSignal(node);
    };

    return Signal;

  })(Activity);

  Subprocess = (function(_super) {
    __extends(Subprocess, _super);

    Subprocess.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.SubProcess";

    Subprocess.prototype.icon = "Icon_SubProcess.png";

    Subprocess.prototype.displayName = "Subprocess";

    function Subprocess(data) {
      Subprocess.__super__.constructor.call(this, data);
    }

    Subprocess.prototype.accept = function(visitor, node) {
      return visitor.visitSubprocess(node);
    };

    return Subprocess;

  })(Activity);

  Loop = (function(_super) {
    __extends(Loop, _super);

    Loop.prototype.typeString = "org.wiredwidgets.cow.server.api.model.v2.Loop";

    Loop.prototype.icon = "Icon_Loop.png";

    Loop.prototype.displayName = "Loop";

    Loop.prototype.folder = true;

    function Loop(data) {
      var childData, _ref;
      Loop.__super__.constructor.call(this, data);
      childData = (_ref = this.data) != null ? _ref.activity : void 0;
      if (childData) {
        this.children = [ActivityFactory.create(childData)];
      } else {
        this.children = [];
      }
      this.loopTask = new HumanTask(data != null ? data.loopTask : void 0);
      this.addAttr("doneName", "Done name", true);
      this.addAttr("repeatName", "Repeat name", true);
      this.addAttr("executionCount", "Execution count", true);
    }

    Loop.prototype.accept = function(visitor, node) {
      return visitor.visitLoop(node);
    };

    return Loop;

  })(Activity);

  ActivityFactory = (function() {
    function ActivityFactory() {}

    ActivityFactory.typeMap = {};

    ActivityFactory.typeMap[Activities.prototype.typeString] = Activities;

    ActivityFactory.typeMap[HumanTask.prototype.typeString] = HumanTask;

    ActivityFactory.typeMap[ServiceTask.prototype.typeString] = ServiceTask;

    ActivityFactory.typeMap[ScriptTask.prototype.typeString] = ScriptTask;

    ActivityFactory.typeMap[Decision.prototype.typeString] = Decision;

    ActivityFactory.typeMap[Exit.prototype.typeString] = Exit;

    ActivityFactory.typeMap[Loop.prototype.typeString] = Loop;

    ActivityFactory.typeMap[Signal.prototype.typeString] = Signal;

    ActivityFactory.typeMap[Option.prototype.typeString] = Option;

    ActivityFactory.create = function(cowData) {
      return new this.typeMap[cowData.declaredType](cowData.value);
    };

    ActivityFactory.createEmpty = function(typeName) {
      return new this.typeMap[typeName]();
    };

    ActivityFactory.createFromTreeData = function(treeData) {
      var type;
      type = this.typeFromTreeData(treeData);
      if (type) {
        return new type();
      } else {
        return null;
      }
    };

    ActivityFactory.draggableActivities = function() {
      var key, val, _ref, _results;
      _ref = this.typeMap;
      _results = [];
      for (key in _ref) {
        val = _ref[key];
        _results.push({
          type: key,
          name: val.prototype.displayName,
          icon: "images/" + val.prototype.icon
        });
      }
      return _results;
    };

    ActivityFactory.getType = function(typeName) {
      return this.typeMap[typeName];
    };

    ActivityFactory.typeNameFromTreeData = function(treeData) {
      var _ref, _ref1;
      return (_ref = treeData.draggable) != null ? (_ref1 = _ref.element) != null ? _ref1.data("component-type") : void 0 : void 0;
    };

    ActivityFactory.typeFromTreeData = function(treeData) {
      return this.getType(this.typeNameFromTreeData(treeData));
    };

    ActivityFactory.createWorkflow = function(cowData) {
      if (cowData == null) {
        cowData = null;
      }
      return new Workflow(cowData);
    };

    ActivityFactory.createEmptyWorkflow = function(name) {
      if (name == null) {
        name = "Workflow";
      }
      return new Workflow(null, name);
    };

    return ActivityFactory;

  })();

  window.ACT_FACTORY = ActivityFactory;

}).call(this);

//# sourceMappingURL=activity-types-lib.map
