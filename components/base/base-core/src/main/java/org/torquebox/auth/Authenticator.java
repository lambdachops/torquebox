/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.auth;

import java.util.Map;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * Authentication bean - integrates with PicketBox to provide JBoss
 * auth bits to ruby apps.
 *
 * @author Lance Ball <lball@redhat.com>
 */
public class Authenticator
{
    public static final String DEFAULT_AUTH_STRATEGY = "file";
    public static final String DEFAULT_DOMAIN        = "other";

    private KernelController controller;
    private String applicationName;
    private Map<String, Map<String, String>> config;


    public void setKernelController(KernelController controller) {
        this.controller = controller;
    }

    public KernelController getKernelController() {
        return this.controller;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }
    
	public void setConfig(Map<String, Map<String, String>> config) {
		this.config = config;
	}

	public Map<String, Map<String, String>> getConfig() {
		return config;
	}

    public void start() {
    	if (config == null) { return; }
    	for(String key: config.keySet()) {
    		String strategy = config.get(key).get("strategy");
    		String domain   = config.get(key).get("domain");
    		
            if (!strategy.equals("file")) {
                System.err.println("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
            } else {
                UsersRolesAuthenticator authenticator = new UsersRolesAuthenticator();
                authenticator.setAuthDomain(domain);
                KernelController controller = this.getKernelController();
                String beanName = this.getApplicationName() + "-authentication-" + key;
                BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, UsersRolesAuthenticator.class.getName());
                BeanMetaData beanMetaData = builder.getBeanMetaData();
                try {
                	System.out.println("Installing bean: " + beanName);
                    controller.install(beanMetaData, authenticator);
                }
                catch (Throwable throwable) {
                    System.err.println("Cannot install PicketBox authentication.");
                    System.err.println(throwable.getMessage());
                    throwable.printStackTrace(System.err);
                }
            }
	
    	}
    }

    public void stop() {
        // release resources
    }
}
