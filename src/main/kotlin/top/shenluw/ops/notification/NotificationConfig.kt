package top.shenluw.ops.notification

import kotlinx.serialization.Serializable

/**
 * @author Shenluw
 * created: 2021/2/8 17:10
 */
@Serializable
data class NotificationConfig(
	val enable: Boolean = true,
	val email: Map<String, EmailConfig>? = null
)

@Serializable
data class EmailConfig(
	val username: String,
	val password: String,
	val smtpHost: String,
	val smtpPort: Int? = null,

	/**
	 * 邮件目的地
	 */
	val toEmails: List<String>
)
