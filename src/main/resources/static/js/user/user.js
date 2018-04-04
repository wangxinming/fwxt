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
                uploadWordTemplate: {method: 'POST', url: "/batchImport", isArray: false,enctype:'multipart/form-data'},
                deployment: {method:'GET',url:"/api/deployments/deploymentList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                getTemplateHtml: {method:'GET',url:"/api/deployments/html", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                commitTemplateHtml: {method:'POST',url:"/api/deployments/commitHtml", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                modeler: {method:'GET',url:"/api/deployments/modelerList", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                uploadFile: {method:'GET',url:"/api/deployments/uploadFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                uploadFileInfo: {method:'GET',url:"/api/deployments/uploadFileInfo", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                saveUploadFileInfo: {method:'POST',url:"/api/deployments/saveUploadFile", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},

                query: {method:'GET',url:"/api/deployments/list", isArray:false,contentType:'application/json; charset=UTF-8',dataType:'json'},
                queryOperations: {method:'GET',url:web_path +"/user/getOperationInfos", isArray: false},
                updatePropertyFlag: {method:'POST',url:web_path +"/user/updatePropertyFlag", isArray: false},
                get: {method: 'GET', url: web_path + "/user/guid", isArray: false},
                reloadOperationInfos: {method: 'GET', url:  web_path +"/user/reloadOperationInfos", isArray: true},
                add: {method: "POST", url: web_path + "/user/guid", isArray: false},
                addPf: {method: "POST", url:web_path +"/user/addPf", isArray: false},
                addRe: {method: "POST", url:web_path +"/user/addRe", isArray: false},
                addFm: {method: "POST", url:web_path +"/user/addFm", isArray: false},
                addRf: {method: "POST", url:web_path +"/user/addRf", isArray: false},
                addPn: {method: "POST", url:web_path +"/user/addPn", isArray: false},
                edit: {method: "PUT", url: web_path + "/user/guid/:id", isArray: false},
                remove: {method: "DELETE", url: web_path + "/user/guid", isArray: false},
                removeModeler: {method: "DELETE", url:  "/api/deployments/removeModeler", isArray: false},

                removeDeployment: {method: "DELETE", url:  "/api/deployments/remove", isArray: false},
                deployDeployment: {method: "POST", url:  "/api/deployments/publish", isArray: false},
                getUsers: {method: 'GET', url: web_path + "/userManage/getUsers", isArray: true},
                getUserLevel: {method: 'GET', url: web_path + "/userManage/getUserLevel", isArray: false},
                updateUser: {method: 'POST', url: web_path + "/userManage/updateUser", isArray: false},
            });
        })
        .controller('user.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.cancelFilter = function (value){
                if (!value) return '';

                if(value.flag=='0'){
                    if(value.name.lastIndexOf(")")<0){
                        value.name+="(已作废)"
                    }
                }
                return value ;
            };
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
                    $scope.addPage.data.createrName = document.getElementById("userName").value;
                    var pfde = $scope.addPage.data.pf.description;
                    if(!pfde){
                        $scope.pfCheck=false;
                        return;
                    }else{
                        $scope.pfCheck=true;
                    }
                    var pfdes = pfde.split("_");
                    $scope.addPage.data.pf.id=pfdes[0];
                    $scope.addPage.data.pf.name=pfdes[1];
                    var fmde = $scope.addPage.data.fm.description;
                    if(!fmde){
                        $scope.fmCheck=false;
                        return;
                    }else{
                        $scope.fmCheck=true;
                    }
                    var fmdes = fmde.split("_");
                    $scope.addPage.data.fm.id=fmdes[0];
                    $scope.addPage.data.fm.name=fmdes[1];
                    var pageNamede = $scope.addPage.data.pageName.description;
                    if(!pageNamede){
                        $scope.pageNameCheck=false;
                        return;
                    }else{
                        $scope.pageNameCheck=true;
                    }
                    var pageNamedes = pageNamede.split("_");
                    $scope.addPage.data.pageName.id=pageNamedes[0];
                    $scope.addPage.data.pageName.name=pageNamedes[1];

                    var rede = $scope.addPage.data.re.description;
                    if(!rede){
                        $scope.reCheck=false;
                        return;
                    }else{
                        $scope.reCheck=true;
                    }
                    var redes = rede.split("_");
                    $scope.addPage.data.re.id=redes[0];
                    $scope.addPage.data.re.name=redes[1];
                    var rfde = $scope.addPage.data.rf.description;
                    if(!rfde){
                        $scope.rfCheck=false;
                        return;
                    }else{
                        $scope.rfCheck=true;
                    }
                    var rfdes = rfde.split("_");
                    $scope.addPage.data.rf.id=rfdes[0];
                    $scope.addPage.data.rf.name=rfdes[1];

                    Loading.show();
                    loader.add($scope.addPage.data,function(data){
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
                    // guid:"",
                    // des:"",
                    // createrName:"",
                    // pf: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
                    // fm: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
                    // re: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
                    // rf: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
                    // pageName: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
                    // user: {
                    //     id: 0,
                    //     name: "",
                    //     description: ""
                    // },
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
//                        level: [2, 3, 4, 5, 6],

                        // key:"",
                        process:{
                            id:0,
                            limit:0,
                            offset:10
                        },
                        id:1,
                        limit: 20, //每页条数(即取多少条数据)
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
            $scope.pageProperty={
                // createBy : document.getElementById("userName").value,
                checkNameExist: function (name,arr) {
                    if(arr.length==0){
                        return false;
                    }
                    for (var i = 0; i < arr.length; i++) {
                        if(name == arr[i].name){
                            return true;
                        }
                    }
                    return false;
                },
                isCancel: function (name,arr,flag) {

                    for (var i = 0; i < arr.length; i++) {
                        if(name == arr[i].id){
                            if(arr[i].flag==flag){
                                return true;
                            }else{
                                return false;
                            }
                        }
                    }
                    return false;
                },
                addPf: function () {
                    var name = $scope.pfText;
                    if($scope.pfText==''){
                        return ;
                    }
                    var isExist = $scope.pageProperty.checkNameExist(name,$scope.pfModel);
                    if(isExist){
                        toaster.pop('warning', "", "该名称已存在");
                        return ;
                    }

                    loader.addPf({'name':name,'createBy':$scope.pageProperty.createBy},{},function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            $scope.pfText='';
                            loader.reloadOperationInfos({'type':'pf'},function(data){
                                $scope.pfModel = data;
                            })
                        }
                    })
                },
                addFm: function () {
                    var name = $scope.fmText;


                    if($scope.fmText==''){
                        return ;
                    }
                    var isExist = $scope.pageProperty.checkNameExist(name,$scope.fmModel);
                    if(isExist){

                        toaster.pop('warning', "", "该名称已存在");
                        return ;
                    }
                    loader.addFm({'name':name,'createBy':$scope.pageProperty.createBy},{},function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            $scope.fmText='';
                            loader.reloadOperationInfos({'type':'fm'},function(data){
                                $scope.fmModel = data;
                            })
                        }
                    })
                },
                addPn: function () {
                    var name = $scope.pnText;

                    if($scope.pnText==''){
                        return ;
                    }
                    var isExist = $scope.pageProperty.checkNameExist(name,$scope.pnModel);
                    if(isExist){
                        toaster.pop('warning', "", "该名称已存在");
                        return ;
                    }
                    loader.addPn({'name':name,'createBy':$scope.pageProperty.createBy},{},function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            $scope.pnText='';
                            loader.reloadOperationInfos({'type':'pn'},function(data){
                                $scope.pnModel = data;
                            })
                        }
                    })
                },
                addRe: function () {
                    var name = $scope.reText;


                    if($scope.reText==''){
                        return ;
                    }
                    var isExist = $scope.pageProperty.checkNameExist(name,$scope.reModel);
                    if(isExist){
                        toaster.pop('warning', "", "该名称已存在");
                        return ;
                    }
                    loader.addRe({'name':name,'createBy':$scope.pageProperty.createBy},{},function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            $scope.reText='';
                            loader.reloadOperationInfos({'type':'re'},function(data){
                                $scope.reModel = data;
                            })
                        }
                    })
                },
                addRf: function () {
                    var name = $scope.rfText;

                    if($scope.rfText==''){
                        return ;
                    }
                    var isExist = $scope.pageProperty.checkNameExist(name,$scope.rfModel);
                    if(isExist){
                        toaster.pop('warning', "", "该名称已存在");
                        return ;
                    }
                    loader.addRf({'name':name,'createBy':$scope.pageProperty.createBy},{},function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            $scope.rfText='';
                            loader.reloadOperationInfos({'type':'rf'},function(data){
                                $scope.rfModel = data;
                            })
                        }
                    })
                },

                updateFlag: function (type,flag) {

                    var ids = '';

                    if(type=='rf'){
                        ids =  $scope.rfMultiple;

                        if(!ids||($scope.pageProperty.isCancel(ids,$scope.rfModel,flag))){
                            return;
                        }
                    }else  if(type=='re'){
                        ids =  $scope.reMultiple;
                        if(!ids||($scope.pageProperty.isCancel(ids,$scope.reModel,flag))){

                            return;
                        }
                    }else  if(type=='pn'){
                        ids =  $scope.pnMultiple;
                        if(!ids||($scope.pageProperty.isCancel(ids,$scope.pnModel,flag))){

                            return;
                        }
                    }else  if(type=='fm'){
                        ids =  $scope.fmMultiple;
                        if(!ids||($scope.pageProperty.isCancel(ids,$scope.fmModel,flag))){

                            return;
                        }
                    }else  if(type=='pf'){
                        ids =  $scope.pfMultiple;
                        if(!ids||($scope.pageProperty.isCancel(ids,$scope.pfModel,flag))){

                            return;
                        }
                    }

                    loader.updatePropertyFlag({'type':type,'flag':flag,'id':ids,'updateBy':$scope.pageProperty.createBy},{}, function(data){
                        if(data.result=="success"){
                            toaster.pop('success', "", "操作成功");
                            loader.reloadOperationInfos({'type':type},function(data){
                                if(type=='rf'){
                                    $scope.rfModel = data;
                                    $scope.rfMultiple="";
                                }else  if(type=='re'){
                                    $scope.reModel = data;
                                    $scope.reMultiple="";
                                }else  if(type=='pn'){
                                    $scope.pnModel = data;
                                    $scope.pnMultiple="";
                                }else  if(type=='fm'){
                                    $scope.fmModel = data;
                                    $scope.fmMultiple="";
                                }else  if(type=='pf'){
                                    $scope.pfModel = data;
                                    $scope.pfMultiple="";
                                }

                            })
                        }
                    })
                }

            };
            $scope.pnModel=[];
            $scope.fmModel=[];
            $scope.pfModel=[];
            $scope.reModel=[];
            $scope.rfModel=[];
            // $scope.pfModel =[{"id":1,"name":"pf1","description":null,"flag":"1"},{"id":2,"name":"pf0","description":null,"flag":"0"}];
            $scope.getSelectInfo  = function(){
                loader.queryOperations(null, function (data) {
                    $scope.pnModel =data.pnList;
                    $scope.fmModel =data.fmList;
                    $scope.pfModel =data.pfList;
                    $scope.reModel =data.reList;
                    $scope.rfModel =data.rfList;
                })
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
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.query($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            $scope.listPage.settings = {
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
                        sTitle: "流程key",
                        mData: "key",
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
                        sTitle: "资源bpmn文件",
                        mData: "resourceName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "资源png文件",
                        mData: "diagramResourceName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "部署对象ID",
                        mData: "deploymentId",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
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

                            return '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,7]}  //第 0,10列不可排序
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


            $scope.getSelectInfo();

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