package com.tencent.bkrepo.docker.resource


import org.springframework.web.bind.annotation.RestController
import com.tencent.bkrepo.docker.api.Base
import org.springframework.http.ResponseEntity

@RestController
class BaseImpl :Base{
    override  fun ping(): ResponseEntity<Any> {
        return ResponseEntity.ok().header("Content-Type","application/json").header("Docker-Distribution-Api-Version", "registry/2.0").body("{}")
    }
}
