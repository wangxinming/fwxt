/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
    'use strict';
    angular.module('deployment',['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster','ngSanitize'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/deployment', {
                templateUrl: 'view/deployment/deployment.html',
                controller: 'deployment.controller'
            })
            .when('/modeler',{
                templateUrl: 'view/deployment/modeler.html',
                controller: 'modeler.controller'
            })
            .when('/upload',{
                templateUrl: 'view/deployment/upload.html',
                controller: 'upload.controller'
            });
        }])
        .controller('modeler.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.replaceDiv = function(){
                AJAX.GET('/models/newModel');
            };
            $scope.searchPage = {
                init: function () {
                    $scope.searchPage.data = {
                        process:{
                            id:0,
                            limit:0,
                            offset:10
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
                    add: function () {
                        $scope.pageDialog.show();
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeModeler({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    update:function (id) {
                        window.open("/modeler.html?modelId="+id,"_blank");
                    },
                    publish:function (id) {
                        $rootScope.$confirm("确定要发布吗？", function () {
                            Loading.show();

                            loader.deployDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '发布');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.modeler($scope.searchPage.data, function (data) {
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
                    //     sTitle: "模型编号",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "模型名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "创建时间",
                        mData: "createTime",
                        mRender: function (mData, type, full) {
                                    if(!mData){
                                        return "";
                                    }
                                    return Util.formatSimpleDate(mData);
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
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            return '<i title="编辑" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.update(\'' + mData + '\')"></i>' +
                                    '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>' +
                                    '<i title="发布" class="fa fa-cog fa-fw" ng-show=userLevel.indexOf("publish")!=-1  ng-click="listPage.action.publish(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,3]}  //第 0,10列不可排序
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
        .controller('upload.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            /** 验证文件是否导入成功  */
            $("#uploadForm").ajaxForm(function(data){
                $scope.listPage.settings.reload()
            });
            $scope.uploadFile = function (){
                $.ajaxFileUpload({
                    method:"POST",
                    url:"/template/batchImport",            //需要链接到服务器地址
                    secureuri:true,
                    fileElementId:'fileName',                        //文件选择框的id属性
                    data:$('#picForm').serialize(),
                    success: function(data,s, status){
                        //上传成功之后的操作
                    },error: function (data, status, e){
                        //上传失败之后的操作
                    }
                });
            }

            $scope.uploadTemplate = function(){
                // AJAX.GET('/models/newModel');
                // window.open("/modeler.html?modelId","_blank");
                var form = $("form[name=uploadForm]");
                var options = {
                    url:'template/batchImport', //上传文件的路径
                    type:'post',
                    success:function(data){
                        console.log(data);
                        $('#htmlTemplateText').val(data);
                    }
                };
                form.ajaxSubmit(options);
                // loader.getTemplateHtml(null,function(data){
                //     $('#htmlTemplateText').val(data.result);
                // })
            }
            $scope.loadHtml = function(){
                // loader.getTemplateHtml(null,function(data){
                    $('#htmlTemplate').html($('#htmlTemplateText').val());
                // })
            }
            $scope.commitTemplate = function(){
                loader.commitTemplateHtml(null,function(data){
                    $('#htmlTemplate').html(data.result);
                })
            }

            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    $scope.pageDialog.model.html = $('#htmlTemplateText').val();
                    $scope.pageDialog.model.name = $('#templateName').val();
                    $scope.pageDialog.model.des = $('#templateDes').val();
                    Loading.show();
                    loader.saveUploadFileInfo($scope.pageDialog.model,function(data){
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
            $scope.pageDialog.model={};

            $scope.addPage={
                data: {
                    id: 0,
                    process:{
                        id:0,
                        limit:0,
                        offset:10
                    },
                    limit: 20, //每页条数(即取多少条数据)
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
                    add: function () {
                        $scope.pageDialog.show();
                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.removeWordTemplate({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '删除');
                    },
                    update:function (id) {
                        $scope.pageDialog.title = "更新";
                        Loading.show();
                        loader.uploadFileInfo({'template_id':id},{},function (data) {
                            $scope.pageDialog.model.id = id;
                            $scope.pageDialog.model.name = data.data.templateName;
                            $scope.pageDialog.model.des = data.data.des;
                            $('#templateName').val(data.data.templateName);
                            // $('#templateDes').val(data.data.des);
                            $('#htmlTemplateText').val(data.data.templateHtml);
                            Loading.hide();
                        });
                        $scope.pageDialog.show();
                    },
                    publish:function (id) {
                        $rootScope.$confirm("确定要发布吗？", function () {
                            Loading.show();

                            loader.deployDeployment({'id': id}, {}, function (data) {
                                if (data.result == "success") {
                                    Loading.hide();
                                    $scope.listPage.settings.reload(true);
                                }
                            }, function (error) {
                                Loading.hide();
                            });
                        }, '发布');
                    },
                    search: function (search, fnCallback) {
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.uploadFile($scope.searchPage.data, function (data) {
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
                    //     sTitle: "文档编号",
                    //     mData: "id",
                    //     mRender: function (mData, type, full) {
                    //         var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                    //         return s;
                    //     }
                    // },
                    {
                        sTitle: "文档名称",
                        mData: "templateName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "创建模板用户",
                        mData: "userName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "创建时间",
                        mData: "templateCreatetime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },

                    {
                        sTitle: "操作",
                        mData:"templateId",
                        mRender:function(mData,type,full) {
                            return '<i title="编辑" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.update(\'' + mData + '\')"></i>' +
                                '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>'
                                // '<i title="发布" class="fa fa-cog fa-fw" ng-show=userLevel.indexOf("publish")!=-1  ng-click="listPage.action.publish(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,3]}  //第 0,10列不可排序
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
        .controller('deployment.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.row = {};
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增",
                hiddenButton:false,
                save:function(){
                    Loading.show();
                    loader.updateTemRelation({"dID":$scope.row.dID,"rID":$scope.row.rID},{},function(data){
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
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    edit: function (dID,rID,name) {
                        $scope.pageDialog.title="关联word模板";
                        Loading.show();
                        loader.getTemplateListTotal({'id':1}, function (data) {
                            // if (data.result == "success") {
                            //部署流程编号、模板编号
                            $scope.row.dID = dID;
                            $scope.row.rID = rID;
                            // $scope.description = name;
                            $scope.sites = data.rows;
                            Loading.hide();
                        }, function (error) {
                            Loading.hide();
                        }, '关联模板');
                        $scope.pageDialog.show();
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

                    {
                        sTitle: "关联word模板名称",
                        mData: "oacontractTemplate.templateName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            return '<i title="关联模板" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.edit(\'' + full.id  +'\','+ full.oacontractTemplate.templateId +',\''+ full.oacontractTemplate.templateName+ '\')"></i>' +
                                    '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,3]}  //第 0,3列不可排序
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