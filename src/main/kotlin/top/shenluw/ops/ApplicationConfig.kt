package top.shenluw.ops

import kotlinx.serialization.Serializable
import top.shenluw.ops.alert.AlertConfig
import top.shenluw.ops.notification.NotificationConfig
import top.shenluw.ops.probe.ProbeConfig

/**
 * @author Shenluw
 * created: 2021/2/8 17:07
 */
@Serializable
data class ApplicationConfig(
	val probe: ProbeConfig,
	val notification: NotificationConfig? = null,
	val alert: AlertConfig? = null,
	val store: MetricsStoreConfig? = null
)