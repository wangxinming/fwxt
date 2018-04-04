/**
 * Created by wangxinming on 2015/10/29.
 */
(function(angular){
    'use strict';
    // Declare app level module which depends on views, and components
    angular.module('webPortal', [
        'ngRoute',
        'ngResource',
        'jk.loading',
        'toaster',
        'webportal.user',
        'deployment'
    ])
        .directive('modalConfirm', function() {
            return {
                restrict: 'AE',
                transclude:false,
                scope: {
                    dialog: '=settings'
                },
                templateUrl: 'template/confirm.html'
            };
        })
        .directive('myEnter', function () {
            return function (scope, element, attrs) {
                element.bind("keydown keypress", function (event) {
                    if(event.which === 13) {
                        scope.$apply(function (){
                            scope.$eval(attrs.myEnter);
                        });
                        event.preventDefault();
                    }
                });
            };
        })
        .directive('select2', function () {
            return {
                restrict: 'A',
                scope: {
                    config: '=',
                    ngModel: '=',
                    select2Model: '='
                },
                link: function (scope, element, attrs) {
                    // 初始化
                    var tagName = element[0].tagName,
                        config = {
                            allowClear: true,
                            multiple: !!attrs.multiple,
                            placeholder: attrs.placeholder || ' '   // 修复不出现删除按钮的情况
                        };

                    // 生成select
                    if(tagName === 'SELECT') {
                        // 初始化
                        var $element = $(element);
                        delete config.multiple;

                        $element.select2(config);

                        // model - view
                        scope.$watch('ngModel', function (newVal) {
                            setTimeout(function () {
                                $element.find('[value^="?"]').remove();    // 清除错误的数据
                                $element.select2('val', newVal);
                            },0);
                        }, true);
                        return false;
                    }

                    // 处理input
                    if(tagName === 'INPUT') {
                        // 初始化
                        var $element = $(element);

                        // 获取内置配置
                        if(attrs.query) {
                            //  scope.config = select2Query[attrs.query]();
                        }

                        // 动态生成select2
                        scope.$watch('config', function () {
                            angular.extend(config, scope.config);
                            $element.select2('destroy').select2(config);
                        }, true);

                        // view - model
                        $element.on('change', function () {
                            scope.$apply(function () {
                                scope.select2Model = $element.select2('data');
                            });
                        });

                        // model - view
                        scope.$watch('select2Model', function (newVal) {
                            $element.select2('data', newVal);
                        }, true);

                        // model - view
                        scope.$watch('ngModel', function (newVal) {
                            // 跳过ajax方式以及多选情况
                            if(config.ajax || config.multiple) { return false }

                            $element.select2('val', newVal);
                        }, true);
                    }
                }
            }
        })
        .run(['$rootScope', '$timeout' ,'$location', function ($rootScope, $timeout,$location) {
            $rootScope.$on('$routeChangeSuccess', function (event) {
                var path = $location.path();
                angular.element('#' + $rootScope.confirm.id).modal('hide');
                $rootScope.currentView = path.substring(1, path.length);
                $(".modal-backdrop").hide();
            });
            $rootScope.confirm = {id: "myconfirm", info: "", save: function () {
                }, cancel: function () {
                }};
            $rootScope.$confirm = function (info, fn,title) {
                $rootScope.confirm.info = info;
                $rootScope.confirm.save = fn;
                $rootScope.confirm.title = title;

                angular.element('#' + $rootScope.confirm.id).modal('show');
            };
        }]);

        // .config(['$routeProvider', function($routeProvider) {
        //     $routeProvider.when('/addproperty', {
        //         templateUrl: 'view/user/addproperty.html',
        //         controller: 'user.controller'
        //     }).when('/usermanage', {
        //         templateUrl: 'view/user/usermanage.html',
        //         controller: 'userManagerController'
        //     });
        //     $routeProvider.otherwise({redirectTo: '/user'});
        //
        // }]);


})(angular);

