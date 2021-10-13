package com;

import com.power.common.util.*;
import com.power.doc.builder.*;
import com.power.doc.model.*;

import java.util.*;

/**
 * Description:
 * ApiDoc测试
 *
 * @author yu 2018/06/11.
 */
public class ApiDocTest {
    public static void main(String[] args) {
        testBuilderControllersApi();
    }

    /**
     * 包括设置请求头，缺失注释的字段批量在文档生成期使用定义好的注释
     */
    public static void testBuilderControllersApi() {
        ApiConfig config = new ApiConfig();
        config.setCoverOld(true);
        config.setCreateDebugPage(true);
        config.setAllInOneDocFileName("index.html");
        config.setAllInOne(true);
        config.setServerUrl("http://localhost:8086");
        config.setOutPath("/Users/Qiao/Desktop/doc");
        //不指定SourcePaths默认加载代码为项目src/main/java下的
        config.setPackageFilters("cn.focus.platform.controller.tool");
        config.setSourceCodePaths(
                SourceCodePath.builder().setDesc("智慧案场")
                        .setPath("/Users/Qiao/Documents/IdeaProjects/wx-platform/code/src/main/java")
        );
        //设置请求头，如果没有请求头，可以不用设置
//        config.setRequestHeaders(
//                ApiReqHeader.header().setName("access_token").setType("string").setDesc("Basic auth credentials"),
//                ApiReqHeader.header().setName("user_uuid").setType("string").setDesc("User Uuid key")
//        );
//        config.setStyle("xt256");
//        对于外部jar的类，api-doc目前无法自动获取注释，
//        如果有这种场景，则自己添加字段和注释，api-doc后期遇到同名字段则直接给相应字段加注释
//        config.setCustomResponseFields(
////                CustomRespField.field().setName("success").setDesc("成功返回true,失败返回false"),
////                CustomRespField.field().setName("message").setDesc("接口响应信息"),
////                CustomRespField.field().setName("data").setDesc("接口响应数据"),
//                CustomField.builder().setName("msg").setDesc("消息测试").setIgnore(true).setValue("000200"),
//                CustomField.builder().setName("code2").setDesc("code测试").setIgnore(false).setValue("010000")
//                .setDesc("响应代码")
//        );
//        config.setCustomRequestFields(
//                CustomField.builder()
//                        .setName("age").setDesc("年龄").setIgnore(false).setValue("13").setRequire(false).setOwnerClassName("com.power.doc.entity.SimpleUser"),
//                CustomField.builder()
//                        .setName("sex").setDesc("性别").setIgnore(false).setValue("男").setRequire(true).setOwnerClassName("com.power.doc.entity.SimpleUser")
//
//
//        );
        //非必须只有当setAllInOne设置为true时文档变更记录才生效，https://gitee.com/sunyurepository/ApplicationPower/issues/IPS4O
//        config.setRevisionLogs(
//                RevisionLog.builder().setRevisionTime("2018/12/15").setAuthor("chen").setRemarks("测试").setStatus("创建").setVersion("V1.0"),
//                RevisionLog.builder().setRevisionTime("2018/12/16").setAuthor("chen2").setRemarks("测试2").setStatus("修改").setVersion("V2.0")
//        );
//        config.setDescriptions(Arrays.asList("1. 本页接口都需要token","2. 本页接口都需要token"));
//        config.setResponseBodyAdvice(ResponseBodyAdvice.builder()
//                .setDataField("data")
//                .setDataField("dadada")
//                .setClassName("com.power.common.model.CommonResult"));


        long start = System.currentTimeMillis();

        //TornaBuilder.buildApiDoc(config);
        //OpenApiBuilder.buildOpenApi(config);
        HtmlApiDocBuilder.buildApiDoc(config);
        long end = System.currentTimeMillis();
        DateTimeUtil.printRunTime(end, start);
    }

}
