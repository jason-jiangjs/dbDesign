说明  
1、时间值都定义为long型(两个原因：方便比较大小；mongodb里保存date类型是GMT时间，直接查看数据时不直观)，  
格式为：yyyyMMddHHmmssSSS，使用北京时间
2、表的缺省主键'_id',若非特殊说明，则是默认的'ObjectId'类型

###### AuditData 通用组件(Object)（这个不是单独的表）
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|valid          | bool   | 数据是否有效  | 缺省为"true", 表示该条数据有效
|creatorId      | long   | 创建者ID
|createdTime    | long   | 创建时间
|modifierId     | long   | 更新者ID
|modifierName   | string | 更新者姓名  | 多保存更新者姓名,以避免再去查询一次
|modifiedTime   | long   | 更新时间

###### db_list
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id           | long      | 数据库ID  | 系统内部ID，自增
|+ auditData   | object    | 审计信息 
|dbName        | string    | 数据库名称(项目名称)
|lastTagName   | string    | 最近一个发布版本的标签名
|desc          | string    | 描述
|dbMode        | int       | 类型  | 1:关系型数据库(mysql/oracle/db2...) 2:非关系型数据库(mongo/solr...)
|dbProvider    | string    | 数据库提供商
|dbVersion     | string    | 数据库版本
|dataTypeId    | int       | 数据库(数据类型)区分  | 1:mysql 2:oracle 3:db2 4:mongo 5:solr...　参照表col_data_type_define，<br>目前只有这些，以后有再添加，因为每种数据库的数据类型都不一样，必须要有一个区分
|gridHeadType  | int       | 表设计时的表头区分  | 1:大多数关系型数据库 2:mongo 3:solr...　参照表col_head_define，<br>目前只有这些，以后有再添加(这个要加代码了)


###### table_list
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id            | long   | 表ID  | 系统内部ID，自增
|dbId           | long   | 数据库ID
|versionId      | long   | 版本ID
|+ auditData    | object | 审计信息 
|tableName      | string | 表名
|aliasName      | string | 别名 
|bizGroup       | string | 业务组  | 选填项,用于区分各业务系统,例如:订单系统/库存系统/商品管理系统/用户管理系统等
|desc           | string | 描述/备注/说明
|viewType       | int    | 类型  | 目前只有关系型数据库用到, 默认为1, (1:表　2:视图)
|viewDefinition | string | 视图定义  | 只有在'viewType'为2时才有值.(此时'column_list'和'index_list'无值)
|currEditorId   | long   | 当前编辑者ID  | 不编辑时值为0
|currEditorName | string | 当前编辑者姓名
|startEditTime  | long   | 开始编辑的时间
|* column_list  | object[]  | 列定义一览
  |\\- columnId 	| long   | 列ID  | 系统内部ID，自增
  |\\+ auditData    | object | 审计信息 
  |\\- columnName 	| string | 列名
  |\\- aliasName    | string | 别名
  |\\- desc		    | string | 备注
  |\\- type 		| string | 类型
  |\\- columnLens 	| string | 列长度 
  |\\- primary 		| string | 是否主键  | 'Y'表示是
  |\\- notnull 		| string | 是否非空  | 'Y'表示是
  |\\- indexDef 	| string | 索引定义
  |\\- unique 		| string | 是否唯一  | 'Y'表示是
  |\\- foreign 		| string | 外键定义
  |\\- increment 	| string | 是否自增长  | 'Y'表示是
  |\\- default 		| string | 缺省值
|* index_list | object[] | 索引定义一览
  |\\- idxId 		| long | 索引ID | 系统内部ID，自增
  |\\+ auditData    | object | 审计信息 
  |\\- idxName 		| string | 索引名称
  |\\- idxCol 		| string | 栏位  | (多个时逗号分隔)
  |\\- idxType 		| int    | 索引类型  | (0:'', 1:'Normal', 2:'Unique', 3:'Full Text')
  |\\- idxMethod 	| int    | 索引方法  | (0:'', 1:'BTREE', 2:'HASH')
  |\\- remarks 		| string | 备注

索引  
① { dbId : 1 }  
② { dbId : 1, _id : 1 }  
③ { dbId : 1, tableName : 1 }

###### table_history
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long   | 数据库ID
|tableId        | long   | 表ID
|versionId      | long   | 版本ID
|searchKey      | string | 检索关键字  | 格式：'表ID'-'版本ID'
|updateDesc     | string | 本次编辑描述  | 保存变更时自动生成概要,主要是修改了哪些字段等等
|+ auditData    | object | 审计信息 
|tableName      | string | 表名
|aliasName      | string | 别名 
|desc           | string | 描述/备注/说明
|* column_list  | object[]  | 列定义一览  | 参照表table_list的column_list
|* index_list   | object[]  | 索引定义一览  | 参照表table_list的index_list


