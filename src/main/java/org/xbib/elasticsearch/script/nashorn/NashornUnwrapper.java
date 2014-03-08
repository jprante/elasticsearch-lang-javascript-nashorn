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

import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.ConsString;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.Undefined;
import jdk.nashorn.internal.runtime.arrays.ArrayData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class NashornUnwrapper {

    private NashornUnwrapper() {
    }

    /**
     * Convert an object from a script wrapper value to a serializable value valid outside
     * of the Nashorn script processor context.
     *
     * This includes converting Array objects to Lists of valid objects.
     *
     * @param value the value to convert from script wrapper object to external object value
     * @return unwrapped and converted value
     */
    public static Object unwrapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof NativeArray) {
            NativeArray nativeArray = (NativeArray)value;
            ArrayData data = nativeArray.getArray();
            int length = (int)data.length();
            ArrayList<Object> list = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++) {
                list.add(unwrapValue(data.getObject(i)));
            }
            return list;
        } else if (value instanceof Map) {
            Map map = (Map)value;
            Map<Object,Object> result = new LinkedHashMap<Object,Object>();
            for (Object key : map.keySet()) {
                result.put(key, unwrapValue(map.get(key)));
            }
            return result;
        } else if (value instanceof ScriptObject) {
            ScriptObject object = (ScriptObject)value;
            Map<Object,Object> result = new LinkedHashMap<Object,Object>();
            for (Object key : object.getOwnKeys(true)) {
                result.put(key, unwrapValue(object.get(key)));
            }
            return result;
        } else if (value instanceof Undefined) {
            return null;
        } else if (value instanceof ConsString) {
            return value.toString();
        } else {
            return value;
        }
    }

}
