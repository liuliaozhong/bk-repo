package com.tencent.bkrepo.common.artifact.auth

import com.tencent.bkrepo.common.api.constant.ANONYMOUS_USER
import com.tencent.bkrepo.common.api.constant.USER_KEY
import com.tencent.bkrepo.common.artifact.exception.ClientAuthException
import com.tencent.bkrepo.common.artifact.permission.AuthProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 依赖源客户端认证拦截器
 *
 * @author: carrypan
 * @date: 2019/11/22
 */
class ClientAuthInterceptor : HandlerInterceptorAdapter() {

    @Autowired
    private lateinit var clientAuthHandlerList: List<ClientAuthHandler>

    @Autowired
    private lateinit var authProperties: AuthProperties

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!authProperties.enabled) {
            logger.debug("Auth disabled, set anonymous user.")
            request.setAttribute(USER_KEY, ANONYMOUS_USER)
            return true
        }
        clientAuthHandlerList.forEach { authHandler ->
            try {
                val authCredentials = authHandler.extractAuthCredentials(request)
                if (authCredentials !is AnonymousCredentials) {
                    val userId = authHandler.onAuthenticate(request, authCredentials)
                    logger.debug("User[$userId] authenticate success by ${authHandler.javaClass.simpleName}.")
                    authHandler.onAuthenticateSuccess(userId, request)
                    request.setAttribute(USER_KEY, userId)
                    return true
                }
            } catch (clientAuthException: ClientAuthException) {
                authHandler.onAuthenticateFailed(response, clientAuthException)
                return false
            }
        }
        // 没有合适的认证handler或为匿名用户
        logger.debug("None ClientAuthHandler authenticate success, set anonymous user.")
        request.setAttribute(USER_KEY, ANONYMOUS_USER)
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClientAuthInterceptor::class.java)
    }
}
