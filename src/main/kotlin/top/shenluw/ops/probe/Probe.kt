package top.shenluw.ops.probe

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
	fun transport(metrics: Metrics)
}
