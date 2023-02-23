package com.tencent.bkrepo.common.service.cluster

import com.tencent.bkrepo.common.api.pojo.ClusterNodeType
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.scheduling.annotation.Scheduled

@Aspect
class CenterJobAspect(
    private val clusterProperties: ClusterProperties
) {
    @Around("@annotation(com.tencent.bkrepo.common.service.cluster.CenterJob)")
    @Throws(Throwable::class)
    fun around(point: ProceedingJoinPoint): Any? {
        val signature = point.signature as MethodSignature
        val method = signature.method
        val scheduled = method.getAnnotation(Scheduled::class.java)
        if (scheduled != null && clusterProperties.role != ClusterNodeType.CENTER) {
            return null
        }
        return point.proceed()
    }
}
