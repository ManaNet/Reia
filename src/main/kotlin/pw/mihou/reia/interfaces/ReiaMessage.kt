package pw.mihou.reia.interfaces

import org.json.JSONArray
import org.json.JSONObject

interface ReiaMessage {

    /**
     * Gets the callback that was received from the message.
     *
     * @return The callback from the message.
     */
    fun callback(): String {
        return full().getString("callback")!!
    }

    /**
     * Gets the string representation of the complete JSON message received which includes the
     * callback and message field.
     *
     * @return The string representation of the full message.
     */
    fun all(): String

    /**
     * Gets the complete [JSONObject] representation of this message which includes the callback and
     * message field.
     *
     * @return The [JSONObject] representation of [ReiaMessage.all].
     */
    fun full(): JSONObject

    /**
     * Attempts to get a [JSONObject] representation of the actual payload.
     *
     * @throws Exception
     * @return The payload from the message as a [JSONObject].
     */
    fun json(): JSONObject? {
        return full().optJSONObject("message")
    }

    /**
     * Attempts to get a [JSONArray] representation of the actual payload.
     *
     * @return The payload from the message as a [JSONArray].
     */
    fun array(): JSONArray? {
        return full().optJSONArray("message")
    }

}