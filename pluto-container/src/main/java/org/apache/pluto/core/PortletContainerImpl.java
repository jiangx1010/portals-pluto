/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.core;

import java.io.IOException;
import java.util.Map;

import javax.portlet.PortletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.impl.ActionRequestImpl;
import org.apache.pluto.internal.impl.ActionResponseImpl;
import org.apache.pluto.internal.impl.PortletWindowImpl;
import org.apache.pluto.internal.impl.RenderRequestImpl;
import org.apache.pluto.internal.impl.RenderResponseImpl;
import org.apache.pluto.spi.PortletURLProvider;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.RequiredContainerServices;

/**
 * Default Pluto Container implementation.
 *
 * @author <a href="mailto:ddewolf@apache.org">David H. DeWolf</a>
 * @author <a href="mailto:zheng@apache.org">ZHENG Zhong</a>
 * @version 1.0
 * @since Sep 18, 2004
 */
public class PortletContainerImpl implements PortletContainer {

    /** Internal logger. */
    private static final Log LOG = LogFactory.getLog(PortletContainerImpl.class);
    
    
    // Private Member Variables ------------------------------------------------
    
    /** The portlet container name. */
    private String name = null;
    
    /** The required container services associated with this container. */
    private RequiredContainerServices requiredContainerServices = null;
    
    /** The optional container services associated with this container. */
    private OptionalContainerServices optionalContainerServices = null;
    
    /** The servlet context associated with this container. */
    private ServletContext servletContext = null;

    /** Flag indicating whether or not we've been initialized. */
    private boolean initialized = false;
    
    
    // Constructor -------------------------------------------------------------
    
    /** Default Constructor.  Create a container implementation
     *  whith the given name and given services.
     *
     * @param name  the name of the container.
     * @param requiredServices  the required container services implementation.
     * @param optionalServices  the optional container services implementation.
     */
    public PortletContainerImpl(String name,
                                RequiredContainerServices requiredServices,
                                OptionalContainerServices optionalServices) {
        this.name = name;
        this.requiredContainerServices = requiredServices;
        this.optionalContainerServices = optionalServices;
    }
    
    
    // PortletContainer Impl ---------------------------------------------------
    
    /**
     * Initialize the container for use within the given configuration scope.
     * @param servletContext  the servlet context of the portal webapp.
     */
    public void init(ServletContext servletContext)
    throws PortletContainerException {
    	if (servletContext == null) {
    		throw new PortletContainerException(
    				"Unable to initialize portlet container [" + name + "]: "
    				+ "servlet context is null.");
    	}
        this.servletContext = servletContext;
        this.initialized = true;
        infoWithName("Container initialized successfully.");
    }

    /**
     * Determine whether this container has been initialized or not.
     * @return true if the container has been initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Destroy this container.
     */
    public void destroy() {
        this.servletContext = null;
        this.initialized = false;
        infoWithName("Container destroyed.");
    }


    /**
     * Renders the portlet associated with the specified portlet window.
     * @param portletWindow  the portlet window.
     * @param request  the servlet request.
     * @param response  the servlet response.
     * @throws IllegalStateException  if the container is not initialized.
     * @throws PortletException
     * @throws IOException
     * @throws PortletContainerException
     * 
     * @see javax.portlet.Portlet#render(RenderRequest, RenderResponse)
     */
    public void doRender(PortletWindow portletWindow,
                         HttpServletRequest request,
                         HttpServletResponse response)
    throws PortletException, IOException, PortletContainerException {
    	
    	ensureInitialized();
    	
        InternalPortletWindow internalPortletWindow =
        		new PortletWindowImpl(servletContext, portletWindow);
        debugWithName("Render request received for portlet: "
        		+ portletWindow.getPortletName());
        
        RenderRequestImpl renderRequest = new RenderRequestImpl(
        		this, internalPortletWindow, request);
        RenderResponseImpl renderResponse = new RenderResponseImpl(
        		this, internalPortletWindow, request, response);

        PortletInvoker invoker = new PortletInvoker(internalPortletWindow);
        invoker.render(renderRequest, renderResponse);
        debugWithName("Portlet rendered for: "
        		+ portletWindow.getPortletName());
    }

