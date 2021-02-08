package top.shenluw.ops.notification

import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.OpsException

/**
 * @author Shenluw
 * created: 2021/2/8 16:11
 */
class NotificationManager(private val config: NotificationConfig) : KSlf4jLogger {

	private val notifications = mutableListOf<Notification>()

	init {
		checkConfig()

		config.email?.forEach {
			notifications.add(EMailNotification(it))
		}
	}

	private fun checkConfig() {
		if (config.email.isNullOrEmpty()) {
			return
		}
		config.email?.forEach {
			if (it.smtpHost.isBlank()) {
				throw OpsException("email smtp 配置为空")
			}
			if (it.username.isBlank()) {
				throw OpsException("email username 配置为空")
			}
			if (it.password.isBlank()) {
				throw OpsException("email password 配置为空")
			}
			if (it.toEmails.isNullOrEmpty()) {
				throw OpsException("email 收件人配置为空")
			}
		}
	}

	fun send(message: Message) {
		notifications.forEach {
			try {
				it.send(message)
			} catch (e: Exception) {
				log.error("消息发送失败 {}", message, e)
			}
		}
	}

}