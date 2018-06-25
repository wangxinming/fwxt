/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
    'use strict';
    angular.module('process',['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster','ngSanitize'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/process', {
                templateUrl: 'view/process/process.html',
                controller: 'process.controller'
            })
                .when('/order',{
                    templateUrl: 'view/process/order.html',
                    controller: 'order.controller'
                })
                .when('/form',{
                    templateUrl: 'view/process/formField.html',
                    controller: 'form.controller'
                })
                .when('/complete',{
                    templateUrl: 'view/process/completeTask.html',
                    controller: 'complete.controller'
                })
                .when('/myProcess',{
                    templateUrl: 'view/process/myProcess.html',
                    controller: 'myProcess.controller'
                })
                .when('/initiator',{
                    templateUrl: 'view/process/initiator.html',
                    controller: 'initiator.controller'
                })
                .when('/pending',{
                    templateUrl: 'view/process/pendingTask.html',
                    controller: 'pending.controller'
                })
                .when('/related',{
                    templateUrl: 'view/process/completeTask.html',
                    controller: 'related.controller'
                })
            ;
        }])
        .controller('form.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {
            // $rootScope.loginUserMenuMap={};
            // var foreachMenus=function(menus){
            //     for(var i=0;i<menus.length;i++){
            //         var menu=menus[i];
            //         $rootScope.loginUserMenuMap[menu.code]=!menu.permission;
            //         // if(menu.children.length>0){
            //         //     foreachMenus(menu.children);
            //         // }
            //     }
            // };
            // loader.loginMenus(function(data) {
            //     // $rootScope.loginUser=data;
            //     // $rootScope.isLogin=true;
            //     foreachMenus(data);
            // });

            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    if(!$scope.addPage.data.name){
                        $('#fieldName').focus();
                        toaster.pop('failed', "", "字段名称不能为空");
                        return;
                    }else if(!$scope.addPage.data.type){
                        $('#fieldType').focus();
                        toaster.pop('failed', "", "字段类型不能为空");
                        return;
                    }else if(!$scope.addPage.data.length){
                        $('#fieldLength').focus();
                        toaster.pop('failed', "", "字段长度不能为空");
                        return;
                    }
                    Loading.show();
                    loader.saveForm({"propertiesId":$scope.addPage.data.id,
                        "fieldType":$scope.addPage.data.type,
                        "fieldName":$scope.addPage.data.name,
                        "fieldValid":$scope.addPage.data.length},function(data){
                        if(data.result=="success"){
                            Loading.hide();
                            toaster.pop('success', "", "操作成功");
                            $scope.listPage.settings.reload();
                            $scope.pageDialog.hide();
                        }else{
                            Loading.hide();
                            toaster.pop('warning', "", data.msg);
                        }
                    })

                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                data:{
                    id:0,
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "name",//排序字段
                    orderByType: "desc" //排序顺序
                },
                init: function () {
                    $scope.searchPage.data = {
                        id:0,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "name",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    },
                    back:function () {
                        window.location.href = "/index.html#/upload";
                    }
                }
            };
            $scope.listPage = {
                data: [],
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    start:function (id,name,type,length) {
                        $scope.pageDialog.title="编辑校验";
                        $scope.addPage.data.id = id;
                        $scope.addPage.data.name = name;
                        $scope.addPage.data.type = type;
                        $scope.addPage.data.length = length;
                        $scope.pageDialog.show();
                    },

                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        // loader
                        // $scope.pageDialog.model.name =
                            Loading.show();

                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        $scope.searchPage.data.offset = search.offset;
                        loader.getFieldList($scope.searchPage.data,function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "模板名称",
                        mData: "templateName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },

                    {
                        sTitle: "原始字段值",
                        mData: "fieldName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "字段唯一标记",
                        mData: "fieldMd5",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "类型",
                        mData: "fieldType",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "长度",
                        mData: "fieldValid",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"propertiesId",
                        mRender:function(mData,type,full) {
                            return '<i><a title="编辑" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData+'\',\'' +full.fieldName+'\',\'' +full.fieldType+'\',\'' +full.fieldValid+ '\')">编辑</a><i>' ;
                                // '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4,5]},  //第 0,3列不可排序
                    { sWidth: "25%", aTargets: [ 2] },
                    { sWidth: "15%", aTargets: [ 0,1,3,4,5 ] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
            $scope.search = $location.search();
            $scope.searchPage.data.id = $scope.search.id;
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);
            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);
        }])
        .controller('complete.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {

            $scope.search = $location.search();
            // Loading.show();
            // loader.uploadFileInfo({'id':3},{},function (data) {
            //     // $scope.pageDialog.model.id = id;
            //     // $scope.pageDialog.model.name = data.data.name;
            //     // $scope.pageDialog.model.des = data.data.des;
            //     // $scope.pageDialog.model.html = data.data.html;
            //     $('#wordId').val(3);
            //     $('#orderForm').html(data.data.html);
            //     Loading.hide();
            // });
            $scope.pageDialogDetail=Tools.dialog({
                id:"pageDialogDetail",
                title:"新增",
                hiddenButton:true,
                save:function(){
                    Loading.show();
                    loader.completedTask({"id":$scope.listPage.info.id},{},function(data){
                        if(data.result=="success"){
                            Loading.hide();
                            toaster.pop('success', "", "操作成功");
                            $scope.listPage.settings.reload();
                            $scope.pageDialog.hide();
                        }else{
                            Loading.hide();
                            toaster.pop('warning', "", data.msg);
                        }
                    })
                }
            });
            $scope.addPageDetail={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };

            $scope.pageID = {};
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        id:1,
                        user:"",
                        title:"",
                        contractId:"",
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    down: function (processId) {
                        window.open("/workflow/process/download?processId="+processId);
                    },
                    detail: function (id) {
                        $('#keyword').html('');
                        $scope.pageDialogDetail.title = "查看详情";
                        // Loading.show();
                        $scope.hash="/workflow/process/queryProPlan?processInstanceId="+id;
                        Loading.show();
                        loader.getTemplateHtmlHistory({"taskId":id},function(data){

                            if(data.result == "success") {
                                if(data.comments) {
                                    $scope.details = data.comments;
                                }
                                $('#htmlTemplate').html(data.info);
                                // var html = "<div align=\"CENTER\"><b>关键信息</b></div><div><label>甲方名称：********</label> <label>乙方名称：*****/label></div>>";

                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        if(data.rows[j].value == 'on'){
                                            $('#' + data.rows[j].key).attr("checked",true);
                                        }else {
                                            $('#' + data.rows[j].key).val(data.rows[j].value);
                                        }
                                        $('#'+data.rows[j].key).attr("disabled", true);
                                    }
                                }
                                if(data.download){
                                    // $('#customFile')[0].style.display = 'none';
                                    var html = '<a href="javascript:void(0);" onclick="javascript:window.open("/template/download?contractId='+data.download+'");">附件下载</a>';
                                    $('#download').html(html);
                                }

                                $('#keyword').html(data.keyword);
                            }
                            Loading.hide();
                        })
                        $scope.pageDialogDetail.show();
                    },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        loader
                        $scope.pageDialog.model.name =
                            Loading.show();

                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.getTaskCompleted($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [

                    // {
                    //     sTitle: "ID",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "流程名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "合同标题",
                        mData: "title",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "申请人",
                        mData: "starter",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "开工状态",
                        mData: "workStatus",
                        mRender: function (mData, type, full) {
                            if (mData == 1) {
                                return '<i title="已开工" class="fa fa-check-circle status-icon statusOn"></i>';
                            } else if (mData == 0) {
                                return '<i title="未开工" class="fa fa-minus-circle status-icon statusOff"></i>';
                            } else {
                                return '<i title="未知" class="fa fa-circle status-icon statuNull"></i>';
                            }

                        }
                    },
                    {
                        sTitle: "申请时间",
                        mData: "timestamp",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {

                            //class="fa fa-list-alt" class="fa fa-download"
                            return '<i><a title="详情"  ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')">详情</a></i>'+
                                     '<i><a title="下载" ng-click="listPage.action.down(\'' + mData + '\')">下载</a></i>';
                                // '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4,5]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);
            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);


        }])
        .controller('related.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {

            $scope.search = $location.search();
            $scope.cancelFilter = function (value){
                if (!value) return '';
                if(value.flag=='0'){
                    if(value.name.lastIndexOf(")")<0){
                        value.name+="(已作废)"
                    }
                }
                return value ;
            };
            Loading.show();

            loader.uploadFileInfo({'id':$scope.search.id},{},function (data) {
                // $scope.pageDialog.model.id = id;
                // $scope.pageDialog.model.name = data.data.name;
                // $scope.pageDialog.model.des = data.data.des;
                // $scope.pageDialog.model.html = data.data.html;
                $('#wordId').val(3);
                $('#orderForm').html(data.data.html);
                Loading.hide();
            });

            $scope.pageID = {};
            // $scope.userLevel = document.getElementById("userLevel").value;

            $scope.menuState = {
                show : false
            };

            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        process:{
                            id:0,
                            limit:10,
                            offset:0
                        },
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    start:function (id) {
                        window.open("/index.html#/process?id="+id,"_blank");
                    },
                    edit: function (id) {

                    },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        loader
                        $scope.pageDialog.model.name =
                            Loading.show();

                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.deployment($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [

                    {
                        sTitle: "ID",
                        mData: "id",
                        mRender: function (mData, type, full) {
                            var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                            return s;
                        }
                    },
                    {
                        sTitle: "流程名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "部署时间",
                        mData: "deploymentTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            // class="fa fa-pencil fa-fw" class="fa fa-trash-o"
                            return '<i><a title="启动流程"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')">启动流程</a></i>' +
                                '<i><a title="删除"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };


            $scope.searchPage.init();
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);

            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);

        }])
        .controller('myProcess.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.search = $location.search();
            $scope.pageID = {};
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    Loading.show();
                    loader.completedTask({"id":$scope.listPage.info.id},{},function(data){
                        if(data.result=="success"){
                            Loading.hide();
                            toaster.pop('success', "", "操作成功");
                            $scope.listPage.settings.reload();
                            $scope.pageDialog.hide();
                        }else{
                            Loading.hide();
                            toaster.pop('warning', "", data.msg);
                        }
                    })
                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        process:{
                            id:0,
                            limit:10,
                            offset:0
                        },
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                info:{},
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    start:function (id,deployId) {
                        window.open("/index.html#/order?state=update&id="+deployId+"&processInstanceId="+id,"_blank");
                        // $scope.listPage.info.id= id;
                        // $scope.pageDialog.title = "审批操作";
                        // $scope.pageDialog.show();
                        //TODO 弹出对话框，审批操作
                        //$("#myModalLabel").prop('innerHTML')
                        // window.open("/demo.html#/process?id="+id,"_blank");
                    },
                    // detail: function (id) {
                    //     $scope.pageDialogDetail.title = "查看详情";
                    //     // Loading.show();
                    //     $scope.hash="/workflow/process/queryProPlan?TaskId="+id;
                    //     $scope.pageDialogDetail.show();
                    // },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        Loading.show();
                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.getMyTask($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    // {
                    //     sTitle: "任务ID",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "流程名称",
                        mData: "deployName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "合同标题",
                        mData: "title",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "当前状态",
                        mData: "status",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起时间",
                        mData: "createTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            return '<i><a title="申请单" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData+'\',\''+full.deployId + '\')">申请单</a></i>';
                                // '<i title="查看详情" class="fa fa-list-alt" ng-show=userLevel.indexOf("detail")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')"></i>';
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();

        }])
        .controller('initiator.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {

            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    Loading.show();
                    loader.completedTask({"id":$scope.listPage.info.id},{},function(data){
                        if(data.result=="success"){
                            Loading.hide();
                            toaster.pop('success', "", "操作成功");
                            $scope.listPage.settings.reload();
                            $scope.pageDialog.hide();
                        }else{
                            Loading.hide();
                            toaster.pop('warning', "", data.msg);
                        }
                    })
                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };

            $scope.pageDialogDetail=Tools.dialog({
                id:"pageDialogDetail",
                title:"新增",
                hiddenButton:true,
                save:function(){
                }
            });
            $scope.addPageDetail={
                data: {
                    id: 0,
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        process:{
                            id:0,
                            limit:10,
                            offset:0
                        },
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                info:{},
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    detail: function (id) {
                        $('#keyword').html('');
                        $scope.pageDialogDetail.title = "查看详情";
                        // Loading.show();
                        $scope.hash="/workflow/process/queryProPlan?processInstanceId="+id;
                        Loading.show();
                        loader.getTemplateHtmlHistory({"taskId":id},function(data){
                            if(data.result == "success") {
                                if(data.comments) {
                                    $scope.details = data.comments;
                                }
                                $('#htmlTemplate').html(data.info);
                                // var html = "<div align=\"CENTER\"><b>关键信息</b></div><div><label>甲方名称：********</label> <label>乙方名称：*****/label></div>>";
                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        // $('#'+data.rows[j].key).val(data.rows[j].value);
                                        if(data.rows[j].value == 'on'){
                                            $('#' + data.rows[j].key).attr("checked",true);
                                        }else {
                                            $('#' + data.rows[j].key).val(data.rows[j].value);
                                        }
                                    }
                                }
                                if(data.download){
                                    // $('#customFile')[0].style.display = 'none';
                                    // var html = '<a href="javascript:void(0);" onclick="javascript:window.open("/template/download?contractId='+data.download+'");">附件下载</a>';
                                    var html = '<a href="javascript:void(0);" onclick=\'javascript:window.open(\"template/download?contractId='+data.download + '\");\'>附件下载</a>';
                                    $('#download').html(html);
                                }
                                $('#keyword').html(data.keyword);
                            }
                            Loading.hide();
                        })
                        $scope.pageDialogDetail.show();
                    },

                    search: function (search, fnCallback) {
                        $scope.searchPage.data.offset =search.offset;
                        loader.commitedTask($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    // {
                    //     sTitle: "任务ID",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "流程名称",
                        mData: "deployName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "请求标题",
                        mData: "title",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "当前状态",
                        mData: "status",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起时间",
                        mData: "createTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            //class="fa fa-info"
                            return '<i><a title="详情查看"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.detail(\'' + mData+'\')">详情查看</a></i>';
                            // '<i title="查看详情" class="fa fa-list-alt" ng-show=userLevel.indexOf("detail")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')"></i>';
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);
            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);

        }])
        .controller('pending.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.approvalItems = [{"status":1,"name":"同意"},{"status":2,"name":"拒绝，回退到申请人"}];
            $scope.pageID = {};
            $scope.pageDialogDetail=Tools.dialog({
                id:"pageDialogDetail",
                title:"新增",
                hiddenButton:true,
                save:function(){
                    Loading.show();
                    loader.completedTask({"id":$scope.listPage.info.id},{},function(data){
                        if(data.result=="success"){
                            Loading.hide();
                            toaster.pop('success', "", "操作成功");
                            $scope.listPage.settings.reload();
                            $scope.pageDialog.hide();
                        }else{
                            Loading.hide();
                            toaster.pop('warning', "", data.msg);
                        }
                    })
                }
            });
            $scope.addPageDetail={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };


            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    if($scope.addPage.data.approvalStatus == 1) {
                        Loading.show();
                        loader.completedTask({"id": $scope.listPage.info.id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                            } else {
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        })
                    }else if ($scope.addPage.data.approvalStatus == 2){

                        Loading.show();
                        //跳转任务 到任务提交人
                        loader.rejectTask({"id": $scope.listPage.info.id,"cause":$scope.addPage.data.cause}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                            } else {
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        })
                    }
                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    approvalStatus:1,
                    cause:'',
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        process:{
                            id:0,
                            limit:10,
                            offset:0
                        },
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                info:{},
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    start:function (id) {
                        $scope.listPage.info.id= id;
                        $scope.pageDialog.title = "审批操作";
                        $scope.pageDialog.show();
                        //TODO 弹出对话框，审批操作
                        //$("#myModalLabel").prop('innerHTML')
                        // window.open("/demo.html#/process?id="+id,"_blank");
                    },
                    detail: function (id) {
                        $('#keyword').html('');
                        $scope.pageDialogDetail.title = "查看详情";
                        // Loading.show();
                        $scope.hash="/workflow/process/queryProPlan?TaskId="+id;
                        Loading.show();
                        loader.getTemplateHtml({"taskId":id},function(data){
                            if(data.result == "success") {
                                if(data.comments) {
                                    $scope.details = data.comments;
                                }
                                // $timeout(function(){
                                //     $scope.details = [{"name":"王新明","title":"请假","user":"user","createTime":new Date(),"status":"发起"},
                                //         {"name":"王新明","title":"请假","user":"user","createTime":new Date(),"status":"审批"}];
                                // },500);
                                $('#htmlTemplate').html(data.info);
                                // var html = "<div align=\"CENTER\"><b>关键信息</b></div><div class=\"cjk\" align=\"LEFT\">　　</div><div align=\"LEFT\">甲方名称：********</div><div align=\"LEFT\"> 乙方名称：*****</div>";
                                // $('#keyword').html(data.keyword);
                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        // $('#'+data.rows[j].key).val(data.rows[j].value);
                                        if(data.rows[j].value == 'on'){
                                            $('#' + data.rows[j].key).attr("checked",true);
                                        }else {
                                            $('#' + data.rows[j].key).val(data.rows[j].value);
                                        }
                                    }
                                }
                                if(data.download){
                                    // $('#customFile')[0].style.display = 'none';
                                    var html = '<a href="javascript:void(0);" onclick=\'javascript:window.open(\"template/download?contractId='+data.download + '\");\'>附件下载</a>';
                                    $('#download').html(html);
                                }
                                $('#keyword').html(data.keyword);
                            }
                            Loading.hide();
                        })
                        $scope.pageDialogDetail.show();
                    },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        Loading.show();
                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.getTaskPending($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    // {
                    //     sTitle: "任务ID",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "流程名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "申请标题",
                        mData: "title",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "处理人",
                        mData: "assignee",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "时间",
                        mData: "timestamp",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            //class="fa fa-pencil fa-fw" class="fa fa-list-alt"
                            return '<i><a title="处理"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')">处理</a></i>' +
                                    '<i><a title="详情"  ng-show=userLevel.indexOf("detail")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')">详情</a></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);
            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);

        }])
        .controller('order.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {

            // $("#orderFormInfo").ajaxForm(function(data){
            //     var html = '<i style="margin-top: 25px;" class="fa fa-download" title="下载" ng-click="download(\''+data.file+'\')">'+data.file+'</i>'
            //     $('#download').html(html);
            // });

            $scope.uploadAttachment = function (){
                $.ajaxFileUpload({
                    method:"POST",
                    url:"/template/custom",            //需要链接到服务器地址
                    dataType: 'json',
                    fileElementId:'fileAttachment',                        //文件选择框的id属性
                    data:{id:$('#processInstanceId').val()},
                    success: function(data,s, status){
                        // $('#sendStatus').text("上传成功");
                        toaster.pop('success', "","上传成功");
                        // var html = '<i class="fa fa-download" title="下载" ng-click="download(\''+data.file+'\')">'+data.displayName+'</i>';
                        var html = '<a href="javascript:void(0);" class="fa fa-download" title="下载" onclick=\'javascript:window.open(\"template/download?fileName='+data.file + '\");\'>'+data.displayName+'</a>';
                        $('#download').html(html);
                        $('#custom').val(data.file);
                    },error: function (data, status, e){
                        toaster.pop('failed', "","上传失败");
                        // $('#sendStatus').text("上传失败");
                    }
                });
            };
            // $scope.uploadAttachment = function (){
            //     $.ajaxFileUpload({
            //         method:"POST",
            //         url:"/template/batchImport",            //需要链接到服务器地址
            //         secureuri:true,
            //         fileElementId:'fileName',                        //文件选择框的id属性
            //         data:$('#picForm').serialize(),
            //         success: function(data,s, status){
            //             //上传成功之后的操作
            //         },error: function (data, status, e){
            //             //上传失败之后的操作
            //         }
            //     });
            // };
            $scope.download = function () {

            };
            $scope.search = $location.search();
            $scope.workStatus = false;
            $scope.showCommit = true;
            //deployment id
            if($scope.search.state=='create') {
                Loading.show();
                loader.uploadFileInfoAdd({'id': $scope.search.id,"contract":$scope.search.contract}, {}, function (data) {
                    $('#wordId').val($scope.search.id);
                    $('#contract').val($scope.search.contract);
                    $('#processInstanceId').val($scope.search.processInstanceId);
                    $('#orderForm').html(data.data.templateHtml);


                    $scope.showCommit =  data.showCommit;
                    $scope.fields = data.fields;
                    if($scope.fields) {
                        for (var i = 0; i < data.fields.length; i++) {
                            if (data.fields[i].fieldType) {
                                $('#' + data.fields[i].fieldMd5).attr('placeholder', data.fields[i].fieldType)
                            }
                        }
                    }
                    Loading.hide();
                });
            }else if($scope.search.state=='update'){
                Loading.show();
                loader.uploadFileInfo({'id': $scope.search.id,"processInstanceId":$scope.search.processInstanceId,"contract":$scope.search.contract}, {}, function (data) {
                    $('#wordId').val($scope.search.id);
                    $('#processInstanceId').val($scope.search.processInstanceId);
                    $('#orderForm').html(data.data.templateHtml);
                    $scope.fields = data.fields;
                    $scope.contractName = data.title;
                    $scope.workStatus = data.workStatus==1?true:false;
                    if(data.rows) {
                        for (var j = 0; j < data.rows.length; j++) {
                            $('#'+data.rows[j].key).val(data.rows[j].value);
                        }
                    }
                    for(var i=0;i<data.fields.length;i++){
                        if(data.fields[i] && data.fields[i].fieldMd5 && data.fields[i].fieldType) {
                            $('#'+data.fields[i].fieldMd5).attr('placeholder', data.fields[i].fieldType)
                        }
                    }
                    $scope.showCommit =  data.showCommit;
                    $('#refuseCause').text(data.refuse)
                    Loading.hide();
                });
            };

            $scope.commitResume = function () {
                for(i=0;i< $scope.fields.length;i++){
                    var text = $('#'+$scope.fields[i].fieldMd5).val();
                    if(!text || text.trim()==""){
                        continue;
                    }
                    if(text.length > parseInt($scope.fields[i].fieldValid)){
                        toaster.pop('failed', "",$scope.fields[i].fieldName+"超过范围");
                        $('#'+$scope.fields[i].fieldMd5).focus();
                        return;
                    }
                    // switch($scope.fields[i].fieldType){
                    //     case 'D':
                    //         if (isNaN( $('#'+$scope.fields[i].fieldMd5).val())){
                    //             toaster.pop('failed', "",$scope.fields[i].fieldName+"格式不正确");
                    //             $('#'+$scope.fields[i].fieldMd5).focus();
                    //             return;
                    //         }
                    //         break;
                    //     case 'T':
                    //         break;
                    //     case 'YYYYMMDD':
                    //         var r = text.match( /^(\d{4})(\d{2})(\d{2})$/);
                    //         if(r==null){
                    //             toaster.pop('failed', "",$scope.fields[i].fieldName+"格式不正确");
                    //             $('#'+$scope.fields[i].fieldMd5).focus();
                    //             return;
                    //         }
                    //         break;
                    //     default:
                    //         break;
                    // }
                }
                var params = $("#orderFormInfo").serializeArray();
                var values = {workStatus:$('#workStatus').val()};
                for(var i in params ){
                    values[params[i].name] = params[i].value;
                }
                values['html'] = $('#orderForm').prop("innerHTML");
                // values['contractName'] = $scope.contractName;
                Loading.show();
                loader.resumeTask(values, function (data) {
                    Loading.hide();
                    if(data.result == "success"){
                        // window.open("/index.html#/myProcess");
                        window.close();
                    }else{

                    }
                })
            };
            $scope.commitOrder = function (index) {
                for(i=0;i< $scope.fields.length;i++){
                    var text = $('#'+$scope.fields[i].fieldMd5).val();
                    if(!text || text.trim()==""){
                        continue;
                    }
                    if(text.length > parseInt($scope.fields[i].fieldValid)){
                        toaster.pop('failed', "",$scope.fields[i].fieldName+"超过范围");
                        $('#'+$scope.fields[i].fieldMd5).focus();
                        return;
                    }
                    switch($scope.fields[i].fieldType){
                        case 'D':
                            if (isNaN( $('#'+$scope.fields[i].fieldMd5).val())){
                                toaster.pop('failed', "",$scope.fields[i].fieldName+"格式不正确");
                                $('#'+$scope.fields[i].fieldMd5).focus();
                                return;
                            }
                            break;
                        case 'T':
                            break;
                        case 'YYYYMMDD':
                            var r = text.match( /^(\d{4})(\d{2})(\d{2})$/);
                            if(r==null){
                                toaster.pop('failed', "",$scope.fields[i].fieldName+"格式不正确");
                                $('#'+$scope.fields[i].fieldMd5).focus();
                                return;
                            }
                            break;
                        default:
                            break;
                    }
                }
                var params = $("#orderFormInfo").serializeArray();
                var values = {workStatus:$scope.workStatus};
                for(var i in params ){
                    values[params[i].name] = params[i].value;
                }
                values['html'] = $('#orderForm').prop("innerHTML");
                values['index'] = index;
                values['contractName'] = $scope.contractName;
                //TODO 提交表单 检查，反馈结果，成功后关闭页面，不成功 需要提示
                Loading.show();
                loader.commitFormTask(values, function (data) {
                    Loading.hide();
                    if(data.result == "success"){
                        // window.open("/index.html#/myProcess");
                        window.close();
                    }else{

                    }
                })
                // AJAX.POST("/workflow/process/start",values);
            }

        }])
        .controller('process.controller', ['$scope','$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.search = $location.search();
            $scope.pageID = {};
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){

                }
            });
            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:10,
                        offset:0
                    },
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "updated",//排序字段
                    orderByType: "desc" //排序顺序
                }
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "updated",//排序字段
                        orderByType: "desc" //排序顺序
                    }
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.row = {};
            loader.getTemplateListTotal({'id':1}, function (data) {
                //部署流程编号、模板编号
                // $scope.description = name;
                $scope.sites = data.rows;
                $scope.deploys = data.deploys;
            });
            $scope.listPage = {
                data: [],
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    start:function () {
                            // window.open("/index.html#/order?state=create&id=" + id + "&contract=" + $scope.row.rID, "_blank");
                            window.open("/index.html#/order?state=create&id=" + $scope.row.dID + "&contract=" + $scope.row.rID, "_blank");
                    },
                    edit: function (id) {
                    },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
                        Loading.show();
                        loader.removeDeployment({'id': id}, {}, function (data) {
                            if (data.result == "success") {
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        }, '关联模板');
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.deploymentContract($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        });
                    }
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [

                    // {
                    //     sTitle: "ID",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "流程名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "版本",
                        mData: "version",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "选择模板",
                        mData: "template",
                        mRender: function (mData, type, full) {
                            return '                <div class="form-group">\n' +
                                // '                    <label  class="col-md-3 control-label">模板名称<span class="required">*</span></label>\n' +
                                '                    <div class="col-md-9">\n' +
                                '                        <!--<select  class="form-control" ng-class="{false: \'has-error\'}[pfCheck]" ng-model="description" required  ng-options="item.id as item.name for item in sites | filter: { flag: \'1\'}">-->\n' +
                                '                        <select  class="form-control"  ng-model="row.rID"  ng-options="item.templateId as item.templateName for item in sites ">\n' +
                                '                            <option value=""> --- 请选择合同模板 --- </option>\n' +
                                '                        </select>\n' +
                                '                    </div>\n' +
                                '                </div>';
                            // return '< type="submit" class="btn btn-primary editable-submit" value="流程申请" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')">';
                        }
                    },
                    {
                        sTitle: "部署时间",
                        mData: "deploymentTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            // return '<i title="申请" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')"><span>流程申请</span></i>';
                            return '<input type="submit" class="btn btn-primary editable-submit" value="发起申请" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')">';
                                // '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]}  //第 0,3列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
            $scope.$watch("listPage.checkAllRow", function (newVal, oldVal) {
                if (newVal) {
                    $scope.listPage.checkedList = Util.copyArray("id", $scope.listPage.data);
                } else {
                    if ($scope.listPage.data.length == $scope.listPage.checkedList.length) {
                        $scope.listPage.checkedList = [];
                    }
                }
            }, false);
            $scope.$watch("listPage.checkedList", function (newVal, oldVal) {
                $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
            }, true);
        }]);
})(angular);