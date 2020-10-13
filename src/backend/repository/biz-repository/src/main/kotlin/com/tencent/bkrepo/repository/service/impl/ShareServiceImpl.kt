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

package com.tencent.bkrepo.repository.service.impl

import com.tencent.bkrepo.common.api.constant.StringPool
import com.tencent.bkrepo.common.api.exception.ErrorCodeException
import com.tencent.bkrepo.common.api.message.CommonMessageCode
import com.tencent.bkrepo.common.artifact.api.ArtifactInfo
import com.tencent.bkrepo.common.artifact.exception.ArtifactNotFoundException
import com.tencent.bkrepo.common.artifact.message.ArtifactMessageCode
import com.tencent.bkrepo.common.artifact.repository.context.ArtifactContextHolder
import com.tencent.bkrepo.common.artifact.repository.context.ArtifactDownloadContext
import com.tencent.bkrepo.repository.model.TShareRecord
import com.tencent.bkrepo.repository.pojo.share.ShareRecordCreateRequest
import com.tencent.bkrepo.repository.pojo.share.ShareRecordInfo
import com.tencent.bkrepo.repository.service.NodeService
import com.tencent.bkrepo.repository.service.RepositoryService
import com.tencent.bkrepo.repository.service.ShareService
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 文件分享服务实现类
 */
@Service
class ShareServiceImpl(
    private val repositoryService: RepositoryService,
    private val nodeService: NodeService,
    private val mongoTemplate: MongoTemplate
) : ShareService {

    override fun create(userId: String, artifactInfo: ArtifactInfo, request: ShareRecordCreateRequest): ShareRecordInfo {
        with(artifactInfo) {
            checkNode(projectId, repoName, getArtifactFullPath())
            val shareRecord = TShareRecord(
                projectId = projectId,
                repoName = repoName,
                fullPath = getArtifactFullPath(),
                expireDate = computeExpireDate(request.expireSeconds),
                authorizedUserList = request.authorizedUserList,
                authorizedIpList = request.authorizedIpList,
                token = generateToken(),
                createdBy = userId,
                createdDate = LocalDateTime.now(),
                lastModifiedBy = userId,
                lastModifiedDate = LocalDateTime.now()
            )
            mongoTemplate.save(shareRecord)
            val shareRecordInfo = convert(shareRecord)
            logger.info("Create share record[$shareRecordInfo] success.")
            return shareRecordInfo
        }
    }

    override fun download(userId: String, token: String, artifactInfo: ArtifactInfo) {
        with(artifactInfo) {
            val query = Query.query(
                Criteria.where(TShareRecord::projectId.name).`is`(artifactInfo.projectId)
                    .and(TShareRecord::repoName.name).`is`(repoName)
                    .and(TShareRecord::fullPath.name).`is`(getArtifactFullPath())
                    .and(TShareRecord::token.name).`is`(token)
            )
            val shareRecord = mongoTemplate.findOne(query, TShareRecord::class.java) ?: throw ErrorCodeException(CommonMessageCode.RESOURCE_NOT_FOUND, token)
            if (shareRecord.authorizedUserList.isNotEmpty() && userId !in shareRecord.authorizedUserList) {
                throw ErrorCodeException(CommonMessageCode.PERMISSION_DENIED)
            }
            if (shareRecord.expireDate?.isBefore(LocalDateTime.now()) == true) {
                throw ErrorCodeException(CommonMessageCode.RESOURCE_EXPIRED, token)
            }
            val repo = repositoryService.getRepoDetail(projectId, repoName) ?: throw ErrorCodeException(ArtifactMessageCode.REPOSITORY_NOT_FOUND, repoName)
            val context = ArtifactDownloadContext(repo)
            val repository = ArtifactContextHolder.getRepository(context.repositoryDetail.category)
            repository.download(context)
        }
    }

    override fun list(projectId: String, repoName: String, fullPath: String): List<ShareRecordInfo> {
        val query = Query.query(
            Criteria.where(TShareRecord::projectId.name).`is`(projectId)
                .and(TShareRecord::repoName.name).`is`(repoName)
                .and(TShareRecord::fullPath.name).`is`(fullPath)
        )
        return mongoTemplate.find(query, TShareRecord::class.java).map { convert(it) }
    }

    private fun checkNode(projectId: String, repoName: String, fullPath: String) {
        val node = nodeService.detail(projectId, repoName, fullPath)
        if (node == null || node.folder) {
            throw ArtifactNotFoundException("Artifact[$fullPath] not found")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ShareServiceImpl::class.java)

        private fun generateToken(): String {
            return UUID.randomUUID().toString().replace(StringPool.DASH, StringPool.EMPTY).toLowerCase()
        }

        private fun generateShareUrl(shareRecord: TShareRecord): String {
            return "/api/share/${shareRecord.projectId}/${shareRecord.repoName}${shareRecord.fullPath}?token=${shareRecord.token}"
        }

        private fun computeExpireDate(expireSeconds: Long?): LocalDateTime? {
            return if (expireSeconds == null || expireSeconds <= 0) null
            else LocalDateTime.now().plusSeconds(expireSeconds)
        }

        private fun convert(tShareRecord: TShareRecord): ShareRecordInfo {
            return tShareRecord.let {
                ShareRecordInfo(
                    fullPath = it.fullPath,
                    repoName = it.repoName,
                    projectId = it.projectId,
                    shareUrl = generateShareUrl(it),
                    authorizedUserList = it.authorizedUserList,
                    authorizedIpList = it.authorizedIpList,
                    expireDate = it.expireDate?.format(DateTimeFormatter.ISO_DATE_TIME)
                )
            }
        }
    }
}
