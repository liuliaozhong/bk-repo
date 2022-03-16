/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2022 THL A29 Limited, a Tencent company.  All rights reserved.
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

package com.tencent.bkrepo.scanner.dao

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.tencent.bkrepo.common.mongo.dao.simple.SimpleMongoDao
import com.tencent.bkrepo.scanner.model.TSubScanTask
import com.tencent.bkrepo.common.scanner.pojo.scanner.SubScanTaskStatus
import com.tencent.bkrepo.scanner.pojo.request.CredentialsKeyFiles
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.lt
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class SubScanTaskDao : SimpleMongoDao<TSubScanTask>() {

    fun findByCredentialsKeyAndSha256List(credentialsKeyFiles: List<CredentialsKeyFiles>): List<TSubScanTask> {
        val criteria = Criteria()
        credentialsKeyFiles.forEach {
            criteria.orOperator(
                Criteria
                    .where(TSubScanTask::credentialsKey.name).isEqualTo(it.credentialsKey)
                    .and(TSubScanTask::sha256.name).`in`(it.sha256List)
            )
        }
        return find(Query(criteria))
    }

    fun findByCredentialsAndSha256(credentialsKey: String?, sha256: String): TSubScanTask? {
        val query = Query(
            TSubScanTask::credentialsKey.isEqualTo(credentialsKey).and(TSubScanTask::sha256.name).isEqualTo(sha256)
        )
        return findOne(query)
    }

    fun deleteById(subTaskId: String): DeleteResult {
        val query = Query(Criteria.where(ID).isEqualTo(subTaskId))
        return remove(query)
    }

    /**
     * 更新任务状态
     *
     * @param subTaskId 待更新的子任务id
     * @param status 更新后的任务状态
     * @param lastModifiedDate 最后更新时间，用于充当乐观锁，只有最后修改时间匹配时候才更新
     *
     * @return 更新结果
     */
    fun updateStatus(
        subTaskId: String,
        status: SubScanTaskStatus,
        lastModifiedDate: LocalDateTime? = null
    ): UpdateResult {
        val criteria = Criteria.where(ID).isEqualTo(subTaskId)
        if (lastModifiedDate != null) {
            criteria.and(TSubScanTask::lastModifiedDate.name).isEqualTo(lastModifiedDate)
        }
        val query = Query(criteria)
        val update = Update()
            .set(TSubScanTask::lastModifiedDate.name, LocalDateTime.now())
            .set(TSubScanTask::status.name, status.name)
        if (status == SubScanTaskStatus.EXECUTING) {
            update.inc(TSubScanTask::executedTimes.name, 1)
        }
        return updateFirst(query, update)
    }

    fun updateStatus(
        subTaskIds: List<String>,
        status: SubScanTaskStatus
    ): UpdateResult {
        val query = Query(Criteria.where(ID).`in`(subTaskIds))
        val update = Update()
            .set(TSubScanTask::lastModifiedDate.name, LocalDateTime.now())
            .set(TSubScanTask::status.name, status.name)
        return updateFirst(query, update)
    }

    fun firstTaskByStatusIn(status: List<String>): TSubScanTask? {
        val query = Query(TSubScanTask::status.inValues(status))
        return findOne(query)
    }

    /**
     * 获取一个执行超时的任务
     *
     * @param timeoutSeconds 允许执行的最长时间
     */
    fun firstTimeoutTask(timeoutSeconds: Long): TSubScanTask? {
        val beforeDate = LocalDateTime.now().minusSeconds(timeoutSeconds)
        val criteria = TSubScanTask::lastModifiedDate.lt(beforeDate)
        return findOne(Query(criteria))
    }
}