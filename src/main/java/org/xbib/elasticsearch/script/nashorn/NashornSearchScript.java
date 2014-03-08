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

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.lookup.SearchLookup;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.Map;

/**
 *
 */
public class NashornSearchScript implements SearchScript {

    private final SearchLookup lookup;

    private final CompiledScript script;

    private final ScriptContext context;

    public NashornSearchScript(SearchLookup lookup, CompiledScript script, Bindings bindings) {
        this.lookup = lookup;
        this.script = script;
        this.context = new SimpleScriptContext();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    @Override
    public void setScorer(Scorer scorer) {
        lookup.setScorer(scorer);
    }

    @Override
    public void setNextReader(AtomicReaderContext reader) {
        lookup.setNextReader(reader);
    }

    @Override
    public void setNextDocId(int doc) {
        lookup.setNextDocId(doc);
    }

    @Override
    public void setNextScore(float score) {
        context.getBindings(ScriptContext.ENGINE_SCOPE).put("_score", score);
    }

    @Override
    public void setNextVar(String name, Object value) {
        context.getBindings(ScriptContext.ENGINE_SCOPE).put(name, value);
    }

    @Override
    public void setNextSource(Map<String, Object> source) {
        lookup.source().setNextSource(source);
    }

    @Override
    public Object run() {
        try {
            return script.eval(context);
        } catch (ScriptException e) {
            throw new org.elasticsearch.script.ScriptException(e.getMessage(), e);
        }
    }

    @Override
    public float runAsFloat() {
        return ((Number) run()).floatValue();
    }

    @Override
    public long runAsLong() {
        return ((Number) run()).longValue();
    }

    @Override
    public double runAsDouble() {
        return ((Number) run()).doubleValue();
    }

    @Override
    public Object unwrap(Object value) {
        return NashornUnwrapper.unwrapValue(value);
    }
}
