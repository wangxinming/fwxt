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
            // $scope.myform = " <form action=\"/workflow/process/process\" method=\"get\">\n" +
            //     "          <p>First name: <input type=\"text\" name=\"fname\" /></p>\n" +
            //     "          <p>Last name: <input type=\"text\" name=\"lname\" /></p>\n" +
            //     "          <input type=\"submit\" value=\"Submit\" />\n" +
            //     "        </form>";
            // $('#tyu').html( " <form action=\"/workflow/process/process\" method=\"get\">\n" +
            //     "          <p>First name: <input type=\"text\" name=\"fname\" /></p>\n" +
            //     "          <p>Last name: <input type=\"text\" name=\"lname\" /></p>\n" +
            //     "          <input type=\"submit\" value=\"Submit\" />\n" +
            //     "        </form>");
            $scope.replaceDiv = function(){
                AJAX.GET('/models/newModel');
                // window.open("/modeler.html?modelId","_blank");
                // loader.getTemplateHtml(null,function(data){
                //     $('#tyu').html(data.result);
                // })
            }
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
                model:{},
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
                        // $rootScope.$confirm("确定要删除吗？", function () {
                        //     Loading.show();
                        //     loader.remove({'id': id}, {}, function (data) {
                        //         if (data.result == "success") {
                        //             Loading.hide();
                        //             $scope.listPage.settings.reload(true);
                        //         }
                        //     }, function (error) {
                        //         Loading.hide();
                        //     });
                        // }, '删除');
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
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [

                    {
                        sTitle: "模型编号",
                        mData: "id",
                        mRender: function (mData, type, full) {
                            var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                            return s;
                        }
                    },
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
                            return '<i title="编辑" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.update(\'' + mData + '\')"></i>' +
                                    '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>' +
                                    '<i title="发布" class="fa fa-cog fa-fw" ng-show=userLevel.indexOf("publish")!=-1  ng-click="listPage.action.publish(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,4]}  //第 0,10列不可排序
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

        }])
        .controller('upload.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            // $scope.myform = " <form action=\"/workflow/process/process\" method=\"get\">\n" +
            //     "          <p>First name: <input type=\"text\" name=\"fname\" /></p>\n" +
            //     "          <p>Last name: <input type=\"text\" name=\"lname\" /></p>\n" +
            //     "          <input type=\"submit\" value=\"Submit\" />\n" +
            //     "        </form>";
            // $('#tyu').html( " <form action=\"/workflow/process/process\" method=\"get\">\n" +
            //     "          <p>First name: <input type=\"text\" name=\"fname\" /></p>\n" +
            //     "          <p>Last name: <input type=\"text\" name=\"lname\" /></p>\n" +
            //     "          <input type=\"submit\" value=\"Submit\" />\n" +
            //     "        </form>");

            /** 验证文件是否导入成功  */
            $("#uploadForm").ajaxForm(function(data){
                $scope.listPage.settings.reload()
                // $('#htmlTemplateText').val(data.result);


            });
            $scope.uploadFile = function (){
                $.ajaxFileUpload({
                    method:"POST",
                    url:"/batchImport",            //需要链接到服务器地址
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
                    url:'/batchImport', //上传文件的路径
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
                // AJAX.GET('/models/newModel');
                // window.open("/modeler.html?modelId","_blank");
                loader.getTemplateHtml(null,function(data){
                    $('#htmlTemplate').html($('#htmlTemplateText').val());
                })
            }
            $scope.commitTemplate = function(){
                // AJAX.GET('/models/newModel');
                // window.open("/modeler.html?modelId","_blank");
                loader.commitTemplateHtml(null,function(data){
                    $('#htmlTemplate').html(data.result);
                })
            }
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
                    $scope.pageDialog.model.html = $('#htmlTemplateText').val();
                    $scope.pageDialog.model.name = $('#templateName').val();
                    $scope.pageDialog.model.des = $('#templateDes').val();
                    Loading.show();
                    loader.saveUploadFileInfo($scope.pageDialog.model,{},function(data){
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
                        $scope.pageDialog.title = "更新";

                        Loading.show();
                        loader.uploadFileInfo({'id':id},{},function (data) {
                            $scope.pageDialog.model.id = id;
                            $scope.pageDialog.model.name = data.data.name;
                            $scope.pageDialog.model.des = data.data.des;
                            // $scope.pageDialog.model.html = data.data.html;
                            $('#htmlTemplateText').val(data.data.html);
                            Loading.hide();
                        });
                        $scope.pageDialog.show();
                        // window.open("/modeler.html?modelId="+id,"_blank");

                        // $rootScope.$confirm("确定要删除吗？", function () {
                        //     Loading.show();
                        //     loader.remove({'id': id}, {}, function (data) {
                        //         if (data.result == "success") {
                        //             Loading.hide();
                        //             $scope.listPage.settings.reload(true);
                        //         }
                        //     }, function (error) {
                        //         Loading.hide();
                        //     });
                        // }, '删除');
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
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [

                    {
                        sTitle: "文档编号",
                        mData: "id",
                        mRender: function (mData, type, full) {
                            var s =  '<input  value="'+mData+'"  onClick="javascript:this.select()" class="tableReadOnlyInput">';
                            return s;
                        }
                    },
                    {
                        sTitle: "文档名称",
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
                            return '<i title="编辑" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.update(\'' + mData + '\')"></i>' +
                                '<i title="删除" class="fa fa-trash-o" ng-show=userLevel.indexOf("delete")!=-1  ng-click="listPage.action.remove(\'' + mData + '\')"></i>'
                                // '<i title="发布" class="fa fa-cog fa-fw" ng-show=userLevel.indexOf("publish")!=-1  ng-click="listPage.action.publish(\'' + mData + '\')"></i>';
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,4]}  //第 0,10列不可排序
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

        }])
        .controller('deployment.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
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
                    add: function (id) {
                        $scope.pageDialog.show();
                    },
                    edit: function (id) {
                        $scope.pageDialog.title="编辑";
                        var model=Util.findFromArray("id",id,$scope.listPage.data);
                        var notIn=true;
                        for(var i=0;i<model.roles.length;i++){
                            if(model.roles[i]==model.mainRoleId){
                                notIn=false;break;
                            }
                        }
                        if(notIn)model.mainRoleId=null;
                        $scope.userDialog.prevRoleId=model.mainRoleId;

                        $scope.editUser={id:model.id,name:model.name};
                        model.password="******";
                        $scope.userDialog.model=angular.copy(model);
                        var departs=model.departs;
                        var ids=",";
                        for(var i=0;i<departs.length;i++){
                            ids+=departs[i]+",";
                        }
                        $scope.pageDialog.show();

                        if(model.departs==null || model.departs.length==0){
                            $scope.userDialog.model.departs=null;
                            var rows=$scope.userDialog.departTree.data;
                            $scope.userDialog.departTree.data=[];
                            $timeout(function(){
                                $scope.userDialog.departTree.data=rows;
                            },100);
                        }else{
                            var departObj = angular.element.fn.zTree.getZTreeObj($scope.userDialog.departTree.treeId);
                            departObj.checkAllNodes(false);
                            var nodes=departObj.getCheckedNodes(false);
                            for(var i=0;i<nodes.length;i++){
                                if(ids.indexOf(","+nodes[i].id+",")>-1){
                                    departObj.checkNode(nodes[i],true,true);
                                    break;
                                }
                            }
                        }
                        var groups=model.groups;
                        var ids=",";
                        for(var i=0;i<groups.length;i++){
                            ids+=groups[i]+",";
                        }
                        var groupTree = angular.element.fn.zTree.getZTreeObj($scope.userDialog.groupTree.treeId);
                        groupTree.checkAllNodes(false);
                        var nodes=groupTree.getCheckedNodes(false);
                        for(var i=0;i<nodes.length;i++){
                            if(ids.indexOf(","+nodes[i].id+",")>-1){
                                groupTree.checkNode(nodes[i],true,true);
                            }
                        }
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

                            return '<i title="关联模板" class="fa fa-pencil fa-fw" ng-show=userLevel.indexOf("update")!=-1  ng-click="listPage.action.add(\'' + mData + '\')"></i>' +
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


            $scope.getSelectInfo();

        }]);
        })(angular);