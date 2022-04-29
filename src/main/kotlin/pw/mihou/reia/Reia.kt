package pw.mihou.reia

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import pw.mihou.reia.core.ReiaInternalCore
import pw.mihou.reia.core.settings.ReiaSettings
import pw.mihou.reia.interfaces.*
import pw.mihou.reia.models.ReiaGeneralMessage
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface Reia {

    companion object {

        /**
         * Creates a new [Reia] instance with the given specifications.
         *
         * @param client The [RedisClient] to use to connect to Redis.
         * @param settings The settings to use for Reia.
         * @return A new instance of [Reia] that matches the given specifications.
         */
        fun new(client: RedisClient, settings: (ReiaSettings) -> ReiaSettings): Reia {
            return ReiaInternalCore(client, settings.invoke(ReiaSettings()))
        }
    }

    /**
     * Attaches a listener onto the given channel for publishing. A channel can only have one publisher at a
     * time which means this will override any existing listeners that are attached to the channel.
     *
     * @param channel The channel to attach the listener onto.
     * @param listener The listener to attach to the channel.
     */
    fun producer(channel: String, listener: ReiaProducer)

    /**
     * Sends a message while expecting a response, this has a default TTL (time-to-live) of one minute before
     * being cancelled with an error.
     *
     * @param channel The channel to send the messages to, no need to append ".requests".
     * @param message The message to send to the given channel.
     *
     * @return The response from the producer in that given channel if any.
     */
    fun send(channel: String, message: ReiaGeneralMessage): CompletableFuture<ReiaMessage> {
        return send(channel, Duration.ofMinutes(1), message)
    }

    /**
     * Sends a message while expecting a response with a customized time-to-live time.
     *
     * @param channel The channel to send the messages to, no need to append ".requests".
     * @param ttl     The time-to-live duration before this request will be cancelled.
     * @param message The message to send to the given channel.
     *
     * @return The response from the producer in that given channel if any.
     */
    fun send(channel: String, ttl: Duration, message: ReiaGeneralMessage): CompletableFuture<ReiaMessage>

    /**
     * Sends a message without expecting a response, unlike [Reia.publish], this appends ".requests" at the
     * end of the channel which is the standard for Reia to identify which comes from a consumer and a publisher.
     *
     * @param channel The channel to send the messages to.
     * @param message The message to send to the given channel.
     * @return A Redis future with the total nodes that received the message, this can be zero if clustering.
     */
    fun sendWithoutResponse(channel: String, message: String): RedisFuture<Long>

    /**
     * Sends a message without expecting a response, unlike [Reia.sendWithoutResponse], this does not append ".requests" at
     * the end of the channel and is simply a short-hand expression of the publisher's publish method.
     *
     * @param channel The channel to send the messages to.
     * @param message The message to send to the given channel.
     * @return A Redis future with the total nodes that received the message, this can be zero if clustering.
     */
    fun publish(channel: String, message: String): RedisFuture<Long>

    /**
     * Connects all the connections to the Redis client and subscribes to all the necessary channels that publishers needs
     * to listen onto.
     */
    fun listen()

}