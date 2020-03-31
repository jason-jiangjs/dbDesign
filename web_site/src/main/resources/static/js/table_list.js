/**
 * 列表页相关js代码（包括初始化）
 */

// 画面项目初始化
// var myUm = null;

$(function () {
    var curDbId = $.trim($('#dbId').val()); // 异常情况下，这里使用了dbId作为error code
    if (curDbId == 0 || curDbId == 1) {
        $.messager.alert({
            iconCls: 'icon-no',
            title: '数据错误',
            closable: false,
            ok: '返回',
            msg: '所选择的数据库不存在，请联系系统管理员。<br/>再见!',
            fn: function(){
                window.location.href = Ap_servletContext + '/home?type=1';
            }
        });
        return;
    } else if (curDbId == 2 || curDbId == 3) {
        $.messager.alert({
            iconCls: 'icon-no',
            title: '权限错误',
            closable: false,
            ok: '返回',
            msg: '没有权限访问选择的数据库，请联系系统管理员。<br/>再见!',
            fn: function(){
                window.location.href = Ap_servletContext + '/home?type=1';
            }
        });
        return;
    }

    // $('#main_page').append("<script type=\"text/plain\" id=\"myEditor\" style=\"width:1000px;height:590px;\"></script>");
    // myUm = UM.getEditor('myEditor', {
    //     /* 传入配置参数,可配参数列表看umeditor.config.js */
    //     toolbar: ['source | undo redo | bold italic underline strikethrough | superscript subscript | forecolor backcolor | removeformat |',
    //         'insertorderedlist insertunorderedlist | selectall cleardoc paragraph | fontfamily fontsize' ,
    //         '| justifyleft justifycenter justifyright justifyjustify |',
    //         '| horizontal preview']
    // });
    // myUm.setDisabled();


    // 下拉菜单的菜单项控制，某些特定场景下不显示或禁用，比如收藏夹，或者是权限控制
    var favDb = $.trim($('#favDb').val());
    toggleFavoriteMenuItem(!(favDb && favDb == $.trim($('#dbId').val())));
    closeErDiagram();

    // 创建搜索框
    $('#tbl_searchbox').textbox({
        prompt: 'Please Input Value',
        iconCls: 'icon-search',
        iconAlign: 'left'
    });
    $('#tbl_searchbox').textbox('textbox').bind({
        keyup: function (e) {
            doSearch($(this).val());
            $('#tbl-def-grid').datagrid('unselectAll');
            $('#tbl-def-grid').datagrid('uncheckAll');
        }
    });
    $(document).keydown(function(e) {
        if (e.ctrlKey && e.key == 'Home') {
            // 回到主页
            jumptoHomeTab();
        } else if (e.ctrlKey && e.key == 'End') {
            // 快捷下拉列表
            showShortcutList();
        }
    });

    // 初始化Tab页
    $('#tbl-tabs').tabs({
        // Tab页切换（切换时要考虑快捷菜单, 有3种形式：只读，可编辑，正在编辑）
        onSelect: function(title, index) {
            _curTblId = null;
            if (index == 0) {
                // 回到主页
                _curTblId = 0;
                // 切换菜单显示
                $('#_home_page_btn').css("display", "inline");
                $('#_tab_page_btn_editable').css("display", "none");
                $('#_tab_page_btn_editing').css("display", "none");
                return;
            }
            var pp = $('#tbl-tabs').tabs('getTab', index); // tab页切换时要记住当前页id
            var tabId = pp.panel('options').id;
            if (tabId) {
                _curTblId = tabId;
            }
            // 取消在首页中的选中行
            $('#tbl-def-grid').datagrid('unselectAll');
            // 切换菜单显示
            $('#_home_page_btn').css("display", "none");
            if (isTabInEditing()) {
                $('#_tab_page_btn_editable').css("display", "none");
                $('#_tab_page_btn_editing').css("display", "inline");
            } else {
                $('#_tab_page_btn_editable').css("display", "inline");
                $('#_tab_page_btn_editing').css("display", "none");
            }
        },
        // Tab页关闭前的验证, 主要是检查是否处于编辑状态
        onBeforeClose: function(title, index) {
            if (index == 0) {
                // 主页
                return false;
            }
            if (isTabInEditing()) {
                var that = this;
                var tabIndex = index;
                var dlgTitle = null;
                if (title == '新建表') {
                    var tblName = $('#' + _curTblId + ' input._tbl_name').textbox('getText');
                    if (tblName) {
                        dlgTitle = '确定要取消新建表\'' + tblName + '\',不保存数据？<br/>该操作不可恢复，是否确认取消？'
                    } else {
                        dlgTitle = '确定要取消新建表,不保存数据？<br/>该操作不可恢复，是否确认取消？'
                    }
                } else {
                    dlgTitle = '确定要取消编辑表\'' + title + '\',不保存修改？<br/>该操作不可恢复，是否确认取消？'
                    // 要去后台保存状态信息
                    var loadLy = layer.load(1);
                    $.ajax({
                        type: 'post',
                        url: Ap_servletContext + '/ajax/endEditable?tableId=' + _curTblId,
                        success: function (data) {
                            layer.close(loadLy);
                        }
                    });
                }
                layer.confirm(dlgTitle, { icon: 7,
                    btn: ['确定','取消'] //按钮
                }, function(index) {
                    layer.close(index);
                    var opts = $(that).tabs('options');
                    var bcFunc = opts.onBeforeClose;
                    opts.onBeforeClose = function(){};  // allowed to close now
                    $(that).tabs('close', tabIndex);
                    opts.onBeforeClose = bcFunc;  // restore the event function
                }, function() {
                    // 无操作
                });
            } else {
                return true;
            }
            return false;	// prevent from closing
        }
    });

    // 获取所有表定义一览, 用于主页
    var defaultPageSize = localStorage.getItem($('#useId').val() + "_" + $('#dbId').val());
    if (defaultPageSize) {
        defaultPageSize = parseInt(defaultPageSize);
    } else {
        defaultPageSize = 100;
    }

    // 加载列定义
    var options = {
        idField: '_id',
        fit: true,
        fitColumns: true,
        nowrap: false,
        striped: true,
        method: 'get',
        pagination: true,
        pageSize: defaultPageSize,
        pageList: [100, 500],
        singleSelect: true,
        checkOnSelect: false,
        selectOnCheck: false
    };
    options.onLoadSuccess = function(data) {
        // 记住翻页参数
        var nowOpts = $('#tbl-def-grid').datagrid('options');
        if (defaultPageSize != nowOpts.pageSize) {
            localStorage.setItem($('#useId').val() + "_" + $('#dbId').val(), nowOpts.pageSize);
        }
    };
    options.onClickRow = function(index, row) {
        // 打开tab页
        var tblId = row._id;
        if (tblId) {
            _curTblId = tblId;
            // 先查看是否已存在，若已存在则切换tab
            if ($('#tbl-tabs').tabs('exists', row.tableName)) {
                $('#tbl-tabs').tabs('select', row.tableName); // 这里不刷新数据，编辑时再确认是否有修改
                return;
            }

            _openNewTab(tblId);
            // 切换菜单显示
            $('#_home_page_btn').css("display", "none");
            $('#_tab_page_btn').css("display", "inline");
        } else {
            $.messager.alert('发生错误', '可能是数据加载错误．', 'error');
        }
    };
    options.url = Ap_servletContext + '/ajax/getTableList?dbId=' + $('#dbId').val() + '&targetType=' + $('#targetType').val() + '&_t=' + new Date().getTime();
    options.columns = [[
        {field:'',checkbox:true},
        {field:'_id',title:'No.', width:25, align:'center',
            formatter: function(value,row,index) { // in v1.7.6 rownumberWidth属性无效，只能先暂时用这个方法
                var nowOpts = $('#tbl-def-grid').datagrid('options');
                return (nowOpts.pageNumber - 1) * nowOpts.pageSize + index + 1;
            }
        },
        {field:'tableName',title:'表名',width:100,
            formatter: function contentFormat(value, row, index) {
                if (value) {
                    return "<div class='grid-cell-textEllipsis'>" + value + "</div>";
                } else {
                    return '';
                }
            }
        },
        {field:'aliasName',title:'别名',width:100,
            formatter: function contentFormat(value, row, index) {
                if (value) {
                    return "<div class='grid-cell-textEllipsis'>" + value + "</div>";
                } else {
                    return '';
                }
            }
        },
        {field:'bizGroup',title:'业务组',width:80},
        {field:'status',title:'状态',width:100,
            formatter: function contentFormat(value, row, index) {
                var showTxt = '';
                if (row.currEditorId) {
                    if (row.currEditorName) {
                        showTxt +=  row.currEditorName + " 正在编辑";
                    } else {
                        showTxt +=  '用户(ID:' + row.currEditorId + ")正在编辑";
                    }
                    if (row.startEditTime) {
                        showTxt +=  "<br>" + moment(row.startEditTime).format('YYYY-MM-DD HH:mm');
                    }
                }
                return showTxt;
            }
        },
        {field:'modifierName',title:'更新者',width:80,
            formatter: function contentFormat(value, row, index) {
                if (row.auditData && row.auditData.modifierName) {
                    return row.auditData.modifierName;
                } else {
                    return '';
                }
            }
        },
        {field:'modifiedTime',title:'更新时间',width:180,fixed:true,
            formatter: function contentFormat(value, row, index) {
                if (row.auditData && row.auditData.modifiedTime) {
                    return moment(row.auditData.modifiedTime).format('YYYY-MM-DD HH:mm:ss');
                } else {
                    return '';
                }
            }
        }
    ]];
    $('#tbl-def-grid').datagrid(options);

    // easyui的textbox未提供onclick事件，只能采用下面方法解决
    $('body').on('click', 'input._tbl_desc + span :first-child', unselectGridItem);
});

