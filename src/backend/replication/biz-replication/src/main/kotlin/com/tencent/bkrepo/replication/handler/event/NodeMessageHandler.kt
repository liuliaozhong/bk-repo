package com.tencent.bkrepo.replication.handler.event

import com.tencent.bkrepo.common.stream.message.node.NodeCopiedMessage
import com.tencent.bkrepo.common.stream.message.node.NodeCreatedMessage
import com.tencent.bkrepo.common.stream.message.node.NodeDeletedMessage
import com.tencent.bkrepo.common.stream.message.node.NodeMovedMessage
import com.tencent.bkrepo.common.stream.message.node.NodeRenamedMessage
import com.tencent.bkrepo.common.stream.message.node.NodeUpdatedMessage
import com.tencent.bkrepo.replication.job.ReplicationContext
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * handler node message and replicate
 * include create ,copy ,rename,move
 * @author: owenlxu
 * @date: 2020/05/20
 */
@Component
class NodeMessageHandler : AbstractMessageHandler() {

    @EventListener(NodeCreatedMessage::class)
    fun handle(message: NodeCreatedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val remoteProjectId = getRemoteProjectId(it, projectId)
                val remoteRepoName = getRemoteRepoName(it, repoName)
                var context = ReplicationContext(it)

                context.currentRepoDetail = getRepoDetail(projectId, repoName, remoteRepoName) ?: run {
                    logger.warn("found no repo detail [$projectId, $repoName]")
                    return
                }
                this.copy(
                    projectId = remoteProjectId,
                    repoName = remoteRepoName
                ).apply { replicationService.replicaNodeCreateRequest(context, this) }
            }
        }
    }

    @EventListener(NodeRenamedMessage::class)
    fun handle(message: NodeRenamedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val context = ReplicationContext(it)
                this.copy(
                    projectId = getRemoteProjectId(it, projectId),
                    repoName = getRemoteRepoName(it, repoName)
                ).apply { replicationService.replicaNodeRenameRequest(context, this) }
            }
        }
    }

    @EventListener(NodeUpdatedMessage::class)
    fun handle(message: NodeUpdatedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val context = ReplicationContext(it)
                this.copy(
                    projectId = getRemoteProjectId(it, projectId),
                    repoName = getRemoteRepoName(it, repoName)
                ).apply { replicationService.replicaNodeUpdateRequest(context, this) }
            }
        }
    }

    @EventListener(NodeCopiedMessage::class)
    fun handle(message: NodeCopiedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val context = ReplicationContext(it)
                this.copy(
                    srcProjectId = getRemoteProjectId(it, projectId),
                    srcRepoName = getRemoteRepoName(it, repoName)
                ).apply { replicationService.replicaNodeCopyRequest(context, this) }
            }
        }
    }

    @EventListener(NodeMovedMessage::class)
    fun handle(message: NodeMovedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val context = ReplicationContext(it)
                context.currentProjectDetail
                this.copy(
                    srcProjectId = getRemoteProjectId(it, projectId),
                    srcRepoName = getRemoteRepoName(it, repoName)
                ).apply { replicationService.replicaNodeMoveRequest(context, this) }
            }
        }
    }

    @EventListener(NodeDeletedMessage::class)
    fun handle(message: NodeDeletedMessage) {
        with(message.request) {
            getRelativeTaskList(projectId, repoName).forEach {
                val context = ReplicationContext(it)
                this.copy(
                    projectId = getRemoteProjectId(it, projectId),
                    repoName = getRemoteRepoName(it, repoName)
                ).apply { replicationService.replicaNodeDeleteRequest(context, this) }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NodeMessageHandler::class.java)
    }
}
