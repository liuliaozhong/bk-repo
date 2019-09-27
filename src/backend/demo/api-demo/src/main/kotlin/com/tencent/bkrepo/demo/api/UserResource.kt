package com.tencent.bkrepo.demo.api

import com.tencent.bkrepo.demo.constant.SERVICE_NAME
import com.tencent.bkrepo.demo.pojo.User
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Api("测试接口")
@FeignClient(SERVICE_NAME, contextId = "userResource")
interface UserResource {

    @ApiOperation("get接口测试")
    @GetMapping("/hello")
    fun sayHello(
        @RequestParam
        @ApiParam(value = "姓名", required = true)
        name: String
    ): String

    @ApiOperation("post接口测试")
    @PostMapping("/hello")
    fun sayHello(
        @RequestBody
        @ApiParam(value = "用户信息", required = true)
        user: User
    ): String
}
