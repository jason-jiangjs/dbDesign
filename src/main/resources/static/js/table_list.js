/**
 *
 */

// 画面项目初始化
$(function () {
    // 设置当前编辑的数据库名
    $('#cc').panel({title: $('#dbn').val()});

    // 创建搜索框
    $('#tbl_searchbox').textbox({
        prompt: 'Please Input Value',
        iconCls: 'icon-search',
        iconAlign: 'left',
        fit: true
    });
    $('#tbl_searchbox').textbox('textbox').bind({
        keyup: function (e) {
            doSearch($(this).val(), 0);
        }
    });

    //$('textarea').each(function () {
    //    this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    //}).on('input', function () {
    //    this.style.height = 'auto';
    //    this.style.height = (this.scrollHeight) + 'px';
    //});

    $('#tbl-tabs').tabs({
        onSelect: function(title, index) {
            _curTblId == null;
            if (index == 0) {
                $('#tblList').datalist("unselectAll");
                return;
            }
            var pp = $('#tbl-tabs').tabs('getTab', index); // tab页切换时要记住当前页id
            var tabId = pp.panel('options').id;
            if (tabId) {
                _curTblId = tabId;
            }
            // 要同步高亮tblList列表
            var rowIdx = $('#tblList').datalist("getRowIndex", tabId);
            if (rowIdx >= 0) {
                // 有可能指定的表不在一览中（比如检索时）
                $('#tblList').datalist("selectRecord", tabId);
            } else {
                $('#tblList').datalist('unselectAll');
            }
        }
    });

    $.extend($.fn.datagrid.defaults.editors, {
        textarea: {
            init: function (container, options) {
                var input = $('<textarea >').appendTo(container);
                return input;
            },
            destroy: function (target) {
                $(target).remove();
            },
            getValue: function (target) {
                return $(target).val();
            },
            setValue: function (target, value) {
                $(target).val(value);
            },
            resize: function (target, width) {
                $(target)._outerWidth(width);
            }
        }
    });
    $.extend($.fn.datagrid.methods, {
        editCell: function(jq,param){
            return jq.each(function(){
                var opts = $(this).datagrid('options');
                var fields = $(this).datagrid('getColumnFields',true).concat($(this).datagrid('getColumnFields'));
                for(var i=0; i<fields.length; i++){
                    var col = $(this).datagrid('getColumnOption', fields[i]);
                    col.editor1 = col.editor;
                    if (fields[i] != param.field){
                        col.editor = null;
                    }
                }
                $(this).datagrid('beginEdit', param.index);
                for(var i=0; i<fields.length; i++){
                    var col = $(this).datagrid('getColumnOption', fields[i]);
                    col.editor = col.editor1;
                }
            });
        }
    });

    $('#tblList').datalist({
        url: Ap_servletContext + '/ajax/getTableList?dbId=' + $('#dbId').val() + '&targetType=' + $('#targetType').val() + '&_t=' + new Date().getTime(),
        border: false,
        lines: true,
        fit: true,
        striped: true,
        valueField: '_id',
        textField: 'tableName',
        idField: '_id',
        loadMsg: '',
        //toolbar: "#tbl_search_bar",
        onClickRow: function (index, row) {
            var tblId = row._id;
            if (tblId) {
                _curTblId = tblId;
                // 先查看是否已存在，若已存在则切换tab
                if ($('#tbl-tabs').tabs('exists', row.tableName)) {
                    $('#tbl-tabs').tabs('select', row.tableName); // TODO-- 这里可能需要刷新数据，再议
                    return;
                }

                _createNewTab(tblId, row.tableName);
            } else {
                $.messager.alert('发生错误', '可能是数据加载错误．', 'error');
            }
        }
    });
});

