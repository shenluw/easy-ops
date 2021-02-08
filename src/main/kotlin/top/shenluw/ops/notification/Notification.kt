package top.shenluw.ops.notification

/**
 * @author Shenluw
 * created: 2021/2/8 16:09
 */
interface Notification {
	fun send(message: Message)
}

data class Message(val subject: String, val message: String)