// 判断当前tab页是否处于编辑状态
// 是则返回true
function isTabInEditing() {
    if (editableMap[_curTblId] || (_curTblId && _curTblId < 100)) {
        // 只要有效值就说明在编辑
        return true;
    }
    return false;
}

function _openNewTab(tblId) {
    var loadLy = layer.load(1);
    // 查询表定义信息，动态加载列定义
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/getTable?tblId=' + tblId + '&_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0 && data.data) {
                _createTblHeadDiv(tblId, data.data.tblName, data.data.tblNameCn, data.data.tblDesc, data.data.lastUpd);
                _createTblGrid(tblId, data.data.columns);
            } else {
                layer.msg(data.msg + ' (code=' + data.code + ")");
            }
        }
    });
}

// 回到列表页
function jumptoHomeTab() {
    $('#tbl-tabs').tabs('select', 0);
}

// 浮出框,显示已打开的表一览(tab控件标题栏超长时才有此功能)
var timeoutID = null;
function showShortcutList() {
    // 监控tab页标题栏是否有滚动条
    if ('none' == $('#tbl-tabs div.tabs-header div.tabs-scroller-left').css("display")) {
        // 没有滚动条
        return false;
    }
    // 获取已打开的表定义一览, 用于弹出框
    var tabPageList = $('#tbl-tabs').tabs('tabs');
    var filteredDataList = [];
    tabPageList.forEach(function (value, index) {
        var tblId = value.panel('options').id;
        var tblIndex = value.panel('options').index;
        if (tblId > 100) {
            var tblName = value.panel('options').title;
            var item = {};
            item.tblId = tblIndex;
            item.tblName = tblName;
            filteredDataList.push(item);
        } else if (tblId > 0) {
            // 新建表
            var tblName = $('#' + tblId + ' input._tbl_name').textbox('getText') || $('#' + tblId + ' input._tbl_name_cn').textbox('getText');
            if (tblName) {
                var item = {};
                item.tblId = tblIndex;
                item.tblName = tblName;
                filteredDataList.push(item);
            }
        }
    });
    if (filteredDataList.length == 0) {
        return false;
    }
    $('#filteredDataDialog').fadeIn();
    $('#filteredDataList').datalist({
        data: filteredDataList,
        striped: true,
        valueField: 'tblId',
        textField: 'tblName',
        idField: 'tblId',
        onClickRow: function (index, row) {
            $('#tbl-tabs').tabs('select', row.tblId);
            $('#filteredDataDialog').fadeOut();
        },
        onLoadSuccess: function(data) {
            // 重新计算下拉列表的高度
            var dlgHeight = $('#filteredDataDialog').css("height");
            dlgHeight = dlgHeight.substring(0, dlgHeight.length - 2);
            if (parseInt(dlgHeight) > $(window).height() - 35) {
                // 如果下拉框高度已经超过浏览器高度
                $('#filteredDataList').datalist('resize',{
                    width: 250,
                    height: $(window).height() - 35
                });
            }
            if (timeoutID) {
                clearTimeout(timeoutID);
            }
            // 延时自动隐藏
            timeoutID = setTimeout(function () {
                if ($('#filteredDataDialog').css("display") == 'block') {
                    $('#filteredDataDialog').fadeOut();
                }
            }, 5000);
        }
    });
}

