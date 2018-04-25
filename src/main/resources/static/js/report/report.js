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
        .controller('report.controller', ['$scope', '$rootScope','user.loader','Util','Tools','Loading','toaster','$filter',function($scope, $rootScope,loader,Util,Tools,Loading,toaster,$filter) {
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
                        type: 'pie',
                        margin: [0, 0, 0, 0] //距离上下左右的距离值
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