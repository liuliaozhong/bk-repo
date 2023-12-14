/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tencent.bkrepo.s3.artifact.configuration

import com.tencent.bkrepo.common.artifact.config.ArtifactConfigurerSupport
import com.tencent.bkrepo.common.artifact.pojo.RepositoryType
import com.tencent.bkrepo.common.security.http.core.HttpAuthSecurityCustomizer
import com.tencent.bkrepo.common.service.util.SpringContextUtils
import com.tencent.bkrepo.s3.artifact.S3LocalRepository
import com.tencent.bkrepo.s3.artifact.S3RemoteRepository
import com.tencent.bkrepo.s3.artifact.S3VirtualRepository
import com.tencent.bkrepo.s3.artifact.auth.AWS4AuthHandler
import org.springframework.context.annotation.Configuration

@Configuration
class S3RegistryArtifactConfigurer : ArtifactConfigurerSupport() {

    override fun getRepositoryType() = RepositoryType.S3
    override fun getRepositoryTypes(): List<RepositoryType> {
        return mutableListOf(RepositoryType.GENERIC)
    }
    override fun getLocalRepository() = SpringContextUtils.getBean<S3LocalRepository>()
    override fun getRemoteRepository() = SpringContextUtils.getBean<S3RemoteRepository>()
    override fun getVirtualRepository() = SpringContextUtils.getBean<S3VirtualRepository>()

    override fun getAuthSecurityCustomizer(): HttpAuthSecurityCustomizer =
        HttpAuthSecurityCustomizer { httpAuthSecurity ->
            val authenticationManager = httpAuthSecurity.authenticationManager!!
            val ociLoginAuthHandler = AWS4AuthHandler(authenticationManager)
            httpAuthSecurity.withPrefix("/s3").addHttpAuthHandler(ociLoginAuthHandler)
        }

}
