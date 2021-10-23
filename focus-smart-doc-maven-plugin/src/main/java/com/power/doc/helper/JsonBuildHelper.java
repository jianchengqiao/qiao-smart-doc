/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2021 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.power.doc.helper;

import com.power.common.util.*;
import com.power.doc.builder.*;
import com.power.doc.constants.*;
import com.power.doc.model.*;
import com.power.doc.utils.*;
import com.thoughtworks.qdox.model.*;

import java.util.*;
import java.util.stream.*;

import static com.power.doc.constants.DocTags.*;


/**
 * @author yu 2019/12/21.
 */
public class JsonBuildHelper {

    /**
     * build return json
     *
     * @param docJavaMethod The JavaMethod object
     * @param builder       ProjectDocConfigBuilder builder
     * @return String
     */
    public static String buildReturnJson(DocJavaMethod docJavaMethod, ProjectDocConfigBuilder builder) {
        JavaMethod method = docJavaMethod.getJavaMethod();
        List<DocletTag> methodTags = method.getTags();
        if (method.getReturns().isVoid() && Objects.isNull(builder.getApiConfig().getResponseBodyAdvice())) {
            return "Doesn't return a value.";
        }
        String returnTypeGenericCanonicalName = method.getReturnType().getGenericCanonicalName();
        if (Objects.nonNull(builder.getApiConfig().getResponseBodyAdvice())
                && Objects.isNull(method.getTagByName(IGNORE_RESPONSE_BODY_ADVICE))) {
            String responseBodyAdvice = builder.getApiConfig().getResponseBodyAdvice().getClassName();
            StringBuilder sb = new StringBuilder();
            sb.append(responseBodyAdvice)
                    .append("<")
                    .append(returnTypeGenericCanonicalName).append(">");
            returnTypeGenericCanonicalName = sb.toString();
        }
        ApiReturn apiReturn = DocClassUtil.processReturnType(returnTypeGenericCanonicalName);
        String typeName = apiReturn.getSimpleName();
        Map<String, JavaType> actualTypesMap = docJavaMethod.getActualTypesMap();
        String returnType = apiReturn.getGenericCanonicalName();
        if (Objects.nonNull(actualTypesMap)) {
            for (Map.Entry<String, JavaType> entry : actualTypesMap.entrySet()) {
                typeName = typeName.replace(entry.getKey(), entry.getValue().getCanonicalName());
                returnType = returnType.replace(entry.getKey(), entry.getValue().getCanonicalName());
            }
        }
        if (JavaClassValidateUtil.isPrimitive(typeName)) {
            if (DocGlobalConstants.JAVA_STRING_FULLY.equals(typeName)) {
                return "string";
            }
            return StringUtil.removeQuotes(DocUtil.jsonValueByType(typeName));
        }
        return JsonFormatUtil.formatJson(buildJson(typeName, returnType, Boolean.TRUE, 0, new HashMap<>(), builder, methodTags));
    }

