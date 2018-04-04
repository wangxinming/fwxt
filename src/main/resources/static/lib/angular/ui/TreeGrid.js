
TreeGrid = function(_config){
	_config = _config || {};
	
	var s = "";
	var __tb=$("");
	var rownum = 0;
	var __root;
	
	var __selectedData = null;
	var __selectedId = null;
	var __selectedIndex = null;

	var folderOpenIcon = (_config.folderOpenIcon || TreeGrid.FOLDER_OPEN_ICON);
	var folderCloseIcon = (_config.folderCloseIcon || TreeGrid.FOLDER_CLOSE_ICON);
	var defaultLeafIcon = (_config.defaultLeafIcon || TreeGrid.DEFAULT_LEAF_ICON);

	//显示表头行
	drowHeader = function(){
		s="";
		s += "<thead><tr class='header' height='" + (_config.headerHeight || "25") + "'>";
		var cols = _config.columns;
		for(i=0;i<cols.length;i++){
			var col = cols[i];
			s += "<td align='" + (col.headerAlign || _config.headerAlign || "center") + "' width='" + (col.width || "") + "'>" + (col.headerText || "") + "</td>";
		}
		s += "</tr></thead>";
		__tb.append($(s));
	}
	
	//递归显示数据行
	drowData = function(){
		var rows = _config.data;
		var cols = _config.columns;
		drowRowData(rows, cols, 1, "");
	}
	
	//局部变量i、j必须要用 var 来声明，否则，后续的数据无法正常显示
	drowRowData = function(_rows, _cols, _level, _pid){
		for(var i=0;i<_rows.length;i++){
			var row = _rows[i];
			var id = _pid + "_" + i;
			var s=addRow(row, _cols, _level, _pid,id);
			var tr=$(s);
			tr.data("rowData",row);
			__tb.append(tr);
			//递归显示下级数据
			if(row.children){
				drowRowData(row.children, _cols, _level+1, id);
			}
		}
	};
	
	addRow=function(row,_cols, _level, _pid,id){
		var folderColumnIndex = (_config.folderColumnIndex || 0);
		s="";
		s += "<tr rid='"+row.id+"' id='TR" + id + "' level='"+_level+"' pid='" + ((_pid=="")?"":("TR"+_pid)) + "' isopen='Y' data=\"" + TreeGrid.json2str(row) + "\" rowIndex='" + rownum++ + "'>";
		for(var j=0;j<_cols.length;j++){
			var col = _cols[j];
			s += "<td onmouseover='this.title=jQuery(this).text();' align='" + (col.dataAlign || _config.dataAlign || "left") + "'";

			//层次缩进
			if(j==folderColumnIndex){
				s += " style='padding-left:" + ((folderColumnIndex==0?10:20)*_level) + "px;'> ";
			}else{
				s += ">";
			}

			//节点图标
			if(j==folderColumnIndex){
				if(_config.nullFolderShow){
					if(row.children){ //有下级数据
						s += "<img name='grid_img' folder='Y' trid='TR" + id + "' src='" + folderOpenIcon + "' class='image_hand'>";
					}else{
						s += "<img name='grid_img' src='" + defaultLeafIcon + "' class='image_nohand'>";
					}
				}else{
					if(row.children && row.children.length>0){ //有下级数据
						s += "<img name='grid_img' folder='Y' trid='TR" + id + "' src='" + folderOpenIcon + "' class='image_hand'>";
					}else{
						s += "<img name='grid_img' src='" + defaultLeafIcon + "' class='image_nohand'>";
					}
				}
			}
			
			//单元格内容
			if(col.handler){
				
				s += col.handler(row,col) + "</td>";
			}else{
				s += (row[col.dataField] || "") + "</td>";
			}
		}
		s += "</tr>";
		return s;
	};
	

	this.setConfig = function(config){
		_config=config;
	};
	
	this.addChild = function(rows,parentId){
		var cols = _config.columns;
		var parent=jQuery("#"+parentId);
		parent.attr("isopen","Y");
		parent.find("img[folder='Y']").attr("src", folderOpenIcon);
		if(parent){
			var row=parent.data("rowData");
			row.children=rows;
			parent.data("rowData",row);
			var level=parent.attr("level");
			var pid=parentId.length>0?parentId.substring(2,parentId.length):"";
			for(var i=0;i<rows.length;i++){
				var id= pid + "_" + i;
				var tr=addRow(rows[i],cols,parseInt(level)+1,pid,id);
				var s=$(tr);
				s.data("rowData",rows[i]);
				parent.after(s);
			}
			init();
		}
	};
	
	//主函数
	this.show = function(){
		this.id = _config.id || ("TreeGrid" + TreeGrid.COUNT++);
		s="";
		s += "<table id='" + this.id + "' cellspacing=0 cellpadding=0 width='" + (_config.width || "100%") + "' class='TreeGrid'>";
		s += "</table>";
		jQuery("#"+_config.renderTo).empty();
		__root = jQuery("#"+_config.renderTo);
		__tb=$(s);
		//__root.append($("<table class='firstGrid'></table>"));
		//.append($('<div class="table-con"></div>')
		__root.append(__tb);
		drowHeader();
		drowData();	
		//初始化动作
		init();
		//__root.find(".firstGrid").append(__root.find(".nextGrid>thead").clone());
	};
	
	initCheck=function(){
		selectedIds=[];
		if(_config.checkbox){
			jQuery(".TreeGrid").find(":checkbox[name='cks']").click(function(){
		 		selectedIds[selectedIds.length]=jQuery(this).val();
		 		var row=jQuery(this).closest("tr").data("rowData");
		 		checkSub(row,jQuery(this).attr("checked"));
		 	});
			jQuery(".TreeGrid").find(":checkbox[name='ckall']").click(function(){
				selectedIds=[];
				jQuery(".TreeGrid").find(":checkbox[name='cks']").each(function(i){
		 			selectedIds[i]=jQuery(this).val();
		 		});
		 	});
		}
	};

	init = function(){
		//以新背景色标识鼠标所指行
		if((_config.hoverRowBackground || "false") == "true"){
			__root.find("tr").hover(
				function(){
					if(jQuery(this).attr("class") && jQuery(this).attr("class") == "header") return;
					jQuery(this).addClass("row_hover");
				},
				function(){
					jQuery(this).removeClass("row_hover");
				}
			);
		}

		//将单击事件绑定到tr标签
		__root.find("tr").bind("click", function(event){
			__root.find("tr").removeClass("row_active");
			jQuery(this).addClass("row_active");
			
			//获取当前行的数据
			__selectedData = this.data || this.getAttribute("data");
			__selectedId = this.id || this.getAttribute("id");
			__selectedIndex = this.rownum || this.getAttribute("rowIndex");

			//行记录单击后触发的事件
			if(_config.itemClick){
				_config.itemClick(__selectedId, __selectedIndex,TreeGrid.str2json(__selectedData));
			}
		});

		//展开、关闭下级节点
		__root.find("img[folder='Y']").unbind( "click" ).bind("click", function(event){
			var trid = this.trid || this.getAttribute("trid");
			var isOpen = __root.find("#" + trid).attr("isopen");
			isOpen = (isOpen == "Y") ? "N" : "Y";
			__root.find("#" + trid).attr("isopen", isOpen);
			//alert(isOpen);
			showHiddenNode(trid, isOpen);

			if(_config.folderClick){
				var tr=jQuery(this).closest("tr");
				var row = tr.data("rowData");
				__selectedId = tr.attr("id");
				__selectedIndex =tr.attr("rowIndex");
				_config.folderClick(__selectedId, __selectedIndex,row);
			}
			//event.stopPropagation();
		});
		
		initCheck();
	}

	//显示或隐藏子节点数据
	showHiddenNode = function(_trid, _open){
		if(_open == "N"){ //隐藏子节点
			__root.find("#"+_trid).find("img[folder='Y']").attr("src", folderCloseIcon);
			__root.find("tr[id^=" + _trid + "_]").css("display", "none");
		}else{ //显示子节点
			__root.find("#"+_trid).find("img[folder='Y']").attr("src", folderOpenIcon);
			showSubs(_trid);
		}
	}

	//递归检查下一级节点是否需要显示
	showSubs = function(_trid){
		var isOpen = __root.find("#" + _trid).attr("isopen");
		if(isOpen == "Y"){
			var trs = __root.find("tr[pid=" + _trid + "]");
			trs.css("display", "");
			
			for(var i=0;i<trs.length;i++){
				showSubs(trs[i].id);
			}
		}
	}

	//展开或收起所有节点
	this.expandAll = function(isOpen){
		var trs = __root.find("tr[pid='']");
		for(var i=0;i<trs.length;i++){
			var trid = trs[i].id || trs[i].getAttribute("id");
			showHiddenNode(trid, isOpen);
		}
	}
	
	//取得当前选中的行记录
	this.getSelectedItem = function(){
		return new TreeGridItem(__root, __selectedId, __selectedIndex, TreeGrid.str2json(__selectedData));
	}

};