// 根据名称查询表一览
function doSearch(value) {
    if (!value) {
        $('#tbl_searchbox').searchbox('clear');
    }
    $('#tbl-def-grid').datagrid('load', {
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

// 创建表头说明
function _createTblHeadDiv(tblId, tblName, tblNameCn, tblDesc, lastUpd) {
    var isCreated = false;
    if (tblName == '' || tblName == null || tblName == undefined) {
        isCreated = true;
        tblName = '新建表';
    }
    // 先动态创建tab
    $('#tbl-tabs').tabs('add', {
        id: tblId,  // tab页的id默认是当前表的id(从数据库而来的)
        title: tblName,
        content: '<div id="tabDiv_' + tblId + '" class="easyui-layout" fit="true"></div>',
        closable: true
    });

    // 动态创建layout
    $('#tabDiv_' + tblId).layout();
    $('#tabDiv_' + tblId).layout('add',{
        region: 'north',
        border: false,
        content: $($('#tb_info').html())
    });
    $('#tabDiv_' + tblId).layout('add',{
        region: 'center',
        border: false,
        content: '<table id="col_grid_' + tblId + '"></table>'
    });

    // 然后加载操作按钮
    var prefixId = "#" + tblId;
    $(prefixId + ' a._linkbtn_add').linkbutton({
        iconCls: 'icon-add'
    });
    $(prefixId + ' a._linkbtn_edit').linkbutton({
        iconCls: 'icon-edit'
    });
    $(prefixId + ' a._linkbtn_remove').linkbutton({
        iconCls: 'icon-remove'
    });
    $(prefixId + ' a._linkbtn_save').linkbutton({
        iconCls: 'icon-save'
    });

    // 设置表名等初始值
    if (isCreated) {
        // 有编辑权限
        _displayEditToolbar(true);
        $('#_tab_page_btn_editable').css("display", "none");
        $('#_tab_page_btn_editing').css("display", "inline");

        $(prefixId + ' input._tbl_name').textbox();
        $(prefixId + ' input._tbl_name_cn').textbox();
        $(prefixId + ' input._tbl_desc').textbox({ multiline: true });
    } else {
        _displayEditToolbar(false);
        $(prefixId + ' input._tbl_name').textbox({value: tblName, 'readonly': true});
        $(prefixId + ' input._tbl_name_cn').textbox({value: tblNameCn, 'readonly': true});
        $(prefixId + ' input._tbl_desc').textbox({value: tblDesc, multiline: true, 'readonly': true});
        $(prefixId + ' input._tbl_name_p').val(tblName);
        $(prefixId + ' input._tbl_name_cn_p').val(tblNameCn);
        $(prefixId + ' input._tbl_desc_p').val(tblDesc);
    }
    // $("input", $(prefixId + ' input._tbl_desc').next("span textarea")).click(function() {
    //     alert("ok"); //unselectGridItem
    // });


    $(prefixId + ' input._tbl_last_updtime').val(lastUpd);
}

// 创建表的定义一览
function _createTblGrid(tblId, colHeader) {
    var colDef = {};
    // 在这里添加列定义的formatter，styler，editor（目前只有这3个）
    // 这里还没有更好的办法直接定位到需要添加属性的所在列，只能循环
    $(colHeader[0]).each(function(index, el) {
        colDef[colHeader[0][index].field] = ''; // 该变量只有在创建表时才有用，数据稍有冗余
        if (colHeader[0][index].field == 'desc') {
            colHeader[0][index].formatter = descformatter;
        } else if (colHeader[0][index].field == 'columnNameCN') {
            colHeader[0][index].formatter = nameformatter;
        } else if (colHeader[0][index].field == 'columnName' && $.trim($('#dbType').val()) == 2) {
            colHeader[0][index].formatter = nameDspformatter;
        }
    });

    // 然后加载列定义
    var options = {
        idField: "columnId",
        fit: true,
        fitColumns: true,
        rownumbers: true,
        nowrap: false,
        striped: true,
        singleSelect: true,
        border: false,
        method: 'get',
        columns: colHeader
    };
    if ($.trim($('#readAttr').val()) == 0) {
        // 有编辑权限
        options.onDblClickCell = onClickRowBegEdit;
        options.onLoadSuccess = function () {
            $(this).datagrid('enableDnd');
        };
        options.onBeforeDrag = function(row) {
            if (dragDropableMap[_curTblId] === true) {
                return true;
            }
            return false;
        };
        options.onDrop = function (targetRow, sourceRow, point) {
            isRowEditedMap[_curTblId] = true;
        };
    }

    if (tblId < 100) {
        // 注意这里必须使用不同的colDef，否则所有行都指向同一个colDef值
        var rows = [colDef,$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef),$.extend({},colDef)];
        options.data = rows;
    } else {
        options.url = Ap_servletContext + '/ajax/getColumnList?tblId=' + tblId + '&_t=' + new Date().getTime();
    }
    $('#col_grid_' + tblId).datagrid(options);
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
    _createTblHeadDiv(newTblId);
    editableMap[_curTblId] = true;

    var loadLy = layer.load(1);
    // 查询表定义信息，动态加载列定义
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/getColDef?type=' + $.trim($('#dbType').val()) + '&_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0 && data.data) {
                _createTblGrid(newTblId, data.data.columns);
            } else {
                layer.msg(data.msg + ' (code=' + data.code + ")");
            }
        }
    });
}

