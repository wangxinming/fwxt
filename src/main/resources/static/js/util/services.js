/**
 * Created by wangxinming on 2015/10/29.
 */
(function(angular){
    angular.module('util.services', ['ngResource'])
        .service('Util',['$parse',function($parse){
            this.shortStr = function(str,length){
                var count = 0;
                for (var i = 0; i < str.length; i++) {
                    if (str.charCodeAt(i) > 255 || str.charCodeAt(i) < 0){
                        count += 2;
                    }else{
                        count++;
                    }
                    if(count+3>length){
                        return str.substring(0,i)+"...";
                    }
                }
                return str;
            };
            //取得变量varName的值
            //变量名支持形如"a.b.c"
            this.getValue = function(varName,scope){
                var getter = $parse(varName);
                return getter(scope);
            };

            //取得变量varName的值为varValue
            //变量名支持形如"a.b.c"
            this.setValue = function(varName,varValue,scope){
                var setter = $parse(varName).assign;
                setter(scope,varValue);
                if(scope.$apply!=undefined){
                    this.go(scope);
                }
            };

            this.go = function(scope){
                if (!scope.$$phase) {
                    scope.$apply();
                }
            };

            //查找参数是否全不为空
            this.notNull = function(){
                if(arguments.length == 0){
                    return false;
                }
                for(var i=0;i<arguments.length;i++){
                    if(arguments[i]==undefined || arguments[i] == null || arguments[i] == ""){
                        return false;
                    }
                }
                return true;
            };

            //查找list中是否存在元素value
            this.exist = function(value,list){
                if(list instanceof Array){
                    for(var i = 0;i<list.length;i++){
                        if(list[i] == value){
                            return true;
                        }
                    }
                    return false;
                }else{
                    return value == list;
                }
            };

            //从list中的每个元素中取得指定属性，形成新的list
            //属性名支持形如 "a.b.c"
            this.copyArray = function(propName,list){
                var result = [];
                for(var i=0;i<list.length;i++){
                    result.push(this.getValue(propName,list[i]));
                }
                return result;
            }

            //取出list中符合条件的元素
            //条件为该元素的属性propName的值为propValue
            //属性名支持形如"a,b,c"
            this.findFromArray = function(propName,propValue,list){
                if (list != null && list.length > 0){
                    for(var i=0;i<list.length;i++){
                        if(this.getValue(propName,list[i])==propValue){
                            return list[i];
                        }
                    }
                }
                return null;
            }

            //删除list中符合条件的元素
            //条件为该元素的属性propName的值为propValue
            //属性名支持形如"a,b,c"
            this.deleteFromArray = function(propName,propValue,list){
                if (list != null && list.length > 0){
                    for(var i=0;i<list.length;i++){
                        if(this.getValue(propName,list[i])==propValue){
                            list.splice(i,1);
                            i-=1;
                        }
                    }
                }
            }

            //取出list中所有符合条件的元素
            //条件为该元素的属性propName的值为propValue
            //属性名支持形如"a,b,c"
            this.findAllFromArray = function(propName,propValue,list){
                var result = [];
                for(var i=0;i<list.length;i++){
                    if(this.getValue(propName,list[i])==propValue){
                        result.push(list[i]);
                    }
                }
                return result;
            }

            //判断变量flagName的值，直到为true时执行fn
            //变量名支持形如"a.b.c"
            this.delay = function(flagNames,fn,scope){
                var self = this;
                if(!(flagNames instanceof Array)){
                    flagNames = [flagNames];
                }
                for(var i = 0;i<flagNames.length;i++){
                    if(this.getValue(flagNames[i],scope)!=true){
                        setTimeout(function(){
                            self.delay(flagNames,fn,scope);
                        },100);
                        return;
                    }
                }
                setTimeout(function(){
                    fn();
                },100);
            };

            //以objB的属性对objA进行初始化
            //##因为当watch objA.xxx 时，直接objA = objB 不会触发watch
            this.init = function(objA,objB){
                for(var key in objB){
                    objA[key] = objB[key];
                }
            }

            this.clone = function(obj){
                if(typeof(obj) != 'object') return obj;
                if(obj == null){
                    return obj;
                }
                var myNewObj = Object.prototype.toString.call(obj) === '[object Array]'?[]:{};
                for(var i in obj){
                    myNewObj[i] = this.clone(obj[i]);
                }
                return myNewObj;
            }


            //table 内容过长加title
            this.str2Html = function(text){
                if(text == null || text == ""){
                    return ""
                }else{
                    var $span=jQuery("<span></span>");
                    $span.text(text);
                    $span.attr("title",text);
                    return $span.prop("outerHTML");
                }
            }

            this.sumMap = function(map1,map2){
                var result = {};
                for(var key in map1){
                    result[key]=map1[key];
                }
                for(var key in map2){
                    result[key]=map2[key];
                }
                return result;
            };
            Date.prototype.format=function(fmt) {
                var o = {
                    "M+" : this.getMonth()+1, //月份
                    "d+" : this.getDate(), //日
                    "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12, //小时
                    "H+" : this.getHours(), //小时
                    "m+" : this.getMinutes(), //分
                    "s+" : this.getSeconds(), //秒
                    "q+" : Math.floor((this.getMonth()+3)/3), //季度
                    "S" : this.getMilliseconds() //毫秒
                };
                if(/(y+)/.test(fmt)){
                    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
                }
                if(/(E+)/.test(fmt)){
                    fmt=fmt.replace(RegExp.$1, ((RegExp.$1.length>1) ? (RegExp.$1.length>2 ? "\u661f\u671f" : "\u5468") : "")+week[this.getDay()+""]);
                }
                for(var k in o){
                    if(new RegExp("("+ k +")").test(fmt)){
                        fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
                    }
                }
                return fmt;
            };

            this.formatSimpleDate = function(t){
                var d=	new Date();
                d.setTime(t);
                var s=d.format('yyyy-MM-dd HH:mm:ss');
                return s;
            };
        }])

        .service('Tools',['$parse','$timeout',function($parse,$timeout){
                this.dialog=function(dialog){
                    dialog.show=function(){
                        angular.element('#'+dialog.id).modal('show');
                    };
                    dialog.hide=function(){
                        angular.element('#'+dialog.id).modal('hide');
                    };
                    return dialog;
                };
                this.stripScript=function(s) {
                    if(s==null)return null;
                    var pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）—|{}【】‘；：”“'。，、？]")
                    var rs = "";
                    for (var i = 0; i < s.length; i++) {
                        rs = rs + s.substr(i, 1).replace(pattern, '');
                    }
                    return rs;
                }
            }]);
})(angular);