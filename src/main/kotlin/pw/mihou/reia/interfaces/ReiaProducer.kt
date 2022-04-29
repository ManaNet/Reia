package pw.mihou.reia.interfaces

import pw.mihou.reia.models.ReiaGeneralMessage

interface ReiaProducer {

    fun onMessage(channel: String, message: ReiaMessage): ReiaGeneralMessage?

}