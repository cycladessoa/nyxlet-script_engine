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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.SimpleBindings;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.util.LRUCache;
import org.cyclades.nyxlet.script_engine.actionhandler.api.SimpleScriptActionHandler;

@AHandler({"groovy"})
public class GroovyScriptActionHandler extends SimpleScriptActionHandler {

    public GroovyScriptActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }
    
    @Override
    public Object executeScripts(List<String> scriptList, Map<String, List<String>> baseParameters, 
            Object scriptInputObject) throws Exception {
        Bindings bindings = new SimpleBindings();
        bindings.put(INPUT_PARAMETER, scriptInputObject);
        bindings.put(OUTPUT_PARAMETER, null);
        for (String script : scriptList) {
            CompiledScript scriptObject = getScript(script);
            scriptObject.eval(bindings);
        }
        return bindings.get(OUTPUT_PARAMETER);
    }

    private CompiledScript getScript (String text) throws Exception{
        if (!scriptCache.containsKey(text)) {
            scriptCache.put(text, scriptEngine.compile(text));
            scriptEngine.getClassLoader().clearCache();
        }
        return scriptCache.get(text);
    }

    @Override
    public void init () throws Exception {
        super.init();
        if (getParentNyxlet().getExternalProperties().containsKey(LRU_CACHE_MAX_COUNT)) 
            maxLRUCount = Integer.parseInt(getParentNyxlet().getExternalProperties().getProperty(LRU_CACHE_MAX_COUNT));
        scriptCache = Collections.synchronizedMap(new LRUCache<String, CompiledScript>(maxLRUCount));
        getParentNyxlet().logInfo("LRU cache initiated with max count set to: " + maxLRUCount);
    }
    
    private GroovyScriptEngineImpl scriptEngine = (GroovyScriptEngineImpl)new GroovyScriptEngineFactory().getScriptEngine();
    private Map<String, CompiledScript> scriptCache = null;
    private int maxLRUCount = 20;
        
    public static final String LRU_CACHE_MAX_COUNT = "lru_cache_max_count";

}
