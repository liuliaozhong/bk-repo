--[[
Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.

Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.

BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.

A copy of the MIT License is included in this file.


Terms of the MIT License:
---------------------------------------------------
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
]]

config = {
  env = "__BKCI_ENV__",
  static_dir = "__INSTALL_PATH__/__MODULE__",
  http_schema = "__HTTP_SCHEMA__", -- 蓝鲸PaaS平台访问协议 http or https, 如果有对接才配置修改，开源默认没对接
  login_url = "__PAAS_LOGIN_URL__",   -- 蓝鲸PaaS平台域名, 如果有对接才配置修改，开源默认没对接
  service_name = "",  -- 指定后台微服务名称，如果对接后端是boot-assembly的单体微服务，则该配置项为bk-ci, 否则请置空会自动路由相应微服务
  allow_hosts = {
    __BKCI_ALLOW_HOST__
  },
  allow_headers = "Authorization,Content-Type,withcredentials,credentials,Accept,Origin,User-Agent,Cache-Control,Keep-Alive,X-Requested-With,If-Modified-Since,X-CSRFToken,X-DEVOPS-PROJECT-ID,X-DEVOPS-TASK-ID,X-BKREPO-UID,X-BKREPO-API-TYPE",
  ns = {
    ip = {
      "127.0.0.1"
    },
    port = __BKCI_CONSUL_DNS_PORT__,
    http_port = __BKCI_CONSUL_PORT__,
    domain = "__BKCI_CONSUL_DOMAIN__",
    tag = "__BKCI_CONSUL_TAG__",
    nodes_url = "/v1/catalog/nodes"
  },
  oauth = {  -- 对接蓝鲸权限中心才需要的配置
    ip = "__IAM_IP0__",
    env = "__IAM_ENV__",
    port = "__IAM_HTTP_PORT__",
    host = "__IAM_HOST__",
    url = "__IAM_TOKEN_URL__",     -- 接口路径
    app_code = "__APP_CODE__",
    app_secret = "__APP_TOKEN__",
  },
  itlogin = {
    ip = "__BKCI_ITLOGIN_IP__",
    port = "__BKCI_ITLOGIN_PORT__",
    host = "__BKCI_ITLOGIN_HOST__"
  },
  bkrepo = {
    domain = "__BKREPO_HOST__",
    authorization = "__BKRPO_AUTHORIZATION__"
  },
  apigwUrl = "__BKCI_APIGW_URL__"
}
  
require("init_common")
require("ip_whitelist")