//公共静态变量
TreeGrid.FOLDER_OPEN_ICON = "img/icon/folderOpen.gif";
TreeGrid.FOLDER_CLOSE_ICON = "img/icon/folderClose.gif";
TreeGrid.DEFAULT_LEAF_ICON = "img/icon/defaultLeaf.gif";
TreeGrid.COUNT = 1;

//将json对象转换成字符串
TreeGrid.json2str = function(obj){
	var arr = [];

	var fmt = function(s){
		if(typeof s == 'object' && s != null){
			if(s.length){
				var _substr = "";
				for(var x=0;x<s.length;x++){
					if(x>0) _substr += ", ";
					_substr += TreeGrid.json2str(s[x]);
				}
				return "[" + _substr + "]";
			}else{
				return TreeGrid.json2str(s);
			}
		}
		return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s;
	}

	for(var i in obj){
		if(typeof obj[i] != 'object'){ //暂时不包括子数据
			arr.push(i + ":" + fmt(obj[i]));
		}
	}

	return '{' + arr.join(', ') + '}';
}

TreeGrid.str2json = function(s){
	var json = null;
	if(jQuery.browser.msie){
		json = eval("(" + s + ")");
	}else{
		json = new Function("return " + s)();
	}
	return json;
}
var selectedIds=[];

//数据行对象
function TreeGridItem (_root, _rowId, _rowIndex, _rowData){
	var __root = _root;
	
	this.id = _rowId;
	this.index = _rowIndex;
	this.data = _rowData;
	
	this.getParent = function(){
		var pid = jQuery("#" + this.id).attr("pid");
		if(pid!=""){
			var rowIndex = jQuery("#" + pid).attr("rowIndex");
			var data = jQuery("#" + pid).attr("data");
			return new TreeGridItem(_root, pid, rowIndex, TreeGrid.str2json(data));
		}
		return null;
	}
	
	this.getChildren = function(){
		var arr = [];
		var trs = jQuery(__root).find("tr[pid='" + this.id + "']");
		for(var i=0;i<trs.length;i++){
			var tr = trs[i];
			arr.push(new TreeGridItem(__root, tr.id, tr.rowIndex, TreeGrid.str2json(tr.data)));
		}
		return arr;
	}
};