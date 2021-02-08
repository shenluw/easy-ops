package top.shenluw.ops.notification

import java.nio.charset.StandardCharsets
import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Shenluw
 * created: 2021/2/8 19:24
 */
class EMailNotification(private val config: EmailConfig) : Notification {
	private var session: Session

	init {
		val properties = Properties()
		properties["mail.smtp.host"] = config.smtpHost
		config.smtpPort?.apply {
			properties["mail.smtp.port"] = this
		}
		properties["mail.smtp.auth"] = "true"
		properties["mail.smtp.starttls.enable"] = "true"

		session = Session.getInstance(properties, LoginAuthenticator(config.username, config.password))
	}

	override fun send(message: Message) {
		val msg = MimeMessage(session)
		msg.setFrom(InternetAddress(config.username))
		config.toEmails.forEach {
			msg.addRecipients(javax.mail.Message.RecipientType.TO, it)
		}

		msg.subject = message.subject
		msg.setText(message.message, StandardCharsets.UTF_8.name())

		Transport.send(msg)
	}

	private class LoginAuthenticator constructor(var username: String, var password: String) : Authenticator() {
		public override fun getPasswordAuthentication(): PasswordAuthentication {
			return PasswordAuthentication(username, password)
		}
	}
}