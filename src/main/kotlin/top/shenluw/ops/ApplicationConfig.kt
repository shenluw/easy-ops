package top.shenluw.ops

import top.shenluw.ops.alert.AlertConfig
import top.shenluw.ops.notification.NotificationConfig
import top.shenluw.ops.probe.ProbeConfig

/**
 * @author Shenluw
 * created: 2021/2/8 17:07
 */
class ApplicationConfig {

	lateinit var probe: ProbeConfig

	var notification: NotificationConfig? = null

	var alert: AlertConfig? = null
}