    /**
     * Process action for the portlet associated with the given portlet window.
     * @param portletWindow  the portlet window.
     * @param request  the servlet request.
     * @param response  the servlet response.
     * @throws PortletException
     * @throws IOException
     * @throws PortletContainerException
     * 
     * @see javax.portlet.Portlet#processAction(ActionRequest, ActionResponse)
     */
    public void doAction(PortletWindow portletWindow,
                         HttpServletRequest request,
                         HttpServletResponse response)
    throws PortletException, IOException, PortletContainerException {
    	
    	ensureInitialized();
    	
        InternalPortletWindow internalPortletWindow =
            	new PortletWindowImpl(servletContext, portletWindow);
    	debugWithName("Action request received for portlet: "
    			+ portletWindow.getPortletName());
    	
        ActionRequestImpl actionRequest = new ActionRequestImpl(
        		this, internalPortletWindow, request);
        ActionResponseImpl actionResponse = new ActionResponseImpl(
        		this, internalPortletWindow, request, response);
        
        PortletInvoker invoker = new PortletInvoker(internalPortletWindow);
        invoker.action(actionRequest, actionResponse);
        debugWithName("Portlet action processed for: "
        		+ portletWindow.getPortletName());
        
        // After processing action, send a redirect URL for rendering.
        String location = actionResponse.getRedirectLocation();

        if (location == null) {
        	
        	// Create portlet URL provider to encode redirect URL.
        	debugWithName("No redirect location specified.");
            PortletURLProvider redirectURL = requiredContainerServices
            		.getPortalCallbackService()
            		.getPortletURLProvider(request, internalPortletWindow);
            
            // Encode portlet mode if it is changed.
            if (actionResponse.getChangedPortletMode() != null) {
                redirectURL.setPortletMode(
                		actionResponse.getChangedPortletMode());
            }
            
            // Encode window state if it is changed.
            if (actionResponse.getChangedWindowState() != null) {
                redirectURL.setWindowState(
                		actionResponse.getChangedWindowState());
            }
            
            // Encode render parameters retrieved from action response.
            Map renderParameters = actionResponse.getRenderParameters();
            redirectURL.clearParameters();
            redirectURL.setParameters(renderParameters);
            
            // Encode redirect URL as a render URL.
            redirectURL.setAction(false);
            
            // Set secure of the redirect URL if necessary.
            if (actionRequest.isSecure()) {
                redirectURL.setSecure();
            }
            
            // Encode the redirect URL to a string.
            location = actionResponse.encodeRedirectURL(redirectURL.toString());
        }

        // Here we intentionally use the original response
        // instead of the wrapped internal response.
        response.sendRedirect(location);
        debugWithName("Redirect URL sent.");
    }

    /**
     * Loads the portlet associated with the specified portlet window.
     * @param portletWindow  the portlet window.
     * @param request  the servlet request.
     * @param response  the servlet response.
     * @throws PortletException
     * @throws IOException
     * @throws PortletContainerException
     */
    public void doLoad(PortletWindow portletWindow,
                       HttpServletRequest request,
                       HttpServletResponse response)
    throws PortletException, IOException, PortletContainerException {
    	
    	ensureInitialized();
    	
        InternalPortletWindow internalPortletWindow =
        		new PortletWindowImpl(servletContext, portletWindow);
        debugWithName("Load request received for portlet: "
        		+ portletWindow.getPortletName());
        
        RenderRequestImpl renderRequest = new RenderRequestImpl(
        		this, internalPortletWindow, request);
        RenderResponseImpl renderResponse = new RenderResponseImpl(
        		this, internalPortletWindow, request, response);
        
        PortletInvoker invoker = new PortletInvoker(internalPortletWindow);
        invoker.load(renderRequest, renderResponse);
        debugWithName("Portlet loaded for: " + portletWindow.getPortletName());
    }

    public String getName() {
        return name;
    }

    public RequiredContainerServices getRequiredContainerServices() {
        return requiredContainerServices;
    }

    public OptionalContainerServices getOptionalContainerServices() {
        return optionalContainerServices;
    }
    
    
    // Private Methods ---------------------------------------------------------
    
    /**
     * Ensures that the portlet container is initialized.
     * @throws IllegalStateException  if the container is not initialized.
     */
    private void ensureInitialized() throws IllegalStateException {
    	if (!isInitialized()) {
    		throw new IllegalStateException(
    				"Portlet container [" + name + "] is not initialized.");
    	}
    }
    
    /**
     * Prints a message at DEBUG level with the container name prefix.
     * @param message  log message.
     */
    private void debugWithName(String message) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Portlet Container [" + name + "]: " + message);
    	}
    }
    
    /**
     * Prints a message at INFO level with the container name prefix.
     * @param message  log message.
     */
    private void infoWithName(String message) {
    	if (LOG.isInfoEnabled()) {
    		LOG.info("Portlet Container [" + name + "]: " + message);
    	}
    }
    
}

