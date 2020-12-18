/**
 * sql脚本输出
 */

// 导出SQL语句
// 主页：导出所选择的表
function exportSelectedSql() {
    var tblIdList = [];
    if (opType == 1) {
        if (_curTblId == 0 || _curTblId == undefined || _curTblId == '') {
            return;
        }
        if (_curTblId < 100) {
            layer.msg("创建表时不可直接导出表定义，必须先保存．");
            return;
        }
        tblIdList.push(_curTblId);
    } else {
        // 获取所选中的表
        var tblList = $('#tbl-def-grid').datagrid('getChecked');
        if (tblList.length == 0) {
            layer.msg('未选择要删除的表');
            return false;
        }
        tblList.forEach(function(obj, index) {
            tblIdList.push(obj._id);
        });
    }

    var url = Ap_servletContext + '/ajax/exportSql';

    var tmpInput = $("<input type='text' name='tblIdList'/>");
    tmpInput.attr("value", JSON.stringify(tblIdList));
    var myform = $("<form></form>").attr("action", url).attr("method", "post");
    myform.append(tmpInput);
    myform.appendTo('body').submit().remove();
}

// 导出SQL变更语句
// 主页：导出所选择的表的变更SQL脚本(相对于上一次发布的版本-标签)
function exportSelectedUpdateSql() {

}


// 导出SQL语句
// 主页：导出当前查看的表
function exportTableSql() {
    var tblIdList = [];
    if (opType == 1) {
        if (_curTblId == 0 || _curTblId == undefined || _curTblId == '') {
            return;
        }
        if (_curTblId < 100) {
            layer.msg("创建表时不可直接导出表定义，必须先保存．");
            return;
        }
        tblIdList.push(_curTblId);
    } else {
        // 获取所选中的表
        var tblList = $('#tbl-def-grid').datagrid('getChecked');
        if (tblList.length == 0) {
            layer.msg('未选择要删除的表');
            return false;
        }
        tblList.forEach(function(obj, index) {
            tblIdList.push(obj._id);
        });
    }

    var url = Ap_servletContext + '/ajax/exportSql';

    var tmpInput = $("<input type='text' name='tblIdList'/>");
    tmpInput.attr("value", JSON.stringify(tblIdList));
    var myform = $("<form></form>").attr("action", url).attr("method", "post");
    myform.append(tmpInput);
    myform.appendTo('body').submit().remove();
}

// 导出SQL变更语句
// 主页：导出当前查看的表的变更SQL脚本(相对于上一次修改内容)
function exportTabledUpdateSql() {

}
