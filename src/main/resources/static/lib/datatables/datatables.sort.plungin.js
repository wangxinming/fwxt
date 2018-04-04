jQuery.extend(jQuery.fn.dataTableExt.oSort, {
  "dnt-sort-pre": function (a) {
    var x = String(a).replace(/<[\s\S]*?>/g, "");    //去除html标记
    x = x.replace(/&amp;nbsp;/ig, "");                   //去除空格
    x = x.replace(/,/ig, "");                   //去除逗号
    return x;
  },

  "dnt-sort-asc": function (a, b) {                //正序排序引用方法
    var aa = Number(a);
    var bb = Number(b);
    if (isNaN(aa) || isNaN(bb)) {
      return ((a + "") < (b + "")) ? -1 : (((a + "") > (b + "")) ? 1 : 0);
    } else {
      return (aa < bb) ? -1 : ((aa > bb) ? 1 : 0);
    }
  },

  "dnt-sort-desc": function (a, b) {                //倒序排序引用方法
    var aa = Number(a);
    var bb = Number(b);
    if (isNaN(aa) || isNaN(bb)) {
      return ((a + "") < (b + "")) ? 1 : (((a + "") > (b + "")) ? -1 : 0);
    } else {
      return (aa < bb) ? 1 : ((aa > bb) ? -1 : 0);
    }
  }
});