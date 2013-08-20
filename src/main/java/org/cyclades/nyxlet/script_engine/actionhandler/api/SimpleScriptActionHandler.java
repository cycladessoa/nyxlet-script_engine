/*******************************************************************************
 * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cyclades.nyxlet.script_engine.actionhandler.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.validator.OneOf;
import org.cyclades.engine.validator.ParameterHasValue;
import org.cyclades.engine.validator.ParameterMatches;
import org.cyclades.io.ResourceRequestUtils;

/**
 * Simple ActionHandler that can be extended to implement different script engine execution strategies with the same input/output
 * pattern.
 */
public abstract class SimpleScriptActionHandler extends ActionHandler {

    public SimpleScriptActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }
    
    public abstract Object executeScripts (List<String> scriptList, Map<String, List<String>> baseParameters, 
            Object scriptInputObject) throws Exception;
    
    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "GeneralScriptActionHandler.handle: ";
        try {
            /********************************************************************/
            /*******                  START CODE BLOCK                    *******/
            /*******                                                      *******/
            /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
            /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
            /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
            /******* OF THIS ACTION HANDLER.                              *******/
            /********************************************************************/
            Object scriptInputObject = (nyxletSession.containsMapChannelKey(INPUT_PARAMETER)) ? 
                    nyxletSession.getMapChannelObject(INPUT_PARAMETER) : baseParameters.get(INPUT_PARAMETER);
            List<String> scriptList = new ArrayList<String>();
            if (baseParameters.containsKey(SCRIPT_URI_PARAMETER)) {
                for (String scriptURI : baseParameters.get(SCRIPT_URI_PARAMETER)) 
                    scriptList.add(new String(ResourceRequestUtils.getData(scriptURI, null), "UTF-8"));
            }
            if (baseParameters.containsKey(SCRIPT_PARAMETER)) {
                if (parameterAsBoolean(RUN_SCRIPT_FIRST_PARAMETER, baseParameters, false)) {
                    scriptList.addAll(0, baseParameters.get(SCRIPT_PARAMETER));
                } else {
                    scriptList.addAll(baseParameters.get(SCRIPT_PARAMETER));
                }
            }
            Object out = executeScripts(scriptList, baseParameters, scriptInputObject);
            if (parameterAsBoolean(USE_MAP_CHANNEL_PARAMETER, baseParameters, false)) {
                nyxletSession.putMapChannelObject(OUTPUT_PARAMETER, out);
            } else {
                XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
                streamWriter.writeStartElement(OUTPUT_PARAMETER);
                streamWriter.writeCharacters((out == null) ? "null" : out.toString());
                streamWriter.writeEndElement();
            }
            /********************************************************************/
            /*******                  END CODE BLOCK                      *******/
            /********************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    @Override
    public boolean isHealthy () throws Exception {
        return true;
    }

    @Override
    public void init () throws Exception {
        getFieldValidators()
        .add(new OneOf()
            .add(new ParameterHasValue(SCRIPT_PARAMETER))
            .add(new ParameterHasValue(SCRIPT_URI_PARAMETER)));
        
        if (getParentNyxlet().getExternalProperties().containsKey(PASSWORD)) {
            getFieldValidators().add(new ParameterMatches(
                    PASSWORD, getParentNyxlet().getExternalProperties().getProperty(PASSWORD)).showValues(false));
        }
    }

    @Override
    public void destroy () throws Exception {
        // your destruction code here, if any
    }
        
    public static final String SCRIPT_PARAMETER             = "script";
    public static final String SCRIPT_URI_PARAMETER         = "script-uri";
    public static final String RUN_SCRIPT_FIRST_PARAMETER   = "script-first";
    public static final String INPUT_PARAMETER              = "input";
    public static final String OUTPUT_PARAMETER             = "output";
    public static final String USE_MAP_CHANNEL_PARAMETER    = "use-map-channel";
    public static final String PASSWORD                     = "password";

}
