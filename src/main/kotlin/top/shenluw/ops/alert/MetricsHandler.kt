package top.shenluw.ops.alert

import top.shenluw.ops.Metrics
import top.shenluw.ops.MetricsStore


/**
 * @author Shenluw
 * created: 2021/2/8 21:24
 */
class MetricsHandler(val id: String, private val store: MetricsStore) {

	/**
	 * 数据处理器
	 * @return 返回值不为空 表示触发告警
	 */
	fun handle(metrics: Metrics): AlertEvent? {
		return null
	}

}