// 删除当前所选择的项目（主页/表一览）
function delSelectedItem() {
    // 先获取列表页中已选择的项目，若没有则返回并提醒
    var tblList = $('#tbl-def-grid').datagrid('getChecked');
    if (tblList.length == 0) {
        layer.msg('未选择要删除的表');
        return false;
    }
    tblList = tblList.map(function(obj) {
        return { 'tableId':obj._id, 'tableName':obj.tableName, 'modifiedTime':obj.modifiedTime === undefined ? null : obj.modifiedTime}
    });

    layer.confirm('确定要删除所选定的项目？<br>该操作不可恢复，是否确认删除？', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        layer.close(index);

        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/bulkDelTableDef',
            data: JSON.stringify(tblList),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    loadLy = layer.load(1);
                    // 删除成功后刷新table list
                    $('#tbl-def-grid').datagrid("load", {});
                    layer.close(loadLy);
                } else {
                    layer.msg(data.msg + ' (code=' + data.code + ")");
                }
            }
        });
    }, function() {
        // 无操作
    });
}

// 切换收藏夹菜单项显示
// 参数toggleFlag为true表示可以加入收藏夹
function toggleFavoriteMenuItem (toggleFlag) {
    if (toggleFlag) {
        // 未设置过收藏夹，只显示"加入收藏夹"
        var item = $('#tab-tools_menu_home').menu('findItem', {name:'setdbenv'});
        if (item) {
            $('#tab-tools_menu_home').menu('enableItem', item.target);
        }
        item = $('#tab-tools_menu_home').menu('findItem', {name:'unsetdbenv'});
        if (item) {
            $('#tab-tools_menu_home').menu('disableItem', item.target);
        }
    } else {
        // 已经设置过收藏夹，只显示"取消"
        var item = $('#tab-tools_menu_home').menu('findItem',  {name:'setdbenv'});
        if (item) {
            $('#tab-tools_menu_home').menu('disableItem', item.target);
        }
        item = $('#tab-tools_menu_home').menu('findItem', {name:'unsetdbenv'});
        if (item) {
            $('#tab-tools_menu_home').menu('enableItem', item.target);
        }
    }
}

