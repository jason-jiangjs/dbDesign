/**
 * 主要是设计稿的更新保存
 */

$(function () {

    // 下面这一段代码是扩展datagrid的编辑功能
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
                // 编辑时，加载内容后，重新设置textarea高度
                $(target).each(function () {
                    this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;width:100%;overflow-y:hidden;');
                });
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

    // 下面这一段代码是用于mongodb表设计的脚本定义，用于节点的展开和收缩
    $('body').on('click', 'span.tree-hit.tree-expanded', function() {
        endEditing();
        var rowObj = $(this);
        rowObj.addClass('tree-collapsed');
        rowObj.removeClass('tree-expanded');
        rowObj.removeClass('tree-expanded-hover');
        // 展开/收缩当前节点
        var rowIdx = rowObj.data('id');
        var itemVal = rowObj.data('value');
        var prex = itemVal.lastIndexOf(" ");

        var trColArr = rowObj.closest('div.datagrid div.datagrid-view').find('div.datagrid-view1 div.datagrid-body div.datagrid-body-inner table.datagrid-btable tbody tr');
        var trDataArr = rowObj.closest('div.datagrid div.datagrid-view').find('div.datagrid-view2 div.datagrid-body table.datagrid-btable tbody tr');

        var allDatas = $('#col_grid_' + _curTblId).datagrid('getRows');
        var inSameLvl = false;
        allDatas.forEach(function(item, index, array) {
            if (index <= rowIdx || inSameLvl) {
                return;
            }
            // 比较是否在同一层
            if (prex >= item.columnName.lastIndexOf(" ")) {
                inSameLvl = true;
                return;
            }
            $(trColArr[index]).hide();
            $(trDataArr[index]).hide();
        });
    });
    $('body').on('mouseenter', 'span.tree-hit.tree-expanded', function() {
        $(this).addClass('tree-expanded-hover');
    });
    $('body').on('mouseleave', 'span.tree-hit.tree-expanded', function() {
        $(this).removeClass('tree-expanded-hover');
    });
    $('body').on('click', 'span.tree-hit.tree-collapsed', function() {
        endEditing();
        var rowObj = $(this);
        rowObj.addClass('tree-expanded');
        rowObj.removeClass('tree-collapsed');
        rowObj.removeClass('tree-collapsed-hover')
        // 展开/收缩当前节点
        var rowIdx = rowObj.data('id');
        var itemVal = rowObj.data('value');
        var prex = itemVal.lastIndexOf(" ");

        var trColArr = rowObj.closest('div.datagrid div.datagrid-view').find('div.datagrid-view1 div.datagrid-body div.datagrid-body-inner table.datagrid-btable tbody tr');
        var trDataArr = rowObj.closest('div.datagrid div.datagrid-view').find('div.datagrid-view2 div.datagrid-body table.datagrid-btable tbody tr');

        var allDatas = $('#col_grid_' + _curTblId).datagrid('getRows');
        var inSameLvl = false;
        allDatas.forEach(function(item, index, array) {
            if (index <= rowIdx || inSameLvl) {
                return;
            }
            // 比较是否在同一层
            if (prex >= item.columnName.lastIndexOf(" ")) {
                inSameLvl = true;
                return;
            }
            $(trColArr[index]).show();
            $(trDataArr[index]).show();
            var exp = $(trDataArr[index]).find('span.tree-hit');
            if (exp) {
                exp.addClass('tree-expanded');
                exp.removeClass('tree-collapsed');
            }
        });
    });
    $('body').on('mouseenter', 'span.tree-hit.tree-collapsed', function() {
        $(this).addClass('tree-collapsed-hover');
    });
    $('body').on('mouseleave', 'span.tree-hit.tree-collapsed', function() {
        $(this).removeClass('tree-collapsed-hover')
    });

});


// 用来标识当前表是否正在编辑的变量必须是独立的，也就是要定义为map类型，key的值为_curTblId
var editIndexMap = [];
var isRowEditedMap = [];
// 保存当前可以编辑的表
var editableMap = [];
// 保存当前可以拖放操作的表
var dragDropableMap = [];

// 双击表格行，开始编辑，只允许一行一行编辑
function onClickRowBegEdit(index, field, value) {
    if (editableMap[_curTblId] == undefined || editableMap[_curTblId] === false) {
        return false;
    }
    if (_curTblId == undefined || _curTblId == null || _curTblId == '') {
        return false;
    }
    isRowEditedMap[_curTblId] = true;

    if (_endEditing()) {
        var gridObj = _getGrid();
        gridObj.datagrid('selectRow', index).datagrid('editCell', { index: index, field: field });
        var ed = gridObj.datagrid('getEditor', { index: index, field: field });
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
        editIndexMap[_curTblId] = index;
    }
}

