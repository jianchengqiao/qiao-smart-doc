### Smart-Doc 修改简化版
**原项目 https://github.com/smart-doc-group/smart-doc**  
本项目在原项目基础上修改，增删一些功能，个人学习使用，正版使用请支持原版

### 说明
* 根据 Controller、Method、传入参数、返回值的 ***JavaDoc注释***
  自动生成文档，一定要写好注释
* 是否必填 ***Required*** 会根据 ***@RequestParam*** ***@NotNull***
  ***@NotEmpty*** 等注解来生成，且会根据 ***@Validated***
  分组来判断，要合理使用 ***Valid*** 系列注解

### 引入依赖
```
<plugins>
    <plugin>
        <groupId>cn.focus</groupId>
         <artifactId>focus-smart-doc-maven-plugin</artifactId>
         <version>4.0</version>
         <configuration>
             <configFile>./src/main/resources/config/smart-doc.json</configFile>
             <includes>
                 <include>com.alibaba:fastjson</include>
             </includes>
         </configuration>
         <executions>
             <execution>
                 <goals>
                     <goal>html</goal>
                 </goals>
             </execution>
         </executions>
     </plugin>
 </plugins>
```

### 最简配置
```
{
  "projectName": "xxx接口文档 v1.0",
  "serverUrl": "https://xxx.test.cn",
  "outPath": "/Users/Qiao/Documents/IdeaProjects/pages/doc/sc-data/api"
}
```

### 配置文件
```
{
  "projectName": "xxx接口文档 v1.0", // 文档title
  "serverUrl": "http://xxx.test.cn", // 文档url，SendRequest也是这个url，注意跨域问题可能导致调不通，不填默认localhost
  "version": "1.0", // 展示版本号
  "outPath": "/Users/Qiao/Documents/IdeaProjects/pages/doc/sc-data/api", // 文档输出地址，可输出到一个git目录下
  "packageFilters": "cn.focus.sc.data.controller.doc", // 包过滤，不填默认全部包
  "requestExample": true, // 是否展示请求示例，默认是
  "responseExample": true, // 是否展示响应示例，默认是
  "randomMock": true, // 是否随机mock示例数值，默认是，否则全输出为null
  "sourceCodePaths":{
    "path":"/Users/Qiao/Documents/IdeaProjects/sc-data/code/src/main/java" // 源码地址，不填默认是当前项目 src/main/java
  },
  "revisionLogs": [ // 版本更新说明
       {
         "version": "1.0",
         "revisionTime": "2021-07-15 16:00",
         "author": "QiaoJianCheng",
         "status": "create",
         "remarks": "1.0 初版"
       }
  ],
  "descriptions": [ // 调用说明
    "此处是调用说明",
    "1.所有请求必须携带token所有请求必须携带token",
    "2.所有请求必须携带token所有请求必须携带token"
  ],
  "dataDictionaries": [ // 枚举列表
    {
      "title": "获取来源",
      "enumClassName": "cn.focus.sc.data.enums.SourceEnum",
      "codeField": "code",
      "descField": "name"
    }
  ],
  "errorCodeDictionaries": [ // 错误码列表
    {
      "title": "错误码",
      "enumClassName": "cn.focus.sc.data.enums.CodeEnum",
      "codeField": "code",
      "descField": "msg"
    }
  ]
}
```

## JavaDoc @tag的使用
所有 ***Controller*** ***Method*** ***Filed*** 都必须使用
***JavaDoc注释***，Doc注释内容即为小标题，换行后使用 **@tag *param***，实现一些功能

|         JavaDoc tag          |                      可用位置                     |               说明                |
|       ----          |                    ----                       |                 -----            |
| @order *100*         | Controller                                       |  文档排序          |
| @author *Qiao*      | Controller <br> Method                            |  作者             |
| @ignore <br> @ignore *editEstate* | Controller <br> Method <br> Filed | 忽略 ***Controller*** 、***Method*** 、***Filed*** ，不生成其文档。 <br> 作用在 ***Filed*** 时，若有分组，则只有与 ***Method*** 分组相同的字段才忽略。 |
| @group *editEstate*       | Method                                            | 方法分组，配合 ***@ignore*** 使用，可忽略某个分组下的字段 |
| @ignoreParams *groupId,estateId*         | Method                                       |  忽略方法的一些参数，不生成文档          |
| @apiNode *方法详细说明*         |   Method                                       |  方法详细说明          |
| @param **estateId***楼盘id\|10086*         |   Method                                       |  参数说明，其中 ***\|*** 后面为mock请求响应示例的值，不写则根据配置随机mock或null          |
| @apiNode *方法详细说明*         |   Method                                       |  方法详细说明          |
| @page */tool/trial.html*         |   Method                                       |  标记该方法为一个页面，点击 ***Send Request*** 会打开这个页面          |
| @download          |   Method                                       |  标记该方法为下载请求，点击 ***Send Request*** 会下载文件          |
| @mock *10086*         |   Filed                                       |  mock请求或响应示例，或者字段的 ***注释\|10086*** 也能实现mock值          |
| @required         |   Filed                                       |  强制为必填参数，建议使用 ***NotNull*** 系列注解来控制是否必填          |

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 