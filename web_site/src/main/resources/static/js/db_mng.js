/**
 * 数据库设计管理
 */

// 画面项目初始化，加载数据库一览
$(function () {

    // 加载列定义
    var options = {
        idField: "_id",
        fit: true,
        fitColumns: true,
        rownumbers: true,
        nowrap: false,
        striped: true,
        singleSelect: true,
        method: 'get',
        pagination: true,
        pageSize: 20,
        pageList: [20,50,100],
        toolbar: '#db_grid_toolbar'
    };
    options.url = Ap_servletContext + '/ajax/mng/getDbList?_t=' + new Date().getTime();
    options.columns = [[
        {field:'_id',title:'ID',width:40},
        {field:'dbName',title:'名称',width:100},
        {field:'typeStr',title:'版本号',width:60,
            formatter: function(value, row, index) {
                return $.trim(row.dbProvider) + ' ' + $.trim(row.dbVersion);
            }},
        {field:'projectStatus',title:'状态',width:40,
            formatter: function(value, row, index) {
                if (value) {
                    return $translate('user_status_val_' + value);
                }
                return '';
            }
        },
        {field:'desc',title:'备注',width:100}
    ]];

    options.onDblClickRow = function(index, row) {
        // 弹出对话框，显示数据库详细信息
        $('#dbId').val(row._id);
        $('#dbName').textbox('setValue', row.dbName);
        $('#dbNameCN').textbox('setValue', row.dbNameCN);
        $('#desc').textbox('setValue', row.desc);
        $('#ver').textbox('setValue', row.typeVer);
        $('#typeStr').combobox('setValue', row.typeStrVal);
        $('#db_dlg').dialog('open');
    };

    $('#db_grid').datagrid(options);

    $('#projStatus').combobox({
        data: [{
            "id": 1,
            "text": "正常"
        },{
            "id": 2,
            "text": "锁定"
        },{
            "id": 4,
            "text": "删除"
        }],
        valueField: 'id',
        textField: 'text'
    });
    $('#typeStr').combobox({
        data: [{
            "id": 1,
            "text": "mysql",
            "type": 1
        },{
            "id": 2,
            "text": "oracle",
            "type": 1
        },{
            "id": 3,
            "text": "db2",
            "type": 1
        },{
            "id": 4,
            "text": "mongodb",
            "type": 2
        },{
            "id": 5,
            "text": "solr",
            "type": 3
        }],
        valueField: 'id',
        textField: 'text'
    });
});

// 提交修改(保存)
function submitForm() {
    // 不判断是否已修改，全部提交至后台
    var postData = {};
    var idStr = $.trim($('#dbId').val());
    if (idStr == '') {
        postData._id = 0;
    } else {
        postData._id = parseInt(idStr);
    }
    postData.dbName = $.trim($('#dbName').textbox('getValue'));
    postData.dbNameCN = $.trim($('#dbNameCN').textbox('getValue'));
    postData.desc = $.trim($('#desc').textbox('getValue'));
    postData.typeVer = $.trim($('#ver').textbox('getValue'));
    var verInfo = $.trim($('#typeStr').combobox('getValue'));
    postData.typeStrVal = verInfo;

    // 先验证必须值
    if (postData.dbName == '' || verInfo == '' || postData.typeVer == '') {
        layer.msg("数据库名称，以及类型和版本必须输入！");
        return;
    }

    var dbData = $('#typeStr').combobox('getData');
    for (idx in dbData) {
        if (verInfo == dbData[idx].id) {
            postData.typeStr = dbData[idx].text;
            postData.type = dbData[idx].type;
            break;
        }
    }
    if (postData.typeStr == undefined || postData.typeStr == null || postData.typeStr == '') {
        layer.msg("类型和版本必须输入！");
        return;
    }

    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/mng/saveDbInfo',
        data: JSON.stringify(postData),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                layer.msg('保存成功，将关闭对话框．');
                setTimeout("_endSave()", 1000)
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 关闭对话框,刷新用户一览(当前分页)
function _endSave() {
    $('#db_grid').datagrid('reload', {});
    $('#db_dlg').dialog('close');
}

// 取消修改，并关闭对话框
function cancelForm() {
    $('#db_dlg').dialog('close');
}

// 添加数据库
function addDb() {
    // 弹出对话框
    $('#dbId').val(null);
    $('#dbName').textbox('setValue', null);
    $('#dbNameCN').textbox('setValue', null);
    $('#desc').textbox('setValue', null);
    $('#ver').textbox('setValue', null);
    $('#typeStr').combobox('clear');
    $('#db_dlg').dialog('open');
}

// 删除数据库
function deleteDb() {
    var gridObj = $('#db_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        layer.msg('请选择一个数据库后再操作．')
        return;
    }
    var s2 = gridObj.datagrid('getRowIndex', s1._id);
    if (s2 < 0) {
        layer.msg('数据错误，请刷新画面后再操作．')
        return;
    }

    layer.confirm('确定要删除选定的数据库［' + s1.dbName + '］?<br>该操作不可恢复，是否确认删除?', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        // 提交请求到后台
        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/mng/delDb?dbId=' + s1._id,
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    layer.close(index);
                    // 刷新用户一览
                    $('#db_grid').datagrid('reload', {});
                } else {
                    layer.msg(data.msg + ' code=' + data.code);
                }
            }
        });
    }, function() {
        // 无操作
    });
}
