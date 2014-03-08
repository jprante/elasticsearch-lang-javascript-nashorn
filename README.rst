Elasticsearch Nashorn Language Plugin
=====================================

This plugin extends Elasticsearch with the script language Nashorn, the implementation of
ECMAScript-262 Edition 5.1 ("Javascript") for the Java 8 VM.

The integration is implemented by JSR 223. It should be easy to reuse the source code of this plugin
as a basis to extend Elasticsearch scripting by other JSR 223 languages.

Installation
------------

Prequisites

- Elasticsearch 1.0.0+
- Java 8

=============  ===========  =================  ===========================================================
ES version     Plugin       Release date       Command
-------------  -----------  -----------------  -----------------------------------------------------------
1.0.1          1.0.1.1      Mar 8, 2014        ./bin/plugin --install nashorn --url http://bit.ly/1lfjhHd
=============  ===========  =================  ===========================================================

Do not forget to restart the node after installing.

Project docs
------------

The Maven project site is available at `Github <http://jprante.github.io/elasticsearch-lang-javascript-nashorn>`_

Binaries
--------

Binaries are available at `Bintray <https://bintray.com/pkg/show/general/jprante/elasticsearch-plugins/elasticsearch-lang-javascript-nashorn>`_

Introduction
------------

Let's say you want to use ``function_score`` using ``nashorn``. Here is a way of doing it::

    curl -XDELETE "http://localhost:9200/test"
    curl -XPUT "http://localhost:9200/test/doc/1" -d '{
        "num": 1.0
    }'
    curl -XPUT "http://localhost:9200/test/doc/2?refresh" -d '{
        "num": 2.0
    }'
    curl -XGET "http://localhost:9200/test/_search?pretty" -d '
    {
      "query": {
        "function_score": {
          "script_score": {
            "script": "doc[\"num\"].value",
            "lang": "nashorn"
         }
       }
      }
    }'

It gives::

    {
       // ...
       "hits": {
          "total": 2,
          "max_score": 2.0,
          "hits": [
             {
                // ...
                "_score": 2.0
             },
             {
                // ...
                "_score": 1.0
             }
          ]
       }
    }

Using Nashorn with script_fields
--------------------------------

Let's define script fields::

    curl -XDELETE "http://localhost:9200/test"
    curl -XPUT "http://localhost:9200/test/doc/1?refresh" -d'
    {
      "obj1": {
       "test": "something"
      },
      "obj2": {
        "arr2": [ "arr_value1", "arr_value2" ]
      }
    }'
    curl -XGET "http://localhost:9200/test/_search?pretty" -d'
    {
      "script_fields": {
        "s_obj1": {
          "script": "_source.obj1", "lang": "nashorn"
        },
        "s_obj1_test": {
          "script": "_source.obj1.test", "lang": "nashorn"
        },
        "s_obj2": {
          "script": "_source.obj2", "lang": "nashorn"
        },
        "s_obj2_arr2": {
          "script": "_source.obj2.arr2", "lang": "nashorn"
        }
      }
    }'


It gives::

    {
      // ...
      "hits": [ {
            // ...
            "fields": {
               "s_obj2_arr2": [ [ "arr_value1", "arr_value2" ] ],
               "s_obj1_test": [ "something" ],
               "s_obj2": [ {
                     "arr2": [ "arr_value1", "arr_value2" ]
               } ],
               "s_obj1": [ {
                     "test": "something"
               } ]
            }
       } ]
    }


License
=======

Elasticsearch Nashorn Plugin

Copyright (C) 2014 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

