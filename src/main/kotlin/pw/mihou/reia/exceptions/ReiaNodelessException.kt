package pw.mihou.reia.exceptions

class ReiaNodelessException(channel: String, message: String): Exception("There were no nodes connected to receive the message on $channel. [message=$message]")