// 显示编辑工具栏
function _displayEditToolbar(dspFlg) {
    var layoutH = '';
    if (dspFlg) {
        layoutH = '125px';  // 这里的高度暂时直接固定，因为画面项目不会增减
        $('#' + _curTblId + ' div._tbl_edit_toolbar').show();
    } else {
        layoutH = '80px';
        $('#' + _curTblId + ' div._tbl_edit_toolbar').hide();
    }

    // 要重新计算layout高度
    var p = $('#tabDiv_' + _curTblId).layout('panel', 'north');	// get the north panel
    var oldHeight = p.panel('panel').outerHeight();
    p.panel('resize', {height: layoutH});
    var newHeight = p.panel('panel').outerHeight();
    $('#tabDiv_' + _curTblId).layout('resize', {
        height: ($('#tabDiv_' + _curTblId).height() + newHeight - oldHeight)
    });
}

// 开始编辑，要先去后台确认该表是否有其他人正在编辑
function chkForEditing() {
    var loadLy = layer.load(1);
    var last_upd = $.trim($('#' + _curTblId + ' input._tbl_last_updtime').val());
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/chkTblEditable?tableId=' + _curTblId + '&lastUpd=' + last_upd,
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                // 可以开始编辑
                editableMap[_curTblId] = true;
                $('#' + _curTblId + ' input._tbl_name').textbox('readonly', false);
                $('#' + _curTblId + ' input._tbl_name_cn').textbox('readonly', false);
                $('#' + _curTblId + ' input._tbl_desc').textbox('readonly', false);

                // 显示编辑工具栏
                _displayEditToolbar(true);
                // 切换菜单
                $('#_tab_page_btn_editable').css("display", "none");
                $('#_tab_page_btn_editing').css("display", "inline");

            } else if (data.code == 1) {
                layer.msg(data.msg);
            } else if (data.code == 2) {
                // 已经被修改过了
                layer.msg(data.msg);
                // 刷新定义数据(先关闭当前tab，再重新打开)
                loadLy = layer.load(1);
                var nowTblId = _curTblId;
                var tabIdx = $('#tbl-tabs').tabs('getTabIndex', $('#tbl-tabs').tabs('getSelected'));
                $('#tbl-tabs').tabs('close', tabIdx);
                _openNewTab(nowTblId);

                // 可以开始编辑
                editableMap[_curTblId] = true;
                $('#' + _curTblId + ' input._tbl_name').textbox('readonly', false);
                $('#' + _curTblId + ' input._tbl_name_cn').textbox('readonly', false);
                $('#' + _curTblId + ' input._tbl_desc').textbox('readonly', false);
                // 显示编辑工具栏
                _displayEditToolbar(true);
                layer.close(loadLy);
                // 提示重新操作
                layer.msg("刷新数据成功，请重新操作．");

            } else if (data.code == 5011 || data.code == 5012) {
                $.messager.confirm({
                    title: '提示信息',
                    msg: data.msg,
                    ok: '强制编辑',
                    cancel: '取消',
                    fn: function(r) {
                        if (r) {
                            var loadLy = layer.load(1);
                            $.ajax({
                                type: 'post',
                                url: Ap_servletContext + '/ajax/forceTblEditable?tableId=' + _curTblId,
                                success: function (data) {
                                    layer.close(loadLy);
                                    if (data.code == 0) {
                                        // 可以开始编辑
                                        editableMap[_curTblId] = true;
                                        $('#' + _curTblId + ' input._tbl_name').textbox('readonly', false);
                                        $('#' + _curTblId + ' input._tbl_name_cn').textbox('readonly', false);
                                        $('#' + _curTblId + ' input._tbl_desc').textbox('readonly', false);

                                    } else {
                                        layer.msg(data.msg + ' code=' + data.code);
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 结束编辑状态
function _endEditing() {
    if (editIndexMap[_curTblId] == null) {
        return true
    }
    var gridObj = _getGrid();
    if (gridObj.datagrid('validateRow', editIndexMap[_curTblId])) {
        gridObj.datagrid('endEdit', editIndexMap[_curTblId]);
        editIndexMap[_curTblId] = null;
        return true;
    } else {
        return false;
    }
}
// 取消编辑(点击菜单而来)
// 这里有两种情况，一种是正常开始的编辑，
// 另一种是新建表，这种情况下需要清除编辑状态，关闭当前tab页
function endEditing() {
    var tabOjb = $('#tbl-tabs').tabs('getSelected');
    var tabTitle = tabOjb.panel("options").title;

    var dlgTipTxt = null;
    if (tabTitle == '新建表') {
        var tblName = $('#' + _curTblId + ' input._tbl_name').textbox('getText');
        if (tblName) {
            dlgTipTxt = '确定要取消新建表\'' + tblName + '\',不保存数据？<br/>该操作不可恢复，是否确认取消？';
        } else {
            dlgTipTxt = '确定要取消新建表,不保存数据？<br/>该操作不可恢复，是否确认取消？';
        }
    } else {
        dlgTipTxt = '确定要取消编辑表\'' + tabTitle + '\',不保存修改？<br/>该操作不可恢复，是否确认取消？';
    }

    layer.confirm(dlgTipTxt, { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        if (tabTitle == '新建表') {
            _doEndCreating();
        } else {
            _doEndEditing();
        }
    }, function() {
        // 无操作
    });

    function _doEndCreating() {
        var opts = $('#tbl-tabs').tabs('options');
        var tabIndex = $('#tbl-tabs').tabs('getTabIndex', tabOjb);
        var bcFunc = opts.onBeforeClose;
        opts.onBeforeClose = function(){};  // allowed to close now
        $('#tbl-tabs').tabs('close', tabIndex);
        opts.onBeforeClose = bcFunc;  // restore the event function
    }
    function _doEndEditing() {
        editableMap[_curTblId] = false;
        dragDropableMap[_curTblId] = false;
        _endEditing();

        if (_curTblId && _curTblId < 100) {
            var curIndex = $('#tbl-tabs').tabs('getTabIndex', $('#tbl-tabs').tabs('getSelected'));
            $('#tbl-tabs').tabs('close', curIndex);
            $('#tbl-tabs').tabs('select', 0);
            return;
        }
        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/endEditable?tableId=' + _curTblId,
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    // 隐藏编辑工具栏
                    _displayEditToolbar(false);
                    _getGrid().datagrid('reload');
                    // 切换菜单
                    $('#_tab_page_btn_editable').css("display", "inline");
                    $('#_tab_page_btn_editing').css("display", "none");
                } else {
                    layer.msg(data.msg + ' code=' + data.code);
                }
            }
        });
    }
}

// 开启行拖放操作
function enableDragDrop() {
    if (editableMap[_curTblId] == undefined || editableMap[_curTblId] === false) {
        return false;
    }
    if (_curTblId == undefined || _curTblId == null || _curTblId == '') {
        return false;
    }
    dragDropableMap[_curTblId] = true;
}

// 取得当前页的grid
function _getGrid() {
    var prefixId = '#col_grid_' + _curTblId;
    return $(prefixId);
}
// 取得当前grid中当前选中行的索引
function _getGridRowIdx() {
    var gridObj = _getGrid();
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        return -1;
    }
    var s2 = -2;
    if (s1.columnId) {
        s2 = gridObj.datagrid('getRowIndex', s1.columnId);
    } else {
        s2 = gridObj.datagrid('getRowIndex', s1);
    }
    return s2;
}

// 点击"表说明"输入框时，取消当前grid的行选择
function unselectGridItem() {
    var gridObj = _getGrid();
    gridObj.datagrid('unselectAll');
}

// 添加栏位
function addColumn() {
    _getGrid().datagrid('appendRow',{'default': ""});
}

// 插入栏位
function insertColumn() {
    var s2 = _getGridRowIdx();
    if (s2 < 0) {
        // 没有选中行，新增
        layer.msg("必须确定插入项目的位置．");
    } else {
        _getGrid().datagrid('insertRow',{
            index: s2,
            row: { 'default': "" }
        });
    }
}

// 删除栏位
function delColumn() {
    var s2 = _getGridRowIdx();
    if (s2 < 0) {
        layer.msg("必须选择项目后才能删除．");
    }

    layer.confirm('确定要删除选定项目？<br/>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        if (s2 >= 0) {
            var gridObj = _getGrid();
            gridObj.datagrid('deleteRow', s2);
            gridObj.datagrid('unselectAll');
        }
        isRowEditedMap[_curTblId] = true;
    }, function() {
        // 无操作
    });
}

// 保存所有修改内容
// 前端不详细判断确实是有内容修改，直接把所有数据提交到后台，由后台负责处理
function saveAll() {
    // 要先定位到当前tab
    var prefixId = '#' + _curTblId;
    var tName = $.trim($(prefixId + ' input._tbl_name').textbox('getValue'));
    var tNameCn = $.trim($(prefixId + ' input._tbl_name_cn').textbox('getValue'));
    var tDesc = $.trim($(prefixId + ' input._tbl_desc').textbox('getValue'));
    var tNamep = $.trim($(prefixId + ' input._tbl_name_p').val());
    var tNameCnp = $.trim($(prefixId + ' input._tbl_name_cn_p').val());
    var tDescp = $.trim($(prefixId + ' input._tbl_desc_p').val());
    if (!isRowEditedMap[_curTblId] && tName == tNamep && tNameCn == tNameCnp && tDesc == tDescp) {
        layer.msg("没有修改，不需要保存。");
        return false;
    }

    var postData = {};
    postData.dbId = $.trim($('#dbId').val());
    postData._tbl_id = _curTblId;
    postData._tbl_name = tName;
    postData._tbl_name_cn = tNameCn;
    postData._tbl_desc = tDesc;
    postData._tbl_last_upd = $.trim($(prefixId + ' input._tbl_last_updtime').val());

    $('#col_grid_' + _curTblId).datagrid('acceptChanges');
    postData.column_list = $('#col_grid_' + _curTblId).datagrid('getRows');

    // 对输入值简单验证，新建表时表名重复，列名重复，缺少类型 todo 换一种方式,从后台查
    var duplicate = false;
    if (_curTblId < 100) {
        // var rowData = $('#tblList').datalist('getRows');
        // for (x in rowData) {
        //     if (rowData[x].tableName == tName) {
        //         duplicate = true;
        //         break;
        //     }
        // }
        // if (duplicate) {
        //     layer.msg("表名: " + tName + " 与现有的重复，请修改后再保存。");
        //     return;
        // }
    }

    if (postData.column_list.length != undefined && postData.column_list.length > 0 && $.trim($('#dbType').val()) != 2) {
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
                // editIndexMap[_curTblId] = null;
                // editableMap[_curTblId] = false;
                // 保存成功后关闭表格的编辑状态，如果是新建表还要刷新tab
                if (_curTblId < 100) {
                    loadLy = layer.load(1);
                    // 刷新当前tab（经试验，只能关闭当前tab,再打开新的tab）
                    var _newTblId = data.data._newTblId;
                    var tabIdx = $('#tbl-tabs').tabs('getTabIndex', $('#tbl-tabs').tabs('getSelected'));
                    $('#tbl-tabs').tabs('close', tabIdx);
                    _openNewTab(_newTblId);

                    // $('#tblList').datalist({
                    //     onLoadSuccess: function(data) {
                    //         $('#tblList').datalist("selectRecord", _newTblId);
                    //     }
                    // });
                    // $('#tblList').datalist("load", {});
                    layer.close(loadLy);
                }
                endEditing();
                $(prefixId + ' input._tbl_last_updtime').val(data.data.lastUpd);
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}


// 使用预定义模板-弹出对话框
function useTemplate() {
    if (_curTblId == 0 || _curTblId == undefined || _curTblId == null) {
        return false;
    }
    // 弹出对话框
    $('#template_dlg').dialog('open');
}

// 使用预定义模板1
function useTemplate1() {
    _getGrid().datagrid('appendRow', { 'columnName': "valid", 'type': "tinyint", 'columnLens': "2", 'notnull': "Y", 'default': "1", 'columnNameCN': "数据是否有效", 'desc': "0:无效 1:有效" });
    _getGrid().datagrid('appendRow', { 'columnName': "created_by", 'type': "bigint", 'columnLens': "20", 'notnull': "Y", 'columnNameCN': "创建者ID" });
    _getGrid().datagrid('appendRow', { 'columnName': "updated_by", 'type': "bigint", 'columnLens': "20", 'notnull': "Y", 'columnNameCN': "更新者ID" });
    _getGrid().datagrid('appendRow', { 'columnName': "created_date", 'type': "datetime ", 'notnull': "Y", 'columnNameCN': "创建时间" });
    _getGrid().datagrid('appendRow', { 'columnName': "updated_date", 'type': "datetime ", 'notnull': "Y", 'columnNameCN': "创建时间" });
    $('#template_dlg').dialog('close');
}
// 使用预定义模板2
function useTemplate2() {
    _getGrid().datagrid('appendRow', { 'columnName': "created_by", 'type': "bigint", 'columnLens': "20", 'notnull': "Y", 'columnNameCN': "创建者ID" });
    _getGrid().datagrid('appendRow', { 'columnName': "created_date", 'type': "datetime ", 'notnull': "Y", 'columnNameCN': "创建时间" });
    $('#template_dlg').dialog('close');
}
