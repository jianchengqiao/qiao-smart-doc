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
package com.power.doc.model;

import com.thoughtworks.qdox.model.*;

import java.util.*;

/**
 * @author yu 2020/3/19.
 */
public class DocJavaField implements Comparable<DocJavaField> {

    /**
     * field info
     */
    private JavaField javaField;

    /**
     * comment
     */
    private String Comment;

    /**
     * tags
     */
    private List<DocletTag> docletTags;

    /**
     * annotations
     */
    private List<JavaAnnotation> annotations;

    /**
     * field fullyQualifiedName
     */
    private String fullyQualifiedName;

    /**
     * field genericCanonicalName
     */
    private String genericCanonicalName;

    /**
     * field generic actualJavaType;
     */
    private String actualJavaType;

    private boolean array;

    private boolean primitive;

    private boolean collection;

    private boolean file;

    private boolean isEnum;

    private int order;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public static DocJavaField builder(int order) {
        DocJavaField docJavaField = new DocJavaField();
        docJavaField.setOrder(order);
        return docJavaField;
    }

    public JavaField getJavaField() {
        return javaField;
    }

    public DocJavaField setJavaField(JavaField javaField) {
        this.javaField = javaField;
        return this;
    }

    public String getComment() {
        return Comment;
    }

    public DocJavaField setComment(String comment) {
        Comment = comment;
        return this;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public DocJavaField setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
        return this;
    }

    public String getGenericCanonicalName() {
        return genericCanonicalName;
    }

    public DocJavaField setGenericCanonicalName(String genericCanonicalName) {
        this.genericCanonicalName = genericCanonicalName;
        return this;
    }

    public String getActualJavaType() {
        return actualJavaType;
    }

    public DocJavaField setActualJavaType(String actualJavaType) {
        this.actualJavaType = actualJavaType;
        return this;
    }

    public List<DocletTag> getDocletTags() {
        if (docletTags == null) {
            return new ArrayList<>();
        }
        return docletTags;
    }

    public DocJavaField setDocletTags(List<DocletTag> docletTags) {
        if (docletTags == null) {
            this.docletTags = new ArrayList<>();
        } else {
            this.docletTags = docletTags;
        }
        return this;
    }

    public DocJavaField addDocletTags(List<DocletTag> docletTags) {
        if (this.docletTags == null) {
            this.docletTags = docletTags;
        } else {
            out:
            for (DocletTag newTag : docletTags) {
                for (DocletTag thisTag : this.docletTags) {
                    if (thisTag.getName().equals(newTag.getName())&&thisTag.getValue().equals(newTag.getValue())){
                        continue out;
                    }
                }
                this.docletTags.add(newTag);
            }
        }
        this.docletTags = docletTags;
        return this;
    }

    public List<JavaAnnotation> getAnnotations() {
        List<JavaAnnotation> fieldAnnotations = javaField.getAnnotations();
        if (fieldAnnotations != null && !fieldAnnotations.isEmpty()) {
            return fieldAnnotations;
        }
        if (annotations == null) {
            return new ArrayList<>();
        }
        return this.annotations;
    }

    public DocJavaField setAnnotations(List<JavaAnnotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public boolean isArray() {
        return array;
    }

    public DocJavaField setArray(boolean array) {
        this.array = array;
        return this;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public DocJavaField setPrimitive(boolean primitive) {
        this.primitive = primitive;
        return this;
    }

    public boolean isCollection() {
        return collection;
    }

    public DocJavaField setCollection(boolean collection) {
        this.collection = collection;
        return this;
    }

    public boolean isFile() {
        return file;
    }

    public DocJavaField setFile(boolean file) {
        this.file = file;
        return this;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean anEnum) {
        isEnum = anEnum;
    }

    @Override
    public int compareTo(DocJavaField o) {
        return this.getOrder() - o.getOrder();
    }

    @Override
    public String toString() {
        return javaField.getName();
    }
}
