package pw.mihou.reia.core

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.mihou.reia.Reia
import pw.mihou.reia.core.models.ReiaCallback
import pw.mihou.reia.core.routers.ReiaInternalMasterListenerCore
import pw.mihou.reia.core.settings.ReiaSettings
import pw.mihou.reia.exceptions.ReiaNodelessException
import pw.mihou.reia.interfaces.*
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong

class ReiaInternalCore(
    private val client: RedisClient,
    val settings: ReiaSettings
): Reia {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Reia::class.java)
    }

    val publishers: MutableMap<String, ReiaProducer> = mutableMapOf()
    private val channels: MutableList<String> = mutableListOf()

    var publisher: RedisPubSubAsyncCommands<String, String>? = null
    private var publisherConsumer: RedisPubSubAsyncCommands<String, String>? = null
    private var consumer: RedisPubSubAsyncCommands<String, String>? = null

    val callbacks: MutableMap<String, ReiaCallback> = mutableMapOf()
    private val increment: AtomicLong = AtomicLong(0)

    private fun sub(channel: String): CompletableFuture<Void> {
        if (channels.contains(channel)) return CompletableFuture.completedFuture(null)

        channels.add(channel)
        return consumer!!.subscribe("$channel.consumer").exceptionally {
            channels.remove(channel)
            settings.failedSubscribe.invoke(channel, it)

            return@exceptionally null
        }.toCompletableFuture()
    }

    private fun add(channel: String, listener: ReiaProducer) {
        publishers["$channel.requests"] = listener
        publisher?.subscribe("$channel.requests")
    }

    override fun producer(channel: String, listener: ReiaProducer) {
        add(channel, listener)
    }

    override fun send(channel: String, ttl: Duration, message: ReiaMessage): CompletableFuture<ReiaMessage> {
        val future: CompletableFuture<ReiaMessage> = CompletableFuture()

        sub("$channel.consumer").thenAccept {
            val designation = designate()

            callbacks[designation] = ReiaCallback(future = future, ttl = ttl.toMillis())

            publisher!!.publish("$channel.requests", message.all()).thenAccept { nodesReceived ->
                if (nodesReceived == 0L && !settings.clustering) {
                    future.completeExceptionally(ReiaNodelessException(channel, message.all()))
                    callbacks.remove(designation)
                }
            }.exceptionally {
                future.completeExceptionally(it)
                callbacks.remove(designation)

                return@exceptionally null
            }
        }

        return future
    }

    override fun sendWithoutResponse(channel: String, message: String): RedisFuture<Long> {
        return publisher!!.publish("$channel.requests", message)
    }

    override fun publish(channel: String, message: String): RedisFuture<Long> {
        return publisher!!.publish(channel, message)
    }

    private fun incrementOrMax(): Long {
        return increment.updateAndGet {
            if (it == Long.MAX_VALUE) 0 else it + 1
        }
    }

    private fun designate(): String {
        val padding = settings.callbackPrefix.substring(ThreadLocalRandom.current().nextInt(settings.callbackPrefix.length))

        return "${settings.callbackPrefix}.${padding}.${incrementOrMax()}"
    }

    override fun listen() {
        publisher = client.connectPubSub().async()
        publisherConsumer = client.connectPubSub().async()
        consumer = client.connectPubSub().async()

        publisherConsumer!!.subscribe(*publishers.keys.map { "$it.requests" }.toTypedArray())
        publisherConsumer!!.statefulConnection.addListener(ReiaInternalMasterListenerCore(this, false))
        consumer!!.statefulConnection.addListener(ReiaInternalMasterListenerCore(this, true))

        settings.scheduler.scheduleAtFixedRate(this::clean, 10, 10, TimeUnit.SECONDS)
    }

    private fun clean() {
        if (callbacks.isEmpty()) return
        for (callback in callbacks) {
            if (callback.value.creation + callback.value.ttl < System.currentTimeMillis()) {
                callback.value.future.completeExceptionally(TimeoutException("The client failed to answer this message within the specified amount of time."))
                callbacks.remove(callback.key)
            }
        }
    }


}