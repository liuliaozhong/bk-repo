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

package com.tencent.bkrepo.repository.service.node.impl.edgeplus

import com.tencent.bkrepo.common.artifact.api.ArtifactInfo
import com.tencent.bkrepo.common.artifact.cluster.FeignClientFactory
import com.tencent.bkrepo.common.service.cluster.ClusterProperties
import com.tencent.bkrepo.common.storage.core.StorageService
import com.tencent.bkrepo.common.stream.event.supplier.MessageSupplier
import com.tencent.bkrepo.repository.api.NodeClient
import com.tencent.bkrepo.repository.config.RepositoryProperties
import com.tencent.bkrepo.repository.dao.NodeDao
import com.tencent.bkrepo.repository.dao.RepositoryDao
import com.tencent.bkrepo.repository.pojo.node.NodeDetail
import com.tencent.bkrepo.repository.pojo.node.service.NodeCreateRequest
import com.tencent.bkrepo.repository.pojo.node.service.NodeUpdateAccessDateRequest
import com.tencent.bkrepo.repository.pojo.node.service.NodeUpdateRequest
import com.tencent.bkrepo.repository.service.file.FileReferenceService
import com.tencent.bkrepo.repository.service.node.impl.base.NodeBaseService
import com.tencent.bkrepo.repository.service.repo.QuotaService
import com.tencent.bkrepo.repository.service.repo.StorageCredentialService
import org.springframework.beans.factory.annotation.Autowired

abstract class EdgePlusNodeBaseService(
    override val nodeDao: NodeDao,
    override val repositoryDao: RepositoryDao,
    override val fileReferenceService: FileReferenceService,
    override val storageCredentialService: StorageCredentialService,
    override val storageService: StorageService,
    override val quotaService: QuotaService,
    override val repositoryProperties: RepositoryProperties,
    override val messageSupplier: MessageSupplier,
) : NodeBaseService(
    nodeDao,
    repositoryDao,
    fileReferenceService,
    storageCredentialService,
    storageService,
    quotaService,
    repositoryProperties,
    messageSupplier
) {

    @Autowired
    private lateinit var clusterProperties: ClusterProperties

    val centerNodeClient: NodeClient by lazy { FeignClientFactory.create(clusterProperties.center) }

    override fun checkExist(artifact: ArtifactInfo): Boolean {
        return centerNodeClient.checkExist(
            artifact.projectId,
            artifact.repoName,
            artifact.getArtifactFullPath()
        ).data!! || super.checkExist(artifact)
    }

    override fun createNode(createRequest: NodeCreateRequest): NodeDetail {
        centerNodeClient.createNode(createRequest)
        return super.createNode(createRequest)
    }

    override fun updateNode(updateRequest: NodeUpdateRequest) {
        centerNodeClient.updateNode(updateRequest)
        super.updateNode(updateRequest)
    }

    override fun updateNodeAccessDate(updateAccessDateRequest: NodeUpdateAccessDateRequest) {
        centerNodeClient.updateNodeAccessDate(updateAccessDateRequest)
        super.updateNodeAccessDate(updateAccessDateRequest)
    }
}