###### table_db_tag_xref
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long     | 数据库ID
|tagName        | string   | 发布版本的标签名
|tagDesc        | string   | 发布版本的说明
|* searchKeys   | string[] | 检索关键字列表  | 格式：'表ID'-'版本ID'
|erChartSearchKey   | string | ER图的检索关键字  | 格式：'ER图ID'-'版本ID';目前的设计是一个项目/数据库只对应一个ER图
|+ auditData    | object   | 审计信息 


###### release_tag_history 表设计和ER图同步创建新标签
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long   | 数据库ID
|tagName        | string | 表名
|desc           | string | 描述/备注/说明
|* tableList   　| object[]  | 表一览  | 这里不包括被删除的表
  |\\- tableId 	     | long  | 表ID 
  |\\- versionId     | long  | 版本ID
|+ erChartInfo     　| object  | ER图数据
  |\\- erChartId     | long  | ER图ID 
  |\\- versionId     | long  | ER图版本ID
|+ auditData    | object | 审计信息 


###### update_history (这个表还只是记录修改历史，目前没有计划用专门画面显示此历史记录) 
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId      | long | 数据库ID
|tableId   | long | 表ID | 没有值时表示修改的数据库本身的属性
|tableName | string | 表名
|columnId  | long | 列ID | 没有值时表示修改的表本身的属性，而不是列
|modifier       | long   | 更新者ID
|modifierName   | string | 更新者姓名  | 多保存更新者姓名,以避免再去查询一次
|modifiedTime   | long   | 更新时间
|type       | int | 类型 | 1:新增　2:修改　3:删除
|contentBef | string | 修改前的内容 | 所修改的数据，json格式保存
|contentAft | string | 修改后的内容 | 所修改的数据，json格式保存


###### user
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id          | long    | 用户ID | 系统内部ID，自增
|account      | string  | 用户登录帐号 | 登录用
|userName     | string  | 用户姓名 | 显示用
|password     | string  | 登录密码 | 使用BCrypt加密, 注意：第三方系统登录过来的用户没有密码
|status       | int     | 用户状态 | 0:用户刚创建(初次登录需要修改密码)　1:正常状态　2:表示该用户已被锁定　4:表示该用户已被删除
|registered   | boolean | 是否是注册用户 | 即是否是在管理界面创建的用户 true:表示是　false:表示第三方登录用户
|from         | string  | 用户来源 | 用于第三方登录的用户　GitLab/jira/mantis...
|+ auditData  | object  | 审计信息 
|inLogin      | bool  | 是否正在使用系统
|loginTime    | long  | 最近一次登录时间
|role         | int   | 用户角色 | 1:只读用户，2:可写用户，8:proj_mng/项目管理员，9:admin(全部/系统管理员)
|* roleList   | object[]  | 可以访问的数据库  | 为空表示没有权限访问任何数据库, 但是系统管理员除外
  |\\- dbId 	 | long   | 数据库ID 
  |\\- role 	 | long   | 用户角色
  |\\- dbName 	 | string | 创建者ID | 见表'db_list'的dbName
|favorite       | long  | 目前常用数据库(收藏夹) | "对应表db_list的_id,设值后不会出现数据库一览选择画面，直接去该数据库内容画面"
|* lastopen     | object   | 上次打开的表
  |\\- %KEY%: dbId      | string | 数据库ID
  |\\- %VAL%: [tableId] | long[] | 表ID | 若退出前已关闭所有打开的表设计画面，则此值会删除

索引  
① { account : 1 }  


###### help 使用或操作说明 -- 目前还未使用
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|no | int | 显示顺序
|title | string | 标题
|desc | string | 内容


###### com_sequence
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|seqName | string | 自增长序列名
|seqValue | long | 自增长序列值

索引  
① { seqName : 1 }


###### com_config
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|propName | string | 属性名
|propValue | string | 属性值
|remarks | string | 备注


###### col_head_define


###### er_chart_list ER图表
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id           | long      | ER图ID  | 系统内部ID，自增
|dbId          | long      | 数据库ID
|versionId     | long      | 版本ID
|+ auditData   | object    | 审计信息 
|title         | string    | ER图文件名
|content       | string    | ER图数据内容


###### er_chart_history
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long   | 数据库ID
|erChartId      | long   | ER图ID
|versionId      | long   | 版本ID
|+ auditData    | object | 审计信息 
|title          | string    | ER图文件名
|content        | string    | ER图数据内容


###### er_table_xref -- 目前还未使用(预备保存项目与ER图的对照关系)



###### file_attachment
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long   | 数据库ID
|tableId        | long   | 表ID          | 值为'0'时表示是全局附件
|versionId      | long   | 版本ID
|fileId         | string    | 文件存储ID
|+ auditData    | object | 审计信息 


###### file_attachment_history
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId           | long   | 数据库ID
|tableId        | long   | 表ID          | 值为'0'时表示是全局附件
|versionId      | long   | 版本ID
|fileId         | string    | 文件存储ID
|type           | int    | 类型 | 1:新增 3:删除 (这里没有修改)
|+ auditData    | object | 审计信息 


