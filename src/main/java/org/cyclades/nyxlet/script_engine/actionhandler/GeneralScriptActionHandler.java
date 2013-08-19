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
package org.cyclades.nyxlet.script_engine.actionhandler;

import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.validator.ParameterHasValue;
import org.cyclades.nyxlet.script_engine.actionhandler.api.SimpleScriptActionHandler;

/**
 * This is the "generic" action handler supporting all scripting languages loaded via the SPI mechanism (whose libraries are
 * included in the classpath). By default, the JDK comes with javascript. It is encouraged to run the script specific ActionHandler
 * if one is provided within this service as further optimizations are possible and implemented. For example, the 
 * GroovyScriptActionHandler ("groovy") caches compiled scripts in a LRU data structure and can run much faster than calling this 
 * ActionHandler with the "groovy" script-type multiple times for the same script.
 */
@AHandler({"generic"})
public class GeneralScriptActionHandler extends SimpleScriptActionHandler {

    public GeneralScriptActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }
    
    @Override
    public Object executeScripts(List<String> scriptList, Map<String, List<String>> baseParameters, 
            Object scriptInputObject) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(baseParameters.get(SCRIPT_TYPE_PARAMETER).get(0));
        if (engine == null) throw new Exception("ScriptEngine not found via SPI mechanism: " + 
                baseParameters.get(SCRIPT_TYPE_PARAMETER).get(0));
        engine.put(INPUT_PARAMETER, scriptInputObject);
        engine.put(OUTPUT_PARAMETER, null);
        for (String script : scriptList) engine.eval(script);
        return engine.get(OUTPUT_PARAMETER);
    }
    
    @Override
    public void init () throws Exception {
        super.init();
        getFieldValidators()
            .add(new ParameterHasValue(SCRIPT_TYPE_PARAMETER));
    }

    public static final String SCRIPT_TYPE_PARAMETER = "script-type";

}
