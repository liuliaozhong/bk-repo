/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2023 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.bkrepo.analyst.pojo.response.filter

import com.tencent.bkrepo.analyst.pojo.Constant.FILTER_RULE_TYPE_IGNORE
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("制品扫描结果忽略规则")
data class FilterRule(
    @ApiModelProperty("id")
    var id: String? = null,

    @ApiModelProperty("规则名")
    val name: String,

    @ApiModelProperty("规则描述")
    val description: String,

    @ApiModelProperty("目标项目，系统级的规则为空字符串")
    val projectId: String,

    @ApiModelProperty("需要应用规则的项目，为空表示全部应用，该字段仅对系统级规则有效")
    val projectIds: List<String>? = null,

    @ApiModelProperty("目标仓库名")
    val repoName: String? = null,

    @ApiModelProperty("目标扫描方案ID")
    val planId: String? = null,

    @ApiModelProperty("目标路径")
    val fullPath: String? = null,

    @ApiModelProperty("目标包名")
    val packageKey: String? = null,

    @ApiModelProperty("目标版本")
    val packageVersion: String? = null,

    @ApiModelProperty("存在风险的包名")
    val riskyPackageKeys: Set<String>? = null,

    @ApiModelProperty("需要忽略的漏洞")
    val vulIds: Set<String>? = null,

    @ApiModelProperty("小于该等级的漏洞将被忽略")
    val severity: Int? = null,

    @ApiModelProperty("需要忽略的许可证")
    val licenseNames: Set<String>? = null,

    @ApiModelProperty("是否忽略")
    val type: Int = FILTER_RULE_TYPE_IGNORE
)
