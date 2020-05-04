/**
 * 主要是表设计的索引定义保存
 */

// 编辑索引时，探出框中索引列表的列定义
var tblidx_grid_cols = [[
    { field:'idxName',title:'索引名称',width:250,editor:'text' },
    { field:'idxCol', title:'栏位', width:350,
        editor : {
            "type" : "combobox",
            "options" : {
                "valueField" : "text",
                "textField" : "name",
                "required" : true,
                limitToList: true,
                multiple: true,
                panelHeight: 250
            }
        }
    },
    { field:'idxType', title:'索引类型', width:100,
        formatter: function(value, row, index) {
            if (value) {
                return $translate('index_type_val_' + value);
            }
            return '';
        },
        editor : {
            "type" : "combobox",
            "options" : {
                "valueField" : "text",
                "textField" : "name",
                "data" : [
                    {text:'0',name:''},
                    {text:'1',name:'Normal'},
                    {text:'2',name:'Unique'},
                    {text:'3',name:'Full Text'}
                ],
                "required" : true,
                limitToList: true,
                panelHeight: 120,
                editable: false
            }
        }
    },
    { field:'idxMethod', title:'索引方法', width:90,
        formatter: function(value, row, index) {
            if (value) {
                return $translate('index_method_val_' + value);
            }
            return '';
        },
        editor : {
            "type" : "combobox",
            "options" : {
                "valueField" : "text",
                "textField" : "name",
                "data" : [
                    {text:'0',name:''},
                    {text:'1',name:'BTREE'},
                    {text:'2',name:'HASH'}
                ],
                "required" : true,
                limitToList: true,
                panelHeight: 100,
                editable: false
            }
        }
    },
    { field:'remarks',title:'备注',width:380,editor:'textbox' },
]];

// 编辑索引(弹出对话框)
function editIndex() {
    if (_curTblId == 0 || _curTblId == undefined || _curTblId == '') {
        return;
    }
    if (_curTblId < 100) {
        layer.msg("创建表时不可直接创建索引，必须先保存表定义．");
        return;
    }
    var prefixId = '#' + _curTblId;
    var tName = $.trim($(prefixId + ' input._tbl_name').textbox('getValue'));

    $('#tblidx_dlg').dialog({
        title: '创建索引- ' + tName
    });

    // 获取当前表定义数据
    var rowsData = _getGrid().datagrid('getRows');
    var idxCols = [];
    rowsData.forEach(function (value, index, array) {
        if (value.columnName) {
            var item = {};
            item.text = value.columnName;
            item.name = value.columnName;
            idxCols.push(item);
        }
    });

    var idxColField = tblidx_grid_cols[0][1];
    idxColField.editor.options.data = idxCols;

    // 先要查询出现有索引定义
    var idxData = [];
    if (_curTblId > 100) {
        var loadLy = layer.load(1);
        $.ajax({
            type: 'get',
            async: false,
            url: Ap_servletContext + "/ajax/getTblIdxList?tblId=" + _curTblId + '&_t=' + new Date().getTime(),
            success: function (data) {
                layer.close(loadLy);
                idxData = data;
            }
        });
    }

    $('#tblidx_grid').datagrid({
        data: idxData,
        columns: tblidx_grid_cols,
        onDblClickCell: onIdxClickCell
    });
    $('#tblidx_grid').datagrid('reload');

    $('#tblidx_dlg').dialog('open');
}

// 添加索引
var defaultVal = 1;
function addTblIndex() {
    defaultVal ++;
    if (defaultVal > 20) {
        layer.msg('你想干什么？');
        return;
    }
    $('#tblidx_grid').datagrid('appendRow', {idxId: defaultVal});
}

// 删除索引
function delTblIndex() {
    var gridObj = $('#tblidx_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        layer.msg('请选择一个项目后再操作．')
        return;
    }
    var s2 = gridObj.datagrid('getRowIndex', s1.idxId);
    if (s2 < 0) {
        layer.msg('数据错误，请刷新画面后再操作．')
        return;
    }

    layer.confirm('确定要删除选定的索引？<br/>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        $('#tblidx_grid').datagrid('deleteRow', s2);
    }, function() {
        // 无操作
    });
}

// 保存索引的修改
function submitTblIdx() {
    $('#tblidx_grid').datagrid('acceptChanges');
    var rowsData = $('#tblidx_grid').datagrid('getRows');
    if (rowsData.length == 0) {
        return;
    }
    var postData = {};
    postData.tblId = _curTblId;
    postData.idxList = rowsData;

    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/saveTblIdxDefine',
        data: JSON.stringify(postData),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                // 保存成功
                layer.msg("保存成功。");
                $('#tblidx_dlg').dialog('close');
                _getGrid().datagrid('reload');
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 取消修改(不保存)
function cancelTblIdx() {
    var rowsData = $('#tblidx_grid').datagrid('getRows');
    if (rowsData.length == 0) {
        $('#tblidx_dlg').dialog('close');
        return;
    }
    layer.confirm('确定要取消修改？<br/>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        $('#tblidx_dlg').dialog('close');
    }, function() {
        // 无操作
    });
}

var idxEditIndex = undefined;

function _endIdxEditing() {
    if (idxEditIndex == undefined) {
        return true
    }
    if ($('#tblidx_grid').datagrid('validateRow', idxEditIndex)) {
        $('#tblidx_grid').datagrid('endEdit', idxEditIndex);
        idxEditIndex = undefined;
        return true;
    } else {
        return false;
    }
}

function onIdxClickCell(index, field) {
    if (_endIdxEditing()) {
        $('#tblidx_grid').datagrid('selectRow', index).datagrid('editCell', { index: index, field: field });
        var ed = $('#tblidx_grid').datagrid('getEditor', {index: index, field: field});
        if (ed && ed.target) {
            if (field == 'desc') {
                $(ed.target).focus();
            } else {
                if ($(ed.target).closest('td').find("input.textbox-text")[0]) {
                    $(ed.target).closest('td').find("input.textbox-text")[0].focus();
                }
            }
        }
        idxEditIndex = index;
    }
}


// 保存索引的修改
function submitTblIdx() {
    $('#tblidx_grid').datagrid('acceptChanges');
    var rowsData = $('#tblidx_grid').datagrid('getRows');
    if (rowsData.length == 0) {
        return;
    }
    var postData = {};
    postData.tblId = _curTblId;
    postData.idxList = rowsData;

    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/saveTblIdxDefine',
        data: JSON.stringify(postData),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                // 保存成功
                layer.msg("保存成功。");
                $('#tblidx_dlg').dialog('close');
                _getGrid().datagrid('reload');
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 取消修改(不保存)
function cancelTblIdx() {
    var rowsData = $('#tblidx_grid').datagrid('getRows');
    if (rowsData.length == 0) {
        $('#tblidx_dlg').dialog('close');
        return;
    }
    layer.confirm('确定要取消修改？<br/>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);
        $('#tblidx_dlg').dialog('close');
    }, function() {
        // 无操作
    });
}
