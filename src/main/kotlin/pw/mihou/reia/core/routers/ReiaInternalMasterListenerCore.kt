package pw.mihou.reia.core.routers

import io.lettuce.core.pubsub.RedisPubSubListener
import org.json.JSONObject
import pw.mihou.reia.core.ReiaInternalCore
import pw.mihou.reia.core.message.ReiaMessageCore
import pw.mihou.reia.interfaces.ReiaMessage
import java.util.concurrent.CompletableFuture

class ReiaInternalMasterListenerCore(
    private val reia: ReiaInternalCore, private val isConsumer: Boolean
): RedisPubSubListener<String, String> {

    private fun propogate(channel: String, message: ReiaMessage): CompletableFuture<ReiaMessage?> {
        if (isConsumer) {

            if (reia.callbacks[message.callback()] == null) {
                reia.settings.noMatches.invoke(channel, message)
                return CompletableFuture.completedFuture(null)
            }

            reia.callbacks[message.callback()]!!.future.complete(message)
            return CompletableFuture.completedFuture(null)
        }

        if (reia.publishers[channel] != null) {
            return CompletableFuture.supplyAsync(
                { reia.publishers[channel]?.onMessage(channel, message) },
                reia.settings.executorService
            )
        }

        reia.settings.noListener.invoke(channel, message)
        return CompletableFuture.completedFuture(null)
    }

    override fun message(channel: String, message: String) {
        try {
            val context = ReiaMessageCore(message, JSONObject(message))

            if (!context.full().has("callback") || context.full().get("callback") !is String) {
                reia.settings.noCallback.invoke(channel, context)
                return
            }

            if (!context.json()!!.has("message")) {
                reia.settings.invalidMessage.invoke(channel, message)
                return
            }

            propogate(channel, context).thenAccept {
                it ?: return@thenAccept

                reia.publisher!!.publish("${channel.removeSuffix(".requests")}.consumer", it.all())
            }
        } catch (exception: Exception) {
            reia.settings.invalidMessage.invoke(channel, message)
        }
    }

    override fun message(pattern: String, channel: String, message: String) {
        message(channel, message)
    }

    override fun subscribed(channel: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun psubscribed(pattern: String, count: Long) {
        subscribed(pattern, count)
    }

    override fun unsubscribed(channel: String, count: Long) {
        TODO("Not yet implemented")
    }

    override fun punsubscribed(pattern: String, count: Long) {
        unsubscribed(pattern, count)
    }

}