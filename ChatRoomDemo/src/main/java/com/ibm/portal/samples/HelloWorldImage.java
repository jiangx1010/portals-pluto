/*  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.ibm.portal.samples;

import java.io.IOException;

import javax.inject.Inject;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.annotations.ContextPath;
import javax.portlet.annotations.Namespace;
import javax.portlet.annotations.HeaderMethod;
import javax.portlet.annotations.RenderMethod;
import javax.portlet.annotations.ServeResourceMethod;

/**
 * Contains a resource method to insert an image. Illustrates injection of several portlet
 * artifacts - the namespace, MimeResponse, context path, and portlet request.
 */
public class HelloWorldImage {

   // Injecting the namespace & MimeResponse
   @Inject
   @Namespace
   private String                pid;
   @Inject
   private MimeResponse          mimeresp;

   @Inject
   @ContextPath
   String                        ctxPath;

   @Inject
   PortletRequest                req;

   @Inject
   NameBean                      nameBean;

   // Some chimp pictures to associate with a name
   private final static String[] chimps = new String[] { 
      "/resources/images/BabyChimp.gif", 
      "/resources/images/bonoboMomKid.gif",
      "/resources/images/Chimpanzee_thinking-small.gif", 
      "/resources/images/monkey4.gif", 
      "/resources/images/monkey5.gif", 
      "/resources/images/monkeySmile.gif", 
      "/resources/images/monkeySmile2.gif", 
      "/resources/images/gorilla2.gif", 
      "/resources/images/gorilla1.gif" };
   
   /**
    * Header method to add dependency on the portlet hub. The markup for the image
    * is loaded and added to the DOM through an Ajax call on the client. The client-side 
    * code makes use of the portlet hub, so a header method is used to add a page-level
    * resource dependency on the portlet hub.
    * 
    * With the portletNames attribute specified as '*', this header method will be 
    * applied to all portlets in the portlet application.
    */
   @HeaderMethod(portletNames="*")
   public void header(HeaderRequest req, HeaderResponse resp) throws IOException {
      resp.addDependency("PortletHub", "javax.portlet", "3.0.0");
   }

   /**
    * Render method using automatic include. If a render method has multiple render methods,
    * the render methods are executed in the order of ascending ordinal number.
    */
   @RenderMethod(portletNames ="BeanPortletDemo", 
                 include = "/WEB-INF/jsp/helloWorldImage.jsp", 
                 ordinal = 100)
   public void getImageInclude() {}

   /**
    * This resource method generates markup for the image associated with a name.
    * The resource ID can be specified when generating a resource URL to address a 
    * specific resource method.
    */
   @ServeResourceMethod(portletNames = { "BeanPortletDemo" }, resourceID = "getImage")
   public String getImage() {

      StringBuilder txt = new StringBuilder(128);
      String name = nameBean.getName();

      if (name != null && name.length() > 0) {
         
         // pick a chimp
         
         int ind = 0;
         for (byte b : name.getBytes()) {
            ind += 0xFF & b;
         }
         ind = ind % chimps.length;
         String path = ctxPath + chimps[ind];
         path = mimeresp.encodeURL(path);
      
         txt.append("<p>Your image appears here:</p>\n");
         txt.append("<img src='").append(path).append("'>\n");
      }

      return txt.toString();
   }

}
