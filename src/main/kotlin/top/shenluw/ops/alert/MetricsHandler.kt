package top.shenluw.ops.alert

import top.shenluw.ops.Metrics


/**
 * @author Shenluw
 * created: 2021/2/8 21:24
 */
class MetricsHandler(val name: String, val source: String) {

	/**
	 * 数据处理器
	 * @return 返回值不为空 表示触发告警
	 */
	fun handle(metrics: Metrics): AlertEvent? {
		return null
	}

}