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
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    Loading.show();
                    loader.saveForm({"propertiesId":$scope.addPage.data.id,"fieldType":$scope.addPage.data.type,"fieldValid":$scope.addPage.data.length},function(data){
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
                        id:1,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0, //从第几条数据开始取
                        orderBy: "name",//排序字段
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
                    start:function (id,type,length) {
                        $scope.pageDialog.title="编辑校验";
                        $scope.addPage.data.id = id;
                        $scope.addPage.data.type = type;
                        $scope.addPage.data.length = length;
                        $scope.pageDialog.show();
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
                    // {
                    //     sTitle: "字段名称",
                    //     mData: "fieldMd5",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    {
                        sTitle: "原始字段值",
                        mData: "fieldName",
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
                            return '<i title="编辑" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData+'\',\'' +full.fieldType+'\',\'' +full.fieldValid+ '\')"></i>' ;
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
                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        $('#'+data.rows[j].key).val(data.rows[j].value);
                                    }
                                }

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
                        sTitle: "标题",
                        mData: "name",
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
                            return '<i title="详情" class="fa fa-list-alt" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')"></i>'+
                                     '<i class="fa fa-download" title="下载" ng-click="listPage.action.down(\'' + mData + '\')"></i>';
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

                            return '<i title="启动流程" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')"></i>' +
                                '<i title="删除" class="fa fa-trash-o" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
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
                        window.open("/index.html#/order?id="+deployId+"&processInstanceId="+id,"_blank");
                        // $scope.listPage.info.id= id;
                        // $scope.pageDialog.title = "审批操作";
                        // $scope.pageDialog.show();
                        //TODO 弹出对话框，审批操作
                        //$("#myModalLabel").prop('innerHTML')
                        // window.open("/demo.html#/process?id="+id,"_blank");
                    },
                    detail: function (id) {
                        $scope.pageDialogDetail.title = "查看详情";
                        // Loading.show();
                        $scope.hash="/workflow/process/queryProPlan?TaskId="+id;
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
                        loader.getMyTask($scope.searchPage.data, function (data) {
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
                            return '<i title="申请单" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData+'\',\''+full.deployId + '\')"></i>';
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
                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        $('#'+data.rows[j].key).val(data.rows[j].value);
                                    }
                                }

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
                            return '<i title="详情查看" class="fa fa-info" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.detail(\'' + mData+'\')"></i>';
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
                                if(data.rows) {
                                    for (var j = 0; j < data.rows.length; j++) {
                                        $('#'+data.rows[j].key).val(data.rows[j].value);
                                    }
                                }

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
                            return '<i title="处理" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')"></i>' +
                                    '<i title="详情" class="fa fa-list-alt" ng-show=userLevel.indexOf("detail")!=-1  ng-click="listPage.action.detail(\'' + mData + '\')"></i>';
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
            $scope.search = $location.search();
            $scope.showCommit = true;
            //deployment id
            if($scope.search.id) {
                Loading.show();
                loader.uploadFileInfo({'id': $scope.search.id,"processInstanceId":$scope.search.processInstanceId}, {}, function (data) {
                    $('#wordId').val($scope.search.id);
                    $('#processInstanceId').val($scope.search.processInstanceId);
                    $('#orderForm').html(data.data.templateHtml);
                    if(data.rows) {
                        for (var j = 0; j < data.rows.length; j++) {
                            $('#'+data.rows[j].key).val(data.rows[j].value);
                        }
                    }
                    $scope.showCommit =  data.showCommit;
                    Loading.hide();
                });
            }
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
                        $scope.pageDialog.title="编辑";
                    },
                    update:function (id) {
                        $scope.pageDialog.title="编辑";
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

                            return '<i title="启动流程" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')"></i>' +
                                '<i title="删除" class="fa fa-trash-o" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
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

            $scope.commitResume = function () {
                var params = $("#orderFormInfo").serializeArray();

                var values = {};
                for(var i in params ){
                    values[params[i].name] = params[i].value;
                }
                values['html'] = $('#orderForm').prop("innerHTML");
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
                var params = $("#orderFormInfo").serializeArray();
                var values = {};
                for(var i in params ){
                    values[params[i].name] = params[i].value;
                }
                values['html'] = $('#orderForm').prop("innerHTML");
                values['index'] = index;
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
        .controller('process.controller', ['$scope', '$location','$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $location,$rootScope,loader,Util,Tools,Loading,toaster) {

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
                        window.open("/index.html#/order?id="+id,"_blank");
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
                        sTitle: "部署时间",
                        mData: "deploymentTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    //
                    // {
                    //     sTitle: "版本",
                    //     mData: "version",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    // {
                    //     sTitle: "资源bpmn文件",
                    //     mData: "resourceName",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    // {
                    //     sTitle: "资源png文件",
                    //     mData: "diagramResourceName",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    // {
                    //     sTitle: "部署对象ID",
                    //     mData: "deploymentId",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    // {
                    //     sTitle: "时间",
                    //     mData: "limit",
                    //     mRender: function (mData, type, full) {
                    //         if(!mData){
                    //             return "";
                    //         }
                    //         return Util.formatSimpleDate(mData);
                    //     }
                    // },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            return '<i title="申请" class="fa fa-play" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.start(\'' + mData + '\')"></i>';
                                // '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2]}  //第 0,3列不可排序
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