    /**
     * @param typeName             type name
     * @param genericCanonicalName genericCanonicalName
     * @param isResp               Response flag
     * @param counter              Recursive counter
     * @param registryClasses      class container
     * @param builder              project config builder
     * @param methodTags
     * @return String
     */
    public static String buildJson(String typeName, String genericCanonicalName,
                                   boolean isResp, int counter, Map<String, String> registryClasses, ProjectDocConfigBuilder builder, List<DocletTag> methodTags) {

        //存储泛型所对应的实体类
        Map<String, String> genericMap = new HashMap<>(10);
        JavaClass javaClass = builder.getJavaProjectBuilder().getClassByName(typeName);
        ApiConfig apiConfig = builder.getApiConfig();
        boolean randomMock = apiConfig.isRandomMock();
        if (counter > apiConfig.getRecursionLimit()) {
            return "{\"$ref\":\"...\"}";
        }
        if (registryClasses.containsKey(typeName) && counter > registryClasses.size()) {
            return "{\"$ref\":\"...\"}";
        }
        int nextLevel = counter + 1;
        registryClasses.put(typeName, typeName);
        if (JavaClassValidateUtil.isMvcIgnoreParams(typeName, builder.getApiConfig().getIgnoreRequestParams())) {
            if (DocGlobalConstants.MODE_AND_VIEW_FULLY.equals(typeName)) {
                return "Forward or redirect to a page view.";
            } else {
                return "Error restful return.";
            }
        }
        if (JavaClassValidateUtil.isPrimitive(typeName)) {
            return StringUtil.removeQuotes(DocUtil.jsonValueByType(typeName));
        }
//        if (javaClass.isEnum()) {
//            return String.valueOf(JavaClassUtil.getEnumValue(javaClass, Boolean.FALSE));
//        }
        boolean skipTransientField = apiConfig.isSkipTransientField();
        StringBuilder data0 = new StringBuilder();
        JavaClass cls = builder.getClassByName(typeName);


        data0.append("{");
        String[] globGicName = DocClassUtil.getSimpleGicName(genericCanonicalName);
        //添加泛型对应关系
        JavaClassUtil.genericParamMap(genericMap, cls, globGicName);
        StringBuilder data = new StringBuilder();
        if (JavaClassValidateUtil.isCollection(typeName) || JavaClassValidateUtil.isArray(typeName)) {
            data.append("[");
            if (globGicName.length == 0) {
                data.append("{\"object\":\"any object\"}");
                data.append("]");
                return data.toString();
            }
            String gNameTemp = globGicName[0];
            String gName = JavaClassValidateUtil.isArray(gNameTemp) ? gNameTemp.substring(0, gNameTemp.indexOf("[")) : globGicName[0];
            if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(gName)) {
                data.append("{\"waring\":\"You may use java.util.Object instead of display generics in the List\"}");
            } else if (JavaClassValidateUtil.isPrimitive(gName)) {
                data.append(DocUtil.jsonValueByType(gName)).append(",");
                data.append(DocUtil.jsonValueByType(gName));
            } else if (gName.contains("<")) {
                String simple = DocClassUtil.getSimpleName(gName);
                String json = buildJson(simple, gName, isResp, nextLevel, registryClasses, builder, methodTags);
                data.append(json);
            } else if (JavaClassValidateUtil.isCollection(gName)) {
                data.append("\"any object\"");
            } else {
                String json = buildJson(gName, gName, isResp, nextLevel, registryClasses, builder, methodTags);
                data.append(json);
            }
            data.append("]");
            return data.toString();
        } else if (JavaClassValidateUtil.isMap(typeName)) {
            String gNameTemp = genericCanonicalName;
            String[] getKeyValType = DocClassUtil.getMapKeyValueType(gNameTemp);
            if (getKeyValType.length == 0) {
                data.append("{\"mapKey\":{}}");
                return data.toString();
            }
            if ((!DocGlobalConstants.JAVA_STRING_FULLY.equals(getKeyValType[0])) && apiConfig.isStrict()) {
                throw new RuntimeException("Map's key can only use String for json,but you use " + getKeyValType[0]);
            }
            String gicName = gNameTemp.substring(gNameTemp.indexOf(",") + 1, gNameTemp.lastIndexOf(">"));
            if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(gicName)) {
                data.append("{").append("\"mapKey\":").append("{\"waring\":\"You may use java.util.Object for Map value; smart-doc can't be handle.\"}").append("}");
            } else if (JavaClassValidateUtil.isPrimitive(gicName)) {
                data.append("{").append("\"mapKey1\":").append(DocUtil.jsonValueByType(gicName)).append(",");
                data.append("\"mapKey2\":").append(DocUtil.jsonValueByType(gicName)).append("}");
            } else if (gicName.contains("<")) {
                String simple = DocClassUtil.getSimpleName(gicName);
                String json = buildJson(simple, gicName, isResp, nextLevel, registryClasses, builder, methodTags);
                data.append("{").append("\"mapKey\":").append(json).append("}");
            } else {
                data.append("{").append("\"mapKey\":").append(buildJson(gicName, gNameTemp, isResp, counter + 1, registryClasses, builder, methodTags)).append("}");
            }
            return data.toString();
        } else if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(typeName)) {
            data.append("{\"object\":\" any object\"},");
            // throw new RuntimeException("Please do not return java.lang.Object directly in api interface.");
        } else if(JavaClassValidateUtil.isReactor(typeName)) {
            data.append(buildJson(globGicName[0], typeName, isResp, nextLevel, registryClasses, builder, methodTags));
            return data.toString();
        } else {
            boolean requestFieldToUnderline = builder.getApiConfig().isRequestFieldToUnderline();
            boolean responseFieldToUnderline = builder.getApiConfig().isResponseFieldToUnderline();
            List<DocJavaField> fields = JavaClassUtil.getFields(cls, 0, new LinkedHashMap<>());
            String methodGroupValue = methodTags == null ? null : methodTags.stream().filter(t -> DocTags.GROUP.equals(t.getName())).findAny().map(DocletTag::getValue).orElse(null);
            String methodIgnoreGroupValue = methodTags == null ? null : methodTags.stream().filter(t -> DocTags.IGNORE.equals(t.getName())).findAny().map(DocletTag::getValue).orElse(null);
            out:
            for (DocJavaField docField : fields) {
                JavaField field = docField.getJavaField();
                String subTypeName = docField.getFullyQualifiedName();
                String fieldName = field.getName();
                if (field.isStatic() || "this$0".equals(fieldName) ||
                        JavaClassValidateUtil.isIgnoreFieldTypes(subTypeName)) {
                    continue;
                }
                if (field.isTransient() && skipTransientField) {
                    continue;
                }
                String comment = docField.getComment();
                if (EmptyUtil.isEmpty(comment)) {
                    continue;
                }
                if ((responseFieldToUnderline && isResp) || (requestFieldToUnderline && !isResp)) {
                    fieldName = StringUtil.camelToUnderline(fieldName);
                }
                Map<String, String> tagsMap = DocUtil.getFieldTagsValue(field, docField);
                String fieldGroup = tagsMap.get(DocTags.GROUP);
                String fieldIgnore = tagsMap.get(DocTags.IGNORE);
                if (EmptyUtil.notEmpty(methodGroupValue)) {
                    if (EmptyUtil.isEmpty(fieldGroup) || !fieldGroup.contains("alwaysShow") && !fieldGroup.contains(methodGroupValue))
                        continue;
                } else if (tagsMap.containsKey(DocTags.IGNORE)) {
                    if (StringUtil.isEmpty(fieldIgnore) || (methodIgnoreGroupValue != null && !methodIgnoreGroupValue.trim().isEmpty() && fieldIgnore.contains(methodIgnoreGroupValue))) {
                        continue;
                    }
                }
                List<JavaAnnotation> annotations = docField.getAnnotations();
                for (JavaAnnotation annotation : annotations) {
                    String annotationName = annotation.getType().getValue();
                    if (DocAnnotationConstants.SHORT_JSON_IGNORE.equals(annotationName)) {
                        continue out;
                    } else if (DocAnnotationConstants.SHORT_JSON_FIELD.equals(annotationName)) {
                        if (null != annotation.getProperty(DocAnnotationConstants.SERIALIZE_PROP)) {
                            if (Boolean.FALSE.toString().equals(annotation.getProperty(DocAnnotationConstants.SERIALIZE_PROP).toString())) {
                                continue out;
                            }
                        } else if (null != annotation.getProperty(DocAnnotationConstants.NAME_PROP)) {
                            fieldName = StringUtil.removeQuotes(annotation.getProperty(DocAnnotationConstants.NAME_PROP).toString());
                        }
                    } else if (DocAnnotationConstants.SHORT_JSON_PROPERTY.equals(annotationName)) {
                        if (null != annotation.getProperty(DocAnnotationConstants.VALUE_PROP)) {
                            fieldName = StringUtil.removeQuotes(annotation.getProperty(DocAnnotationConstants.VALUE_PROP).toString());
                        }
                    }
                }
                String typeSimpleName = field.getType().getSimpleName();
                String fieldGicName = docField.getGenericCanonicalName();
                CustomField customResponseField = builder.getCustomRespFieldMap().get(fieldName);
                CustomField customRequestField = builder.getCustomReqFieldMap().get(fieldName);
                if(customRequestField !=null && typeName.equals(customRequestField.getOwnerClassName()) && (customRequestField.isIgnore()) && !isResp){
                    continue;
                }
                if(customResponseField !=null && typeName.equals(customResponseField.getOwnerClassName()) && (customResponseField.isIgnore()) && isResp){
                    continue;
                }
                data0.append("\"").append(fieldName).append("\":");
                String fieldValue = "";
                if (tagsMap.containsKey(DocTags.MOCK) && StringUtil.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                    fieldValue = tagsMap.get(DocTags.MOCK);
                    if (!DocUtil.javaPrimaryType(typeSimpleName)
                            && !JavaClassValidateUtil.isCollection(subTypeName)
                            && !JavaClassValidateUtil.isMap(subTypeName)
                            && !JavaClassValidateUtil.isArray(subTypeName)) {
                        fieldValue = DocUtil.handleJsonStr(fieldValue);
                    }
                }else {
                    if (comment !=null&& comment.contains("|")){
                        String[] split = comment.split("\\|");
                        if (split.length>1){
                            fieldValue = split[1];
                            if (!DocUtil.javaPrimaryType(typeSimpleName)
                                    && !JavaClassValidateUtil.isCollection(subTypeName)
                                    && !JavaClassValidateUtil.isMap(subTypeName)
                                    && !JavaClassValidateUtil.isArray(subTypeName)) {
                                fieldValue = DocUtil.handleJsonStr(fieldValue);
                            }
                        }
                    }
                }
                if (JavaClassValidateUtil.isPrimitive(subTypeName)) {
                    int data0Length  = data0.length();
                    if (StringUtil.isEmpty(fieldValue)) {
                        if (randomMock) {
                            fieldValue = DocUtil.getValByTypeAndFieldName(typeSimpleName, field.getName());
                        }
                    }
                    if (null != customRequestField && !isResp && customRequestField.getOwnerClassName().equals(typeName)) {
                        JavaFieldUtil.buildCustomField(data0, typeSimpleName, customRequestField);
                    }
                     if (null != customResponseField && isResp && customResponseField.getOwnerClassName().equals(typeName)) {
                        JavaFieldUtil.buildCustomField(data0, typeSimpleName, customResponseField);
                    }
                     if (fieldValue.trim().isEmpty()){
                         fieldValue = "null";
                     }
                     if(data0.length()==data0Length) {
                         if ("\"null\"".equals(fieldValue)){
                             data0.append("null").append(",");
                         }else {
                             data0.append(fieldValue).append(",");
                         }
                     }
                } else {
                    if (JavaClassValidateUtil.isCollection(subTypeName) || JavaClassValidateUtil.isArray(subTypeName)) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            data0.append(fieldValue).append(",");
                            continue out;
                        }
                        if (globGicName.length > 0 && "java.util.List".equals(fieldGicName)) {
                            fieldGicName = fieldGicName + "<T>";
                        }
                        fieldGicName = JavaClassValidateUtil.isArray(subTypeName) ? fieldGicName.substring(0, fieldGicName.indexOf("[")) : fieldGicName;

                        if (DocClassUtil.getSimpleGicName(fieldGicName).length == 0) {
                            data0.append("[{\"object\":\"any object\"}],");
                            continue out;
                        }
                        String gicName = DocClassUtil.getSimpleGicName(fieldGicName)[0];

                        if (DocGlobalConstants.JAVA_STRING_FULLY.equals(gicName)) {
                            data0.append("[").append(DocUtil.jsonValueByType(gicName)).append("]").append(",");
                        } else if (DocGlobalConstants.JAVA_LIST_FULLY.equals(gicName)) {
                            data0.append("[{\"object\":\"any object\"}],");
                        } else if (gicName.length() == 1) {
                            if (globGicName.length == 0) {
                                data0.append("[{\"object\":\"any object\"}],");
                                continue out;
                            }
                            String gicName1 = genericMap.get(gicName) == null ? globGicName[0] : genericMap.get(gicName);
                            if (DocGlobalConstants.JAVA_STRING_FULLY.equals(gicName1)) {
                                data0.append("[").append(DocUtil.jsonValueByType(gicName1)).append("]").append(",");
                            } else {
                                if (!typeName.equals(gicName1)) {
                                    data0.append("[").append(buildJson(DocClassUtil.getSimpleName(gicName1), gicName1, isResp, nextLevel, registryClasses, builder, methodTags)).append("]").append(",");
                                } else {
                                    data0.append("[{\"$ref\":\"..\"}]").append(",");
                                }
                            }
                        } else {
                            if (!typeName.equals(gicName)) {
                                if (JavaClassValidateUtil.isMap(gicName)) {
                                    data0.append("[{\"mapKey\":{}}],");
                                    continue out;
                                }
                                JavaClass arraySubClass = builder.getJavaProjectBuilder().getClassByName(gicName);
//                                if (arraySubClass.isEnum()) {
//                                    Object value = JavaClassUtil.getEnumValue(arraySubClass, Boolean.FALSE);
//                                    data0.append("[").append(value).append("],");
//                                    continue out;
//                                }
                                data0.append("[").append(buildJson(gicName, fieldGicName, isResp, nextLevel, registryClasses, builder, methodTags)).append("]").append(",");
                            } else {
                                data0.append("[{\"$ref\":\"..\"}]").append(",");
                            }
                        }
                    } else if (JavaClassValidateUtil.isMap(subTypeName)) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            data0.append(fieldValue).append(",");
                            continue out;
                        }
                        if (JavaClassValidateUtil.isMap(fieldGicName)) {
                            data0.append("{").append("\"mapKey\":{}},");
                            continue out;
                        }
                        String gicName = fieldGicName.substring(fieldGicName.indexOf(",") + 1, fieldGicName.indexOf(">"));
                        if (gicName.length() == 1) {
                            String gicName1 = genericMap.get(gicName) == null ? globGicName[0] : genericMap.get(gicName);
                            if (DocGlobalConstants.JAVA_STRING_FULLY.equals(gicName1)) {
                                data0.append("{").append("\"mapKey\":").append(DocUtil.jsonValueByType(gicName1)).append("},");
                            } else {
                                if (!typeName.equals(gicName1)) {
                                    data0.append("{").append("\"mapKey\":").append(buildJson(DocClassUtil.getSimpleName(gicName1), gicName1, isResp, nextLevel, registryClasses, builder, methodTags)).append("},");
                                } else {
                                    data0.append("{\"mapKey\":{}},");
                                }
                            }
                        } else {
                            data0.append("{").append("\"mapKey\":").append(buildJson(gicName, fieldGicName, isResp, nextLevel, registryClasses, builder, methodTags)).append("},");
                        }
                    } else if (subTypeName.length() == 1) {
                        if (!typeName.equals(genericCanonicalName)) {
                            String gicName = genericMap.get(subTypeName) == null ? globGicName[0] : genericMap.get(subTypeName);
                            if (JavaClassValidateUtil.isPrimitive(gicName)) {
                                data0.append(DocUtil.jsonValueByType(gicName)).append(",");
                            } else {
                                String simple = DocClassUtil.getSimpleName(gicName);
                                data0.append(buildJson(simple, gicName, isResp, nextLevel, registryClasses, builder, methodTags)).append(",");
                            }
                        } else {
                            data0.append("{\"waring\":\"You may have used non-display generics.\"},");
                        }
                    } else if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(subTypeName)) {
                        if (StringUtil.isNotEmpty(field.getComment())) {
                            // from source code
                            data0.append("{\"object\":\"any object\"},");
                        } else if (globGicName.length > 0) {
                            String gicName = genericMap.get(subTypeName) == null ? globGicName[0] : genericMap.get(subTypeName);
                            if (!typeName.equals(genericCanonicalName)) {
                                if (JavaClassValidateUtil.isPrimitive(gicName)) {
                                    data0.append("\"").append(buildJson(gicName, genericCanonicalName, isResp, nextLevel, registryClasses, builder, methodTags)).append("\",");
                                } else {
                                    String simpleName = DocClassUtil.getSimpleName(gicName);
                                    data0.append(buildJson(simpleName, gicName, isResp, nextLevel, registryClasses, builder, methodTags)).append(",");
                                }
                            } else {
                                data0.append("{\"waring\":\"You may have used non-display generics.\"},");
                            }
                        } else {
                            data0.append("{\"waring\":\"You may have used non-display generics.\"},");
                        }
                    } else if (typeName.equals(subTypeName)) {
                        data0.append("{\"$ref\":\"...\"}").append(",");
                    } else {
                        javaClass = builder.getJavaProjectBuilder().getClassByName(subTypeName);
                        if (!isResp && javaClass.isEnum()) {
                            Object value = JavaClassUtil.getEnumValue(javaClass, Boolean.FALSE);
                            data0.append(value).append(",");
                        } else {
                            data0.append(buildJson(subTypeName, fieldGicName, isResp, nextLevel, registryClasses, builder, methodTags)).append(",");
                        }
                    }
                }
            }
        }
        if (data0.toString().contains(",")) {
            data0.deleteCharAt(data0.lastIndexOf(","));
        }
        data0.append("}");
        String toString = data0.toString();
        if (toString.equals("{}")){
            return "any object";
        }
        return toString;
    }




}
