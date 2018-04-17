/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
//    var web_path = "./data/user";
    var web_path = "/api";
    'use strict';
    angular.module('webportal.user', ['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/user', {
                templateUrl: 'view/user/user.html',
                controller: 'user.controller'
            });
        }])
        .directive('dialog',['Util',function(Util) {
            return {
                restrict: 'AE',
                transclude:true,
                scope: {
                    dialog: '=info',
                    model: '=data'
                },
                templateUrl: 'view/user/dialog.html',
                link: function(scope, element, attrs) {

                }
            };
        }])
        .factory('user.loader', function($resource){
            return $resource(web_path+'/:id', {}, {
                uploadWordTemplate: {method: 'POST', url: "template/batchImport", isArray: false,enctype:'multipart/form-data'},
                //拒绝任务
                rejectTask: {method:'POST',url:"/workflow/process/reject", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //恢复任务
                resumeTask: {method:'POST',url:"/workflow/process/jump", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //提交表单任务，创建工作流
                commitFormTask: {method:'POST',url:"/workflow/process/start", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                deployment: {method:'GET',url:"/api/deployments/deploymentList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getTemplateHtml: {method:'GET',url:"/api/deployments/html", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getTemplateHtmlHistory: {method:'GET',url:"/api/deployments/htmlHistory", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                commitTemplateHtml: {method:'POST',url:"/api/deployments/commitHtml", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                modeler: {method:'GET',url:"/api/deployments/modelerList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新模板关系表
                updateTemRelation: {method:'POST',url:"/api/deployments/updateTemRelation", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //增加用户
                addUser: {method:'PUT',url:"/user/create", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //用户列表
                userList: {method:'GET',url:"/user/userList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取用户信息
                userInfo: {method:'GET',url:"/user/userInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新用户
                updatePassword: {method:'POST',url:"/user/updatePassword", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新用户
                userUpdate: {method:'POST',url:"/user/update", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //上传文件
                uploadFile: {method:'GET',url:"/api/deployments/uploadFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取上传文件详情
                uploadFileInfo: {method:'GET',url:"/api/deployments/uploadFileInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取当前任务列表
                getTaskPending: {method:'GET',url:"/workflow/process/process", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取我发起的申请
                getMyTask: {method:'GET',url:"/workflow/process/myTask", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取历史完成任务列表
                getTaskCompleted: {method:'GET',url:"/workflow/process/processHistory", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取字段列表
                getFieldList: {method:'GET',url:"template/fieldList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //保存表单字段信息
                saveForm: {method:'POST',url:"template/updateFieldInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //完成工作流任务
                completedTask: {method:'POST',url:"/workflow/process/complete", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //保存上传word文档
                saveUploadFileInfo: {method:'POST',url:"/api/deployments/saveUploadFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取发布流程列表
                query: {method:'GET',url:"/api/deployments/list", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //获取word模板以及部署流程关联列表
                //审计信息列表
                auditList: {method:'GET',url:"/audit/auditList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //删除word模板
                removeWordTemplate: {method: "DELETE", url:  "template/deleteWordTemplate", isArray: false},

                //删除word模板
                removeModeler: {method: "DELETE", url:  "/api/deployments/removeModeler", isArray: false},
                //增加部署流程，关联word模板
                updateDeployment2Word: {method: "POST", url: "/user/addPn", isArray: false},
                //删除部署流程
                removeDeployment: {method: "DELETE", url:  "/api/deployments/remove", isArray: false},
                //获取word模板列表
                getTemplateList: {method: "GET", url:  "template/templateList", isArray: false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取word模板列表,所有
                getTemplateListTotal: {method: "GET", url:  "template/templateListTotal", isArray: false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //部署流程
                deployDeployment: {method: "POST", url:  "/api/deployments/publish", isArray: false}
            });
        })
        .controller('user.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.pageDialogUpdate=Tools.dialog({
                id:"pageDialogUpdate",
                title:"修改密码",
                hiddenButton:false,
                save:function() {
                        if($scope.addPageUpdate.data.userPwdNew != $scope.addPageUpdate.data.userPwdRenew){
                            $scope.addPageUpdate.data.userPwdNew = "";
                            $scope.addPageUpdate.data.userPwdRenew = "";
                            toaster.pop('failed', "", "新密码不一致");
                            return;
                        }
                        if($scope.addPageUpdate.data.userPwd == $scope.addPageUpdate.data.userPwdRenew){
                            $scope.addPageUpdate.data.userPwdNew = "";
                            $scope.addPageUpdate.data.userPwdRenew = "";
                            toaster.pop('failed', "", "新密码和老密码不能一样");
                        }
                        Loading.show();
                        loader.updatePassword($scope.addPageUpdate.data,function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                                $scope.addPage.init();
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        })
                    }
            });
            $scope.addPageUpdate={
                init:function(){
                    $scope.addPageUpdate.data={userStatus:true}
                },
                data: {
                    userStatus:true
                }
            };


            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增用户",
                hiddenButton:false,
                save:function() {
                    if ($scope.pageDialog.title === "新增用户") {
                        Loading.show();
                        $scope.addPage.data.userStatus = $scope.addPage.data.userStatus===true?1:0;
                        loader.addUser($scope.addPage.data,function(data){
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

                    } else if ($scope.pageDialog.title === "修改用户") {
                        Loading.show();
                        $scope.addPage.data.userStatus = $scope.addPage.data.userStatus===true?1:0;
                        loader.userUpdate($scope.addPage.data,function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                                $scope.addPage.init();
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        })
                    }
                }
            });
            $scope.addPage={
                init:function(){
                    $scope.addPage.data={userStatus:true}
                },
                data: {
                    userStatus:true
                }
            };
            $scope.searchPage = {
                data: {
                    id: 0,
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        id:0,
                        limit: 10, //每页条数(即取多少条数据)
                        offset: 0 //从第几条数据开始取
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
                        $scope.pageDialog.title = "新增用户";
                        $scope.pageDialog.show();
                        $scope.addPage.init();
                    },
                    update: function (id) {
                        $scope.pageDialogUpdate.title = "修改密码";
                        $scope.addPageUpdate.data.userId = id;
                        $scope.pageDialogUpdate.show();
                    },
                    edit: function (id) {
                        $scope.pageDialog.title = "修改用户";
                        Loading.show();
                        // $timeout(function(){
                            loader.userInfo({"userId":id},{},function (data) {
                                $scope.addPage.data.userId = data.userId;
                                $scope.addPage.data.userName = data.userName;
                                $scope.addPage.data.userMobile = data.userMobile;
                                $scope.addPage.data.userEmail = data.userEmail;
                                $scope.addPage.data.userCompany = data.userCompany;
                                $scope.addPage.data.userDepartment = data.userDepartment;
                                $scope.addPage.data.userPosition = data.userPosition;
                                $scope.addPage.data.userAddress = data.userAddress;
                                $scope.addPage.data.userPostcode = data.userPostcode;
                                $scope.addPage.data.userWeixin = data.userWeixin;
                                $scope.addPage.data.userStatus = data.userStatus===1?true:false;
                                Loading.hide();
                                // $('#userName').attr("disabled","disabled");
                                $scope.pageDialog.show();
                                // $scope.addPage.init();
                            })
                        // },500);
                    },
                    active: function (active,userId,userName) {
                        Loading.show();
                        loader.userUpdate({"userId":userId,"userStatus":active==true?1:0,"userName":userName},function(data){
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
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.remove({'id': id}, {}, function (data) {
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
                        loader.userList($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            var resolve = function (mData, type, full) {
                if (mData == 1) {
                    return '<i title="激活" class="fa fa-check-circle status-icon statusOn"></i>';
                } else if (mData == 0) {
                    return '<i title="未激活" class="fa fa-minus-circle status-icon statusOff"></i>';
                } else {
                    return '<i title="未知" class="fa fa-circle status-icon statuNull"></i>';
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "用户名称",
                        mData: "userName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "邮箱",
                        mData: "userEmail",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },

                    {
                        sTitle: "手机",
                        mData: "userMobile",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "创建时间",
                        mData: "userCreatetime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "状态",
                        mData: "userStatus",
                        mRender: function (mData, type, full) {
                            return resolve(mData, type, full);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"userId",
                        mRender:function(mData,type,full) {
                            return '<i title="编辑" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-pencil" ng-click="listPage.action.edit(\'' + mData +'\')"> </i>' +
                                    '<i title="修改密码" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-user" ng-click="listPage.action.update(\'' + mData +'\')"> </i>' +
                                    '<i title="'+(full.userStatus==0?'停用':'启用')+'" class="'+(full.userStatus==1?'fa fa-stop':'fa fa-play')+'" ng-click="listPage.action.active('+(full.userStatus==0?'false':'true')+',\''+mData+'\',\''+full.userName+'\')"></i>';
                                    // '<i title="删除" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-trash-o" ng-click="listPage.action.remove(\'' + mData + '\')"></i>';

                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,5]},  //第 0,10列不可排序
                    { sWidth: "15%", aTargets: [ 0,2,5 ] },
                    { sWidth: "20%", aTargets: [ 1,3 ] },
                    { sWidth: "10%", aTargets: [ 4 ] }
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
        }]) .controller('userManagerController', ['$scope','user.loader','Loading','toaster',function($scope,loader,Loading,toaster) {

        $scope.userLevel = document.getElementById("userLevel").value;
        $scope.userList=[];
        $scope.userManage={};
        $scope.userManage.user="";
        $scope.getUserInfo  = function(){
            if($scope.userList.length!=0){
                return;
            }
            loader.getUsers(null, function (data) {
                $scope.userList =data;
            })
        };
        $scope.updateUser  = function(){
            if(!$scope.userManage.user){
                return;
            }
            if($scope.userManage.level==undefined||$scope.userManage.level.length==0){
                $scope.userManage.level="";
            }
            Loading.show();
            loader.updateUser({'username':$scope.userManage.user,'level':$scope.userManage.level},{}, function(data){
                Loading.hide();
                if(data.result=="success"){
                    toaster.pop('success', "", "操作成功");
                }
            })
        };

        $scope.$watch("userManage.user", function (newVal, oldVal) {
            if(!newVal){
                $scope.userManage.level = "";
                return;
            }
            loader.getUserLevel({'username':newVal},{}, function(data){

                $scope.userManage.level = [];
                if (data.result!=null&&data.result.indexOf("add") > -1) {
                    $scope.userManage.level.push("add");
                }
                if (data.result!=null&&data.result.indexOf("delete") > -1) {
                    $scope.userManage.level.push("delete");
                }
                if (data.result!=null&&data.result.indexOf("update") > -1) {
                    $scope.userManage.level.push("update");
                }
                if (data.result!=null&&data.result.indexOf("admin") > -1) {
                    $scope.userManage.level.push("admin");
                }
            })
        });
        $scope.getUserInfo();
    }],true);


})(angular);