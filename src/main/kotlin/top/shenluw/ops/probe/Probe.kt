package top.shenluw.ops.probe

import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.Metrics

/**
 * @author Shenluw
 * created: 2021/2/8 16:14
 */
interface Probe {

	var transport: MetricsTransport?

	/**
	 * 启动探针
	 */
	fun start()

	fun stop()

}

/**
 * 采集数据传输接口
 */
interface MetricsTransport {
	/**
	 * @param group 数据分组
	 * @param source 数据来源
	 */
	fun transport(id: String, metrics: Metrics)
}

class MetricsTransportComposite(private val transports: List<MetricsTransport>) : MetricsTransport {

	override fun transport(id: String, metrics: Metrics) {
		transports.forEach {
			it.transport(id, metrics)
		}
	}
}

class LogMetricsTransport : MetricsTransport, KSlf4jLogger {
	override fun transport(id: String, metrics: Metrics) {
		log.debug("metrics: {}, {}", id, metrics)
	}
}