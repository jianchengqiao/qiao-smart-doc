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
package com.smartdoc.mojo;

import com.power.common.constants.*;
import com.power.common.util.*;
import com.power.doc.model.*;
import com.power.doc.utils.*;
import com.smartdoc.constant.*;
import com.smartdoc.util.FileUtil;
import com.smartdoc.util.*;
import com.thoughtworks.qdox.*;
import org.apache.maven.artifact.*;
import org.apache.maven.artifact.repository.*;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.artifact.resolver.filter.*;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.apache.maven.repository.*;
import org.apache.maven.shared.dependency.graph.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;


/**
 * @author yu 2020/1/8.
 */
public abstract class BaseDocsGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Component
    protected RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Parameter(property = "scope")
    private String scope;

    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;

    @Parameter(property = "serverUrl")
    private String serverUrl;

    @Parameter(property = "projectName")
    private String projectName;

    @Parameter(required = false)
    private Set excludes;

    @Parameter(required = false)
    private Set includes;

    @Parameter(property = "skip")
    private String skip;

    private DependencyNode rootNode;

    protected JavaProjectBuilder javaProjectBuilder;

    private List<String> projectArtifacts;

    public abstract void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)
            throws MojoExecutionException, MojoFailureException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //skip
        if ("true".equals(skip)) {
            return;
        }
        if (Objects.nonNull(configFile) && !configFile.exists()) {
            // Throwing an exception will cause an error in the multi-module maven project build.
            this.getLog().warn("Can't find config file: " + configFile.getName() + " from [" + project.getName()
                    + "],If it is a non-web module, please ignore the error.");
            return;
        }
        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("Smart-doc Start preparing sources at: " + DateTimeUtil.nowStrTime());
        projectArtifacts = new ArrayList<>();
        javaProjectBuilder = buildJavaProjectBuilder();
        javaProjectBuilder.setEncoding(Charset.DEFAULT_CHARSET);
        ApiConfig apiConfig = MojoUtils.buildConfig(configFile, projectName, project, projectArtifacts, getLog());
        if (Objects.isNull(apiConfig)) {
            this.getLog().info(GlobalConstants.ERROR_MSG);
            return;
        }
        if (EmptyUtil.notEmpty(serverUrl)) {
            apiConfig.setServerUrl(serverUrl);
        }
        String rpcConsumerConfig = apiConfig.getRpcConsumerConfig();
        if (!FileUtil.isAbsPath(rpcConsumerConfig) && StringUtil.isNotEmpty(rpcConsumerConfig)) {
            apiConfig.setRpcConsumerConfig(project.getBasedir().getPath() + "/" + rpcConsumerConfig);
        }
        String outPath = apiConfig.getOutPath();
        if (!FileUtil.isAbsPath(outPath) && StringUtil.isNotEmpty(outPath)) {
            apiConfig.setOutPath(project.getBasedir().getPath() + "/" + outPath);
        }
        getLog().info("Smart-doc Starting Create API Documentation at: " + DateTimeUtil.nowStrTime());
        getLog().info("API documentation is output to => " + apiConfig.getOutPath().replace("\\", "/"));
        this.executeMojo(apiConfig, javaProjectBuilder);
    }


    /**
     * Classloading
     *
     * @return
     * @throws MojoExecutionException
     */
    private JavaProjectBuilder buildJavaProjectBuilder() throws MojoExecutionException {
        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.setEncoding(Charset.DEFAULT_CHARSET);
        javaDocBuilder.setErrorHandler(e -> getLog().warn(e.getMessage()));
        //addSourceTree
        javaDocBuilder.addSourceTree(new File("/src/main/java"));
        //sources.stream().map(File::new).forEach(javaDocBuilder::addSourceTree);
        javaDocBuilder.addClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        loadSourcesDependencies(javaDocBuilder);
        return javaDocBuilder;
    }

    /**
     * load sources
     *
     * @param javaDocBuilder
     */
    private void loadSourcesDependencies(JavaProjectBuilder javaDocBuilder) throws MojoExecutionException {
        try {
            List<String> currentProjectModules = getCurrentProjectArtifacts(this.project);
            ArtifactFilter artifactFilter = this.createResolvingArtifactFilter();
            ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(this.session.getProjectBuildingRequest());
            buildingRequest.setProject(this.project);
            this.rootNode = this.dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifactFilter, this.reactorProjects);
            List<DependencyNode> dependencyNodes = this.rootNode.getChildren();
            List<Artifact> artifactList = this.getArtifacts(dependencyNodes);
            artifactList.forEach(artifact -> {
                if (ArtifactFilterUtil.ignoreSpringBootArtifactById(artifact)) {
                    return;
                }
                String artifactName = artifact.getGroupId() + ":" + artifact.getArtifactId();
                if (currentProjectModules.contains(artifactName)) {
                    this.projectArtifacts.add(artifactName);
                    return;
                }
                if (RegexUtil.isMatches(excludes, artifactName)) {
                    return;
                }
                if (RegexUtil.isMatches(includes, artifactName)) {
                    getLog().debug("load includes artifact: " + artifactName);
                    Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(artifact.getGroupId(),
                            artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), "sources");
                    this.projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                    return;
                }
                if (CollectionUtil.isEmpty(includes)) {
                    Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(artifact.getGroupId(),
                            artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), "sources");
                    this.projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                }
                getLog().debug("smart-doc loaded artifact:" + artifactName);
            });
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Can't build project dependency graph", e);
        }
    }

    /**
     * reference https://github.com/sfauvel/livingdocumentation
     *
     * @param javaDocBuilder  JavaProjectBuilder
     * @param sourcesArtifact Artifact
     */
    private void loadSourcesDependency(JavaProjectBuilder javaDocBuilder, Artifact sourcesArtifact) {
        // create request
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(sourcesArtifact);
        //request.setResolveTransitively(true);
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        // resolve deps
        ArtifactResolutionResult result = repositorySystem.resolve(request);

        // load source file into javadoc builder
        result.getArtifacts().forEach(artifact -> {
            try (JarFile jarFile = new JarFile(artifact.getFile())) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("smart-doc loaded jar source:" + artifact.getFile().toURI().toURL().toString());
                }
                for (Enumeration<?> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".java") && !name.endsWith("/package-info.java")) {
                        javaDocBuilder.addSource(
                                new URL("jar:" + artifact.getFile().toURI().toURL().toString() + "!/" + name));
                    }
                }
            } catch (Exception e) {
                getLog().warn("Unable to load jar source " + artifact + " : " + e.getMessage());
            }
        });
    }

    /**
     * reference maven-dependency-plugin tree
     *
     * @return ArtifactFilter
     */
    private ArtifactFilter createResolvingArtifactFilter() {
        ScopeArtifactFilter filter;
        if (this.scope != null) {
            this.getLog().debug("+ Resolving dependency tree for scope '" + this.scope + "'");
            filter = new ScopeArtifactFilter(this.scope);
        } else {
            filter = null;
        }
        return filter;
    }

    private List<Artifact> getArtifacts(List<DependencyNode> dependencyNodes) {
        List<Artifact> artifacts = new ArrayList<>();
        if (CollectionUtil.isEmpty(dependencyNodes)) {
            return artifacts;
        }
        for (DependencyNode dependencyNode : dependencyNodes) {
            if (ArtifactFilterUtil.ignoreArtifact(dependencyNode.getArtifact())) {
                continue;
            }
            artifacts.add(dependencyNode.getArtifact());
            if (dependencyNode.getChildren().size() > 0) {
                artifacts.addAll(getArtifacts(dependencyNode.getChildren()));
            }
        }
        return artifacts;
    }

    private List<String> getCurrentProjectArtifacts(MavenProject project) {
        if (!project.hasParent()) {
            return new ArrayList<>(0);
        }
        List<String> finalArtifactsName = new ArrayList<>();
        MavenProject mavenProject = project.getParent();
        if (Objects.nonNull(mavenProject)) {
            File file = mavenProject.getBasedir();
            if (!Objects.isNull(file)) {
                String groupId = mavenProject.getGroupId();
                List<String> moduleList = mavenProject.getModules();
                moduleList.forEach(str -> finalArtifactsName.add(groupId + ":" + str));
            }
        }
        return finalArtifactsName;
    }
}
