package top.shenluw.ops

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.google.common.io.Resources
import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.alert.*
import top.shenluw.ops.notification.Message
import top.shenluw.ops.notification.NotificationManager
import top.shenluw.ops.probe.LogMetricsTransport
import top.shenluw.ops.probe.MetricsTransport
import top.shenluw.ops.probe.ProbeManager
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

/**
 * @author Shenluw
 * created: 2021/2/8 13:07
 */
class Context : KSlf4jLogger {

	var alertManager: AlertManager? = null
	var notificationManager: NotificationManager? = null
	var probeManager: ProbeManager? = null
	var metricsStore: MetricsStore? = null

	/**
	 * 加载配置
	 */
	fun load() {
		val configSource = Resources.toString(Resources.getResource("application.yml"), StandardCharsets.UTF_8)
		val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
		val config = yaml.decodeFromString(ApplicationConfig.serializer(), configSource)

		metricsStore = MetricsStore(config.store ?: MetricsStoreConfig())

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
			alertManager = AlertManager(this, metricsStore!!)
			notificationManager?.also {
				alertManager?.register(NotificationAlertReceiver(it))
			}
		}
		probeManager?.subscribe(LogMetricsTransport())
		probeManager?.subscribe(object : MetricsTransport {
			override fun transport(id: String, metrics: Metrics) {
				alertManager?.receiveMetrics(id, metrics)
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
				.append("触发时间： ").append(format.format(evt.timestamp)).append('\n')
			if (evt is SingleAlertEvent) {
				msg.append("原因： ").append(evt.reason)
			}
			if (evt is ComboAlertEvent) {
				msg.append('\n').append("触发组合事件：")
				val children = evt.children

				children.forEach {
					msg.append('\n')
						.append("事件名称： ").append(it.name).append('\n')
						.append("触发时间： ").append(format.format(it.timestamp))
					if (it is SingleAlertEvent) {
						msg.append('\n').append("原因： ").append(it.reason)
					}
				}

			}

			notificationManager.sendAsync(Message(subject, msg.toString()))
		}
	}
}

