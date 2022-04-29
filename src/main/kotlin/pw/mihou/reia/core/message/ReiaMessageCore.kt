package pw.mihou.reia.core.message

import org.json.JSONObject
import pw.mihou.reia.interfaces.ReiaMessage

data class ReiaMessageCore(private val message: String, private val representation: JSONObject): ReiaMessage {

    override fun all(): String {
        return message
    }

    override fun full(): JSONObject {
        return representation
    }

    override fun toString(): String {
        return message
    }

}
