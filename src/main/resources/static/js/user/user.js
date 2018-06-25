/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
//    var web_path = "./data/user";
    var web_path = "/api";
    'use strict';
    angular.module('webportal.user', ['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster','zTreeDirective'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/user', {
                templateUrl: 'view/user/user.html',
                controller: 'user.controller'
            })
            .when('/password', {
                    templateUrl: 'view/user/password.html',
                    controller: 'password.controller'
                })
            .when('/dashboard', {
                    templateUrl: 'view/user/dashboard.html',
                    controller: 'dashboard.controller'
            })
            .when('/enterprise', {
                templateUrl: 'view/user/enterprise.html',
                controller: 'enterprise.controller'
            })
            .when('/group', {
                    templateUrl: 'view/user/group.html',
                    controller: 'group.controller'
                });
        }])

        .directive('editZtree',["$compile","$timeout","Util",function($compile,$timeout,Util){
            return {
                link:function(scope, element, attrs) {
                    var ztreeId=attrs.id;
                    var ztreeData=Util.getValue(attrs.editZtree,scope);
                    var zTreeOnClick=function(event, treeId, treeNode){
                        if(treeNode.id && treeNode.id>0){
                            ztreeData.active(treeNode);
                        }
                    };
                    var settings={
                        view: {
                            addHoverDom: function(treeId, treeNode){
                                var sObj = angular.element("#" + treeNode.tId + "_a");
                                if (angular.element("#addBtn_"+treeNode.tId).length==0 && (treeNode.isJF==null || treeNode.isJF==false)){
                                    var add =$("<span class='button add' id='addBtn_" + treeNode.tId
                                        + "' title='添加节点' onfocus='this.blur();'></span>");
                                    sObj.append(add);
                                    add.bind("click", function(){
                                        ztreeData.add(treeNode);
                                    });
                                };

                                if (angular.element("#editBtn_"+treeNode.tId).length==0 && (treeNode.delete==null || treeNode.delete==false)){
                                    var edit =$("<span class='button edit' id='editBtn_" + treeNode.tId
                                        + "' title='编辑节点' onfocus='this.blur();'></span>");
                                    sObj.append(edit);
                                    edit.bind("click", function(){
                                        ztreeData.edit(treeNode);
                                    });
                                };

                                if (angular.element("#removeBtn_"+treeNode.tId).length==0 && (treeNode.delete==null || treeNode.delete==false)){
                                    var remove =$("<span class='button remove' id='removeBtn_" + treeNode.tId
                                        + "' title='删除节点' onfocus='this.blur();'></span>");
                                    sObj.append(remove);
                                    remove.bind("click", function(){
                                        ztreeData.remove(treeNode);
                                    });
                                };
                            },
                            removeHoverDom:function(treeId, treeNode){
                                angular.element("#addBtn_"+treeNode.tId).unbind().remove();
                                angular.element("#editBtn_"+treeNode.tId).unbind().remove();
                                angular.element("#removeBtn_"+treeNode.tId).unbind().remove();
                            },
                            selectedMulti: false
                        },
                        callback: {
                            onClick:zTreeOnClick
                        }
                    };
                    var initData=function(){
                        ztreeData=Util.getValue(attrs.editZtree,scope);
                        if(ztreeData.hideButton){
                            settings.view={};
                        }
                        if(ztreeData.settingData){
                            settings.data=ztreeData.settingData;
                        }
                        var treeObj =angular.element.fn.zTree.init(angular.element("#"+attrs.id), settings,ztreeData.data);
                        //treeObj.expandAll(true);
                        var nodes=treeObj.getNodes();

                        treeObj.selectNode(treeObj.getNodes()[0]);
                        if(treeObj.getNodes()[0]){
                            ztreeData.active(treeObj.getNodes()[0]);
                        }

                        if(nodes.length>0)treeObj.expandNode(nodes[0]);

                    };
                    return scope.$watch(attrs.editZtree,function(){
                        $timeout(initData,200);
                    },true);
                }
            };
        }])
        .directive('userZtree',["Util", function(Util) {
        return {
            restrict: 'AE',
            transclude:false,
            link: function(scope, element, attrs) {
                var ztreeData=Util.getValue(attrs.userZtree,scope);
                var on_treeData_change=function(){
                    var setting = {
                        view: {
                            selectedMulti: true
                        },
                        data:ztreeData.settingData?ztreeData.settingData:{},
                        callback: {
                            beforeClick: function(treeId, treeNode){
                                if(ztreeData.crossParent==null){
                                    if(treeNode.isParent){
                                        return false;
                                    }
                                }
                                click(treeNode);
                                return true;
                            },
                            onClick:function(event, treeId, treeNode){
                                var zTree =angular.element.fn.zTree.getZTreeObj(ztreeData.treeId)
                                zTree .checkNode(treeNode, !treeNode.checked, true, true);
//                                zTree.checkNode(treeNode, true, true);
                            },
                            beforeAsync:function(){
                                return true;
                            },
                            onCheck:function(event, treeId, treeNode){
                                check();
                            },
                            onExpand:function(event, treeId, treeNode){
                                if(ztreeData.onExpand){
                                    ztreeData.onExpand(treeNode)
                                }
                            },
                            onAsyncSuccess:function(event, treeId, treeNode, msg) {
                                if(ztreeData.checked){
                                    var zTree =angular.element.fn.zTree.getZTreeObj(ztreeData.treeId);
                                    zTree.checkAllNodes(false);
                                    var ids=ztreeData.checked.split(",");
                                    for(var i=0;i<ids.length;i++){
                                        var node =zTree.getNodeByParam("id",ids[i], null);
                                        if(node){
                                            zTree.checkNode(node, true,true);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    if(ztreeData.checkbox=="all"){
                        var checkAccessories=function(treeNode, btn) {
                            var r = document.getElementsByName("radio_"+treeNode.id);
                            if(r.length>0){
                                var checkedRadio = getCheckedRadio("radio_"+treeNode.id);
                                if (btn.attr("checked")) {
                                    if (!checkedRadio) {
                                        $("#radio_" + treeNode.children[0].id).attr("checked", true);
                                    }
                                } else {
                                    if (!checkedRadio)
                                        checkedRadio.attr("checked", false);
                                }
                            }else{
                                if (btn.attr("checked")) {
                                    $(":checkbox[name='checkbox_"+treeNode.id+"']").attr("checked", true);
                                } else {
                                    $(":checkbox[name='checkbox_"+treeNode.id+"']").removeAttr("checked");
                                }
                                $(":checkbox[name='checkbox_"+treeNode.id+"']").each(function(){
                                    $(this).change();
                                });
                            }
                        };
                        var checkBrand=function(treeNode, btn) {
                            if (btn.attr("checked")) {
                                var pObj = $("#checkbox_" + treeNode.getParentNode().id);
                                if (!pObj.attr("checked")) {
                                    pObj.attr("checked", true);
                                }
                            }
                        };
                        var getCheckedRadio=function (radioName) {
                            var r = document.getElementsByName(radioName);
                            for(var i=0; i<r.length; i++)    {
                                if(r[i].checked)    {
                                    return $(r[i]);
                                }
                            }
                            return null;
                        };

                        setting.view ={
                            addDiyDom: function(treeId, treeNode) {
                                var aObj = $("#" + treeNode.tId + "_a");
                                if (treeNode.level != ztreeData.level) {
                                    var pid=0;
                                    if(treeNode.getParentNode()){
                                        pid=treeNode.getParentNode().id;
                                    }
                                    var editStr = "<input type='checkbox' class='checkboxBtn' id='checkbox_" +treeNode.id+ "' name='checkbox_"+pid+"' onfocus='this.blur();'></input>";
                                    aObj.before(editStr);
                                    var btn = $("#checkbox_"+treeNode.id);
                                    if (btn) btn.bind("change", function() {checkAccessories(treeNode, btn);});
                                } else if (treeNode.level == ztreeData.level) {
                                    var editStr = "<input type='radio' class='radioBtn' id='radio_" +treeNode.id+ "' name='radio_"+treeNode.getParentNode().id+"' onfocus='this.blur();'></input>";
                                    aObj.before(editStr);
                                    var btn = $("#radio_"+treeNode.id);
                                    if (btn) btn.bind("click", function() {checkBrand(treeNode, btn);});
                                }
                            }
                        };
                    }else if(ztreeData.checkbox){
                        setting.check={
                            enable: true,
                            chkboxType: ztreeData.checkType?ztreeData.checkType:{"Y" : "s", "N" : "ps"},
                            chkStyle :ztreeData.checkbox=='radio'?'radio':'checkbox'
                        };
                        if(ztreeData.checkbox=='radio'){
                            setting.check.radioType="all";
                        }
                    }
                    var treeobj=angular.element.fn.zTree.init(angular.element("#"+ztreeData.treeId), setting,ztreeData.data);
                    if(ztreeData.selectReport){
                        var node=treeobj.getNodesByParam("key", ztreeData.selectReport, null);
                        if(node){
                            var pnode=treeobj.getNodeByTId(node[0].parentTId);
                            treeobj.expandNode(pnode, true, true, true);
                            treeobj.checkNode(node, true, true);
                            treeobj.selectNode(node,false);
                            treeobj.refresh();
                        }
                    }



                };
                var click=function(node){
                    if(ztreeData.treeClick){
                        ztreeData.treeClick(node);
                    }
                };
                var check=function(){
                    if(ztreeData.onCheck){
                        var treeObj = angular.element.fn.zTree.getZTreeObj(ztreeData.treeId);
                        var nodes =treeObj.getCheckedNodes(true);
                        ztreeData.onCheck(nodes);
                    }
                };
                return scope.$watch(attrs.userZtree, on_treeData_change, true);
            }
        };
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
                /*企业管理*/
                //创建分公司
                createEnterprise: {method:'PUT',url:"/user/enterprise", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新分公司
                updateEnterprise: {method:'POST',url:"/user/enterprise", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新分公司状态
                enterpriseUpdateStatus: {method:'POST',url:"/user/enterpriseStatus", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //删除分公司
                deleteEnterprise: {method:'DELETE',url:"/user/enterprise", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取分公司列表
                enterpriseList: {method:'GET',url:"/user/listEnterprise", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取分公司信息
                queryCompany: {method:'GET',url:"/user/queryCompany", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},


                /*用户管理*/
                //增加用户
                addUser: {method:'PUT',url:"/user/create", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //用户列表
                userList: {method:'GET',url:"/user/userList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取用户信息
                userInfo: {method:'GET',url:"/user/userInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取用户组信息
                userGroup: {method:'GET',url:"/user/userGroup", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取首页数据
                dashboard: {method:'GET',url:"/workflow/process/dashboard", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新用户
                updatePassword: {method:'POST',url:"/user/updatePassword", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新用户
                userUpdate: {method:'POST',url:"/user/update", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                /*用户组管理*/
                //创建组
                createGroup: {method:'PUT',url:"/user/group", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新组
                updateGroup: {method:'POST',url:"/user/group", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //更新组状态
                userUpdateStatus: {method:'POST',url:"/user/updateStatus", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //删除组
                deleteGroup: {method:'DELETE',url:"/user/group", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取组列表
                groupList: {method:'GET',url:"/user/list", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //按照ID获取组信息
                groupInfo: {method:'GET',url:"/user/getGroupById", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getPrivileges: {method:'GET',url:"/user/getPrivileges", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取权限列表
                loginMenus: {method:'GET',url:"/user/loginMenus", isArray:true,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取菜单树
                loginBar: {method:'GET',url:"/user/bars", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                /*任务管理*/
                //上传模板
                uploadWordTemplate: {method: 'POST', url: "template/batchImport", isArray: false,enctype:'multipart/form-data'},
                //拒绝任务
                rejectTask: {method:'POST',url:"/workflow/process/reject", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //恢复任务
                resumeTask: {method:'POST',url:"/workflow/process/jump", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //提交表单任务，创建工作流
                commitFormTask: {method:'POST',url:"/workflow/process/start", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                deployment: {method:'GET',url:"/api/deployments/deploymentList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取活跃流程
                deploymentContract: {method:'GET',url:"/api/deployments/deploymentContract", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getTemplateHtml: {method:'GET',url:"/api/deployments/html", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getTemplateHtmlHistory: {method:'GET',url:"/api/deployments/htmlHistory", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取审批人名字
                previewInfo: {method:'GET',url:"/workflow/process/previewInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                modelerReviewInfo: {method:'GET',url:"/workflow/process/modelerReviewInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                commitTemplateHtml: {method:'POST',url:"/api/deployments/commitHtml", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                modeler: {method:'GET',url:"/api/deployments/modelerList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新模板关系表
                updateTemRelation: {method:'POST',url:"/api/deployments/updateTemRelation", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //获取个人报表
                myReport: {method:'GET',url:"/report/myReport", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //法务报表统计
                fawuReport: {method:'GET',url:"/report/fawuReport", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新流程状态
                updateProcessStatus: {method:'POST',url:"/api/deployments/updateProcessStatus", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //更新文档状态
                templateUpdate: {method:'POST',url:"/api/deployments/templateUpdate", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //上传文件
                uploadFile: {method:'GET',url:"/api/deployments/uploadFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取上传文件详情更新
                uploadFileInfo: {method:'GET',url:"/api/deployments/uploadFileInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取上传文件详情
                concatFile: {method:'GET',url:"/api/deployments/concatFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                //获取上传文件详情增加
                uploadFileInfoAdd: {method:'GET',url:"/api/deployments/uploadFileInfoAdd", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取当前任务列表
                getTaskPending: {method:'GET',url:"/workflow/process/process", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取我发起的申请，草稿
                getMyTask: {method:'GET',url:"/workflow/process/myTask", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取我提交发起的申请
                commitedTask: {method:'GET',url:"/workflow/process/commitedTask", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
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

                //获取
                rejectReport: {method:'GET',url:"/report/rejectReport", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //获取word模板列表,所有
                getTemplateListTotal: {method: "GET", url:  "template/templateListTotal", isArray: false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                //登录菜单
                // loginMenus:{url:'/user/loginMenus',method:'GET',isArray:true},
                //部署流程
                deployDeployment: {method: "POST", url:  "/api/deployments/publish", isArray: false}
            });
        })
        .controller('dashboard.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.dashBoard = function () {
                Loading.show();
                loader.dashboard({},function (data) {
                    if(data.result == 'success'){
                        $('#myPending').text(data.myPending);
                        $('#myComplete').text(data.myComplete);
                        $('#initiator').text(data.initiator);
                        $scope.pendingList = data.pendingList;
                        $scope.initiatorList = data.initiatorList;
                    }
                    Loading.hide();
                }, function (error) {
                    Loading.hide();

                })
            };
            $scope.dashBoard();

        }])
        .controller('password.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.addPageUpdate={
                init:function(){
                    $scope.addPageUpdate.data={
                        userStatus:true
                    }
                },
                data: {
                    userStatus:true
                }
            };
            $scope.update = function () {
                if(!$scope.addPageUpdate.data.userPwd){
                    $('#userPsw').focus();
                    toaster.pop('failed', "", "原有密码不能为空");
                    return;
                }
                if(!$scope.addPageUpdate.data.userPwdNew){
                    $('#userPswNew').focus();
                    toaster.pop('failed', "", "新密码不能为空");
                    return;
                }
                if(!$scope.addPageUpdate.data.userPwdRenew){
                    $('#userPwdRenew').focus();
                    toaster.pop('failed', "", "新密码确认不能为空");
                    return;
                }

                if($scope.addPageUpdate.data.userPwdNew != $scope.addPageUpdate.data.userPwdRenew){
                    $scope.addPageUpdate.data.userPwdNew = "";
                    $scope.addPageUpdate.data.userPwdRenew = "";
                    $('#userPswNew').focus();
                    toaster.pop('failed', "", "新密码不一致");
                    return;
                }
                if($scope.addPageUpdate.data.userPwd == $scope.addPageUpdate.data.userPwdRenew){
                    $('#userPsw').focus();
                    $scope.addPageUpdate.data.userPwdNew = "";
                    $scope.addPageUpdate.data.userPwdRenew = "";
                    toaster.pop('failed', "", "新密码和老密码不能一样");
                    return;
                }

                Loading.show();
                $scope.addPageUpdate.data.operater = 'user';
                loader.updatePassword($scope.addPageUpdate.data,function(data){
                    if(data.result=="success"){
                        Loading.hide();
                        toaster.pop('success', "", "操作成功");
                        $scope.listPage.settings.reload();
                        $scope.pageDialogUpdate.hide();
                        $scope.addPageUpdate.init();
                    }else{
                        Loading.hide();
                        toaster.pop('warning', "", data.msg);
                    }
                }, function (error) {
                    Loading.hide();

                })
            }
        }])
        .controller('enterprise.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.addPage={
                data:{
                    companyStatus:true,
                    companyName:''
                },
                init:function(){
                    $scope.addPage.data = {
                        companyStatus:true,
                        companyName:""
                    }
                }
            };
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"创建公司",
                hiddenButton:true,
                save:function() {
                    Loading.show();
                    $scope.addPage.data.companyStatus = $scope.addPage.data.companyStatus === true?1:0;
                    if( $scope.pageDialog.title == "创建公司"){
                        loader.createEnterprise($scope.addPage.data,function(data){
                            Loading.hide();
                            $scope.pageDialog.hide();
                            $scope.addPage.init();
                            $scope.listPage.settings.reload(true);

                        }, function (error) {
                            Loading.hide();

                        })
                    }else{
                        loader.updateEnterprise($scope.addPage.data,function(data){
                            Loading.hide();
                            $scope.addPage.init();
                            $scope.pageDialog.hide();
                            $scope.listPage.settings.reload(true);

                        }, function (error) {
                            Loading.hide();

                        })
                    }

                }
            });
            $scope.searchPage = {
                data: {
                    id: 0,
                    groupName:'',
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
                        $scope.pageDialog.title = "创建公司";
                        $scope.addPage.init();
                        $scope.pageDialog.show();

                    },
                    edit: function (id) {
                        $scope.pageDialog.title = "修改公司";
                        Loading.show();
                        // $timeout(function(){
                        loader.queryCompany({"id":id},{},function (data) {
                            $scope.addPage.data.enterpriseId = data.data.enterpriseId;
                            $scope.addPage.data.companyName = data.data.companyName;
                            $scope.addPage.data.companyProvince = data.data.companyProvince;
                            $scope.addPage.data.companyCity = data.data.companyCity;
                            $scope.addPage.data.companyOwner = data.data.companyOwner;
                            $scope.addPage.data.ownerMobile = data.data.ownerMobile;
                            $scope.addPage.data.location = data.data.location;
                            $scope.addPage.data.subCompanyName = data.data.subCompanyName;
                            $scope.addPage.data.companyStatus = data.data.companyStatus==1?true:false;
                            $scope.addPage.data.createTime = data.data.createTime;
                            Loading.hide();
                            $scope.pageDialog.show();
                        }, function (error) {
                            Loading.hide();

                        })
                        // },500);
                    },
                    active: function (active,id,name) {
                        Loading.show();
                        loader.enterpriseUpdateStatus({"enterpriseId":id,"companyStatus":active==true?1:0},function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        },function (error) {
                            Loading.hide();
                        })

                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.deleteEnterprise({'enterpriseId': id}, function (data) {
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
                        loader.enterpriseList($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            };

            $scope.submit = function(e) {
                if(e.keyCode=="13"){
                    $scope.searchPage.action.search();
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
                        sTitle: "公司名称",
                        mData: "companyName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "负责人",
                        mData: "companyOwner",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "手机号码",
                        mData: "ownerMobile",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "公司状态",
                        mData: "companyStatus",
                        mRender: function (mData, type, full) {
                            return resolve(mData, type, full);
                        }
                    },
                    // {
                    //     sTitle: "省区",
                    //     mData: "companyProvince",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
                    // {
                    //     sTitle: "市区",
                    //     mData: "companyCity",
                    //     mRender: function (mData, type, full) {
                    //         return Util.str2Html(mData);
                    //     }
                    // },
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
                        sTitle: "操作",
                        mData:"enterpriseId",
                        mRender:function(mData,type,full) {
                            //class="fa fa-pencil" class="fa fa-trash-o" class="'+(full.companyStatus==1?'fa fa-stop':'fa fa-play')+'"
                            return  '<i><a title="编辑"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.edit(\'' + mData +'\')">编辑</a></i>' +
                                // '<i title="编辑" ng-hide="loginUserMenuMap[currentView]" class="fa fa-pencil" ng-click="listPage.action.edit(\'' + mData +'\')"> </i>' +
                                '<i><a title="删除"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a></i>'+
                                '<i><a title="'+(full.companyStatus==1?'停用':'启用')+'" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.active('+(full.companyStatus==1?'false':'true')+',\''+mData+'\',\''+full.companyStatus+'\')">'+(full.companyStatus==1?'停用':'启用')+'</a></i>';
                            // '<i title="删除" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-trash-o" ng-click="listPage.action.remove(\'' + mData + '\')"></i>';

                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4,5]},  //第 0,10列不可排序
                    { sWidth: "10%", aTargets: [ 3] },
                    { sWidth: "15%", aTargets: [ 0,1,2,4] },
                    { sWidth: "30%", aTargets: [5] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
        }])
        .controller('group.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {

            /*{id:1,name:"1",displayName:"rewrew",unit:"11",valType:"string",
                            children:[
                                {id:1,name:"2",displayName:"fhsdduf",unit:"11",valType:"string",checked:true},
                                {id:2,name:"3",displayName:"uuuu",unit:"11",valType:"string"},
                                {id:3,name:"4",displayName:"8888",unit:"11",valType:"string",checked:true},
                            ]}*/
            $scope.editPage = {
                datas:{
                    metricTree:{
                        data:[],
                        returnData:[],
                        checkType: { "Y" : "ps", "N" : "ps" },
                        checked:"",
                        treeId: 'locTree',
                        checkbox: "true",
                        level:2,
                        treeClick:function(){},
                        init:function(){
                            // Operate.getUserLocs({needCount:1,needJf:1},function(data){
                            //     for(var i=0;i<data.legnth;i++){
                            //         var row=data[i]
                            //         for(var j=0;j<row.children.length;j++){
                            //             row.children[j].open=true;
                            //         }
                            //     }
                            //     $scope.departTree.data=data;
                            // });
                        },
                        remove:function(node){
                            var tip="确定要删除吗？";
                            if(node.isParent){
                                tip="确定要删除该节点及其子节点吗？";
                            }
                            $rootScope.$confirm(tip,function(){
                                Loading.show();
                                Depart.remove({id:node.id},function(data){
                                    Loading.hide();
                                    $rootScope.$alert("删除成功");
                                    $scope.departDialog.node={
                                        upDisabled: true,
                                        downDisabled: true,
                                        rightDisabled: true,
                                        leftDisabled: true
                                    };
                                    if(data.result=="success"){
                                        var treeObj = angular.element.fn.zTree.getZTreeObj($scope.departTree.treeId);
                                        treeObj.removeNode(node);
                                    }
                                },function(data){
                                    Loading.hide();
                                });
                            },"删除");
                        },
                        add:function(node){
                            jQuery(".modal-body textarea").css({"width":"280px","height":"30px"});
                            // $scope.departDialog.title="新增";
                            // $scope.departDialog.model={isJF:0,delete:0};
                            // $scope.departDialog.model.name=null;
                            // $scope.departDialog.model.remark=null;
                            // $scope.departDialog.model.desc=null;
                            // if(node.name.indexOf("省局")>-1){
                            //     $scope.departDialog.model.orgLevel="省";
                            // }else if(node.orgLevel=="省"){
                            //     $scope.departDialog.model.orgLevel="市州";
                            // }else{
                            //     $scope.departDialog.model.orgLevel="区县";
                            // }
                            $scope.form.$setPristine();
                            // if(node) $scope.departDialog.model.pid=node.id;
                            // $scope.departDialog.show();
                        },
                        edit:function(node){
                            jQuery(".modal-body textarea").css({"width":"280px","height":"30px"});
                        },
                        active:function(node){
                            $scope.listPage.settings.reload();
                        },
                        onCheck:function(nodes){
                            // $scope.roleDailog.model.locId=null;
                            // if(nodes.length>0)$scope.roleDailog.model.locId=nodes[0].id;
                            $scope.$apply();
                        }
                    }
                },
                data:{metricsId:0}
            };
            // treeobj.expandNode(pnode, true, true, true);
            //
            $scope.groupDialog={
                groupName:'',
                description:'',
                flow:'0',
                task:'0',
                attachment:'0',
                oaPrivileges:[],
                init:function(){
                    $scope.groupDialog.groupName    =        '';
                    $scope.groupDialog.description    =        '';
                    $scope.groupDialog.flow         =       '0';
                    $scope.groupDialog.task         =       '0';
                    $scope.groupDialog.attachment         =   '0';
                    $scope.groupDialog.oaPrivileges =        [];
                }
            };
            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增组",
                hiddenButton:true,
                save:function() {
                    if(!$scope.groupDialog.groupName){
                        $('#name').focus();
                        toaster.pop('failed', "", "用户组名称不能为空");
                        return;
                    }

                    var treeObj = angular.element.fn.zTree.getZTreeObj($scope.editPage.datas.metricTree.treeId);
                    var allNodes = treeObj.transformToArray(treeObj.getNodes());
                    // var nodes=new Array();
                    for(var i=0;i<allNodes.length;i++){
                        var node=allNodes[i];
                        if(node.isParent) continue;
                        var s=node.getCheckStatus();
                        var tmp = {
                            privilegeId:node.id,
                            name:node.content
                        };
                        if(s==null){
                            $scope.groupDialog.oaPrivileges.push(tmp);
                            // nodes.push(node);
                        }else if(s.checked || s.half){
                            $scope.groupDialog.oaPrivileges.push(tmp);
                            // nodes.push(node);
                        }
                    }
                    Loading.show();
                    if( $scope.pageDialog.title == "新建组"){
                        loader.createGroup($scope.groupDialog,function(data){
                            Loading.hide();
                            $scope.pageDialog.hide();
                            $scope.groupDialog.init();
                            $scope.listPage.settings.reload(true);

                        }, function (error) {
                            Loading.hide();

                        })
                    }else{
                        loader.updateGroup($scope.groupDialog,function(data){
                            Loading.hide();
                            $scope.groupDialog.init();
                            $scope.pageDialog.hide();
                            $scope.listPage.settings.reload(true);

                        }, function (error) {
                            Loading.hide();

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
                    groupName:'',
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
                        $scope.pageDialog.title = "新建组";
                        Loading.show();
                        loader.getPrivileges({},{},function (data) {
                            Loading.hide();
                            $scope.editPage.datas.metricTree.data = data.data;
                        }, function (error) {
                            Loading.hide();

                        })
                        // $("#formPassword")[0].style.display = 'inherit';
                        $scope.pageDialog.show();
                        $scope.addPage.init();
                    },
                    edit: function (id) {
                        $scope.pageDialog.title = "修改组";
                        Loading.show();
                        // $timeout(function(){
                        loader.groupInfo({"groupId":id},{},function (data) {
                            $scope.groupDialog.groupId= id;
                            $scope.groupDialog.groupName = data.groupName;
                            $scope.groupDialog.description = data.describe;
                            $scope.groupDialog.flow = data.flow;
                            $scope.groupDialog.task = data.task;
                            $scope.groupDialog.attachment = data.attachment;
                            $scope.editPage.datas.metricTree.data = data.data;
                                // [ {id:1,name:"123",displayName:"123",unit:"11",valType:"string",
                                // children:[
                                //     {id:1,name:"123",displayName:"123",unit:"11",valType:"string",checked:true},
                                //     {id:2,name:"123",displayName:"123",unit:"11",valType:"string"},
                                //     {id:3,name:"123",displayName:"123",unit:"11",valType:"string",checked:true},
                                // ]}];
                            Loading.hide();
                            $scope.pageDialog.show();
                        }, function (error) {
                            Loading.hide();

                        })
                        // },500);
                    },
                    active: function (active,groupId,userName) {
                        Loading.show();
                        loader.userUpdateStatus({"groupId":groupId,"status":active==true?1:0},function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialog.hide();
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        },function (error) {
                            Loading.hide();
                        })

                    },
                    remove:function (id) {
                        $rootScope.$confirm("确定要删除吗？", function () {
                            Loading.show();
                            loader.deleteGroup({'id': id}, function (data) {
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
                        loader.groupList($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            };

            $scope.submit = function(e) {
                if(e.keyCode=="13"){
                    $scope.searchPage.action.search();
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
                        sTitle: "组名称",
                        mData: "groupName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "描述",
                        mData: "describe",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "组状态",
                        mData: "status",
                        mRender: function (mData, type, full) {
                            return resolve(mData, type, full);
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
                        sTitle: "操作",
                        mData:"groupId",
                        mRender:function(mData,type,full) {
                            //class="fa fa-pencil" class="fa fa-trash-o" class="'+(full.status==1?'fa fa-stop':'fa fa-play')+'"
                            return  '<i><a title="编辑"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.edit(\'' + mData +'\')"> 编辑</a></i>' +
                                // '<i title="编辑" ng-hide="loginUserMenuMap[currentView]" class="fa fa-pencil" ng-click="listPage.action.edit(\'' + mData +'\')"> </i>' +
                                    '<i><a title="删除"  ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.remove(\'' + mData + '\')">删除</a></i>'+
                                    '<i><a title="'+(full.status==1?'停用':'启用')+'" ng-hide="loginUserMenuMap[currentView]"  ng-click="listPage.action.active('+(full.status==1?'false':'true')+',\''+mData+'\',\''+full.status+'\')">'+(full.status==1?'停用':'启用')+'</a></i>';
                                // '<i title="删除" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-trash-o" ng-click="listPage.action.remove(\'' + mData + '\')"></i>';

                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "20%", aTargets: [ 0,1,2,3 ,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
            $scope.searchPage.init();
        }])
        .controller('user.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $scope.editPage = {
                datas:{
                    metricTree:{
                        data:[ {id:1,name:"123",displayName:"123",unit:"11",valType:"string",
                            children:[
                                {id:1,name:"123",displayName:"123",unit:"11",valType:"string"},
                                {id:2,name:"123",displayName:"123",unit:"11",valType:"string"},
                                {id:3,name:"123",displayName:"123",unit:"11",valType:"string"},
                            ]}],
                        checked:"",
                        settingData:{key:{name:"display"}},
                        hideButton:false,
                        treeId: 'locTree',
                        checkType: { "Y" : "", "N" : "" },
                        checkbox: "radio",
                        init:function(){
                            // Operate.getUserLocs({needCount:1,needJf:1},function(data){
                            //     for(var i=0;i<data.legnth;i++){
                            //         var row=data[i]
                            //         for(var j=0;j<row.children.length;j++){
                            //             row.children[j].open=true;
                            //         }
                            //     }
                            //     $scope.departTree.data=data;
                            // });
                        },
                        remove:function(node){
                            var tip="确定要删除吗？";
                            if(node.isParent){
                                tip="确定要删除该节点及其子节点吗？";
                            }
                            $rootScope.$confirm(tip,function(){
                                Loading.show();
                                Depart.remove({id:node.id},function(data){
                                    Loading.hide();
                                    $rootScope.$alert("删除成功");
                                    $scope.departDialog.node={
                                        upDisabled: true,
                                        downDisabled: true,
                                        rightDisabled: true,
                                        leftDisabled: true
                                    };
                                    if(data.result=="success"){
                                        var treeObj = angular.element.fn.zTree.getZTreeObj($scope.departTree.treeId);
                                        treeObj.removeNode(node);
                                    }
                                },function(data){
                                    Loading.hide();
                                });
                            },"删除");
                        },
                        add:function(node){
                            jQuery(".modal-body textarea").css({"width":"280px","height":"30px"});
                            $scope.departDialog.title="新增";
                            $scope.departDialog.model={isJF:0,delete:0};
                            $scope.departDialog.model.name=null;
                            $scope.departDialog.model.remark=null;
                            $scope.departDialog.model.desc=null;
                            if(node.name.indexOf("省局")>-1){
                                $scope.departDialog.model.orgLevel="省";
                            }else if(node.orgLevel=="省"){
                                $scope.departDialog.model.orgLevel="市州";
                            }else{
                                $scope.departDialog.model.orgLevel="区县";
                            }
                            $scope.form.$setPristine();
                            if(node) $scope.departDialog.model.pid=node.id;
                            $scope.departDialog.show();
                        },
                        edit:function(node){
                            jQuery(".modal-body textarea").css({"width":"280px","height":"30px"});
                            $scope.departDialog.title="编辑";
                            $scope.departDialog.model={delete:0,id:node.id,name:node.name,pid:node.pid,desc:node.desc,remark:node.remark,orgLevel:node.orgLevel,isJF:node.isJF==true?1:0};
                            $scope.departDialog.show();
                        },
                        active:function(node){
                            $scope.departDialog.depart=node;
                            $scope.searchPage.departId=node.id;
                            $scope.departDialog.isShow(node);
                            $scope.listPage.settings.reload();
                        },
                        onCheck:function(nodes){
                            // $scope.roleDailog.model.locId=null;
                            // if(nodes.length>0)$scope.roleDailog.model.locId=nodes[0].id;
                            $scope.$apply();
                        }
                    }
                },
                data:{metricsId:0}
            };


            // $scope.listPreparePage = {
            //     action: {
            //         metricNodeDom: function (data) {
            //             var htmlStr = "";
            //             if ("string" == data.valType) {
            //                 htmlStr = "<span style='width:130px;display: inline-block;'>" + data.displayName + "</span>" +
            //                     "<span style='width:80px;display: inline-block;'><select id='a" + data.id + "' style='width:80px;height: 20px;line-height: 20px;padding: 0px 0px;'><option value=''>请选择</option><option value='=='>==</option><option value='包含'>包含</option></select></span>" +
            //                     "<span style='width:80px;display: inline-block;'><input id='b" + data.id + "' placeholder='请输入' type='text' style='width:80px;height: 20px;line-height: 20px;padding: 0px 0px;'/></span>" +
            //                     "<span style='width:30px;display: inline-block;'>" + (data.unit ? data.unit : "") + "</span>";
            //             } else if ("enum" == data.valType) {
            //                 var opt = "";
            //                 $.each(data.enumMap, function (k, v) {
            //                     if (k == 'normal') {
            //                         opt += "<option value='" + k + "'>" + "正常" + "</option>";
            //                     } else if (k == 'warning') {
            //                         opt += "<option value='" + k + "'>" + "警告" + "</option>";
            //                     } else if (k == 'critical') {
            //                         opt += "<option value='" + k + "'>" + "危急" + "</option>";
            //                     } else if (k == 'shutdown') {
            //                         opt += "<option value='" + k + "'>" + "关闭" + "</option>";
            //                     } else if (k == 'notPresent') {
            //                         opt += "<option value='" + k + "'>" + "不存在" + "</option>";
            //                     } else if (k == 'notFunctioning') {
            //                         opt += "<option value='" + k + "'>" + "不工作" + "</option>";
            //                     } else {
            //                         opt += "<option value='" + k + "'>" + k + "</option>";
            //                     }
            //                 });
            //                 htmlStr = "<span style='width:130px;display: inline-block;'>" + data.displayName + "</span>" +
            //                     "<span style='width:80px;display: inline-block;'><select id='a" + data.id + "' style='width:80px;height: 20px;line-height: 20px;padding: 0px 0px;'><option value=''>请选择</option><option value='=='>==</option><option value='!='>!=</option></select></span>" +
            //                     "<span style='width:80px;display: inline-block;'><select id='b" + data.id + "' style='width:80px;height: 20px;line-height: 20px;padding: 0px 0px;'><option value=''>请选择</option>" + opt + "</select></span>" +
            //                     "<span style='width:30px;display: inline-block;'>" + (data.unit ? data.unit : "") + "</span>";
            //             } else {//double
            //                 htmlStr = "<span style='width:130px;display: inline-block;'>" + data.displayName + "</span>";
            //                 htmlStr += "<span style='width:80px;display: inline-block;'><select id='a" + data.id + "' style='width:80px;height: 20px;line-height: 20px;padding: 0px 0px;'><option value=''>请选择</option><option value='=='>==</option><option value='>'>></option><option value='<'><</option><option value='<='><=</option><option value='>='>>=</option></select></span>";
            //                 htmlStr += '<span style="width:80px;display: inline-block;">';
            //                 if (!data.unit) {
            //                     htmlStr += '<input id="b' + data.id + '" placeholder="请输入" maxlength="10" type="text" style="width:80px;height: 20px;line-height: 20px;padding: 0px 0px;" onblur="javascript:checkNumber(this);"/>';
            //                 } else if (data.unit && data.unit == "%") {
            //                     htmlStr += '<input id="b' + data.id + '" placeholder="请输入" maxlength="10" type="text" style="width:80px;height: 20px;line-height: 20px;padding: 0px 0px;" onblur="javascript:checkPercent(this);"/>';
            //                 } else {
            //                     htmlStr += '<input id="b' + data.id + '" placeholder="请输入" maxlength="10" type="text" style="width:80px;height: 20px;line-height: 20px;padding: 0px 0px;" onblur="javascript:a(this);"/>';
            //                 }
            //                 htmlStr += '</span>';
            //                 htmlStr += "<span style='width:30px;display: inline-block;'>" + (data.unit ? data.unit : "") + "</span>";
            //             }
            //             return htmlStr;
            //         }
            //     }
            // }
            // $scope.editPage.datas.metricTree = [{},{},{}];

            $scope.pageDialogDetail=Tools.dialog({
                id:"pageDialogDetail",
                title:"查看详情",
                hiddenButton:true,
                save:function() {

                }
            });
            $scope.addPageDetail = {
                init:function(){
                    $scope.addPageUpdate.data={userStatus:true}
                },
                data: {
                    userStatus:true
                }
            };

            $scope.pageDialogUpdate=Tools.dialog({
                id:"pageDialogUpdate",
                title:"修改密码",
                hiddenButton:false,
                save:function() {
                        // if($scope.addPageUpdate.data.userPwdNew != $scope.addPageUpdate.data.userPwdRenew){
                        //     $scope.addPageUpdate.data.userPwdNew = "";
                        //     $scope.addPageUpdate.data.userPwdRenew = "";
                        //     toaster.pop('failed', "", "新密码不一致");
                        //     return;
                        // }
                        // if($scope.addPageUpdate.data.userPwd == $scope.addPageUpdate.data.userPwdRenew){
                        //     $scope.addPageUpdate.data.userPwdNew = "";
                        //     $scope.addPageUpdate.data.userPwdRenew = "";
                        //     toaster.pop('failed', "", "新密码和老密码不能一样");
                        // }
                        if($scope.addPageUpdate.data.userPwdNew){

                        }

                        Loading.show();
                        loader.updatePassword($scope.addPageUpdate.data,function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                toaster.pop('success', "", "操作成功");
                                $scope.listPage.settings.reload();
                                $scope.pageDialogUpdate.hide();
                                $scope.addPageUpdate.init();
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        }, function (error) {
                            Loading.hide();
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
                hiddenButton:true,
                save:function() {
                    if ($scope.pageDialog.title === "新增用户") {
                        if(!$scope.addPage.data.userName){
                            $('#userName').focus();
                            toaster.pop('failed', "", "用户名不能为空");
                            return;
                        }else if(!$scope.addPage.data.userPwd){
                            $('#userPwd').focus();
                            toaster.pop('failed', "", "用户密码不能为空");
                            return;
                        }
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
                        }, function (error) {
                            Loading.hide();

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
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            });
            $scope.addPage={
                init:function(){
                    $scope.addPage.data={
                        userName:"",
                        userPwd:"",
                        userMobile:'',
                        userStatus:true}
                },
                data: {
                    userName:"",
                    userPwd:"",
                    userMobile:'',
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
                    searchInfo:function () {
                        loader.userList($scope.searchPage.data, function (data) {
                            if(data.total == 0 ){
                                $rootScope.$confirm("查询不到该人", function () {
                                }, '确认');
                            }else{
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();

                        });

                        // $scope.listPage.settings.reload(true);
                    },
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
                        $("#formPassword")[0].style.display = 'inherit';
                        $('#userName').removeAttr("disabled");
                        Loading.show();
                        loader.userGroup({},{},function (data) {
                            $scope.approvalGroup = data.group;
                            $scope.approvalEnterprise = data.company;
                            Loading.hide();
                        }, function (error) {
                            Loading.hide();

                        });
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
                        $("#formPassword")[0].style.display = 'none';
                        $('#userName').attr("disabled","disabled");
                        Loading.show();
                        // $timeout(function(){
                            loader.userInfo({"userId":id},{},function (data) {

                                $scope.addPage.data.userId = data.data.userId;
                                $scope.addPage.data.userName = data.data.userName;
                                $scope.addPage.data.userPwd = data.data.userPwd;
                                $scope.addPage.data.userMobile = data.data.userMobile;
                                $scope.addPage.data.userEmail = data.data.userEmail;
                                $scope.addPage.data.enterpriseId = data.data.enterpriseId;
                                $scope.addPage.data.userDepartment = data.data.userDepartment;
                                $scope.addPage.data.userPosition = data.data.userPosition;
                                $scope.addPage.data.userAddress = data.data.userAddress;
                                $scope.addPage.data.userPostcode = data.data.userPostcode;
                                $scope.addPage.data.userWeixin = data.data.userWeixin;
                                $scope.addPage.data.userStatus = data.data.userStatus===1?true:false;
                                $scope.approvalGroup = data.group;
                                $scope.approvalEnterprise = data.company;
                                $scope.addPage.data.groupId = data.data.groupId;
                                Loading.hide();
                                // $('#userName').attr("disabled","disabled");
                                $scope.pageDialog.show();
                                // $scope.addPage.init();
                            }, function (error) {
                                Loading.hide();

                            })
                        // },500);
                    },
                    detail:function (id) {
                        $scope.pageDialogDetail.title = "用户详情";
                        Loading.show();
                        // $timeout(function(){
                        loader.userInfo({"userId":id},{},function (data) {
                            $scope.addPageDetail.data.userId = data.data.userId;
                            $scope.addPageDetail.data.userName = data.data.userName;
                            $scope.addPageDetail.data.userMobile = data.data.userMobile;
                            $scope.addPageDetail.data.userGroup = data.data.groupId;
                            $scope.addPageDetail.data.userEmail = data.data.userEmail;
                            $scope.addPageDetail.data.enterpriseId = data.data.enterpriseId;
                            // $scope.addPageDetail.data.userCompany = data.data.userCompany;
                            $scope.addPageDetail.data.userDepartment = data.data.userDepartment;
                            $scope.addPageDetail.data.userPosition = data.data.userPosition;
                            $scope.addPageDetail.data.userAddress = data.data.userAddress;
                            $scope.addPageDetail.data.userPostcode = data.data.userPostcode;
                            $scope.addPageDetail.data.userWeixin = data.data.userWeixin;
                            $scope.addPageDetail.data.userStatus = data.data.userStatus===1?true:false;
                            $scope.approvalGroup = data.group;
                            $scope.approvalEnterprise = data.company;
                            Loading.hide();
                            // $('#userName').attr("disabled","disabled");
                            $scope.pageDialogDetail.show();
                            // $scope.addPage.init();
                        }, function (error) {
                            Loading.hide();

                        })
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
                        }, function (error) {
                            Loading.hide();

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
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };

            $scope.submit = function(e) {
                if(e.keyCode=="13"){
                    $scope.searchPage.action.search();
                }
            };
            var resolve = function (mData, type, full) {
                if (mData == 1) {
                    return '<a title="激活" class="fa fa-check-circle status-icon statusOn">激活</a>';
                } else if (mData == 0) {
                    return '<a title="未激活" class="fa fa-minus-circle status-icon statusOff">未激活</a>';
                } else {
                    return '<a title="未知" class="fa fa-circle status-icon statuNull">未知</a>';
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
                        sTitle: "电子邮箱",
                        mData: "userEmail",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },

                    {
                        sTitle: "手机号码",
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
                        //class="fa fa-info"  class="fa fa-pencil"  class="fa fa-user" class="+(full.userStatus==1?'fa fa-stop':'fa fa-play')+'" '
                        mRender:function(mData,type,full) {
                            return  '<i><a title="详情"  ng-click="listPage.action.detail(\'' + mData +'\')">详情</a></i>' +
                                    '<i><a title="编辑" ng-hide="loginUserMenuMap[currentView]" ng-click="listPage.action.edit(\'' + mData +'\')">编辑</a></i>' +
                                    '<i><a title="修改密码" ng-hide="loginUserMenuMap[currentView]" ng-click="listPage.action.update(\'' + mData +'\')">修改密码</a></i>' +
                                    '<i><a title="'+(full.userStatus==1?'停用':'启用')+'" ng-hide="loginUserMenuMap[currentView]" ' +
                                'ng-click="listPage.action.active('+(full.userStatus==1?'false':'true')+',\''+mData+'\',\''+full.userName+'\')">' + (full.userStatus==1?'停用':'启用')+
                                '</a></i>';
                                    // '<i title="删除" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-trash-o" ng-click="listPage.action.remove(\'' + mData + '\')"></i>';

                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4,5]},  //第 0,10列不可排序
                    { sWidth: "12.5%", aTargets: [ 0,2] },
                    { sWidth: "20%", aTargets: [ 1,3 ] },
                    { sWidth: "10%", aTargets: [ 4 ] },
                    { sWidth: "25%", aTargets: [ 5 ] }
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
        .controller('navBarController', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            var totalBefore = '     <ul>\n';
            var totalEnd = '    </ul>\n';
            var userParentBefore = '                        <li class="header-menu"></li>\n' +
                '                        <li class="sidebar-dropdown">\n' +
                '                            <a id="user"><i class="fa fa-user" ></i><span>用户管理模块</span><span class="label label-danger"></span></a>\n' +
                '                            <div class="sidebar-submenu">\n' +
                '                                <ul>';
            var user = '<li><a href="/index.html#/user">用户信息 <span class="label label-success"></span></a> </li>';
            var group = '<li><a href="/index.html#/group">用户组 <span class="label label-success"></span></a> </li>';
            var password = '<li><a href="/index.html#/password">修改密码 <span class="label label-success"></span></a> </li>';
            var enterprise = '<li><a href="/index.html#/enterprise">公司管理 <span class="label label-success"></span></a> </li>';
            var end = '                                </ul>\n' +
                '                            </div>\n' +
                '                        </li>';

            var uploadBefore = '                        <li class="sidebar-dropdown">\n' +
                '                            <a id="deployment"><i class="fa fa-calendar"></i><span>合同模板管理模块</span><span class="badge"></span></a>\n' +
                '                            <div class="sidebar-submenu" >\n' +
                '                                <ul>';
            var upload = '<li><a href="index.html#/upload">合同模板管理</a></li>';
            // var form = '<li><a href="index.html#/form">合同模板字段检查</a></li>';

            var deployParent = '                        <li class="sidebar-dropdown">\n' +
                '                            <a id="deploymentProcess"><i class="fa fa-tv"></i><span>合同流程管理模块</span><span class="badge"></span></a>\n' +
                '                            <div class="sidebar-submenu" >\n' +
                '                                <ul>';
            var modeler = '<li><a href="index.html#/modeler">合同流程设定</a></li>';
            var deployment = '<li><a href="index.html#/deployment">合同流程发布</a></li>';

            var processParent = ' <li class="sidebar-dropdown">\n' +
                '                            <a  id="process"><i class="fa fa-film"></i><span>合同日常流转模块</span></a>\n' +
                '                            <div class="sidebar-submenu">\n' +
                '                                <ul>';

            var process = '<li><a href="/index.html#/process">新合同建立</a></li>';
            // var myProcess = '<li><a href="/index.html#/myProcess">待处理合同</a></li>';
            var initiator = '<li><a href="/index.html#/initiator">我发起的合同</a></li>';

            var pendingParent = '  <li class="sidebar-dropdown">\n' +
                '                            <a id="shenpi"><i class="fa fa-tasks"></i><span>合同同归档管理模块</span></a>\n' +
                '                            <div class="sidebar-submenu" >\n' +
                '                                <ul>';
            var pending = ' <li><a href="/index.html#/pending">合同审核及批复</a></li>';
            var complete = ' <li><a href="/index.html#/complete">归档文件查询</a></li>';
            var reportParent = '  <li class="sidebar-dropdown">\n' +
                '                            <a id="report"><i class="fa fa-diamond"></i><span>合同分类统计模块</span></a>\n' +
                '                            <div class="sidebar-submenu">\n' +
                '                            <ul>';

            var privateReport  = ' <li><a href="/index.html#/privateReport">个人任务统计</a></li>';
            var fawu = '<li><a href="/index.html#/fawuReport">法务任务统计</a></li>';

            var logParent = '                <li class="sidebar-dropdown">\n' +
                '                            <a id="log"><i class="fa fa-bar-chart-o"></i><span>系统日志文件模块</span></a>\n' +
                '                            <div class="sidebar-submenu" >\n' +
                '                                <ul>';

            var log = '<li><a href="/index.html#/audit">管理员日志查询</a></li>';
            var bars = totalBefore;
            loader.loginBar(null, function (data) {
                    var tmp = '';
                    if(data.user){
                        tmp += user;
                    }
                    if(data.group){
                        tmp += group;
                    }
                    if(data.enterprise){
                        tmp += enterprise;
                    }
                    if(data.password){
                        tmp += password;
                    }
                    if(tmp.length > 0){
                        bars += userParentBefore;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.upload){
                        tmp += upload;
                    }
                    // if(data.form){
                    //     tmp += form;
                    // }
                    if(tmp.length > 0){
                        bars += uploadBefore;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.modeler){
                        tmp += modeler;
                    }
                    if(data.deployment){
                        tmp += deployment;
                    }
                    if(tmp.length > 0){
                        bars += deployParent;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.process){
                        tmp += process;
                    }
                    if(data.pending){
                        tmp += pending;
                    }
                    // if(data.myProcess){
                    //     tmp += myProcess;
                    // }
                    if(data.initiator){
                        tmp += initiator;
                    }
                    if(tmp.length > 0){
                        bars += processParent;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.complete){
                        tmp += complete;
                    }
                    if(tmp.length > 0){
                        bars += pendingParent;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.privateReport){
                        tmp += privateReport;
                    }
                    if(data.fawuReport){
                        tmp += fawu;
                    }
                    if(tmp.length > 0){
                        bars += reportParent;
                        bars += tmp;
                        bars += end;
                    }
                    tmp = '';
                    if(data.audit){
                        tmp += log;
                    }

                    if(tmp.length > 0){
                        bars += logParent;
                        bars += tmp;
                        bars += end;
                    }
                bars+=totalEnd;
                $('#menuBar').html(bars);
                $(".sidebar-dropdown > a").click(function(){
                    $(".sidebar-submenu").slideUp(250);
                    if ($(this).parent().hasClass("active")){
                        $(".sidebar-dropdown").removeClass("active");
                        $(this).parent().removeClass("active");
                    }else{
                        $(".sidebar-dropdown").removeClass("active");
                        $(this).next(".sidebar-submenu").slideDown(250);
                        $(this).parent().addClass("active");
                    }

                });

                switch ($rootScope.currentView){
                    case 'user':
                        $('#mbx1').html('<a href="index.html#/user"><span class="fa fa-angle-double-right"></span> 用户管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/user"><span class="fa fa-angle-double-right"></span> 用户信息</a>');
                        if (!$("#user").parent().hasClass("active")) {
                            $("#user").trigger("click");
                        }
                        break;
                    case 'group':
                        $('#mbx1').html('<a href="index.html#/group"><span class="fa fa-angle-double-right"></span> 用户管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/group"><span class="fa fa-angle-double-right"></span> 用户组</a>');
                        if (!$("#user").parent().hasClass("active")) {
                            $("#user").trigger("click");
                        }
                        break;
                    case 'enterprise':
                        $('#mbx1').html('<a href="index.html#/enterprise"><span class="fa fa-angle-double-right"></span> 用户管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/enterprise"><span class="fa fa-angle-double-right"></span> 公司管理</a>');
                        if (!$("#user").parent().hasClass("active")) {
                            $("#user").trigger("click");
                        }
                        break;
                    case 'password':
                        $('#mbx1').html('<a href="index.html#/group"><span class="fa fa-angle-double-right"></span> 用户管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/group"><span class="fa fa-angle-double-right"></span> 修改密码</a>');
                        if (!$("#user").parent().hasClass("active")) {
                            $("#user").trigger("click");
                        }
                        break;

                    case 'upload':
                        $('#mbx1').html('<a href="index.html#/upload"><span class="fa fa-angle-double-right"></span> 合同模板管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/upload"><span class="fa fa-angle-double-right"></span> 合同模板管理</a>');
                        if (!$("#deployment").parent().hasClass("active")) {
                            $("#deployment").trigger("click");
                        }
                        break;
                    case 'form':
                        $('#mbx1').html('<a href="index.html#/form"><span class="fa fa-angle-double-right"></span> 合同模板管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/form"><span class="fa fa-angle-double-right"></span> 合同模板字段检查</a>');
                        if (!$("#deployment").parent().hasClass("active")) {
                            $("#deployment").trigger("click");
                        }
                        break;
                    case 'modeler':
                        $('#mbx1').html('<a href="index.html#/modeler"><span class="fa fa-angle-double-right"></span> 合同流程管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/modeler"><span class="fa fa-angle-double-right"></span> 合同流程设定</a>');
                        if (!$("#deploymentProcess").parent().hasClass("active")) {
                            $("#deploymentProcess").trigger("click");
                        }
                        break;

                    case 'deployment':
                        $('#mbx1').html('<a href="index.html#/deployment"><span class="fa fa-angle-double-right"></span> 合同流程管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/deployment"><span class="fa fa-angle-double-right"></span> 合同流程发布</a>');
                        if (!$("#deploymentProcess").parent().hasClass("active")) {
                            $("#deploymentProcess").trigger("click");
                        }
                        break;
                    case 'process':
                        $('#mbx1').html('<a href="index.html#/process"><span class="fa fa-angle-double-right"></span> 合同日常流转模块</a>');
                        $('#mbx2').html('<a href="index.html#/process"><span class="fa fa-angle-double-right"></span> 新合同建立</a>');
                        if (!$("#process").parent().hasClass("active")) {
                            $("#process").trigger("click");
                        }
                        break;
                    case 'myProcess':
                        $('#mbx1').html('<a href="index.html#/myProcess"><span class="fa fa-angle-double-right"></span> 合同日常流转模块</a>');
                        $('#mbx2').html('<a href="index.html#/myProcess"><span class="fa fa-angle-double-right"></span> 待处理合同</a>');
                        if (!$("#process").parent().hasClass("active")) {
                            $("#process").trigger("click");
                        }
                        break;
                    case 'initiator':
                        $('#mbx1').html('<a href="index.html#/initiator"><span class="fa fa-angle-double-right"></span> 合同日常流转模块</a>');
                        $('#mbx2').html('<a href="index.html#/initiator"><span class="fa fa-angle-double-right"></span> 我发起的合同</a>');
                        if (!$("#process").parent().hasClass("active")) {
                            $("#process").trigger("click");
                        }
                        break;
                    case 'pending':
                        $('#mbx1').html('<a href="index.html#/pending"><span class="fa fa-angle-double-right"></span> 合同同归档管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/pending"><span class="fa fa-angle-double-right"></span> 合同审核及批复</a>');
                        if (!$("#process").parent().hasClass("active")) {
                            $("#process").trigger("click");
                        }
                        break;
                    case 'complete':
                        $('#mbx1').html('<a href="index.html#/complete"><span class="fa fa-angle-double-right"></span> 合同同归档管理模块</a>');
                        $('#mbx2').html('<a href="index.html#/complete"><span class="fa fa-angle-double-right"></span> 归档文件查询</a>');
                        if (!$("#shenpi").parent().hasClass("active")) {
                            $("#shenpi").trigger("click");
                        }
                        break;

                    case 'privateReport':
                        $('#mbx1').html('<a href="index.html#/privateReport"><span class="fa fa-angle-double-right"></span> 合同分类统计模块</a>');
                        $('#mbx2').html('<a href="index.html#/privateReport"><span class="fa fa-angle-double-right"></span> 个人任务统计</a>');
                        if (!$("#report").parent().hasClass("active")) {
                            $("#report").trigger("click");
                        }
                        break;
                    case 'fawuReport':
                        $('#mbx1').html('<a href="index.html#/fawuReport"><span class="fa fa-angle-double-right"></span> 合同分类统计模块</a>');
                        $('#mbx2').html('<a href="index.html#/fawuReport"><span class="fa fa-angle-double-right"></span> 法务任务统计</a>');
                        if (!$("#report").parent().hasClass("active")) {
                            $("#report").trigger("click");
                        }
                        break;
                    case 'audit':
                        $('#mbx1').html('<a href="index.html#/audit"><span class="fa fa-angle-double-right"></span> 系统日志文件模块</a>');
                        $('#mbx2').html('<a href="index.html#/audit"><span class="fa fa-angle-double-right"></span> 管理员日志查询</a>');
                        if (!$("#log").parent().hasClass("active")) {
                            $("#log").trigger("click");
                        }
                        break;
                    case 'dashboard':
                    default:
                        $('#mbx1').html('');
                        $('#mbx2').html('');
                        break;
                }


            }, function (error) {
                Loading.hide();

            });

            // $('#menuBar').html( '    <ul>\n' +
            //     '                        <li class="header-menu"></li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="user"><i class="fa fa-user" ></i><span>用户管理模块</span><span class="label label-danger"></span></a>\n' +
            //     '                            <div class="sidebar-submenu">\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="/index.html#/user">新建用户 <span class="label label-success"></span></a> </li>\n' +
            //     '                                    <li><a href="/index.html#/password">修改密码 <span class="label label-success"></span></a> </li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="deployment"><i class="fa fa-calendar"></i><span>合同模板管理模块</span><span class="badge"></span></a>\n' +
            //     '                            <div class="sidebar-submenu" >\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="index.html#/upload">合同模板管理</a></li>\n' +
            //     '                                    <li><a href="index.html#/form">合同模板字段检查</a></li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="deploymentProcess"><i class="fa fa-tv"></i><span>合同流程管理模块</span><span class="badge"></span></a>\n' +
            //     '                            <div class="sidebar-submenu" >\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="index.html#/modeler">工作流定义</a></li>\n' +
            //     '                                    <li><a href="index.html#/deployment">已发布流程</a></li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a  id="process"><i class="fa fa-film"></i><span>合同日常流转模块</span></a>\n' +
            //     '                            <div class="sidebar-submenu">\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="/index.html#/process">新合同建立</a></li>\n' +
            //     '                                    <li><a href="/index.html#/myProcess">待处理申请</a></li>\n' +
            //     '                                    <li><a href="/index.html#/initiator">我发起的申请</a></li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="shenpi"><i class="fa fa-tasks"></i><span>合同同归档管理模块</span></a>\n' +
            //     '                            <div class="sidebar-submenu" >\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="/index.html#/pending">合同审核及批复</a></li>\n' +
            //     '                                    <li><a href="/index.html#/complete">归档文件查询</a></li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="report"><i class="fa fa-diamond"></i><span>合同分类统计模块</span></a>\n' +
            //     '                            <div class="sidebar-submenu">\n' +
            //     '                            <ul>\n' +
            //     '                                <li><a href="/index.html#/privateReport">个人任务统计</a></li>\n' +
            //     '                                <li><a href="/index.html#/fawuReport">法务任务统计</a></li>\n' +
            //     '                            </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '                        <li class="sidebar-dropdown">\n' +
            //     '                            <a id="log"><i class="fa fa-bar-chart-o"></i><span>系统日志文件模块</span></a>\n' +
            //     '                            <div class="sidebar-submenu" >\n' +
            //     '                                <ul>\n' +
            //     '                                    <li><a href="/index.html#/audit">管理员日志查询</a></li>\n' +
            //     '                                </ul>\n' +
            //     '                            </div>\n' +
            //     '                        </li>\n' +
            //     '\n' +
            //     '                    </ul>');
        }])
        .controller('navHeaderController', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$timeout',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$timeout) {
            $rootScope.loginUserMenuMap={};
            var foreachMenus=function(menus){
                for(var i=0;i<menus.length;i++){
                    var menu=menus[i];
                    $rootScope.loginUserMenuMap[menu.code]= !menu.permission;
                    // if(menu.children.length>0){
                    //     foreachMenus(menu.children);
                    // }
                }
            };
            loader.loginMenus(null, function (data) {
                foreachMenus(data);
            }, function (error) {
                Loading.hide();

            })
        }])
        .controller('userManagerController', ['$scope','user.loader','Loading','toaster',function($scope,loader,Loading,toaster) {
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
            }, function (error) {
                Loading.hide();

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
            }, function (error) {
                Loading.hide();

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
            }, function (error) {
                Loading.hide();

            })
        });
        $scope.getUserInfo();
    }],true);


})(angular);