function _createNewTab(tblId, tabTitle) {
    var loadLy = layer.load(1);
    // 查询表定义信息，动态加载列定义
    var tblName = encodeURIComponent(tabTitle);
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/getTable?tblId=' + tblId + '&tblName=' + tblName + '&_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0 && data.data) {
                // 先动态创建tab
                var prefixId = 'tbltab_' + data.data.tblId;
                $('#tbl-tabs').tabs('add', {
                    id: data.data.tblId,  // tab页的id默认是当前表的id(从数据库而来的)
                    title: data.data.tblName,
                    content: '<div id="' + prefixId + '" data-tblidx="' + data.data.tblId + '"></div>',
                    closable: true
                });
                // 然后加载tab内容
                prefixId = "#" + prefixId;
                var tabDiv = $(prefixId);
                tabDiv.append($('#tb_info').html());
                // 修改输入框字体颜色
                $("div" + prefixId + ' table td span .textbox-text').removeClass("textbox-prompt");

                $(prefixId + ' input._tbl_name').textbox({ value: data.data.tblName });
                $(prefixId + ' input._tbl_name_cn').textbox({ value: data.data.tblNameCn });
                $(prefixId + ' input._tbl_desc').textbox({ value: data.data.tblDesc, multiline: true });
                $(prefixId + ' input._tbl_name_p').val(data.data.tblName);
                $(prefixId + ' input._tbl_name_cn_p').val(data.data.tblNameCn);
                $(prefixId + ' input._tbl_desc_p').val(data.data.tblDesc);

                tabDiv.append($('<div style="height:1px;background:#e0e0e0"></div>'));
                tabDiv.append($('<table id="col_grid_' + data.data.tblId + '" class="easyui-datagrid" idField="columnId" data-options="fitColumns:true,rownumbers:true,nowrap:false,striped:true,singleSelect:true,border:false"></table>'));

                var colHeader = data.data.columns;
                // 在这里添加列定义的formatter，styler，editor（目前只有这3个）
                // 这里还没有更好的办法直接定位到需要添加属性的所在列，只能循环
                $(colHeader[0]).each(function(index, el) {
                    if (colHeader[0][index].field == 'desc') {
                        colHeader[0][index].formatter = descformatter;
                    } else if (colHeader[0][index].field == 'columnNameCN') {
                        colHeader[0][index].formatter = nameformatter;
                    }
                });

                // 然后加载列定义
                //$("#col_grid_" + data.data.tblId).datagrid("loadData", data.data.gridData);
                $("#col_grid_" + data.data.tblId).datagrid({
                    url: Ap_servletContext + '/ajax/getColumnList?tblId=' + tblId + '&_t=' + new Date().getTime(),
                    columns: colHeader,
                    onDblClickCell: onClickRowBegEdit,
                    onLoadSuccess: function () {
                        $(this).datagrid('enableDnd');
                    },
                    onDrop: function (targetRow, sourceRow, point) {
                        isRowEdited = true;
                    }
                });

            } else {
                layer.msg(data.msg);
            }
        }
    });
}

// 弹出菜单，目前只有两个
function popmenu(e, menuKey) {
    $('#' + menuKey).menu('show', {
        left: e.pageX,
        top: e.pageY
    });
}

// 根据名称查询表一览
function doSearch(value, freshFlg) {
    if (freshFlg == 1) {
        $('#tbl_searchbox').searchbox({ value: '' });
    }
    $('#tblList').datalist('load', {
        dbId: $('#dbId').val(),
        tblName: value,
        targetType: $('#targetType').val()
    });
}

// 切换表／视图
function convertType(value) {
    $('#targetType').val(value);
    // 然后重新加载 table_list


}

var newTblId = 0;
// 创建表／视图
function createNewTable(value) {
    if (newTblId > 10) {
        layer.msg("新建了太多项目，慢慢来吧。");
        return false;
    }
    newTblId ++;
    _curTblId = newTblId;
    var prefixId = 'tbltab_' + newTblId;
    $('#tbl-tabs').tabs('add', {
        id: newTblId,
        title: "新建表",
        content: '<div id="' + prefixId + '" data-tblidx="' + newTblId + '"></div>',
        closable: true
    });
    // 然后加载tab内容
    prefixId = "#" + prefixId;
    var tabDiv = $(prefixId);
    tabDiv.append($('#tb_info').html());
    // 修改输入框字体颜色
    $("div" + prefixId + ' table td span .textbox-text').removeClass("textbox-prompt");

    $(prefixId + ' input._tbl_name').textbox();
    $(prefixId + ' input._tbl_name_cn').textbox();
    $(prefixId + ' input._tbl_desc').textbox({ multiline: true });

    tabDiv.append($('<div style="height:1px;background:#e0e0e0"></div>'));
    tabDiv.append($('<table id="col_grid_' + newTblId + '" class="easyui-datagrid" idField="columnId" data-options="fitColumns:true,rownumbers:true,nowrap:false,striped:true,singleSelect:true,border:false"></table>'));

    var loadLy = layer.load(1);
    // 查询表定义信息，动态加载列定义
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/getColDef?_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0 && data.data) {
                var colHeader = data.data.columns;
                var colDef = {};
                // 在这里添加列定义的formatter，styler，editor（目前只有这3个）
                // 这里还没有更好的办法直接定位到需要添加属性的所在列，只能循环
                $(colHeader[0]).each(function(index, el) {
                    colDef[colHeader[0][index].field] = '';
                    if (colHeader[0][index].field == 'desc') {
                        colHeader[0][index].formatter = descformatter;
                    } else if (colHeader[0][index].field == 'columnNameCN') {
                        colHeader[0][index].formatter = nameformatter;
                    }
                });

                // 注意这里必须使用不同的colDef，否则所有行都指向同一个colDef值
                var rows = [colDef,$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef)];
                // 然后加载列定义
                $("#col_grid_" + newTblId).datagrid({
                    data: rows,
                    columns: colHeader,
                    onDblClickCell: onClickRowBegEdit,
                    onLoadSuccess: function () {
                        $(this).datagrid('enableDnd');
                    },
                    onDrop: function (targetRow, sourceRow, point) {
                        isRowEdited = true;
                    }
                });
            }
        }
    });
}

