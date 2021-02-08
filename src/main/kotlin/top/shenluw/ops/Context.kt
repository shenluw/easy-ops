package top.shenluw.ops

import com.google.common.io.Resources
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.alert.AlertEvent
import top.shenluw.ops.alert.AlertManager
import top.shenluw.ops.alert.AlertReceiver
import top.shenluw.ops.notification.Message
import top.shenluw.ops.notification.NotificationManager
import top.shenluw.ops.probe.MetricsTransport
import top.shenluw.ops.probe.ProbeManager
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import javax.swing.text.DateFormatter

/**
 * @author Shenluw
 * created: 2021/2/8 13:07
 */
class Context : KSlf4jLogger {

	var alertManager: AlertManager? = null
	var notificationManager: NotificationManager? = null
	var probeManager: ProbeManager? = null

	/**
	 * 加载配置
	 */
	fun load() {
		val configSource = Resources.toString(Resources.getResource("application.yml"), StandardCharsets.UTF_8)
		val yaml = Yaml(Representer().apply {
			propertyUtils.isSkipMissingProperties = true
		})
		val config = yaml.loadAs(configSource, ApplicationConfig::class.java)

		probeManager = ProbeManager(config.probe)

		config.notification?.apply {
			if (!this.enable) {
				return
			}
			notificationManager = NotificationManager(this)
		}
		config.alert?.apply {
			if (!this.enable) {
				return
			}
			alertManager = AlertManager(this)
			notificationManager?.also {
				alertManager?.register(NotificationAlertReceiver(it))
			}
		}
		probeManager?.subscribe(object : MetricsTransport {
			override fun transport(group: String, metrics: Metrics, source: String) {
				alertManager?.receiveMetrics(group, metrics, source)
			}
		})
	}

	fun start() {
		probeManager?.start()
	}

	private class NotificationAlertReceiver(private val notificationManager: NotificationManager) : AlertReceiver {

		private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:SSS")

		override fun receive(evt: AlertEvent) {
			val subject = "告警事件: " + evt.source
			val msg = StringBuilder()
			msg.append("事件名称： ").append(evt.name).append('\n')
				.append("触发时间： ").append(format.format(System.currentTimeMillis())).append('\n')
				.append("原因： ").append(evt.reason)
			notificationManager.sendAsync(Message(subject, msg.toString()))
		}
	}
}

