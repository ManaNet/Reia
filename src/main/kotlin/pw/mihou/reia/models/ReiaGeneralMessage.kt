package pw.mihou.reia.models

import org.json.JSONArray
import org.json.JSONObject
import pw.mihou.reia.interfaces.ReiaMessage

class ReiaGeneralMessage: ReiaMessage {

    private val message: JSONObject

    constructor(message: String, callback: String?) {
        this.message = JSONObject().put("message", message).put("callback", callback)
    }

    constructor(message: Any, callback: String?) {
        this.message = JSONObject().put("message", message).put("callback", callback)
    }

    constructor(message: JSONObject, callback: String?) {
        this.message = JSONObject().put("message", message).put("callback", callback)
    }

    constructor(message: JSONArray, callback: String?) {
        this.message = JSONObject().put("message", message).put("callback", callback)
    }

    override fun all(): String {
        return message.toString()
    }

    override fun full(): JSONObject {
        return message
    }

}