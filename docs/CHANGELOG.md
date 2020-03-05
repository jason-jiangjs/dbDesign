
2020-02-28 添加ER图设计功能，目前ER图和表设计还未实时关联，只能分开编辑，在表设计中所作的修改不会直接反映到ER图，以后再添加此功能
修改draw.io源代码如下：
\js\diagramly\sidebar\Sidebar.js
\js\diagramly\App.js
\js\diagramly\EditorUi.js
\js\diagramly\Init.js
\js\diagramly\LocalFile.js
\js\diagramly\Menus.js
\js\diagramly\Settings.js
\js\mxgraph\Dialogs.js
\js\mxgraph\EditorUi.js
\js\mxgraph\Init.js
\js\mxgraph\Sidebar.js
对java代码中的Servlet类添加注解 @WebServlet (有些其实没用到)
包括: AppShortcutServlet ExportProxyServlet ImgurRedirectServlet LogServlet OpenServlet ProxyServlet SaveServlet

其他：
从mxgraph复制文件'mxClient.js'和'mxClient.min.js' 到目录 '\drawio\src\main\resources\static\mxgraph\'下
下载 https://js.pusher.com/4.3/js/pusher.min.js 到目录 '\drawio\src\main\resources\static\js\'
原draw.io下的index.html改名为index2.html
新增open2.html

2018-08-14
以下列定义暂不显示
        {
            "field" : "unique",
            "align" : "center",
            "width" : 35.0,
            "title" : "唯一",
            "resizable" : false
        }, 
        {
            "field" : "foreign",
            "align" : "center",
            "width" : 35.0,
            "title" : "外键",
            "resizable" : false,
            "editor" : {
                "type" : "checkbox",
                "options" : {
                    "on" : "Y",
                    "off" : ""
                }
            }
        }, 