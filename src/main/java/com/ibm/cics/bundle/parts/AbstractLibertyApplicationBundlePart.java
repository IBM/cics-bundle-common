package com.ibm.cics.bundle.parts;

/*-
 * #%L
 * CICS Bundle Common Parent
 * %%
 * Copyright (C) 2026 IBM Corp.
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Defines a CICS Java bundle part that is deployed to a Liberty server.
 */
public abstract class AbstractLibertyApplicationBundlePart extends AbstractJavaBundlePart {
    /**
     * Controls whether CICS adds the 'cicsAllAuthenticated' security role to the
     * application.
     */
    private final boolean addCicsAllAuthenticatedRole;

    /**
     * The file containing application configuration to apply to the application.
     */
    private final File libertyAppConfigFile;

    /**
     * @param name                        The name of the bundle part.
     * @param type                        The type of the bundle part.
     * @param symbolicName                The symbolic name of the bundle part.
     * @param jvmServer                   The JVM server the bundle part is deployed
     *                                    to.
     * @param addCicsAllAuthenticatedRole Controls whether CICS adds the
     *                                    'cicsAllAuthenticated' security role to
     *                                    the application.
     * @param libertyAppConfigFile        The file containing application
     *                                    configuration to apply to the application.
     * @param bin                         The file to deploy in the bundle part.
     * @param binExtension                The extension of the file.
     */
    public AbstractLibertyApplicationBundlePart(String name, BundlePartType type, String symbolicName, String jvmServer,
            boolean addCicsAllAuthenticatedRole, File libertyAppConfigFile, File bin,
            String binExtension) {
        super(name, type, symbolicName, jvmServer, bin, binExtension);
        this.addCicsAllAuthenticatedRole = addCicsAllAuthenticatedRole;
        this.libertyAppConfigFile = libertyAppConfigFile;
    }

    @Override
    protected void addAdditionalNodes(Element rootElement) {
        // Add the appConfigFile element, if set.
        if (this.libertyAppConfigFile != null) {
            rootElement.setAttribute("appConfigFile", libertyAppConfigFile.getName());
        }

        // Add the addCICSAllAuth element, if the role shouldn't be added.
        if (!this.addCicsAllAuthenticatedRole) {
            rootElement.setAttribute("addCICSAllAuth", Boolean.toString(this.addCicsAllAuthenticatedRole));
        }
    }

    @Override
    public List<BundleResource> getDynamicResources() {
        List<BundleResource> dynamicResources = super.getDynamicResources();

        if (this.libertyAppConfigFile == null) {
            // Nothing else to add, so we can return early
            return dynamicResources;
        }

        // dynamicResource currently returns an unmodifiable list, so we need to create
        // a new list with the contents of the super one to be able to add to them.
        List<BundleResource> resources = new ArrayList<>(dynamicResources);

        BundleResource configFileResource = new StaticBundleResource(Paths.get(this.libertyAppConfigFile.getName()),
                () -> new FileInputStream(this.libertyAppConfigFile));
        resources.add(configFileResource);

        return resources;
    }

}
