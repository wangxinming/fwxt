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
            $scope.pageDialogDetail = Tools.dialog({
                id:"pageDialogDetail",
                title:"预览",
                hiddenButton:true,
                save:function(){
                }
            });
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
                    browse:function (id) {
                        $scope.hash="/workflow/process/modelerPreviewImage?modelerId="+id;
                        Loading.show();
                        loader.modelerReviewInfo({"modelerId":id},function(data){
                            if(data.result == "success") {
                                $scope.details = data.flows;
                            }
                            Loading.hide();
                        })
                        $scope.pageDialogDetail.show();
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
                            //class="fa fa-pencil fa-fw" class="fa fa-trash-o"  class="fa fa-cog fa-fw"
                            return '<i><a title="编辑" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.update(\'' + mData + '\')">编辑</a></i>' +
                                    '<i><a title="预览" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.browse(\'' + mData + '\')">预览</a></i>' +
                                    '<i><a title="删除" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a></i>' +
                                    '<i><a title="发布" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.publish(\'' + mData + '\')">发布</a>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3]}  //第 0,10列不可排序
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
        .controller('upload.controller', ['$scope','$location', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope,$location, $rootScope,loader,Util,Tools,Loading,toaster) {
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

                    var params = $("#uploadFormUpdate").serializeArray();
                    for(var i in params ){
                        $scope.pageDialog.model[params[i].name] = params[i].value;
                    }
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
                    active: function (status,id) {
                        Loading.show();
                        loader.templateUpdate({"id":id,"status":status==true?1:0},function(data){
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
                    },
                    add: function () {
                        $('#templateName').removeAttr("disabled");
                        $scope.pageDialog.show();
                    },
                    fieldsList: function(id){
                        window.location.href = "/index.html#/form?id="+id;
                            // $location.path("/index.html#/form?id="+id);
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
                        loader.concatFile({'template_id':id},{},function (data) {
                            $scope.pageDialog.model.id = id;
                            $scope.pageDialog.model.name = data.data.templateName;
                            $scope.pageDialog.model.des = data.data.templateDes;
                            $('#templateName').val(data.data.templateName);
                            // $('#templateDes').val(data.data.des);
                            $('#htmlTemplateText').val(data.data.templateHtml);
                            $('#htmlTemplate').html($('#htmlTemplateText').val());
                            $("input[type=checkbox]").show();
                            for(var i=0;i<data.fields.length;i++){
                                if(data.fields[i].status == 1){
                                    $('#checkbox'+data.fields[i].fieldMd5.substring(4)).prop("checked",true);
                                }
                            }
                            if(data.data.templateStatus == 1){
                                $('#templateName').attr("disabled","disabled");
                            }else{
                                $('#templateName').removeAttr("disabled");
                            }

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
                        sTitle: "状态",
                        mData: "templateStatus",
                        mRender: function (mData, type, full) {
                            if (mData == 1) {
                                return '<i title="激活" class="fa fa-check-circle status-icon statusOn"></i>';
                            } else if (mData == 0) {
                                return '<i title="未激活" class="fa fa-minus-circle status-icon statusOff"></i>';
                            } else {
                                return '<i title="未知" class="fa fa-circle status-icon statuNull"></i>';
                            }
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
                            // class="fa fa-pencil fa-fw"  class="fa fa-key"  class="'+(full.templateStatus==1?'fa fa-stop':'fa fa-play')+'" class="fa fa-trash-o"
                            return '<i><a title="编辑" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.update(\'' + mData + '\')">编辑</a></i>' +
                                '<i><a title="模板字段"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.fieldsList(\'' + mData + '\')">模板字段</a></i>' +
                                '<i><a title="'+(full.templateStatus==1?'停用':'启用')+'" ng-hide="loginUserMenuMap[currentView]" ng-click="listPage.action.active('+(full.templateStatus==1?'false':'true')+',\''+mData+'\')">'+(full.templateStatus==1?'停用':'启用')+'</a></i>'+
                                '<i><a title="删除"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a></i>'
                                // '<i title="发布" class="fa fa-cog fa-fw" ng-show=userLevel.indexOf("publish")!=-1  ng-click="listPage.action.publish(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "25%", aTargets: [ 0,3] },
                    { sWidth: "30%", aTargets: [ 4] },
                    { sWidth: "10%", aTargets: [ 1,2] }
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

            $scope.pageDialogDetail = Tools.dialog({
                id:"pageDialogDetail",
                title:"预览",
                hiddenButton:true,
                save:function(){
                }
            });
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
                    active: function (status,id) {
                        Loading.show();
                        loader.updateProcessStatus({"id":id,"status":status==true?1:0},{},function(data){
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
                    },
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    browse:function (id) {
                        $scope.hash="/workflow/process/previewImage?depId="+id;
                        Loading.show();
                        loader.previewInfo({"depId":id},function(data){
                            if(data.result == "success") {
                                $scope.details = data.flows;
                            }
                            Loading.hide();
                        })
                        $scope.pageDialogDetail.show();
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
                        sTitle: "版本",
                        // mData: "oacontractTemplate.templateName",
                        mData: "version",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "状态",
                        mData: "status",
                        mRender: function (mData, type, full) {
                            if (mData == 1) {
                                return '<i title="激活" class="fa fa-check-circle status-icon statusOn"></i>';
                            } else if (mData == 0) {
                                return '<i title="未激活" class="fa fa-minus-circle status-icon statusOff"></i>';
                            } else {
                                return '<i title="未知" class="fa fa-circle status-icon statuNull"></i>';
                            }
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"id",
                        mRender:function(mData,type,full) {
                            //class="fa fa-key" class="'+(full.status==1?'fa fa-stop':'fa fa-play')+'"  class="fa fa-trash-o"
                            // return '<i title="关联模板" class="fa fa-pencil fa-fw" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.edit(\'' + full.id  +'\','+ full.oacontractTemplate.templateId +',\''+ full.oacontractTemplate.templateName+ '\')"></i>' +
                            return '<i><a title="预览" ng-hide="loginUserMenuMap[currentView]"   ng-click="listPage.action.browse(\'' + mData + '\')">预览</a></i>' +
                                    '<i><a title="'+(full.status==1?'停用':'启用')+'" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.active('+(full.status==1?'false':'true')+',\''+mData+'\')">'+(full.status==1?'停用':'启用')+'</a></i>'+
                                    '<i><a title="删除" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a>';
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