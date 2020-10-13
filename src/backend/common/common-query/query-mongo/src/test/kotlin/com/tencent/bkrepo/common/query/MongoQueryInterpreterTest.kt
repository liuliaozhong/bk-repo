/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.  
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.tencent.bkrepo.common.query

import com.mongodb.BasicDBList
import com.tencent.bkrepo.common.query.builder.MongoQueryInterpreter
import com.tencent.bkrepo.common.query.model.PageLimit
import com.tencent.bkrepo.common.query.model.QueryModel
import com.tencent.bkrepo.common.query.model.Rule
import com.tencent.bkrepo.common.query.model.Sort
import org.bson.Document
import org.junit.jupiter.api.Test

class MongoQueryInterpreterTest {

    @Test
    fun buildTest() {
        val projectId = Rule.QueryRule("projectId", "1")
        val repoName = Rule.QueryRule("repoName", "repoName")
        val path = Rule.QueryRule("path", "/a/b/c")
        val rule1 = Rule.NestedRule(mutableListOf(path, projectId), Rule.NestedRule.RelationType.AND)

        val rule2 = Rule.NestedRule(mutableListOf(repoName, rule1), Rule.NestedRule.RelationType.AND)

        val queryModel = QueryModel(
            page = PageLimit(0, 10),
            sort = Sort(listOf("name"), Sort.Direction.ASC),
            select = mutableListOf("projectId", "repoName", "fullPath", "metadata"),
            rule = rule2
        )

        val builder = MongoQueryInterpreter()
        val query = builder.interpret(queryModel).mongoQuery
        println(query.queryObject)

        println(findProjectId(query.queryObject))
    }

    private fun findProjectId(document: Document): Any? {
        for ((key, value) in document) {
            if (key == "projectId") return value
            if (key == "\$and") {
                for (element in value as BasicDBList) {
                    findProjectId(element as Document)?.let { return it }
                }
            }
        }
        return null
    }
}
