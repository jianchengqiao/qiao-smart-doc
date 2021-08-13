### Smart-Doc 修改简化版
**原项目 https://github.com/smart-doc-group/smart-doc**
本项目在原项目基础上修改，增加一些功能，个人学习使用，正版使用请支持原项目

#### 引入依赖
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

#### 最简配置
```
{
  "projectName": "智慧案场-客户管理接口文档 v1.0",
  "serverUrl": "https://sc-data.focus-test.cn",
  "outPath": "/Users/Qiao/Documents/IdeaProjects/pages/doc/sc-data/api"
}
```

#### 配置文件
```{
  "projectName": "智慧案场-客户管理接口文档 v1.0", // 文档title
  "serverUrl": "http://sc-data.focus-test.cn", // 文档url，sendRequest也是这个url，注意跨域问题可能导致调不通，不填默认localhost
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
}```

## @tag使用

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 