// 删除当前项目
function delCurrTable() {
    var pTab = $('#tbl-tabs').tabs('getSelected');
    var tabIdx = $('#tbl-tabs').tabs('getTabIndex', pTab);
    var tabId = pTab.panel('options').id;
    if (tabId == 0) { // 如果是主页，直接关闭
        $('#tbl-tabs').tabs('close', tabIdx);
    }

    if (tabId < 100 ) {
        // 新建的项目，直接关闭当前tab
        // 还要确认一下是否有填写了内容
        layer.confirm('确定要删除当前新建项目？<br>该操作不可恢复，已经填写的内容不会保存，是否确认删除？', { icon: 7,
            btn: ['确定','取消'] //按钮
        }, function(index) {
            layer.close(index);
            $('#tbl-tabs').tabs('close', tabIdx);
        }, function() {
            // 无操作
        });
        return;
    }

    // 先简单验证tab栏和tablelist中的表明是否一致
    var tblName = pTab.panel('options').title;
    var pTblItem = $('#tblList').datalist("getSelected");
    if (pTblItem == null || pTblItem == undefined) {
        // 可以删除
    } else if (pTblItem._id != undefined && pTblItem.tableName != undefined && tabId == pTblItem._id && pTblItem.tableName == tblName) {
        // 可以删除
    } else {
        // 数据不一致
        layer.msg("当前选择的数据出错了，不能删除。<br>请联系管理员。");
        console.log('tab栏和tablelist中所选数据不一致:', pTblItem, tabId, tblName);
        return;
    }

    layer.confirm('确定要删除选定项目: ' + tblName + '？<br>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        var postData = {};
        postData.tblId = _curTblId;

        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/delTableDef',
            data: JSON.stringify(postData),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    loadLy = layer.load(1);
                    // 删除成功后关闭tab
                    var pp = $('#tbl-tabs').tabs('getSelected');
                    var tabIdx = $('#tbl-tabs').tabs('getTabIndex', pp);
                    $('#tbl-tabs').tabs('close', tabIdx);

                    // 还要刷新table list
                    $('#tblList').datalist({
                        onLoadSuccess: function(data) {
                            var pp = $('#tbl-tabs').tabs('getSelected');
                            var _newTblId = pp.panel('options').id;
                            $('#tblList').datalist("selectRecord", _newTblId);
                        }
                    });
                    $('#tblList').datalist("load", {});
                    layer.close(loadLy);
                }
            }
        });
    }, function() {
        // 无操作
    });
}

// 取得当前tab框的id(实际也是当前datagrid的id)
// 小于100视为新建
function _getCurTabId() {
    // 要区分新增还是编辑, 用于jquery选择对象
    return;
}

// 当前所查看的表id（点击左边表一栏时会刷新，tab切换时会刷新）
var _curTblId = null;

var editIndex = undefined;
var isRowEdited = false;
// 双击表格行，开始编辑，只允许一行一行编辑
function onClickRowBegEdit(index,　field,　value) {
    if (_curTblId == undefined || _curTblId == null || _curTblId == '') {
        return false;
    }
    isRowEdited = true;

        if (endEditing()) {
            $('#col_grid_' + _curTblId).datagrid('selectRow', index).datagrid('editCell', { index: index, field: field });
            var ed = $('#col_grid_' + _curTblId).datagrid('getEditor', { index: index, field: field });
            if (ed && ed.target) {
                if (field == 'desc') {
                    $(ed.target).focus();
                } else {
                    if ($(ed.target).closest('td').find("input.textbox-text")[0]) {
                        $(ed.target).closest('td').find("input.textbox-text")[0].focus();
                    }
                }
            }
            //$(ed.target).focus();
            editIndex = index;
        }

}

function endEditing() {
    if (editIndex == undefined) {
        return true
    }
    if ($('#col_grid_' + _curTblId).datagrid('validateRow', editIndex)) {
        $('#col_grid_' + _curTblId).datagrid('endEdit', editIndex);
        editIndex = undefined;
        return true;
    } else {
        return false;
    }
}

// 添加栏位
function addColumn() {
    var pp = $('#tbl-tabs').tabs('getSelected');
    var tabId = pp.panel('options').id;
    var prefixId = '#col_grid_' + tabId;
    $(prefixId).datagrid('appendRow', { default: "" });
}

