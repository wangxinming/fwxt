/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
//    var web_path = "./data/user";
    'use strict';
    angular.module('audit', ['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider.when('/audit', {
                templateUrl: 'view/audit/audit.html',
                controller: 'audit.controller'
            });
        }])
        .controller('audit.controller', [ '$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function( $scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data:{
                    userName:null,
                    startTime: $filter('date')(new Date(current.getTime() - 30 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    limit: 10, //每页条数(即取多少条数据)
                    offset: 0, //从第几条数据开始取
                    orderBy: "createTime",//排序字段
                    orderByType: "desc" //排序顺序
                },
                action:{
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                action:{
                    search: function (search, fnCallback) {
                        $scope.searchPage.data.offset =search.offset;
                        var t   = $('#fromDateEx').val();
                        if(t != "") {
                            var date = new Date(Date.parse(t.replace(/-/g, "/")));
                            $scope.searchPage.data.startTime = $filter('date')(date, 'yyyy-MM-dd HH:mm:ss');
                        }
                        t = $('#toDateEx').val();
                        if(t != "") {
                            var date = new Date(Date.parse(t.replace(/-/g, "/")));
                            $scope.searchPage.data.endTime = $filter('date')(date, 'yyyy-MM-dd HH:mm:ss');
                        }
                        loader.auditList($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                            // $scope.searchPage.init();
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
                        sTitle: "用户",
                        mData: "userName",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "时间",
                        mData: "createTime",
                        mRender: function (mData, type, full) {
                            if(!mData){
                                return "";
                            }
                            return Util.formatSimpleDate(mData);
                        }
                    },
                    {
                        sTitle: "操作内容",
                        mData: "content",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2]},  //第 0,10列不可排序
                    { sWidth: "10%", aTargets: [ 0] },
                    { sWidth: "20%", aTargets: [ 1 ] },
                    { sWidth: "70%", aTargets: [ 2 ] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
        }]);
})(angular);