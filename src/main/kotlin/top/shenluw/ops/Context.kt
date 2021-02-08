package top.shenluw.ops

import com.google.common.io.Resources
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.alert.AlertManager
import top.shenluw.ops.notification.NotificationManager
import top.shenluw.ops.probe.ProbeManager
import java.nio.charset.StandardCharsets

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
			notificationManager = NotificationManager(this)
		}
		config.alert?.apply {
			alertManager = AlertManager(this)
		}
	}

	fun start() {
		probeManager?.start()
	}
}

