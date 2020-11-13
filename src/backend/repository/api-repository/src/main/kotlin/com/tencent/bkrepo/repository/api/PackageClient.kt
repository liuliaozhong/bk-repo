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

package com.tencent.bkrepo.repository.api

import com.tencent.bkrepo.common.api.constant.DEFAULT_PAGE_NUMBER
import com.tencent.bkrepo.common.api.constant.DEFAULT_PAGE_SIZE
import com.tencent.bkrepo.common.api.constant.REPOSITORY_SERVICE_NAME
import com.tencent.bkrepo.common.api.pojo.Page
import com.tencent.bkrepo.common.api.pojo.Response
import com.tencent.bkrepo.common.query.model.QueryModel
import com.tencent.bkrepo.repository.pojo.packages.PackageSummary
import com.tencent.bkrepo.repository.pojo.packages.PackageVersion
import com.tencent.bkrepo.repository.pojo.packages.request.PackageVersionCreateRequest
import io.swagger.annotations.ApiOperation
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Primary
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Primary
@FeignClient(REPOSITORY_SERVICE_NAME, contextId = "PackageClient")
@RequestMapping("/service")
interface PackageClient {

    @ApiOperation("查询包信息")
    @GetMapping("/package/info/{projectId}/{repoName}")
    fun findPackageByKey(
        @PathVariable projectId: String,
        @PathVariable repoName: String,
        @RequestParam packageKey: String
    ): Response<PackageSummary?>

    @ApiOperation("查询版本信息")
    @GetMapping("/version/info/{projectId}/{repoName}")
    fun findVersionByName(
        @PathVariable projectId: String,
        @PathVariable repoName: String,
        @RequestParam packageKey: String,
        @RequestParam version: String
    ): Response<PackageVersion?>

    @ApiOperation("创建包版本")
    @PostMapping("/version/create")
    fun createVersion(
        @RequestBody request: PackageVersionCreateRequest
    ): Response<Void>

    @ApiOperation("删除包")
    @DeleteMapping("/package/delete/{projectId}/{repoName}")
    fun deletePackage(
        @PathVariable projectId: String,
        @PathVariable repoName: String,
        @RequestParam packageKey: String
    ): Response<Void>

    @ApiOperation("删除版本")
    @DeleteMapping("/version/delete/{projectId}/{repoName}")
    fun deleteVersion(
        @PathVariable projectId: String,
        @PathVariable repoName: String,
        @RequestParam packageKey: String,
        @RequestParam version: String
    ): Response<Void>

    @ApiOperation("搜索包")
    @DeleteMapping("/package/search")
    fun searchPackage(
        @RequestBody queryModel: QueryModel
    ): Response<Page<MutableMap<*, *>>>

    @ApiOperation("分页查询版本")
    @GetMapping("/version/page/{projectId}/{repoName}")
    fun listVersionPage(
        @PathVariable projectId: String,
        @PathVariable repoName: String,
        @RequestParam packageKey: String,
        @RequestParam version: String? = null,
        @RequestParam stageTag: String? = null,
        @RequestParam pageNumber: Int = DEFAULT_PAGE_NUMBER,
        @RequestParam pageSize: Int = DEFAULT_PAGE_SIZE
    ): Response<Page<PackageVersion>>
}
