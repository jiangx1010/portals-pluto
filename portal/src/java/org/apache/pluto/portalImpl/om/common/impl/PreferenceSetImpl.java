/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Pluto", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/* 

 */

package org.apache.pluto.portalImpl.om.common.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.portlet.PreferencesValidator;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.PreferenceSetCtrl;
import org.apache.pluto.util.StringUtils;
import org.apache.pluto.portalImpl.services.log.Log;
import org.apache.pluto.services.log.Logger;

public class PreferenceSetImpl extends HashSet
implements PreferenceSet, PreferenceSetCtrl, java.io.Serializable {

    private String castorPreferencesValidator; 
    private ClassLoader classLoader;
    private Logger log = null;

    public PreferenceSetImpl() {
        this.log = Log.getService().getLogger(getClass());
    }

    // PreferenceSet implementation.

    public Preference get(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                return preference;
            }
        }
        return null;
    }

    public PreferencesValidator getPreferencesValidator()
    {
        if (this.classLoader == null)
            throw new IllegalStateException("Portlet class loader not yet available to load preferences validator.");

        if (castorPreferencesValidator == null)
            return null;

        try {
            Object validator = classLoader.loadClass(castorPreferencesValidator).newInstance();
            if (validator instanceof PreferencesValidator)
                return(PreferencesValidator)validator;
            else
                log.error("Specified class " + castorPreferencesValidator +" is no preferences validator.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    // PreferenceSetCtrl implementation.

    public Preference add(String name, Collection values)
    {
        PreferenceImpl preference = new PreferenceImpl();
        preference.setName(name);
        preference.setValues(values);

        super.add(preference);

        return preference;
    }

    public Preference remove(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                super.remove(preference);
                return preference;
            }
        }
        return null;
    }

    public void remove(Preference preference)
    {
        super.remove(preference);
    }

    // additional methods.
    
    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": ");
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buffer.append(((PreferenceImpl)iterator.next()).toString(indent+2));
        }
        return buffer.toString();
    }


    // additional internal methods

    public String getCastorPreferencesValidator()
    {
        return castorPreferencesValidator;
    }

    public void setCastorPreferencesValidator(String castorPreferencesValidator)
    {
        this.castorPreferencesValidator = castorPreferencesValidator;
    }

    public Collection getCastorPreferences()
    {
        return this;
    }

    public void setClassLoader(ClassLoader loader)
    {
        this.classLoader = loader;
    }

    /**
     * @see java.util.Collection#addAll(Collection)
     * makes a deep copy
     */
    public boolean addAll(Collection c) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            PreferenceImpl pref = (PreferenceImpl) it.next();
            this.add(pref.getName(), pref.getClonedCastorValuesAsCollection());
        }

        return true;  //always assume something changed
    }

}
