(function(angular){
    var systemheader = angular.module('systemheader',['ngResource']);
    var api_path="/dmonitor-webapi/";
    var itsm_path="/dmonitor-itsmapp/";
    systemheader.factory('LoginService',['$resource',function(resource){
        return resource('',{},{
            getLoginUser:{url:api_path+'loginUser',method:'GET',isArray:false},
            loginMenus:{url:'/user/loginMenus',method:'GET',isArray:true},
            isLogin:{url:api_path+'isLogin',method:'GET',isArray:false},
            logout:{url:api_path+'logout',method:'GET',isArray:false}
        });
    }]);

    systemheader.directive('systemHeader', function(){
        return {
            replace: true,
            restrict: 'E',
            templateUrl: 'lib/dnt-angular-ui/systemheader/systemheader.html'
        }
    });
    systemheader.controller('editPassController',['$scope','$rootScope','LoginService','UserService','Tools',function($scope,$rootScope,Login,User,Tools){
        $scope.passDialog=Tools.dialog({
            id:"passDialog",
            title:"修改密码",
            hiddenButton:true,
            model:{},
            edit:function(){
                $scope.passDialog.model.rpassword=null;
                $scope.passDialog.model.repassword=null;
                $scope.passDialog.model.password=null;
                $scope.passDialog.show();
            },
            save:function(){
                if($scope.passDialog.model.password!=$scope.passDialog.model.repassword){
                    $rootScope.alert("确认密码与新密码不一致");
                    return;
                }
                User.editPass($scope.passDialog.model,function(data){
                    $rootScope.alert("修改密码成功");
                });
            }
        });
    }]);
    systemheader.controller('systembarController',['$scope','$rootScope','LoginService','UserService','Tools','$timeout',function($scope,$rootScope,Login,User,Tools,$timeout){
        $rootScope.isLogin=false;
        $rootScope.loginUserMenuMap={};
        var foreachMenus=function(menus){
            for(var i=0;i<menus.length;i++){
                var menu=menus[i];
                $rootScope.loginUserMenuMap[menu.code]=!menu.permission;
                if(menu.children.length>0){
                    foreachMenus(menu.children);
                }
            }
        };
        $scope.logout = function(){
            try{
                for(var i=0;i<$rootScope.openWindows.length;i++){
                    if($rootScope.openWindows[i])$rootScope.openWindows[i].close();
                }
            }catch(e){}
            Login.logout();
            location.href="./login.html";
        };

        Login.getLoginUser(function(data){
            $rootScope.loginUser=data;
            $rootScope.isLogin=true;
            foreachMenus(data.menus);
            $timeout(function(){
                $scope.refreshLoginUser();
            },180000);
        });

        $scope.refreshLoginUser=function(){
            Login.loginMenus(function(data){
                $rootScope.loginUser.menus=data;
                foreachMenus(data);
            });
            $timeout(function(){
                $scope.refreshLoginUser();
            },180000);
        };
    }]);
})(angular);