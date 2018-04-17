/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
//    var web_path = "./data/user";
    var web_path = "/api";
    'use strict';
    angular.module('report', ['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster','highcharts-ng'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/report', {
                templateUrl: 'view/report/report.html',
                controller: 'report.controller'
            });
        }])
        .controller('report.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster',function($scope, $rootScope,loader,Util,Tools,Loading,toaster) {
            $scope.chartSeries =  [{
                name: 'Hestavollane',
                data: [4.3, 5.1, 4.3, 5.2, 5.4, 4.7, 3.5, 4.1, 5.6, 7.4, 6.9, 7.1,
                    7.9, 7.9, 7.5, 6.7, 7.7, 7.7, 7.4, 7.0, 7.1, 5.8, 5.9, 7.4,
                    8.2, 8.5, 9.4, 8.1, 10.9, 10.4, 10.9, 12.4, 12.1, 9.5, 7.5,
                    7.1, 7.5, 8.1, 6.8, 3.4, 2.1, 1.9, 2.8, 2.9, 1.3, 4.4, 4.2,
                    3.0, 3.0]
            }, {
                name: 'Voll',
                data: [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.3, 0.0,
                    0.0, 0.4, 0.0, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    0.0, 0.6, 1.2, 1.7, 0.7, 2.9, 4.1, 2.6, 3.7, 3.9, 1.7, 2.3,
                    3.0, 3.3, 4.8, 5.0, 4.8, 5.0, 3.2, 2.0, 0.9, 0.4, 0.3, 0.5, 0.4]
            }];
            $scope.chartConfigColoum = {
                options: {
                    chart: {
                        type: 'column'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        spline: {
                            marker: {
                                enabled: false
                            }
                        },
                        column: {
                            stacking: 'normal',
                            dataLabels: {
                                enabled: false,
                                color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'red'
                            }
                        }
                    }
                },
                yAxis: {
                    title: {
                        text: '包数'
                    }
                },
                legend:{
                    align:'right'
                },
                series: $scope.chartSeries,
                title: {
                    text: '实时流量'
                },
                credits: {
                    enabled: false
                },
                loading: false,
                size: {height:300}
            };

            $scope.pageDialog=Tools.dialog({
                id:"pageDialog",
                title:"新增用户",
                hiddenButton:false,
                save:function() {
                    if ($scope.pageDialog.title === "新增用户") {
                        Loading.show();
                        loader.addUser($scope.addPage.data,{},function(data){
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
                        loader.userUpdate($scope.addPage.data,{},function(data){
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
                    edit: function (name,mobile,email) {
                        $scope.pageDialog.title = "修改用户";
                        $scope.addPage.data.name = name;
                        $scope.addPage.data.mobile = mobile;
                        $scope.addPage.data.email = email;
                        $('#userName').attr("disabled","disabled");
                        $scope.pageDialog.show();
                    },
                    active: function (active,name,mobile) {
                        //TODO 更新激活状态
                        Loading.show();
                        loader.userUpdate({"mobile":mobile,"name":name,"active":active},{},function(data){
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
                        var k = ''==$scope.key? 'NULL' : $scope.key;
                        // $scope.searchPage.data.key =k;
                        $scope.searchPage.data.offset =search.offset;
                        loader.userList($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        })
                    }
                }
            };
            var resolve = function (mData, type, full) {
                if (mData == true) {
                    return '<i title="激活" class="fa fa-check-circle status-icon statusOn"></i>';
                } else if (mData == false) {
                    return '<i title="未激活" class="fa fa-minus-circle status-icon statusOff"></i>';
                } else {
                    return '<i title="未知" class="fa fa-circle status-icon statuNull"></i>';
                }
            };
            $scope.listPage.settings = {
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "用户名称",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "邮箱",
                        mData: "email",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },

                    {
                        sTitle: "手机",
                        mData: "mobile",
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
                        sTitle: "状态",
                        mData: "active",
                        mRender: function (mData, type, full) {
                            return resolve(mData, type, full);
                        }
                    },
                    {
                        sTitle: "操作",
                        mData:"name",
                        mRender:function(mData,type,full) {
                            return '<i title="编辑" ng-disabled="loginUserMenuMap[currentView]" class="fa fa-pencil" ng-click="listPage.action.edit(\'' + mData + '\',\''+full.mobile+'\',\''+full.email+'\')"> </i>' +
                                '<i title="'+(full.active?'停用':'启用')+'" class="'+(full.active?'fa fa-stop':'fa fa-play')+'" ng-click="listPage.action.active('+(full.active?'false':'true')+',\''+mData+'\',\''+full.mobile+'\')"></i>';
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

        }]);

})(angular);