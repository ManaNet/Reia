package pw.mihou.reia.core.settings

import pw.mihou.reia.core.ReiaInternalCore
import pw.mihou.reia.interfaces.ReiaMessage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ReiaSettings {

    /**
     * Checks whether the Redis client is in clustering mode or not, this is important to determine whether to perform
     * nodes received checks or not when sending a message.
     */
    val clustering: Boolean = false

    /**
     * An important section of the callback that should be unique to the application or node, this will also be used to add
     * additional padding to ensure that the callback is unique at the time.
     */
    var callbackPrefix: String = "PLEASE-CHANGE-THIS"

    /**
     * Executed when [pw.mihou.reia.Reia] fails to subscribe to a specific channel.
     */
    var failedSubscribe: (channel: String, throwable: Throwable) -> Unit = { channel, throwable ->
        ReiaInternalCore.logger.error("Reia failed to subscribe to $channel with the following error.", throwable)
    }

    /**
     * Executed when [pw.mihou.reia.Reia] receives a message that doesn't follow the message-callback JSON format that
     * Reia uses to identify and parse messages.
     */
    var invalidMessage: (channel: String, message: String) -> Unit = { channel, message ->
        ReiaInternalCore.logger.warn("A message was received from $channel without following the format, this was ignored. [message=$message]")
    }

    /**
     * Executed when Reia has no publishers listening for a specific channel but a message was received at that channel.
     */
    var noListener: (channel: String, message: ReiaMessage) -> Unit = { channel, message ->
        ReiaInternalCore.logger.warn("A message was received from $channel without any listeners, this was ignored. [message=$message]")
    }

    /**
     * Executed when Reia receives a response message to a callback but the callback doesn't exist anymore.
     */
    var noMatches: (channel: String, message: ReiaMessage) -> Unit = { channel, message ->
        ReiaInternalCore.logger.warn("A message was received from $channel but there were no requests that matches, this was ignored. [message=$message]")
    }

    /**
     * Executed when Reia receives a request message but the message doesn't follow the message-callback format.
     */
    var noCallback: (channel: String, message: ReiaMessage) -> Unit = { channel, message ->
        ReiaInternalCore.logger.warn("A message was received from $channel without any callbacks, this was ignored. [message=$message]")
    }

    /**
     * This is used by Reia to propogate the handling from [pw.mihou.reia.interfaces.ReiaProducer] onto their own threads to prevent delaying
     * the event loop of Lettuce.
     */
    var executorService: ExecutorService = Executors.newCachedThreadPool()

    /**
     * This is used by Reia for cleaning up callbacks that have reached their TTL (time-to-live), this shouldn't be used at all for any other
     * purpose as it could block the cleaning procedures.
     */
    var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
}
