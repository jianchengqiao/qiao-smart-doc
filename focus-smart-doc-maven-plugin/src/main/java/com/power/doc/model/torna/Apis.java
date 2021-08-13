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
package com.power.doc.model.torna;

import java.util.List;

/**
 * @author xingzi 2021/2/8 10:07
 **/
public class Apis {
    private String name;
    private String description;
    private String url;
    private String httpMethod;
    private String contentType;
    private String isFolder;
    private String parentId;
    private String isShow;
    private List<HttpParam> headerParams;
    private List<HttpParam> pathParams;
    private List<HttpParam> requestParams;
    private List<HttpParam> responseParams;
    private String errorCodeParams;
    private List<Apis> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(String isFolder) {
        this.isFolder = isFolder;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }


    public List<HttpParam> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<HttpParam> requestParams) {
        this.requestParams = requestParams;
    }

    public List<HttpParam> getHeaderParams() {
        return headerParams;
    }

    public List<HttpParam> getPathParams() {
        return pathParams;
    }

    public Apis setPathParams(List<HttpParam> pathParams) {
        this.pathParams = pathParams;
        return this;
    }

    public void setHeaderParams(List<HttpParam> headerParams) {
        this.headerParams = headerParams;
    }

    public List<HttpParam> getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(List<HttpParam> responseParams) {
        this.responseParams = responseParams;
    }

    public String getErrorCodeParams() {
        return errorCodeParams;
    }

    public void setErrorCodeParams(String errorCodeParams) {
        this.errorCodeParams = errorCodeParams;
    }

    public List<Apis> getItems() {
        return items;
    }

    public void setItems(List<Apis> items) {
        this.items = items;
    }
}
