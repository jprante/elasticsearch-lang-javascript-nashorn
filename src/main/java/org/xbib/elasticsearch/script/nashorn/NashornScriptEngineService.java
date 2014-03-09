/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.script.nashorn;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.ScriptEngineService;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.lookup.SearchLookup;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import java.util.List;
import java.util.Map;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

/**
 *
 */
public class NashornScriptEngineService extends AbstractComponent implements ScriptEngineService {

    private final ScriptEngine engine;

    @Inject
    public NashornScriptEngineService(Settings settings) {
        super(settings);
        // setup the engine to share the definition of the Ecma script built-in objects: aka NashornGlobal.
//      System.setProperty("nashorn.args", "--global-per-engine");
//      ScriptEngineManager m = new ScriptEngineManager();
//      this.engine = m.getEngineByName("nashorn");
        // changing a system property is not allowed by the tests.
        // Use the internal API instead
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        this.engine = factory.getScriptEngine(new String[] { "--global-per-engine" });
    }

    @Override
    public void close() {
    }

    @Override
    public String[] types() {
        List<String> list = engine.getFactory().getNames();
        return list.toArray(new String[list.size()]);
    }

    @Override
    public String[] extensions() {
        List<String> list = engine.getFactory().getExtensions();
        return list.toArray(new String[list.size()]);
    }

    @Override
    public Object compile(String script) {
        Compilable compilable = (Compilable)engine;
        try {
            return compilable.compile(script);
        } catch (ScriptException e) {
            throw new org.elasticsearch.script.ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public ExecutableScript executable(Object compiledScript, Map<String, Object> vars) {
        CompiledScript script = (CompiledScript)compiledScript;
        return new NashornExecutableScript(script, bind(vars));
    }

    @Override
    public SearchScript search(Object compiledScript, SearchLookup lookup, @Nullable Map<String, Object> vars) {
        return new NashornSearchScript(lookup, (CompiledScript)compiledScript, bind(bind(lookup.asMap()), vars));
    }

    @Override
    public Object execute(Object compiledScript, Map<String, Object> vars) {
        CompiledScript script = (CompiledScript)compiledScript;
        try {
            return script.eval(bind(vars));
        } catch (ScriptException e) {
            throw new org.elasticsearch.script.ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public Object unwrap(Object value) {
        return NashornUnwrapper.unwrapValue(value);
    }

    /**
     * Nashorn does not accept Java Lists/Collections, and silently fails.
     * Convert them to object arrays before binding them
     *
     * @param vars vars
     * @return bindings
     */
    private Bindings bind(Map<String, Object> vars) {
        return bind(new SimpleBindings(), vars);
    }

    private Bindings bind(Bindings bindings, Map<String, Object> vars) {
        if (vars != null) {
            for (Map.Entry<String,Object> me : vars.entrySet()) {
                Object o = me.getValue();
                bindings.put(me.getKey(), o instanceof List ? ((List)o).toArray() : o);
            }
        }
        return bindings;
    }

}