// 插入栏位
function insertColumn() {
    var pp = $('#tbl-tabs').tabs('getSelected');
    var tabId = pp.panel('options').id;
    var prefixId = '#col_grid_' + tabId;
    var s1 = $(prefixId).datagrid('getSelected');
    var s2 = $(prefixId).datagrid('getRowIndex', s1.columnId);
    if (s2 == -1) {
        // 没有选中行，新增
        $(prefixId).datagrid('appendRow', { default: "" });
    } else {
        $(prefixId).datagrid('insertRow',{
            index: s2,
            row: { default: "" }
        });
    }
}

// 删除栏位
function delColumn() {
    // 先去后台验证删除权限码
    layer.confirm('确定要删除选定列？该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        var pp = $('#tbl-tabs').tabs('getSelected');
        var tabId = pp.panel('options').id;
        var prefixId = '#col_grid_' + tabId;
        var s1 = $(prefixId).datagrid('getSelected');
        var s2 = $(prefixId).datagrid('getRowIndex', s1.columnId);
        if (s2 >= 0) {
            $(prefixId).datagrid('deleteRow', s2);
            $(prefixId).datagrid('unselectAll');
        }
        isRowEdited = true;
    }, function() {
        // 无操作
    });
}

// 保存所有修改内容
// 前端不详细判断确实是有内容修改，直接把所有数据提交到后台，由后台负责处理
function saveAll(event) {
    var $this = $(event.target);
    // 要先定位到当前tab
    var pp = $('#tbl-tabs').tabs('getSelected');
    var tabId = pp.panel('options').id;
    var prefixId = '#tbltab_' + tabId;
    var tName = $.trim($(prefixId + ' input._tbl_name').textbox('getValue'));
    var tNameCn = $.trim($(prefixId + ' input._tbl_name_cn').textbox('getValue'));
    var tDesc = $.trim($(prefixId + ' input._tbl_desc').textbox('getValue'));
    var tNamep = $.trim($(prefixId + ' input._tbl_name_p').val());
    var tNameCnp = $.trim($(prefixId + ' input._tbl_name_cn_p').val());
    var tDescp = $.trim($(prefixId + ' input._tbl_desc_p').val());
    if (!isRowEdited && tName == tNamep && tNameCn == tNameCnp && tDesc == tDescp) {
        layer.msg("没有修改，不需要保存。");
        return false;
    }

    var postData = {};
    postData._tbl_id = _curTblId;
    postData._tbl_name = tName;
    postData._tbl_name_cn = tNameCn;
    postData._tbl_desc = tDesc;

    $('#col_grid_' + _curTblId).datagrid('acceptChanges');
    postData.column_list = $('#col_grid_' + _curTblId).datagrid('getRows');

    // 对输入值简单验证，新建表时表名重复，列名重复，缺少类型
    var duplicate = false;
    if (_curTblId < 100) {
        var rowData = $('#tblList').datalist('getRows');
        for (x in rowData) {
            if (rowData[x].tableName == tName) {
                duplicate = true;
                break;
            }
        }
        if (duplicate) {
            layer.msg("表名: " + tName + " 与现有的重复，请修改后再保存。");
            return;
        }
    }

    if (postData.column_list.length != undefined && postData.column_list.length > 0) {
        var colNmArr = [];
        for (x in postData.column_list) {
            var colNm = $.trim(postData.column_list[x].columnName);
            if (colNm == '') {
                continue;
            }
            if (colNmArr.indexOf(colNm) >= 0) {
                layer.msg("列名: " + colNm + " 与现有的重复，请修改后再保存。");
                return;
            }
            colNmArr.push(colNm);
        }
    }

    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/saveColDefine',
        data: JSON.stringify(postData),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {

                // 保存成功后关闭表格的编辑状态，如果是新建表还要刷新tab
                if (_curTblId < 100) {
                    loadLy = layer.load(1);
                    // 刷新当前tab（经试验，只能关闭当前tab,再打开新的tab）
                    var _newTblId = data.data._newTblId;
                    var tabIdx = $('#tbl-tabs').tabs('getTabIndex', pp);
                    $('#tbl-tabs').tabs('close', tabIdx);
                    _createNewTab(_newTblId, tName);

                    $('#tblList').datalist({
                        onLoadSuccess: function(data) {
                            $('#tblList').datalist("selectRecord", _newTblId);
                        }
                    });
                    $('#tblList').datalist("load", {});
                    layer.close(loadLy);
                }
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 备注一栏的显示形式
function descformatter(value, row, index) {
    if (row.desc) {
        return '<div style="width: 100%;display:block;word-break: break-all;word-wrap: break-word">' + row.desc + '</div>';
    }
    return '';
}
function nameformatter(value, row, index) {
    if (row.columnNameCN) {
        return '<div style="width: 100%;display:block;word-break: break-all;word-wrap: break-word">' + row.columnNameCN + '</div>';
    }
    return '';
}
