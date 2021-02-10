package top.shenluw.ops.probe

import top.shenluw.ops.log.KSlf4jLogger
import top.shenluw.ops.OpsException

/**
 *
 * 探针
 *
 * @author Shenluw
 * created: 2021/2/8 16:12
 */
class ProbeManager(private val config: ProbeConfig) : KSlf4jLogger {

	private val probes = mutableListOf<Probe>()

	private var metricsTransports = mutableListOf<MetricsTransport>()

	private fun checkConfig() {
		val count = config.http!!.stream()
			.map { it.url }
			.distinct()
			.count().toInt()
		if (count != config.http.size) {
			throw OpsException("http配置url不能重复")
		}
	}

	@Synchronized
	fun start() {
		if (config.http.isNullOrEmpty()) {
			log.warn("http 探针未配置")
			return
		}
		checkConfig()
		config.http.forEach {
			val probe = HttpProbe(it)
			probe.transport = MetricsTransportComposite(metricsTransports)
			probes.add(probe)
			probe.start()
		}
	}

	@Synchronized
	fun stop() {
		probes.forEach {
			it.stop()
		}
		probes.clear()
	}

	fun subscribe(transport: MetricsTransport) {
		metricsTransports.add(transport)
	}

}