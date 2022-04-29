package pw.mihou.reia.core.models

import pw.mihou.reia.interfaces.ReiaMessage
import java.time.Duration
import java.util.concurrent.CompletableFuture

data class ReiaCallback(
    val future: CompletableFuture<ReiaMessage>,
    val creation: Long = System.currentTimeMillis(),
    val ttl: Long = Duration.ofMinutes(1).toMillis()
)