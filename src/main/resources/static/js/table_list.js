/**
 *
 */

// 画面项目初始化
$(function () {
    $('#cc').panel({title: $('#dbn').val()});
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
            $('#tblList').datalist("selectRecord", tabId);
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

    $('#tblList').datalist({
        url: Ap_servletContext + '/ajax/getTableList?dbId=' + $('#dbId').val() + '&targetType=' + $('#targetType').val() + '&_t=' + new Date().getTime(),
        border: false,
        lines: true,
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
                tabDiv.append($('<table id="col_grid_' + data.data.tblId + '" class="easyui-datagrid" idField="columnId" data-options="fitColumns:true,rownumbers:true,nowrap:false,singleSelect:true,border:false"></table>'));

                var colHeader = data.data.columns;
                // 在这里添加列定义的formatter，styler，editor（目前只有这3个）
                // 这里还没有更好的办法直接定位到需要添加属性的所在列，只能循环
                $(colHeader[0]).each(function(index, el) {
                    if (colHeader[0][index].field == 'desc') {
                        colHeader[0][index].formatter = descformatter;
                    }
                });

                // 然后加载列定义
                //$("#col_grid_" + data.data.tblId).datagrid("loadData", data.data.gridData);
                $("#col_grid_" + data.data.tblId).datagrid({
                    url: Ap_servletContext + '/ajax/getColumnList?tblId=' + tblId + '&_t=' + new Date().getTime(),
                    columns: colHeader,
                    onDblClickRow: onClickRowBegEdit,
                    onLoadSuccess: function () {
                        $(this).datagrid('enableDnd');
                    },
                    onDrop: function (targetRow, sourceRow, point) {
                        console.log(targetRow + ' ' + sourceRow + " " + point)
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
    if (freshFlg == undefined && $.trim(value) == '') {
        return false;
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
    tabDiv.append($('<table id="col_grid_' + newTblId + '" class="easyui-datagrid" idField="columnId" data-options="fitColumns:true,rownumbers:true,nowrap:false,singleSelect:true,border:false"></table>'));

    var loadLy = layer.load(1);
    // 查询表定义信息，动态加载列定义
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/getColDef?_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0 && data.data) {
                var colHeader = data.data.columns;
                // 在这里添加列定义的formatter，styler，editor（目前只有这3个）
                // 这里还没有更好的办法直接定位到需要添加属性的所在列，只能循环
                $(colHeader[0]).each(function(index, el) {
                    if (colHeader[0][index].field == 'desc') {
                        colHeader[0][index].formatter = descformatter;
                    }
                });

                var rows = [{},{},{},{},{},{},{},{},{},{},{}];
                // 然后加载列定义
                $("#col_grid_" + newTblId).datagrid({
                    data: rows,
                    columns: colHeader,
                    onDblClickRow: onClickRowBegEdit
                });
            }
        }
    });
}

// 删除当前项目
function delCurrTable() {
    layer.confirm('确定要删除选定项目？该操作不可恢复，是否确认删除？', { icon: 7,
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

var isRowEdited = false;
// 双击表格行，开始编辑，只允许一行一行编辑
function onClickRowBegEdit(index, rowVal) {
    if (_curTblId == undefined || _curTblId == null || _curTblId == '') {
        return false;
    }
    isRowEdited = true;
    if (editIndex != index) {
        if (endEditing()) {
            $('#col_grid_' + _curTblId).datagrid('selectRow', index).datagrid('beginEdit', index);
            editIndex = index;
        } else {
            $('#col_grid_' + _curTblId).datagrid('selectRow', editIndex);
        }
    }
}

var editIndex = undefined;
function endEditing() {
    if (editIndex == undefined) {
        return true
    }
    if ($('#col_grid_' + _curTblId).datagrid('validateRow', editIndex)) {
        //var ed = $('#col_grid_' + _curTblId).datagrid('getEditor', {index: editIndex, field: 'productid'});
        //var productname = $(ed.target).combobox('getText');
        //$('#col_grid_' + _curTblId).datagrid('getRows')[editIndex]['productname'] = productname;
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
function descformatter(value,row,index) {
    if (row.desc) {
        return '<div style="width: 100%;display:block;word-break: break-all;word-wrap: break-word">' + row.desc + '</div>';
    }
    return '';
}