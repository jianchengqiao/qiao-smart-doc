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

/**
 * @author yu 2019/12/25.
 */
public class FormDataBuildHelper {

    /**
     * build form data
     *
     * @param className       class name
     * @param registryClasses Class container
     * @param counter         invoked counter
     * @param builder         ProjectDocConfigBuilder
     * @param pre             pre
     * @return list of FormData
     */
    public static List<FormData> getFormData(String className, Map<String, String> registryClasses, int counter, ProjectDocConfigBuilder builder, String pre, List<DocletTag> methodTags) {
        if (StringUtil.isEmpty(className)) {
            throw new RuntimeException("Class name can't be null or empty.");
        }
        ApiConfig apiConfig = builder.getApiConfig();
        List<FormData> formDataList = new ArrayList<>();
        if (counter > apiConfig.getRecursionLimit()) {
            return formDataList;
        }
        // Check circular reference
        if (registryClasses.containsKey(className) && counter > registryClasses.size()) {
            return formDataList;
        }
        // Registry class
        registryClasses.put(className, className);
        counter++;
        boolean skipTransientField = apiConfig.isSkipTransientField();
        boolean requestFieldToUnderline = apiConfig.isRequestFieldToUnderline();
        boolean responseFieldToUnderline = apiConfig.isResponseFieldToUnderline();
        String simpleName = DocClassUtil.getSimpleName(className);
        String[] globGicName = DocClassUtil.getSimpleGicName(className);
        JavaClass cls = builder.getJavaProjectBuilder().getClassByName(simpleName);
        List<DocJavaField> fields = JavaClassUtil.getFields(cls, 0, new LinkedHashMap<>());

        if (JavaClassValidateUtil.isPrimitive(simpleName)) {
            FormData formData = new FormData();
            formData.setKey(pre);
            formData.setType("text");
            formData.setValue(StringUtil.removeQuotes(RandomUtil.randomValueByType(className)));
            formDataList.add(formData);
            return formDataList;
        }
        if (JavaClassValidateUtil.isCollection(simpleName) || JavaClassValidateUtil.isArray(simpleName)) {
            String gicName = globGicName[0];
            if (JavaClassValidateUtil.isArray(gicName)) {
                gicName = gicName.substring(0, gicName.indexOf("["));
            }
            formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + "[]", methodTags));
        }
        int n = 0;
        String methodGroupValue = methodTags == null ? null : methodTags.stream().filter(t -> DocTags.GROUP.equals(t.getName())).findAny().map(DocletTag::getValue).orElse(null);
        out:
        for (DocJavaField docField : fields) {
            JavaField field = docField.getJavaField();
            String fieldName = field.getName();
            String subTypeName = docField.getFullyQualifiedName();
            String fieldGicName = docField.getGenericCanonicalName();
            JavaClass javaClass = builder.getJavaProjectBuilder().getClassByName(subTypeName);
            if (field.isStatic() || "this$0".equals(fieldName) ||
                    JavaClassValidateUtil.isIgnoreFieldTypes(subTypeName)) {
                continue;
            }
            if (field.isTransient() && skipTransientField) {
                continue;
            }
            if (responseFieldToUnderline || requestFieldToUnderline) {
                fieldName = StringUtil.camelToUnderline(fieldName);
            }
            Map<String, String> tagsMap = DocUtil.getFieldTagsValue(field, docField);
            String fieldIgnore = tagsMap.get(DocTags.IGNORE);
            if (tagsMap.containsKey(DocTags.IGNORE)) {
                if (StringUtil.isEmpty(fieldIgnore) || (methodGroupValue != null && !methodGroupValue.trim().isEmpty() && fieldIgnore.contains(methodGroupValue))) {
                    continue out;
                }
            }
            String typeSimpleName = field.getType().getSimpleName();
            if (JavaClassValidateUtil.isMap(subTypeName)) {
                continue;
            }
            String comment = docField.getComment();
            if (StringUtil.isNotEmpty(comment)) {
                comment = DocUtil.replaceNewLineToHtmlBr(comment);
            }
            if (JavaClassValidateUtil.isFile(fieldGicName)) {
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("file");
                formData.setDescription(comment);
                formData.setValue("");
                formDataList.add(formData);
            } else if (JavaClassValidateUtil.isPrimitive(subTypeName)) {
                String fieldValue = "";
                if (tagsMap.containsKey(DocTags.MOCK) && StringUtil.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                    fieldValue = tagsMap.get(DocTags.MOCK);
                } else {
                    fieldValue = DocUtil.getValByTypeAndFieldName(typeSimpleName, field.getName());
                }
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("text");
                formData.setValue(StringUtil.removeQuotes(fieldValue));
                formData.setDescription(comment);
                formDataList.add(formData);
            } else if (javaClass.isEnum()) {
                Object value = JavaClassUtil.getEnumValue(javaClass, Boolean.TRUE);
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("text");
                formData.setValue(StringUtil.removeQuotes(String.valueOf(value)));
                formData.setDescription(comment);
                formDataList.add(formData);
            } else if (JavaClassValidateUtil.isCollection(subTypeName)) {
                String gNameTemp = field.getType().getGenericCanonicalName();
                String[] gNameArr = DocClassUtil.getSimpleGicName(gNameTemp);
                if (gNameArr.length == 0) {
                    continue out;
                }
                String gName = DocClassUtil.getSimpleGicName(gNameTemp)[0];
                if (!JavaClassValidateUtil.isPrimitive(gName)) {
                    if (!simpleName.equals(gName) && !gName.equals(simpleName)) {
                        if (gName.length() == 1) {
                            int len = globGicName.length;
                            if (len > 0) {
                                String gicName = (n < len) ? globGicName[n] : globGicName[len - 1];
                                if (!JavaClassValidateUtil.isPrimitive(gicName) && !simpleName.equals(gicName)) {
                                    formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + fieldName + "[0].", methodTags));
                                }
                            }
                        } else {
                            formDataList.addAll(getFormData(gName, registryClasses, counter, builder, pre + fieldName + "[0].", methodTags));
                        }
                    }
                }
            } else if (subTypeName.length() == 1 || DocGlobalConstants.JAVA_OBJECT_FULLY.equals(subTypeName)) {
                //  For Generics,do nothing, spring mvc not support
//                if (n < globGicName.length) {
//                    String gicName = globGicName[n];
//                    formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + fieldName + "."));
//                }
//                n++;
                continue;
            } else {
                formDataList.addAll(getFormData(fieldGicName, registryClasses, counter, builder, pre + fieldName + ".", methodTags));
            }
        }
        return formDataList;
    }
}
