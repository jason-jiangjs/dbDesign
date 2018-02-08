###### db_list
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id           | long      | 数据库ID | 系统内部ID，自增
|deleteFlg     | bool      | 数据是否有效 | 缺省为"false", "true"表示该条数据已被删除
|creator       | long      | 创建者ID 
|creatorName   | string    | 创建者姓名
|createdTime   | timestamp | 创建时间
|modifier      | long      | 更新者ID
|modifierName  | string 　 | 更新者姓名
|modifiedTime  | timestamp | 更新时间
|dbName        | string    | 数据库名称
|dbNameCN      | string    | 数据库名称（中文）
|desc          | string    | 描述
|type          | int       | 类型 | 1:sql(mysql/oracle/db2...) 2:mongo 3:solr

索引  
① { dbId : 1 }

###### table_list
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id  | long | 表ID | 系统内部ID，自增
|dbId | long | 数据库ID
|deleteFlg | bool | 数据是否有效 | 缺省为"false", "true"表示该条数据已被删除
|creator | long | 创建者ID
|creatorName   | string    | 创建者姓名
|createdTime | timestamp | 创建时间
|modifier | long | 更新者ID
|modifierName  | string 　 | 更新者姓名
|modifiedTime | timestamp | 更新时间
|tableName | string | 表名
|tableNameCN | string | 表名（中文） 
|type | int | 类型 | 1:表　2:视图
|desc | string | 描述
|+ column_list | map[] | 列定义一览
|\\- columnId | long | 列ID | 系统内部ID，自增
|\\- creator | long | 创建者ID
|\\- creatorName   | string    | 创建者姓名
|\\- createdTime | timestamp | 创建时间
|\\- modifier | long | 更新者ID
|\\- modifierName  | string 　 | 更新者姓名
|\\- modifiedTime | timestamp | 更新时间
|\\- columnName | string | 列名
|\\- columnNameCN | string | 列名（中文） | 对该列的说明
|\\- desc | string | 备注
|\\- type | string | 类型
|\\- columnLens | string | 列长度
|\\- primary | bool | 是否主键
|\\- notnull | bool | 是否非空
|\\- indexDef | string | 索引定义
|\\- unique | bool | 是否唯一
|\\- foreign | bool | 是否外键
|\\- increment | bool | 是否自增长
|\\- default | string | 缺省值

索引  
① { dbId : 1 }  
② { dbId : 1, tableId : 1 }  
③ { dbId : 1, tableName : 1 }

###### update_history (这个表还只是记录修改历史，目前没有计划用专门画面显示此历史记录) 
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|dbId | long | 数据库ID
|tableId | long | 表ID | 没有值时表示修改的数据库本身的属性
|tableName | string | 表名
|columnId | long | 列ID | 没有值时表示修改的表本身的属性，而不是列
|userId | long | 修改者ID
|userName | string | 修改者姓名
|modifiedTime | timestamp | 修改时间
|type | int | 类型 | 1:新增　2:修改　3:删除
|contentBef | string | 修改前的内容 | 所修改的数据，json格式保存
|contentAft | string | 修改后的内容 | 所修改的数据，json格式保存


###### user
| 列名 | 类型 | 说明 | 备注
|-----|------|------|------
|_id       | long | 用户ID | 系统内部ID，自增
|deleteFlg | bool | 数据是否有效 | 缺省为"false", "true"表示该用户已被删除
|creator   | long | 创建者ID
|createdTime | timestamp | 创建时间
|modifier | long | 更新者ID
|modifiedTime | timestamp | 更新时间
|userId | string | 用户登录帐号 | 登录用
|userName | string | 用户姓名 | 显示用
|password | string | 登录密码 | 使用BCrypt加密
|permission | int | 用户权限 | 1:只读，2:修改列(增删改)，3:修改表/库(增删改)，4:导出SQL文，9:admin(全部)
|dbs | array | 可以访问的数据库
|favorite | long | 目前常用数据库 | "对应表db_list的_id,设值后不会出现数据库一览选择画面，直接去该数据库内容画面"
|lastopen | map | 上次打开的表设计
|. %KEY%: dbId | string | 数据库ID
|. %VAL%: [tableId] | long[] | 表ID | 若退出前已关闭所有打开的表设计画面，则此值会删除

索引  
① { userId : 1 }  
② { userName : 1 }


###### help 使用或操作说明
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