// 设置缺省工作环境
// 参数devType为１时表示设置，为０时表示取消
function setDevEnv(devType) {
    var loadLy = layer.load(1);
    var postData = {};
    postData.checkFlg = devType;
    postData.dbId = $.trim($('#dbId').val());

    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/setDefaultDbEnv',
        data: JSON.stringify(postData),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                layer.msg('操作成功');
                // 刷新菜单
                toggleFavoriteMenuItem(devType == 0);
            } else {
                layer.msg(data.msg + ' (code=' + data.code + ")");
            }
        }
    });
}

// 当前所查看的表id（点击左边表一栏时会刷新，tab切换时会刷新）
var _curTblId = null;


// 备注一栏的显示形式
function descformatter(value, row, index) {
    if (value) {
        var reg = new RegExp("\n", "g");
        var str = value.replace(reg, "<br/>");
        return '<div style="width:100%;display:block;word-break: break-all;word-wrap: break-word;margin-top:3px;margin-bottom:3px">' + str + '</div>';
    }
    return '';
}
// 列别名一栏的显示形式
function nameformatter(value, row, index) {
    if (value) {
        return '<div style="width: 100%;display:block;word-break: break-all;word-wrap: break-word">' + value + '</div>';
    }
    return '';
}
// 列名一栏的显示形式(目前有mongodb定义时用到)
function nameDspformatter(value, row, index) {
    if (value) {
        var txt = '';
        var prex = value.lastIndexOf(" ") + 1;
        if (row.type == 'object') {
            txt = value.substring(0, prex).replace(/ /g, "&nbsp;") + '<span data-id="' + index + '" data-value="' + value + '" class="tree-hit tree-expanded"/>+&nbsp;' + value.substring(prex);
        } else if (row.type == 'object array') {
            txt = value.substring(0, prex).replace(/ /g, "&nbsp;") + '<span data-id="' + index + '" data-value="' + value + '" class="tree-hit tree-expanded"/>*&nbsp;' + value.substring(prex);
        } else if (row.type && row.type != 'object array' && row.type.indexOf('array') > 0) {
            txt = value.substring(0, prex).replace(/ /g, "&nbsp;") + '&nbsp;&nbsp;*&nbsp;' + value.substring(prex);
        } else {
            txt = '&nbsp;&nbsp;&nbsp;&nbsp;' + value.replace(/ /g, "&nbsp;");
        }
        return '<span style="font-family:Consolas;font-size:14px">' + txt + '</span>';
    }
    return '';
}
