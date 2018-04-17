/**
 * Created by wangxinming on 2015/10/29.
 */
(function(angular){
    var Util = function($parse){
        this._parse = $parse;
    };

    //取得变量varName的值
    //变量名支持形如"a.b.c"
    Util.prototype.getValue = function(varName,scope){
        var getter = this._parse(varName);
        return getter(scope);
    };

    //取得变量varName的值为varValue
    //变量名支持形如"a.b.c"
    Util.prototype.setValue = function(varName,varValue,scope){
        var setter = this._parse(varName).assign;
        setter(scope,varValue);
        if(scope.$apply!=undefined){
            if (!scope.$$phase) {
                scope.$apply();
            }
        }
    };

//tree相关的指令元素需要有id属性

/*table 自分页封装
* 在controller中定义列，参照syslog实现
* */
angular.module('dataTablesDirective', [])
    .directive('jqueryTable',[ function(){
        return {
            link: function(scope, element, attrs) {
                var options = {};
                if (attrs.jqueryTable.length > 0) {
                    options = scope.$eval(attrs.jqueryTable);
                    options.oLanguage={
                        "sLengthMenu": "每页显示 _MENU_ 条记录",
                        "sZeroRecords": "对不起，查询不到任何相关数据",
                        "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                        "sInfoEmpty": " ",
                        "sInfoFiltered": "（数据表中共为 _MAX_ 条记录）",
                        "sProcessing": "正在加载中...",
                        "sSearch": "搜索",
                        "sUrl": "", //多语言配置文件，可将oLanguage的设置放在一个txt文件中，例：Javascript/datatable/dtCH.txt
                        "oPaginate": {
                            "sFirst": "第一页",
                            "sPrevious": " 上一页 ",
                            "sNext": " 下一页 ",
                            "sLast": " 最后一页 "
                        }
                    };
                } else {

                    options = {
                        "bStateSave": true,
                        "iCookieDuration": 2419200, /* 1 month */
                        "bJQueryUI": false,
                        "bPaginate": true,
                        "bLengthChange": true,
                        "bFilter": true,
                        "bInfo": true,
                        "bDestroy": true,
                        "iDisplayLength": 20,
                        "aLengthMenu": [20,50],
                        "oLanguage": {
                            "sLengthMenu": "每页显示 _MENU_ 条记录",
                            "sZeroRecords": "对不起，查询不到任何相关数据",
                            "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                            "sInfoEmpty": " ",
                            "sInfoFiltered": "（数据表中共为 _MAX_ 条记录）",
                            "sProcessing": "正在加载中...",
                            "sSearch": "搜索",
                            "sUrl": "", //多语言配置文件，可将oLanguage的设置放在一个txt文件中，例：Javascript/datatable/dtCH.txt
                            "oPaginate": {
                                "sFirst": "第一页",
                                "sPrevious": " 上一页 ",
                                "sNext": " 下一页 ",
                                "sLast": " 最后一页 "
                            }
                        }//多语言配置
                    };
                }
                var explicitColumns = [];
                element.find('th').each(function(index, elem) {
                    explicitColumns.push($(elem).text());
                });
                if (explicitColumns.length > 0) {
                    options["aoColumns"] = explicitColumns;
                } else if (attrs.aoColumns) {
                    options["aoColumns"] = scope.$eval(attrs.aoColumns);
                }
                if (attrs.aoColumnDefs) {
                    options["aoColumnDefs"] = scope.$eval(attrs.aoColumnDefs);
                }
                if (attrs.fnRowCallback) {
                    options["fnRowCallback"] = scope.$eval(attrs.fnRowCallback);
                }
                var dataTable = element.dataTable(options);
                scope.$watch(attrs.aaData, function(value) {
                    var val = value || null;
                    if (val) {
                        dataTable.fnClearTable();
                        dataTable.fnAddData(scope.$eval(attrs.aaData));
                    }
                });
            }
        }
    }])
//    settings通过jqTable属性指定变量名
//    var settings = {
//        reload : null,  //该变量会被赋值为刷新table内容的函数
//        getData:function(search,fnCallback){
//            search = {
//                limit : 20, //每页条数(即取多少条数据)
//                offset : 0 , //从第几条数据开始取
//                orderBy : "updateTime",//排序字段
//                orderByType : "desc" //排序顺序
//            }
//            fnCallback(data); //取得数据后需调用该回调函数通知表格更新
//            data = {
//                count : 200, //总记录数
//                data : []  //记录行的array
//            }  //data 约定服务端返回的格式
//        }, //getData应指定获取数据的函数
//        columns : [
//            {
//                "mData" : "id",
//                "mRender" : function(mData,type,full) {
//                    return mData;
//                }
//            },...
//        ] , //定义列的形式,mRender可返回html
//        columnDefs : [
//            { "bSortable": false, "aTargets": [ 0 ] }  //第0列不可排序
//        ] , //定义列的约束
//        defaultOrderBy : [ 4, "desc" ]  //定义默认排序列为第4列倒序
//    }

.directive('jqTable',['$compile','$parse', function($compile,$parse){

    var util = new Util($parse);
    return {
        link: function(scope, element, attrs) {
            var settings = util.getValue(attrs.jqTable,scope);
            var rowsName = "rows";
            var countName = "total";
            var paging = true;
            var pageSize = 10;
            if(settings.rowsName){
                rowsName = settings.rowsName;
            }
            if(settings.countName){
                countName = settings.countName;
            }
            if(settings.paging!=null){
                paging = settings.paging;
            }
            if(settings.pageSize!=null){
                pageSize = settings.pageSize;
            }
            var sortBy = [];
            var tableObj = element.dataTable({
                "bFilter": false, //自带的搜索框,默认值true
//                "bJQueryUI": true ,//启用jQuery UI样式.默认值false
//                "aaData": aDataSet, //使用的js array格式数据
//                "bLengthChange": false,//每页的条数选择框,默认true
                "bPaginate": paging , //分页，默认true
                "bInfo":paging,
                "bProcessing": true, //正在加载数据提示，默认false
//                "bSort": true, //排序功能，默认true
//                "sPaginationType": "full_numbers",//分页，一共两种样式，full_numbers和two_button(默认),ace只提供了two_button的美化
                "bStateSave": false, //保存状态到cookie,如每页数量
                "bProcessing": true,
                "bServerSide": true,
                "sAjaxSource": "",
                "fnServerData": function ( sSource, aoData, fnCallback ) {
                    var sEcho = aoData[0].value;
                    var colsNum = aoData[1].value;
                    var offset = aoData[3].value;
                    var limit = aoData[4].value;
                    var orderByList = [];
                    var orderByTypeList = [];
                    sortBy = [];
                    for(var i=5+colsNum;i<aoData.length;i=i+2){
                        if(aoData[i].name == "iSortingCols"){
                            break;
                        }
                        orderByList.push(settings.columns[aoData[i].value].mData);
                        orderByTypeList.push(aoData[i+1].value);
                        sortBy.push([aoData[i].value,aoData[i+1].value]);
                    }
                    var orderBy = orderByList.join(",");
                    var orderByType = orderByTypeList.join(",");
                    settings.getData(
                        {
                            limit : limit, //每页条数(即取多少条数据)
                            offset : offset , //从第几条数据开始取
                            orderBy : orderBy,//排序字段
                            orderByType : orderByType //排序顺序
                        },
                        function(data){
                            fnCallback({
                                sEcho: sEcho,
                                iTotalRecords: data[countName],
                                iTotalDisplayRecords:data[countName],
                                aaData:data[rowsName]
                            });
                        }
                    );
                },
                "fnCreatedRow":function(nRow, aData, iDataIndex){
                    $compile(nRow)(scope)
                },
                "fnInitComplete": function(oSettings, json) {
                    $compile(element.find("thead"))(scope);
                },
                "oLanguage": {
                    "sLengthMenu": "每页显示 _MENU_ 条记录",
                    "sZeroRecords": "对不起，查询不到任何相关数据",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sInfoEmpty": " ",
                    "sInfoFiltered": "（数据表中共为 _MAX_ 条记录）",
                    "sProcessing": "正在加载中...",
                    "sSearch": "搜索",
                    "sUrl": "", //多语言配置文件，可将oLanguage的设置放在一个txt文件中，例：Javascript/datatable/dtCH.txt
                    "oPaginate": {
                        "sFirst": "第一页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 最后一页 "
                    }
                }, //多语言配置
				"aoColumns": settings.columns,
                "aoColumnDefs": settings.columnDefs,
                "aaSorting": settings.defaultOrderBy,
                "iDisplayLength": pageSize,
//                "aLengthMenu": [[10,20,50,-1], [10,20,50,"All"]]
                "aLengthMenu": [20,50]
            });

            settings.reload = function(cancelSort){
                if(cancelSort || sortBy.length==0){
                    tableObj.fnSort([]);
                }else{
                    tableObj.fnSort(sortBy);
                }
            };

            //调整分页条自适应table宽度
            var _adjustWidth = function(){
                var width = element[0].scrollWidth;
                $(element[0].nextSibling).css({width:width + "px"});
            };
            $(window).resize(_adjustWidth);
        }
    };
} ]);

})(angular);