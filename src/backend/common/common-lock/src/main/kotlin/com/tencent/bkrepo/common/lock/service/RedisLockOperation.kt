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

package com.tencent.bkrepo.common.lock.service

import com.tencent.bkrepo.common.lock.service.LockOperation.Companion.EXPIRED_TIME_IN_SECONDS
import com.tencent.bkrepo.common.redis.RedisLock
import com.tencent.bkrepo.common.redis.RedisOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
class RedisLockOperation(
    private val redisOperation: RedisOperation
) : LockOperation {
    override fun <T> getLock(lockKey: String): T {
        logger.info("Will use redis to lock the key $lockKey")
        return RedisLock(
            redisOperation = redisOperation,
            lockKey = lockKey,
            expiredTimeInSeconds = EXPIRED_TIME_IN_SECONDS
        ) as T
    }

    override fun <T> acquireLock(lockKey: String, lock: T): Boolean {
        return (lock as RedisLock).tryLock()
    }

    override fun <T> close(lockKey: String, lock: T) {
        logger.info("Will try to close redis lock for $lockKey")
        (lock as RedisLock).unlock()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RedisLockOperation::class.java)
    }
}
