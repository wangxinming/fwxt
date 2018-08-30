/**
 * Created by wangxinming on 2018/04/02.
 */
(function(angular){
//    var web_path = "./data/user";
    var web_path = "/api";
    'use strict';
    angular.module('report', ['ngRoute','ngResource','dataTablesDirective','util.services','checkListDirective','toaster','highcharts-ng'])
        .config(['$routeProvider', function($routeProvider) {
            $routeProvider
                .when('/report', {
                templateUrl: 'view/report/report.html',
                controller: 'report.controller'})
                .when('/privateReport', {
                templateUrl: 'view/report/privateReport.html',
                controller: 'privateReport.controller'})
                .when('/parentEnterpriseReport', {
                    templateUrl: 'view/report/parentEnterpriseReport.html',
                    controller: 'parentEnterpriseReport.controller'})
                .when('/secondaryEnterpriseReport', {
                    templateUrl: 'view/report/secondaryEnterpriseReport.html',
                    controller: 'secondaryEnterpriseReport.controller'})
                .when('/thirdEnterpriseReport', {
                    templateUrl: 'view/report/thirdEnterpriseReport.html',
                    controller: 'thirdEnterpriseReport.controller'})
                .when('/locationEnterpriseReport', {
                    templateUrl: 'view/report/locationEnterpriseReport.html',
                    controller: 'locationEnterpriseReport.controller'})
                .when('/nonFormatEnterpriseReport', {
                    templateUrl: 'view/report/nonFormatEnterpriseReport.html',
                    controller: 'nonFormatEnterpriseReport.controller'})
                .when('/fieldEnterpriseReport', {
                    templateUrl: 'view/report/fieldsContractReport.html',
                    controller: 'fieldEnterpriseReport.controller'})
                .when('/rejectReport', {
                    templateUrl: 'view/report/rejectReport.html',
                    controller: 'rejectReport.controller'})
                .when('/fawuReport', {
                    templateUrl: 'view/report/fawuReport.html',
                    controller: 'fawuReport.controller'});
        }])
        .controller('rejectReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss')
                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss')

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
                    search: function (search, fnCallback) {
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
                        $scope.searchPage.data.offset = search.offset;
                        $scope.searchPage.data.limit = search.limit;
                        loader.rejectReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.searchPage.init();
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "用户",
                        mData: "name",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "拒绝次数",
                        mData: "y",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }

                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1]},  //第 0,10列不可排序
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };
        }])
        .controller('parentEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    headOffice:1
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        parentCompany:'',
                        contractPromoter:'',
                        contractType:'',
                        headOffice:1
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {
                        var uri = "report/export?headOffice="+ $scope.searchPage.data.headOffice+ +"&startTime"+$scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime+"&subCompany=true";
                        if($scope.searchPage.data.parentCompany){
                            uri += "&parentCompany="+ $scope.searchPage.data.parentCompany;
                        }
                        if($scope.searchPage.data.contractType){
                            uri += "&contractType="+ $scope.searchPage.data.contractType;
                        }
                        if($scope.searchPage.data.contractPromoter){
                            uri += "&contractPromoter="+ $scope.searchPage.data.contractPromoter
                        }
                        window.open(uri);
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',name:'',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                    $scope.chartSeriesPie[0].name = '发起合同数量';
                                }
                                if($scope.listPage.refuse) {
                                    $scope.chartSeriesPie[0].name = '被打回合同数量';
                                    tmp.y = $scope.listPage.data[i].refuse;
                                }
                                if($scope.listPage.complete) {
                                    $scope.chartSeriesPie[0].name = '存档合同数量';
                                    tmp.y = $scope.listPage.data[i].complete;
                                }
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
                    },
                    search:function () {
                        $scope.listPage.settings.reload(true);
                    }
                }
            };
            $scope.listPage = {
                data: [],
                graph:true,
                checkedList: [],
                checkAllRow: false,
                users: [],
                ready: false,
                action:{
                    getContractPromoter:function () {
                        loader.contractPromoter({"company":$scope.searchPage.data.parentCompany}, function (data) {
                            if (data.result == "success") {
                                $scope.contractPromoters = data.users;
                            }
                        }, function (error) {
                        });
                    },
                    load:function(){
                        Loading.show();
                        loader.queryParentsEnterprise({level:1}, function (data) {
                            if (data.result == "success") {
                                $scope.parentEnterprise = data.enterprises;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },
                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.parentEnterpriseReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.chartSeriesPie =  [{type: 'pie',data:[]}];
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                yAxis:{
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: ['Jan','Feb','Mar'],
                    type:'category',
                    visible:true,
                    labels: {
                        enabled:false,
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: true
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "部门名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    {
                        render: function (data, type) {
                            return '<div align="center"><input type="checkbox"></div>' ;
                        },
                        aTargets: [0,1,2,3,4] //最后一列
                    },
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.parentCompany", function (newVal, oldVal) {
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
        }])
        .controller('secondaryEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    subCompany:true,
                    headOffice:2
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        subCompany:true,
                        headOffice:2
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {

                        var uri = "report/export?headOffice="+ $scope.searchPage.data.headOffice+"&startTime"+ $scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime;

                        if($scope.searchPage.data.parentCompany){
                            uri += "&parentCompany="+ $scope.searchPage.data.parentCompany;
                        }
                        if($scope.searchPage.data.subCompany){
                            uri += "&subCompany="+ $scope.searchPage.data.subCompany;
                        }
                        if($scope.searchPage.data.contractPromoter){
                            uri += "&contractPromoter="+ $scope.searchPage.data.contractPromoter
                        }
                        if($scope.searchPage.data.contractType){
                            uri += "&contractType="+ $scope.searchPage.data.contractType
                        }
                        window.open(uri);
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            // $scope.chartConfigColumn.xAxis.categories =['Jan','Feb','Mar'];
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
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
                    getContractPromoter:function () {
                        loader.contractPromoter({"company":$scope.searchPage.data.parentCompany}, function (data) {
                            if (data.result == "success") {
                                $scope.contractPromoters = data.users;
                            }
                        }, function (error) {
                        });
                    },
                    load:function(){
                        Loading.show();
                        loader.queryParentsEnterprise({level:2}, function (data) {
                            if (data.result == "success") {
                                $scope.parentEnterprise = data.enterprises;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },

                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.parentEnterpriseReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                yAxis:{
                    title:'',
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: [],
                    labels: {
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "部门名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.parentCompany", function (newVal, oldVal) {
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
        }])
        .controller('thirdEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    subCompany:true,
                    headOffice:3
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        subCompany:true,
                        headOffice:3
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {
                        var uri = "report/export?headOffice="+ $scope.searchPage.data.headOffice+"&startTime"+ $scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime;

                        if($scope.searchPage.data.parentCompany){
                            uri += "&parentCompany="+ $scope.searchPage.data.parentCompany;
                        }
                        if($scope.searchPage.data.subCompany){
                            uri += "&subCompany="+ $scope.searchPage.data.subCompany;
                        }
                        if($scope.searchPage.data.contractPromoter){
                            uri += "&contractPromoter="+ $scope.searchPage.data.contractPromoter
                        }
                        if($scope.searchPage.data.contractType){
                            uri += "&contractType="+ $scope.searchPage.data.contractType
                        }
                        window.open(uri);

                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
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
                    getContractPromoter:function () {
                        loader.contractPromoter({"company":$scope.searchPage.data.parentCompany}, function (data) {
                            if (data.result == "success") {
                                $scope.contractPromoters = data.users;
                            }
                        }, function (error) {
                        });
                    },
                    load:function(){
                        Loading.show();
                        loader.queryParentsEnterprise({level:3}, function (data) {
                            if (data.result == "success") {
                                $scope.parentEnterprise = data.enterprises;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
                    },
                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.parentEnterpriseReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                yAxis:{
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: $scope.categories,
                    labels: {
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: true
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "部门名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.parentCompany", function (newVal, oldVal) {
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
        }])
        .controller('locationEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    headOffice:3
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        headOffice:3
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {
                        var uri = "report/locationReportExcel?startTime="+ $scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime;

                        if($scope.searchPage.data.location){
                            uri += "&location="+ $scope.searchPage.data.location;
                        }
                        if($scope.searchPage.data.province){
                            uri += "&province="+ $scope.searchPage.data.province;
                        }
                        if($scope.searchPage.data.city){
                            uri += "&city="+ $scope.searchPage.data.city
                        }
                        if($scope.searchPage.data.contractType){
                            uri += "&contractType="+ $scope.searchPage.data.contractType
                        }
                        window.open(uri);
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
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
                    load:function(){
                        Loading.show();
                        loader.locationList({}, function (data) {
                            if (data.result == "success") {
                                $scope.locations = data.locations;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
                    },
                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.locationReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                        // enabled: false
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                yAxis:{
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: $scope.categories,
                    labels: {
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: true
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "区域名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.location", function (newVal, oldVal) {
                loader.provinceList({"location":$scope.searchPage.data.location}, function (data) {
                    if (data.result == "success") {
                        $scope.provinces = data.provinces;
                    }
                }, function (error) {
                });
            }, true);
            $scope.$watch("searchPage.data.province", function (newVal, oldVal) {
                loader.cityList({"location":$scope.searchPage.data.location,"province":$scope.searchPage.data.province}, function (data) {
                    if (data.result == "success") {
                        $scope.cities = data.cities;
                    }
                }, function (error) {
                });
            }, true);
        }])
        .controller('nonFormatEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            $scope.companyLevels = [{id:1,name:'总公司部门'},{id:2,name:'二级单位'},{id:3,name:'三级单位'}];
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    subCompany:true,
                    headOffice:2
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取

                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        subCompany:true,
                        headOffice:2
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {
                        var uri = "report/fieldReportExcel?startTime="+ $scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime+"&templateType=custom";
                        if($scope.searchPage.data.headOffice){
                            uri += "&headOffice="+ $scope.searchPage.data.headOffice;
                        }
                        if($scope.searchPage.data.parentCompany){
                            uri += "&parentCompany="+ $scope.searchPage.data.parentCompany;
                        }
                        if($scope.searchPage.data.subCompany){
                            uri += "&subCompany="+ $scope.searchPage.data.subCompany;
                        }
                        if($scope.searchPage.data.contractPromoter){
                            uri += "&contractPromoter="+ $scope.searchPage.data.contractPromoter
                        }
                        window.open(uri);
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
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
                    load:function(){
                        Loading.show();
                        loader.queryParentsEnterprise({level:$scope.searchPage.data.headOffice}, function (data) {
                            if (data.result == "success") {
                                $scope.parentEnterprise = data.enterprises;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },
                    getContractPromoter:function () {
                        loader.contractPromoter({"company":$scope.searchPage.data.parentCompany,"subCompany":$scope.searchPage.data.subCompany}, function (data) {
                            if (data.result == "success") {
                                $scope.contractPromoters = data.users;
                            }
                        }, function (error) {
                        });
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
                    },
                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.nonFormatReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        // headerFormat: '{series.name}<br>',
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            // pointWidth:25, //柱子宽度
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                yAxis:{
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: $scope.categories,
                    labels: {
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: true
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "公司/部门名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.headOffice", function (newVal, oldVal) {
                $scope.listPage.action.load();
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                // $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
            $scope.$watch("searchPage.data.parentCompany", function (newVal, oldVal) {
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
        }])
        .controller('fieldEnterpriseReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {

            $scope.companyLevels = [{id:1,name:'总公司部门'},{id:2,name:'二级单位'},{id:3,name:'三级单位'}];
            var current = new Date();
            $scope.searchPage = {
                data: {
                    startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                    subCompany:true,
                    headOffice:2
                    // limit: 10, //每页条数(即取多少条数据)
                    // offset: 0 //从第几条数据开始取
                },
                init: function () {
                    $scope.searchPage.data = {
                        startTime: $filter('date')(new Date(current.getTime() - 365*24*60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                        endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss'),
                        subCompany:true,
                        headOffice:2
                    }
                },
                action:{
                    reset:function () {
                        $scope.searchPage.init();
                    },
                    export:function () {
                        var uri = "report/fieldReportExcel?startTime="+ $scope.searchPage.data.startTime+ "&endTime="+ $scope.searchPage.data.endTime;

                        if($scope.searchPage.data.headOffice){
                            uri += "&headOffice="+ $scope.searchPage.data.headOffice;
                        }
                        if($scope.searchPage.data.province){
                            uri += "&parentCompany="+ $scope.searchPage.data.parentCompany;
                        }
                        if($scope.searchPage.data.subCompany){
                            uri += "&subCompany="+ $scope.searchPage.data.subCompany
                        }
                        if($scope.searchPage.data.contractPromoter){
                            uri += "&contractPromoter="+ $scope.searchPage.data.contractPromoter
                        }
                        if($scope.searchPage.data.field){
                            uri += "&field="+ $scope.searchPage.data.field
                        }
                        if($scope.searchPage.data.condition){
                            uri += "&condition="+ $scope.searchPage.data.condition
                        }
                        if($scope.searchPage.data.contractType){
                            uri += "&contractType="+ $scope.searchPage.data.contractType
                        }
                        window.open(uri);
                    },
                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
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
                    getContractPromoter:function () {
                        loader.contractPromoter({"company":$scope.searchPage.data.parentCompany,"subCompany":$scope.searchPage.data.subCompany}, function (data) {
                            if (data.result == "success") {
                                $scope.contractPromoters = data.users;
                            }
                        }, function (error) {
                        });
                    },
                    load:function(){
                        Loading.show();
                        loader.queryParentsEnterprise({level:$scope.searchPage.data.headOffice}, function (data) {
                            if (data.result == "success") {
                                $scope.parentEnterprise = data.enterprises;
                                $scope.templates = data.templates;
                                Loading.hide();
                                $scope.listPage.settings.reload(true);
                            }
                        }, function (error) {
                            Loading.hide();
                        });
                    },

                    pie:function () {
                        var i = 0;
                        var total = {name: '发起合同数量', data: []};
                        var refuse = {name: '被打回合同数量', data: []};
                        var complete = {name: '存档合同数量', data: []};

                        if($scope.listPage.total){
                            i++;
                        }
                        if($scope.listPage.refuse){
                            i++;
                        }
                        if($scope.listPage.complete){
                            i++;
                        }
                        // if($scope.listPage.rate)i++;
                        if(i > 1) {
                            $scope.listPage.graph = false;
                            $scope.categories = [];
                            $scope.chartSeriesColumn = [];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                $scope.categories.push($scope.listPage.data[i].enterprise);
                                if($scope.listPage.total) {
                                    total.data.push($scope.listPage.data[i].total);
                                }
                                if($scope.listPage.refuse)
                                    refuse.data.push($scope.listPage.data[i].refuse);
                                if($scope.listPage.complete)
                                    complete.data.push($scope.listPage.data[i].complete);
                            }
                            $scope.chartConfigColumn.xAxis.categories = $scope.categories;
                            if($scope.listPage.total) {
                                $scope.chartSeriesColumn.push(total);
                            }
                            if($scope.listPage.refuse) {
                                $scope.chartSeriesColumn.push(refuse);
                            }
                            if($scope.listPage.complete) {
                                $scope.chartSeriesColumn.push(complete);
                            }
                            $scope.chartConfigColumn.series = $scope.chartSeriesColumn;
                        }
                        else {
                            $scope.listPage.graph = true;
                            $scope.chartSeriesPie = [{type: 'pie',data:[]}];
                            for(var i = 0;i<$scope.listPage.data.length;i++){
                                var tmp = {};
                                tmp.name = $scope.listPage.data[i].enterprise;
                                if($scope.listPage.total) {
                                    tmp.y = $scope.listPage.data[i].total;
                                }
                                if($scope.listPage.refuse)
                                    tmp.y = $scope.listPage.data[i].refuse;
                                if($scope.listPage.complete)
                                    tmp.y = $scope.listPage.data[i].complete;
                                // if($scope.listPage.rate)
                                //     tmp.add("y",$scope.listPage.data[i].rate);

                                $scope.chartSeriesPie[0].data.push(tmp);
                            }
                            $scope.chartConfigPie.series = $scope.chartSeriesPie;
                        }
                    },
                    search: function (search, fnCallback) {
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
                        // $scope.searchPage.data.offset = search.offset;
                        // $scope.searchPage.data.limit = search.limit;
                        loader.fieldReport($scope.searchPage.data, function (data) {
                            $scope.listPage.data = data.rows;
                            fnCallback(data);
                        }, function (error) {
                            Loading.hide();

                        });
                    }
                }
            };
            $scope.listPage.action.load();
            $scope.searchPage.init();
            $scope.categories=[];
            $scope.chartSeriesColumn =  [];
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: false,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: '',
                        pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>'
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeriesPie,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'column',
                        backgroundColor:'#eff3f8'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        series: {
                            dataLabels: {
                                enabled: true
                            }
                        },
                        column: {
                            pointPadding: 0.2,
                            borderWidth: 0,
                            maxPointWidth: 20
                        }
                    },
                    legend: {
                        align: 'center',
                        verticalAlign: 'top',
                        enabled: true
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                yAxis:{
                    min: 0,
                },
                xAxis: {
                    // categories: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                    categories: $scope.categories,
                    labels: {
                        rotation: 0,
                        style: {
                            fontSize: 12
                        }
                    }
                },

                credits: {
                    enabled: false
                },
                legend: {
                    enabled: true
                },
                series: $scope.chartSeriesColumn
                //     [{
                //     name: 'Tokyo',
                //     data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
                // }, {
                //     name: 'New York',
                //     data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
                // }, {
                //     name: 'London',
                //     data: [48.9, 38.8, 39.3, 41.4, 47.0, 48.3, 59.0, 59.6, 52.4, 65.2, 59.3, 51.2]
                // }, {
                //     name: 'Berlin',
                //     data: [42.4, 33.2, 34.5, 39.7, 52.6, 75.5, 57.4, 60.4, 47.6, 39.1, 46.8, 51.1]
                // }]
                ,
                title: {
                    text: ''
                }
            };
            $scope.listPage.settings = {
                pageSize:10,
                reload: null,
                getData:  $scope.listPage.action.search,//getData应指定获取数据的函数
                columns: [
                    {
                        sTitle: "部门名称",
                        mData: "enterprise",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "发起合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.total'><i></i></label></div>",
                        mData: "total",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "被打回合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.refuse'><i></i></label></div>",
                        mData: "refuse",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档合同数量<div class='checkbox'><label><input type='checkbox' ng-model='listPage.complete'><i></i></label></div>",
                        mData: "complete",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    },
                    {
                        sTitle: "存档/发起比例",
                        mData: "rate",
                        mRender: function (mData, type, full) {
                            return Util.str2Html(mData);
                        }
                    }
                ], //定义列的形式,mRender可返回html
                columnDefs: [
                    {bSortable: false, aTargets: [0,1,2,3,4]},  //第 0,10列不可排序
                    { sWidth: "28%", aTargets: [ 0] },
                    { sWidth: "18%", aTargets: [ 1,2,3,4] }
                ], //定义列的约束
                defaultOrderBy: [
                    [1, "desc"]
                ]  //定义默认排序列为第8列倒序
            };

            $scope.$watch("searchPage.data.headOffice", function (newVal, oldVal) {
                $scope.listPage.action.load();
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                // $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
            $scope.$watch("searchPage.data.contractType", function (newVal, oldVal) {
                // $scope.listPage.action.load();
                Loading.show();
                loader.getFieldList({id:$scope.searchPage.data.contractType,offset:0,limit:10000}, function (data) {
                    if (data.result == "success") {
                        $scope.templateFields = data.rows;
                        Loading.hide();
                    }
                }, function (error) {
                    Loading.hide();
                });
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                // $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);

            $scope.$watch("searchPage.data.parentCompany", function (newVal, oldVal) {
                // $scope.listPage.checkAllRow = newVal && newVal.length > 0 && newVal.length == $scope.listPage.data.length;
                $scope.listPage.action.getContractPromoter();
                // $scope.contractPromoters=[{"subCompanyName":"123"}];
            }, true);
        }])

        .controller('fawuReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            $scope.chartSeries = [{
                dataLabels: {
                    enabled: true,
                    rotation: -90,
                    color: '#eff3f8',
                    align: 'right',
                    format: '{point.y:.1f}', // one decimal
                    y: 10 // 10 pixels down from the top

                },
                name: "合同统计",
                maxPointWidth: 20,
                data: [],
                type: "column"
            }
            ];
            //按类型统计
            $scope.chartColumn = {
                    options: {
                        exporting: {
                            // 是否允许导出
                            enabled: false
                        },
                        chart: {
                            backgroundColor:'#eff3f8',
                            type: 'column'
                        },
                        plotOptions: {
                            series: {
                                dataLabels: {
                                    enabled: true
                                }
                            },
                            column: {
                                colorByPoint: true,
                                maxPointWidth: 20,
                                pointWidth:20,
                                events: {
                                    // click: function (event) {
                                    //     for (var i = 0; i < $scope.resource.moc.length; i++) {
                                    //         if ($scope.resource.moc[i].displayName == event.point.category) {
                                    //             window.location.href = 'index.html#/sourceInstance?mocpId=' + $scope.resource.moc[i].id;
                                    //             break;
                                    //         }
                                    //     }
                                    // }
                                }
                            }
                        },
                        legend: {
                            enabled: false
                        },
                        tooltip: {
                            enabled: false
                        }
                    },
                    maxPointWidth: 20,
                    title: {
                        text: ''
                    },
                    xAxis: {
                        type: 'category',
                        labels: {
                            rotation: -45,
                            style: {
                                fontSize: '13px',
                                fontFamily: 'Verdana, sans-serif'
                            }
                        }
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: ''
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    series: $scope.chartSeries
            };
            $scope.profile = {
                total:0,
                template:0,
                custom:0
            };
            var current = new Date();
            $scope.searchPage = {
                data:{
                    startTime: $filter('date')(new Date(current.getTime() - 365 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss')
                },
                action:{
                    search:function () {
                        Loading.show();
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
                        loader.fawuReport({"startTime": $scope.searchPage.data.startTime,"endTime":$scope.searchPage.data.endTime},function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                // toaster.pop('success', "", "操作成功");
                                $scope.profile.total = data.data.total;
                                $scope.profile.template = data.data.templateNum;
                                $scope.profile.custom = data.data.customNum;

                                // $scope.listPage.settings.reload();
                                $scope.chartColumn.yAxis.title.text = "模板合同统计";
                                $scope.chartSeries[0].data= data.data.reportItemList;
                                //     [
                                //     ['Shanghai', 24.2],
                                //     ['Beijing', 20.8],
                                //     ['Karachi', 14.9],
                                //     ['Shenzhen', 13.7],
                                //     ['Guangzhou', 13.1],
                                //     ['Istanbul', 12.7],
                                //     ['Mumbai', 12.4],
                                //     ['Moscow', 12.2],
                                //     ['São Paulo', 12.0],
                                //     ['Delhi', 11.7],
                                //     ['Kinshasa', 11.5],
                                //     ['Tianjin', 11.2],
                                //     ['Lahore', 11.1],
                                //     ['Jakarta', 10.6],
                                //     ['Dongguan', 10.6],
                                //     ['Lagos', 10.6]
                                // ];
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            };
            $scope.searchPage.action.search();
        }])
        .controller('privateReport.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            $scope.chartSeries =  [{
                type: 'pie',
                name: '浏览器访问量占比',
                data: [
                    ['Firefox',   45.0],
                    ['IE',       26.8],
                    {
                        name: 'Chrome',
                        y: 12.8
                        // sliced: true, // 突出显示这个点（扇区），用于强调。
                    },
                    ['Safari',    8.5],
                    ['Opera',     6.2],
                    ['其他',   0.7]
                ]
            }];

            $scope.chartSeriesColumn =  [{
                type: 'bubble',
                name: '浏览器访问量占比',
                lineWidth: 0,
                data: [
                    ['Firefox',   45.0],
                    ['IE',       26.8],
                    {
                        name: 'Chrome',
                        y: 12.8
                        // sliced: true, // 突出显示这个点（扇区），用于强调。
                    },
                    ['Safari',    8.5],
                    ['Opera',     6.2],
                    ['其他',   0.7]
                ]
            }];
            $scope.total = 100;

            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        backgroundColor:'#eff3f8',
                        type: 'pie'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                format: '<b>{point.name}</b>: {point.y:.1f} 个',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeries,
                title: {
                    text: ''
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'bubble',
                        plotBorderWidth: 1,
                        zoomType: 'xy'
                    }
                },
                xAxis:{
                    gridLineWidth: 1
                },
                yAxis:{
                    startOnTick: false,
                    endOnTick: false
                },
                title:{
                    text: '工单分布图'
                },
                series:[{
                    data: [
                        [9, 81, 63],
                        [98, 5, 89],
                        [51, 50, 73],
                        [41, 22, 14],
                        [58, 24, 20],
                        [78, 37, 34],
                        [55, 56, 53],
                        [18, 45, 70],
                        [42, 44, 28],
                        [3, 52, 59],
                        [31, 18, 97],
                        [79, 91, 63],
                        [93, 23, 23],
                        [44, 83, 22]
                    ],
                    marker: {
                        fillColor: {
                            radialGradient: { cx: 0.4, cy: 0.3, r: 0.7 },
                            stops: [
                                [0, 'rgba(255,255,255,0.5)'],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0.5).get('rgba')]
                            ]
                        }
                    }
                }, {
                    data: [
                        [42, 38, 20],
                        [6, 18, 1],
                        [1, 93, 55],
                        [57, 2, 90],
                        [80, 76, 22],
                        [11, 74, 96],
                        [88, 56, 10],
                        [30, 47, 49],
                        [57, 62, 98],
                        [4, 16, 16],
                        [46, 10, 11],
                        [22, 87, 89],
                        [57, 91, 82],
                        [45, 15, 98]
                    ],
                    marker: {
                        fillColor: {
                            radialGradient: { cx: 0.4, cy: 0.3, r: 0.7 },
                            stops: [
                                [0, 'rgba(255,255,255,0.5)'],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[1]).setOpacity(0.5).get('rgba')]
                            ]
                        }
                    }
                }
                ]
            };
            // $scope.chartConfigColoum.series.push({"data": $scope.chartSeries });
            var current = new Date();
            $scope.searchPage = {
                data:{
                    startTime: $filter('date')(new Date(current.getTime() - 365 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss')
                },
                action:{
                    search:function () {
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
                        Loading.show();
                        loader.myReport({"startTime": $scope.searchPage.data.startTime,"endTime":$scope.searchPage.data.endTime},function(data){
                            if(data.result=="success"){
                                Loading.hide();
                                // toaster.pop('success', "", "操作成功");
                                // $scope.listPage.settings.reload();
                                $scope.chartSeries.name = "个人任务统计";
                                if(data.data.completedCount == 0 && data.data.involveCount ==0){
                                    $scope.chartConfigPie.series =[{
                                        type: 'pie',
                                        name: '任务',
                                        data: []
                                    }]
                                }else {
                                    $scope.chartConfigPie.title.text="工单";
                                    $scope.chartConfigPie.series = [{
                                        type: 'pie',
                                        name: '任务',
                                        data: [
                                            {name: '已完成任务', y: data.data.completedCount, sliced: true},
                                            {name: '参与的任务', y: data.data.involveCount}
                                        ]
                                    }]
                                }
                            }else{
                                Loading.hide();
                                toaster.pop('warning', "", data.msg);
                            }
                        }, function (error) {
                            Loading.hide();

                        })
                    }
                }
            };
            $scope.searchPage.action.search();
        }])
        .controller('report.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
            $("#excelForm").ajaxForm(function(data){
                if(data.result == "success") {
                    toaster.pop("success", "导入成功");
                }else{
                    toaster.pop("failed", "导入失败");
                }
            });

            $scope.chartSeries =  [{
                type: 'pie',
                name: '浏览器访问量占比',
                data: [
                    ['Firefox',   45.0],
                    ['IE',       26.8],
                    {
                        name: 'Chrome',
                        y: 12.8
                        // sliced: true, // 突出显示这个点（扇区），用于强调。
                    },
                    ['Safari',    8.5],
                    ['Opera',     6.2],
                    ['其他',   0.7]
                ]
            }];

            $scope.chartSeriesColumn =  [{
                type: 'bubble',
                name: '浏览器访问量占比',
                lineWidth: 0,
                data: [
                    ['Firefox',   45.0],
                    ['IE',       26.8],
                    {
                        name: 'Chrome',
                        y: 12.8
                        // sliced: true, // 突出显示这个点（扇区），用于强调。
                    },
                    ['Safari',    8.5],
                    ['Opera',     6.2],
                    ['其他',   0.7]
                ]
            }];
            $scope.total = 100;
            $scope.search = function () {
                $scope.total++;
                $scope.chartSeries =  [{
                    type: 'pie',
                    name: '浏览器访问量占比',
                    data: [
                        ['Firefox',   $scope.total],
                        ['IE',       21.8],
                        {
                            name: 'Chrome',
                            y: 12.8,
                            sliced: true, // 突出显示这个点（扇区），用于强调。
                        },
                        ['Safari',    8.5],
                        ['Opera',     6.2],
                        ['其他',   0.7]
                    ]
                }];
            }
            $scope.chartConfigPie = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'pie'
                        // margin: [0, 0, 0, 0] //距离上下左右的距离值
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                format: '<b>{point.name}</b>: {point.percentage:.1f}',
                                style: {
                                    color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                                }
                            },
                            point: {                  // 每个扇区是数据点对象，所以事件应该写在 point 下面
                                events: {
                                    // 鼠标滑过是，突出当前扇区
                                    mouseOver: function() {
                                        this.slice();
                                    },
                                    // 鼠标移出时，收回突出显示
                                    mouseOut: function() {
                                        this.slice();
                                    },
                                    // 默认是点击突出，这里屏蔽掉
                                    click: function() {
                                        return false;
                                    }
                                }
                            }
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    tooltip: {
                        enabled: false
                    }
                },
                legend: {
                    enabled: false
                },
                series: $scope.chartSeries,
                title: {
                    text: '工单'
                }
            };
            $scope.chartConfigColumn = {
                options: {
                    exporting: {
                        // 是否允许导出
                        enabled: false
                    },
                    chart: {
                        type: 'bubble',
                        plotBorderWidth: 1,
                        zoomType: 'xy'
                    }
                },
                xAxis:{
                    gridLineWidth: 1
                },
                yAxis:{
                    startOnTick: false,
                    endOnTick: false
                },
                title:{
                    text: '工单分布图'
                },
                series:[{
                    data: [
                        [9, 81, 63],
                        [98, 5, 89],
                        [51, 50, 73],
                        [41, 22, 14],
                        [58, 24, 20],
                        [78, 37, 34],
                        [55, 56, 53],
                        [18, 45, 70],
                        [42, 44, 28],
                        [3, 52, 59],
                        [31, 18, 97],
                        [79, 91, 63],
                        [93, 23, 23],
                        [44, 83, 22]
                    ],
                    marker: {
                        fillColor: {
                            radialGradient: { cx: 0.4, cy: 0.3, r: 0.7 },
                            stops: [
                                [0, 'rgba(255,255,255,0.5)'],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0.5).get('rgba')]
                            ]
                        }
                    }
                }, {
                    data: [
                        [42, 38, 20],
                        [6, 18, 1],
                        [1, 93, 55],
                        [57, 2, 90],
                        [80, 76, 22],
                        [11, 74, 96],
                        [88, 56, 10],
                        [30, 47, 49],
                        [57, 62, 98],
                        [4, 16, 16],
                        [46, 10, 11],
                        [22, 87, 89],
                        [57, 91, 82],
                        [45, 15, 98]
                    ],
                    marker: {
                        fillColor: {
                            radialGradient: { cx: 0.4, cy: 0.3, r: 0.7 },
                            stops: [
                                [0, 'rgba(255,255,255,0.5)'],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[1]).setOpacity(0.5).get('rgba')]
                            ]
                        }
                    }
                }
                ]
            // options: {
                //     exporting: {
                //         // 是否允许导出
                //         enabled: false
                //     },
                //     chart: {
                //         type: 'bubble',
                //         zoomType: 'xy'
                //     }
                // },
                //
                // title:{
                //     text: 'Highcharts bubbles with radial gradient fill'
                // },
                // series:[{
                //     data: [[97, 36, 79], [94, 74, 60], [68, 76, 58], [64, 87, 56], [68, 27, 73], [74, 99, 42], [7, 93, 87], [51, 69, 40], [38, 23, 33], [57, 86, 31]]
                // }, {
                //     data: [[25, 10, 87], [2, 75, 59], [11, 54, 8], [86, 55, 93], [5, 3, 58], [90, 63, 44], [91, 33, 17], [97, 3, 56], [15, 67, 48], [54, 25, 81]]
                // }, {
                //     data: [[47, 47, 21], [20, 12, 4], [6, 76, 91], [38, 30, 60], [57, 98, 64], [61, 17, 80], [83, 60, 13], [67, 78, 75], [64, 12, 10], [30, 77, 82]]
                // }]

            };
            // $scope.chartConfigColoum.series.push({"data": $scope.chartSeries });
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
                        }, function (error) {
                            Loading.hide();

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
                        }, function (error) {
                            Loading.hide();

                        })
                    }
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
            var current = new Date();
            $scope.searchPage = {
                data:{
                    startTime: $filter('date')(new Date(current.getTime() - 30 * 60 * 1000), 'yyyy-MM-dd HH:mm:ss'),
                    endTime: $filter('date')(current, 'yyyy-MM-dd HH:mm:ss')
                },
                action:{
                    search:function () {

                        //TODO
                    }
                }
            };
        }]);

})(angular);