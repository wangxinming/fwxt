(function (angular) {


    var DataSourceTree = function (data, settings) {
        this._description = settings.description;
        this._children = settings.children;
        this._data = [];
        this._parentNoCheck = settings.type == "radio";
        this._allNoCheck = settings.type == "none";
        this._allNoParent = settings.type == "all_radio" || settings.type == "all_checkbox";
        this._depth = settings.depth;
        this._icon = settings.icon;
        this._iconPath = settings.iconPath;
        this._isLeaf = settings.isLeaf;
        this.setData(0, 0, data);
    };

    DataSourceTree.prototype.setData = function (rootId, depth, data) {
        if (depth + 1 > this._depth) {
            return;
        }
        for (var i = 0; i < data.length; i++) {
            var nodeData = data[i];
            var node = {};
            this._data.push(node);
            node.id = this._data.length;
            node.pid = rootId;
            node.name = nodeData[this._description];
            node.data = nodeData;
            if (this._icon != "" && nodeData[this._icon] && nodeData[this._icon] != "") {
                node.icon = this._iconPath + nodeData[this._icon];
            }

            if (depth + 1 < this._depth) {
                node.isParent = !this._allNoParent;
                if (this._parentNoCheck) {
                    node.nocheck = true;
                }
            }
            if (this._allNoCheck) {
                node.nocheck = true;
            }

            if (this._isLeaf) {
                node.nocheck = !this._isLeaf(nodeData);
                node.isParent = !this._isLeaf(nodeData);
            }

            if (nodeData[this._children] && nodeData[this._children].length > 0) {
                this.setData(node.id, depth + 1, nodeData[this._children]);
            }
        }
    };

    DataSourceTree.prototype.getTreeData = function () {
        return this._data;

    };

    //    var settings = {
    //        type:"radio", //string, radio或者checkbox
    //        description:"description", //数据中的字段名，其值显示为节点名
    //        children:"children", //数据中的字段名，标示子节点集合
    //        depth:2  //展现的数据层数
    //    }
    //用于zTree的dataSource
    //原始数据data应为list
//    this.data = function(data,settings){
//        return new DataSourceTree(data,settings).getTreeData();
//    };

    var Util = function ($parse) {
        this._parse = $parse;
    };

    //取得变量varName的值
    //变量名支持形如"a.b.c"
    Util.prototype.getValue = function (varName, scope) {
        var getter = this._parse(varName);
        return getter(scope);
    };

    //取得变量varName的值为varValue
    //变量名支持形如"a.b.c"
    Util.prototype.setValue = function (varName, varValue, scope) {
        var setter = this._parse(varName).assign;
        setter(scope, varValue);
        if (scope.$apply != undefined) {
            if (!scope.$$phase) {
                scope.$apply();
            }
        }
    };

    //查找list中是否存在元素value
    Util.prototype.exist = function (value, list) {
        if (list instanceof Array) {
            for (var i = 0; i < list.length; i++) {
                if (list[i] == value) {
                    return true;
                }
            }
            return false;
        } else {
            return value == list;
        }
    };


//tree相关的指令元素需要有id属性
    angular.module('zTreeDirective', [])
            .directive('zTree', ['$parse', '$compile', function ($parse, $compile) {
                var util = new Util($parse);

//    settings通过zTree属性指定变量名
//    var settings = {
//        inData:"",  //string,tree的dataSource数据(绑定scope变量名),必须
//        outData:"", //string,tree的checked节点的值(绑定scope变量名)，必须
//        outParentData:"", //string,tree的checked节点的父节点的值绑定的变量名，仅radio时有效
//        type:"radio", //string类型, radio、checkbox、all_radio、all_checkbox、none,必须
//        description:"description", //数据中的字段名，其值显示为节点名,必须
//        children:"children", //数据中的字段名，标示子节点集合,必须
//        value:"name", //数据中的字段名，其值将绑定到ourData,必须
//        depth:2,  //展现的数据层数
//        nodeDom : "",  //html字符串,定制节点显示样式(绑定scope变量名)
//        nodeDomAllNode : false , //是否所有节点显示定制样式，如果false则只有非父节点显示定制样式，默认false
//        radioCancel : false , //如果是单选按钮形式，能否取消选择恢复到全部都不选中状态
//        inputWidth : 400 , //如果是inputTree(在input元素中),控制弹出的tree的div的宽度，单位px
//        icon:"img", //数据中的字段名，其值显示为图标名
//        iconPath:"../dmonitor-webapp/img/topo", //数据中的字段名，其值显示为图标的路径,
//        selectNode:""  //选中的节点(绑定scope变量名)
//        selectData:""  //选中的节点的数据(绑定scope变量名)
//        hasSameValue:false  //对于none类型，outData不对节点选中做初始化;对于checkbox、all_checkbox，不自动展开选中的父节点
//        isLeaf: boolean function(nodeData);  //判断该节点是否有勾选框的函数(绑定scope变量名)
//        disableClick:true
//    }

                return {
                    link: function (scope, element, attrs) {
                        var model2View = false;

                        var parentNodeCheckId = "";

                        var settings = scope.$eval(attrs.zTree);
                        if (settings.disableClick) {
                            settings.disableClick = true;
                        } else {
                            settings.disableClick = false;
                        }
                        if (!settings.inputWidth) {
                            settings.inputWidth = 200;
                        }
                        if (!settings.icon) {
                            settings.icon = "";
                        }
                        if (!settings.iconPath) {
                            settings.iconPath = "";
                        }

                        if (settings.isLeaf) {
                            settings.isLeaf = util.getValue(settings.isLeaf, scope);
                        }

                        var inputTree = element[0].tagName === 'INPUT';

                        var divId = attrs.id;
                        if (inputTree) {
                            divId = attrs.id + "_div";
                        }
                        var $div = $('<div id="' + divId + '" class="ztree inputTreeDiv"></div>');

                        function showMenu() {
                            var cityOffset = element.position();
                            $div.css({left: cityOffset.left - 0.5 + "px", top: cityOffset.top + element.outerHeight() + "px", width: settings.inputWidth}).show();
                            $("body").bind("mousedown", onBodyDown);
                        }

                        function hideMenu() {
                            $div.hide();
                            $("body").unbind("mousedown", onBodyDown);
                        }

                        function onBodyDown(event) {
                            if (!(event.target.id == attrs.id || event.target.id == divId || $(event.target).parents("#" + divId).length > 0)) {
                                hideMenu();
                            }
                        }

                        if (inputTree) {
                            $div.insertAfter(element);
                            element.on("click", showMenu);
                        }

                        var radioCancel = false;
                        if (settings.radioCancel) {
                            radioCancel = settings.radioCancel;
                        }

                        var hasSameValue = false;
                        if (settings.hasSameValue) {
                            hasSameValue = settings.hasSameValue;
                        }

                        var addDiyDom = null;
                        if (settings.nodeDom) {
                            var nodeDom = util.getValue(settings.nodeDom, scope);
                            addDiyDom = function (treeId, treeNode) {
                                if (treeNode.isParent && !settings.nodeDomAllNode) return;
                                var aObj = $("#" + treeNode.tId + "_span");
                                aObj.html(nodeDom(treeNode.data));
                                $compile(aObj)(scope)
                            }
                        }


                        var initOutData = function (newVal, oldVal) {
                            var userCheck = true;
                            var treeObj = $.fn.zTree.getZTreeObj(divId);
                            if (treeObj) {
                                var nodes = [];
                                var values = util.getValue(settings.outData, scope);
                                if ((values instanceof Array && values.length > 0) || (!(values instanceof Array) && values != null && values != "")) {
                                    nodes = treeObj.getNodesByFilter(function (node) {
                                        if (settings.type == "checkbox" || settings.type == "all_checkbox") {
                                            if (!(values instanceof Array)) {
                                                values = [values];
                                            }
                                            for (var i = 0; i < values.length; i++) {
                                                if (node.data[settings.value] == values[i]) {
                                                    return true;
                                                }
                                            }
                                            return false;
                                        } else {
                                            return (settings.type == "none" || settings.type == "all_radio" || !node.isParent)
                                                    && node.data[settings.value] == values;
                                        }
                                    });
                                }

                                var allNodes = treeObj.transformToArray(treeObj.getNodes());
                                for (var i = 0; i < allNodes.length; i++) {
                                    var node = allNodes[i];
                                    var isOutNode = false;
                                    for (var j = 0; j < nodes.length; j++) {
                                        var outNode = nodes[j];
                                        if (node.tId == outNode.tId) {
                                            isOutNode = true;
                                            break;
                                        }
                                    }
                                    if (isOutNode) {
                                        if (!node.checked) {
                                            if (settings.type == "all_checkbox" || settings.type == "all_radio") {
                                                model2View = true;
                                                treeObj.checkNode(node, true, false, true);
                                                userCheck = false;
                                            } else {
                                                model2View = true;
                                                treeObj.checkNode(node, true, true, true);
                                                userCheck = false;
                                            }
                                        }
                                        if (settings.type == "none" && !hasSameValue) {
                                            treeObj.selectNode(node, false);
                                        }
                                    } else {
                                        if (node.checked) {
                                            if (settings.type == "all_checkbox" || settings.type == "all_radio") {
                                                model2View = true;
                                                treeObj.checkNode(node, false, false, true);
                                                userCheck = false;
                                            } else {
                                                model2View = true;
                                                treeObj.checkNode(node, false, true, true);
                                                userCheck = false;
                                            }
                                        }
                                        if (settings.type == "none" && !hasSameValue) {
                                            treeObj.cancelSelectedNode(node, false);
                                        }
                                    }
                                }
                                if (!userCheck) {
                                    if (nodes.length > 0) {
                                        for (var i = 0; i < nodes.length; i++) {
                                            if (!settings.hasSameValue) {
                                                treeObj.expandNode(nodes[i].getParentNode(), true);
                                            }
                                        }
                                    }
                                    if (nodes.length == 0 && settings.type == "radio") {
                                        treeObj.expandAll(false);
                                    }
                                }
                                if (inputTree) {
                                    element.val(createUsersText(values));
                                }

                            }
                        }

                        scope.$watch(settings.inData, function () {  //watch inData，初始化tree
                            var inData = util.getValue(settings.inData, scope);
                            if (inData) {
                                var chkboxType = { "Y": "ps", "N": "ps" };
                                if (settings.checkBoxType == "YpNp") {
                                    chkboxType = { "Y": "p", "N": "p"}
                                } else if (settings.checkBoxType == "YpNps") {
                                    chkboxType = { "Y": "p", "N": "ps" };
                                } else if (settings.checkBoxType == "YpsNp") {
                                    chkboxType = { "Y": "ps", "N": "p" };
                                } else if (settings.checkBoxType == "YpsN") {
                                    chkboxType = { "Y": "ps", "N": "" };
                                } else if (settings.checkBoxType == "YsN") {
                                    chkboxType = { "Y": "s", "N": "" };
                                } else if (settings.checkBoxType == "YsNs") {
                                    chkboxType = { "Y": "s", "N": "s" };
                                }

                                var treeSetting = {
                                    view: {
                                        dblClickExpand: true,
                                        selectedMulti: false,
                                        addDiyDom: addDiyDom,
                                        expandSpeed: "",
                                        showTitle: false
                                    },
                                    check: {
                                        enable: true,
                                        chkStyle: (settings.type == "radio" || settings.type == "all_radio") ? "radio" : "checkbox",
                                        radioType: "all",
                                        autoCheckTrigger: true,
                                        chkboxType: chkboxType
                                    },
                                    data: {
                                        simpleData: {
                                            enable: true,
                                            idKey: "id",
                                            pIdKey: "pid",
                                            rootPId: 0
                                        }
                                    },
                                    callback: {
                                        onClick: function (event, treeId, treeNode) {
                                            var treeObj = $.fn.zTree.getZTreeObj(divId);
                                            if (settings.selectNode || settings.selectData) {
                                                var selectNodes = treeObj.getSelectedNodes();
                                                if (selectNodes.length > 0) {
                                                    if (settings.selectNode) {
                                                        util.setValue(settings.selectNode, selectNodes[0], scope);
                                                    }
                                                    if (settings.selectData) {
                                                        util.setValue(settings.selectData, selectNodes[0].data, scope);
                                                    }
                                                } else {
                                                    if (settings.selectNode) {
                                                        util.setValue(settings.selectNode, null, scope);
                                                    }
                                                    if (settings.selectData) {
                                                        util.setValue(settings.selectData, null, scope);
                                                    }
                                                }
                                            }
                                            if (settings.disableClick) {
                                                return;
                                            }
                                            if (settings.type == "all_radio" || settings.type == "all_checkbox") {
                                                treeObj.cancelSelectedNode(treeNode);
                                                treeObj.checkNode(treeNode, !treeNode.checked, true, true);
                                            } else if (settings.type == "none") {
                                                if (treeNode.level == settings.depth - 1 || (settings.isLeaf && settings.isLeaf(treeNode.data))) {
                                                    if (settings.outParentData != "" && treeNode.getParentNode()) {
                                                        util.setValue(settings.outParentData, treeNode.getParentNode().data[settings.value], scope);
                                                    }
                                                    util.setValue(settings.outData, treeNode.data[settings.value], scope);
                                                }
                                                if (treeNode.children && treeNode.children.length > 0) { //有子节点
                                                    treeObj.expandNode(treeNode);
                                                }
                                            } else {
                                                if (treeNode.children && treeNode.children.length > 0) { //有子节点
                                                    treeObj.expandNode(treeNode);
                                                } else {
                                                    treeObj.checkNode(treeNode, null, true, true);
                                                }
                                                treeObj.cancelSelectedNode(treeNode);
                                            }
                                        },

                                        beforeCheck: function (treeId, treeNode) {
                                            if ((settings.type == "all_checkbox" || settings.type == "checkbox") && parentNodeCheckId == "" && treeNode.isParent) {
                                                parentNodeCheckId = treeNode.id;
                                            }
                                        },
                                        onCheck: function (event, treeId, treeNode) {
                                            if (parentNodeCheckId != "") {
                                                if (treeNode.id != parentNodeCheckId) {
                                                    return;
                                                } else {
                                                    parentNodeCheckId = "";
                                                }
                                            }
                                            var treeObj = $.fn.zTree.getZTreeObj(divId);
                                            var checkedNodes = treeObj.getCheckedNodes();
                                            if (!model2View && !radioCancel && (settings.type == "radio" || settings.type == "all_radio")) {
                                                if (checkedNodes.length == 0) {
                                                    treeObj.checkNode(treeNode, true, false, false);
                                                    return;
                                                }
                                            }
                                            model2View = false;
                                            var result = [];
                                            for (var i = 0; i < checkedNodes.length; i++) {
                                                var node = checkedNodes[i];
                                                if (node.level == settings.depth - 1 || settings.type == "all_radio" || settings.type == "all_checkbox" || (settings.isLeaf && settings.isLeaf(node.data))) {
                                                    if (settings.type == "checkbox" || settings.type == "all_checkbox") {
                                                        var v = node.data[settings.value];
                                                        if (!util.exist(v, result)) {
                                                            result.push(v);
                                                        }
                                                    } else {
                                                        if (settings.outParentData != "") {
                                                            util.setValue(settings.outParentData, node.getParentNode().data[settings.value], scope);
                                                        }
                                                        util.setValue(settings.outData, node.data[settings.value], scope);
                                                        if (inputTree) {
                                                            element.val(createUsersText([node.data[settings.value]]));
                                                            hideMenu();
                                                        }
                                                        return;
                                                    }
                                                }
                                            }
                                            if (settings.type == "checkbox" || settings.type == "all_checkbox") {
                                                util.setValue(settings.outData, result, scope);
                                                if (inputTree) {
                                                    element.val(createUsersText(result));
                                                }
                                            } else {
                                                util.setValue(settings.outData, "", scope);
                                                if (settings.outParentData != "") {
                                                    util.setValue(settings.outParentData, "", scope);
                                                }
                                                if (inputTree) {
                                                    element.val("");
                                                }
                                            }
                                        }
                                    }
                                };
                                if (inputTree) {
                                    $.fn.zTree.init($div, treeSetting, new DataSourceTree(inData, settings).getTreeData());
                                } else {
                                    $.fn.zTree.init(element, treeSetting, new DataSourceTree(inData, settings).getTreeData());
                                }
                                initOutData();
                            }
                        }, true);

                        scope.$watch(settings.outData, initOutData, true);

                        function createUsersText(ids) {
                            var add = function (values, keys, users, ids) {
                                if (users) {
                                    for (var i = 0; i < users.length; i++) {
                                        var user = users[i];
                                        var v = user[settings.value];
                                        if (!util.exist(v, keys) && util.exist(v, ids)) {
                                            values.push(user[settings.description]);
                                            keys.push(v);
                                        }
                                        var children = user[settings.children];
                                        if (children && children.length > 0) {
                                            add(values, keys, children, ids);
                                        }
                                    }
                                }
                            }
                            var values = [];
                            var keys = [];
                            add(values, keys, util.getValue(settings.inData, scope), ids);
                            return values.join(",");
                        }
                    }
                };
            } ]);

})(angular);