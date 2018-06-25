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
        'deployment',
        'systemheader',
        "audit",
        "report",
        'process'
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
            $rootScope.$alert = function (info, level) {
                if (null != info.code && info.code == 18 && info.name == "SecurityError"){
                    return;
                } else if (null == info.code && info.name == "TypeError" && info.message.indexOf("indexOf") > 0){
                    info = "上传文件未找到，请查看上传文件位置！";
                }
                if(level)$rootScope.alert.level=level;
                else $rootScope.alert.level="info";
                $rootScope.alert.info = info;
                if (!$rootScope.$$phase) {
                    $rootScope.$apply();
                }
                angular.element('#' + $rootScope.alert.id).modal('show');
            };
            // var userName = jQuery.cookie("userName");
            // if(userName == "admin") {
            //     $rootScope.loginUserMenuMap = {
            //         "user":false,
            //         "audit":false,
            //         "modeler":false,
            //         "upload":false,
            //         "form":false,
            //         "deployment":false,
            //         "process":true,
            //         "myProcess":true,
            //         "pending":true,
            //         "complete":true,
            //         "report":false
            //     };
            // }else{
            //     $rootScope.loginUserMenuMap = {
            //         "user":true,
            //         "audit":true,
            //         "modeler":true,
            //         "upload":true,
            //         "form":true,
            //         "deployment":true,
            //         "process":false,
            //         "myProcess":false,
            //         "pending":false,
            //         "complete":false,
            //         "report":false
            //     };
            // }
        }])
        .config(['$httpProvider','$routeProvider', function ($httpProvider,$routeProvider) {
            // $routeProvider.when('/user', {
            //     templateUrl: 'view/user/user.html',
            //     controller: 'user.controller'
            // });
            $routeProvider.otherwise({redirectTo: '/dashboard'});

            $httpProvider.interceptors.push("interceptor");}])
        .factory('interceptor', function ($q, $rootScope) {
            return {
                'request': function (config) {
                    if (!new RegExp("html$").test(config.url)) {
                        config.url = config.url + '?r=' + new Date().getTime();
                    }
                    return config || $q.when(config);
                },
                'response': function (response) {
                    return response || $q.when(response);
                },
                'responseError': function (e) {
                    var info = "";
                    if (e.data && e.data.msg) {
                        info = e.data.msg;
                    } else if (e.status == 0) {
                        info = "服务器无法访问";
                    } else {
                        info = "未知错误";
                    }
                    if (info && info != "") {
                        jQuery.cookie("wxm_url", window.location.href);
                        location.href = "./login.html";
                        // if (info.indexOf("用户未登录") > -1 || info.indexOf("数据库异常") > -1) {
                        //     jQuery.cookie("wxm_url", window.location.href);
                        //     location.href = "./login.html";
                        // } else {
                        //     $rootScope.$alert(info, "alarm");
                        // }
                    }
                    return $q.reject(e);
                }
            };
             });
})(angular);

