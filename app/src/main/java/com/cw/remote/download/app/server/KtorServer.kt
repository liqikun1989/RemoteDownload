package com.cw.remote.download.app.server

import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import java.io.File
import java.net.URLEncoder

object KtorServer {
    private val server by lazy {
        embeddedServer(Netty, 8080) {
            routing {
                get("/download") {
//                    val path = call.parameters["path"]
                    call.request.queryParameters["path"]?.apply {
                        val file = File(this)
                        if (file.exists()) {
                            val fileName = URLEncoder.encode(file.name, "UTF-8")
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, fileName).toString()
                            )
                            call.respondFile(file, configure = {
                                this.setProperty(AttributeKey("Content-Disposition"), "attachment; filename=\"$fileName\"")
                            })
                        } else {
                            call.respondText("找不到你要下载的文件")
                        }
                    }
                }
            }
        }
    }

    /** 启动服务器 */
    fun start() {
        server.start(true)
    }

    /** 停止服务器 */
    fun stop() {
        server.stop(1_000, 2_000)
    }
}