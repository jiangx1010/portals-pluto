/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
/* 

 */


package org.apache.pluto.om.common;

/**
 * <P>
 * The <CODE>SecurityRole</CODE> interface  ...
 * </P>
 * <P>
 * This interface defines the model as known from the MVC pattern.
 * Its purpose is to provide read access to the data stored in the model.
 * </P>
 * 
 */
public interface SecurityRole extends org.apache.pluto.om.Model {

    /**
     * Returns the description
     * 
     * @return the description
     */
    public String getDescription();

    /**
      * Returns the role_name
      * 
      * @return the role_name
      */
    public String getRoleName();
}