/*
 * smart-doc
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
package com.power.doc.utils;

import com.power.doc.model.CustomField;
import com.power.doc.model.DocJavaField;

import java.util.List;

/**
 * @author yu 2019/12/21.
 */
public class JavaFieldUtil {

    /**
     * @param fields list of fields
     * @return boolean
     */
    public static boolean checkGenerics(List<DocJavaField> fields) {
        for (DocJavaField field : fields) {
            if (field.getJavaField().getType().getFullyQualifiedName().length() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param data0 data0
     * @param typeSimpleName typeName
     * @param customField config field
     */
    public static void buildCustomField(StringBuilder data0, String typeSimpleName, CustomField customField) {
        Object val = customField.getValue();
        if (null != val) {
            if (DocUtil.javaPrimaryType(typeSimpleName)) {
                data0.append(val).append(",");
            } else {
                data0.append(DocUtil.handleJsonStr(String.valueOf(val))).append(",");
            }
        }
    }
}
