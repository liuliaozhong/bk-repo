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

package com.tencent.bkrepo.docker.artifact

import com.tencent.bkrepo.common.artifact.util.PackageKeys
import com.tencent.bkrepo.docker.context.RequestContext
import com.tencent.bkrepo.repository.api.PackageClient
import com.tencent.bkrepo.repository.api.PackageDownloadStatisticsClient
import com.tencent.bkrepo.repository.pojo.download.service.DownloadStatisticsAddRequest
import com.tencent.bkrepo.repository.pojo.packages.PackageVersion
import com.tencent.bkrepo.repository.pojo.packages.request.PackageVersionCreateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DockerPackageRepo @Autowired constructor(
    private val packageClient: PackageClient,
    private val packageDownloadStatisticsClient: PackageDownloadStatisticsClient
) {

    /**
     * check is the node is exist
     * @param request the request to create version
     * @return Boolean is the package version create success
     */
    fun createVersion(request: PackageVersionCreateRequest): Boolean {
        return packageClient.createVersion(request).isOk()
    }

    /**
     * check is the node is exist
     * @param context  the request context
     * @return Boolean is the package version delete success
     */
    fun deletePackage(context: RequestContext): Boolean {
        with(context) {
            return packageClient.deletePackage(projectId, repoName, PackageKeys.ofDocker(artifactName)).isOk()
        }
    }

    /**
     * check is the node is exist
     * @param context  the request context
     * @param version package version
     * @return Boolean is the package version exist
     */
    fun deletePackageVersion(context: RequestContext, version: String): Boolean {
        with(context) {
            return packageClient.deleteVersion(projectId, repoName, PackageKeys.ofDocker(artifactName), version).isOk()
        }
    }

    /**
     * check is the node is exist
     * @param context  the request context
     * @param version package version
     * @return PackageVersion the package version detail
     */
    fun getPackageVersion(context: RequestContext, version: String): PackageVersion? {
        with(context) {
            return packageClient.findVersionByName(projectId, repoName, PackageKeys.ofDocker(artifactName), version).data
        }
    }

    /**
     * check is the node is exist
     * @param context  the request context
     * @param version package version
     * @return Boolean is add download static success
     */
    fun addDownloadStatic(context: RequestContext, version: String): Boolean {
        with(context) {
            val request = DownloadStatisticsAddRequest(
                projectId,
                repoName,
                PackageKeys.ofDocker(artifactName),
                artifactName,
                version
            )
            return packageDownloadStatisticsClient.add(request).isOk()
        }
    }
}