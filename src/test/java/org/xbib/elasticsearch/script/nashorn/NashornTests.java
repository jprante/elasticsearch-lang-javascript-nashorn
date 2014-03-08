package org.xbib.elasticsearch.script.nashorn;

import org.junit.Test;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NashornTests {

    @Test
    public void intArrayLengthTest() throws ScriptException {
        Map<String, Object> vars = new HashMap<String, Object>();
        Integer[] l = new Integer[] { 1,2,3 };
        vars.put("l", l);
        String script = "l.length";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        ScriptContext context = engine.getContext();
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.putAll(vars);
        Compilable compilable = (Compilable)engine;
        CompiledScript compiledScript = compilable.compile(script);
        Object o = compiledScript.eval();
        assertThat(((Number) o).intValue(), equalTo(3));
    }

    @Test
    public void objectArrayTest() throws ScriptException {
        Map<String, Object> vars = new HashMap<String, Object>();
        final Map<String, Object> obj2 = new HashMap<String,Object>() {{
            put("prop2", "value2");
        }};
        final Map<String, Object> obj1 = new HashMap<String,Object>() {{
            put("prop1", "value1");
            put("obj2", obj2);
        }};
        vars.put("l", new Object[] { "1", "2", "3", obj1 });
        String script = "l.length";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.putAll(vars);
        Compilable compilable = (Compilable)engine;
        CompiledScript compiledScript = compilable.compile(script);
        Object o = compiledScript.eval(bindings);
        assertThat(((Number) o).intValue(), equalTo(4));
    }
}
