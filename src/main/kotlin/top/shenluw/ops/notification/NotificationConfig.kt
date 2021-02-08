package top.shenluw.ops.notification

/**
 * @author Shenluw
 * created: 2021/2/8 17:10
 */
class NotificationConfig {
	var enable = true
	var email: List<EmailConfig>? = null
}

class EmailConfig {
	lateinit var username: String
	lateinit var password: String
	lateinit var smtpHost: String
	var smtpPort: Int? = null

	/**
	 * 邮件目的地
	 */
	lateinit var toEmails: List<String>

}