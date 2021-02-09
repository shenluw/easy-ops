package top.shenluw.ops

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Shenluw
 * created: 2021/2/9 17:41
 */
class MetricsStore(private val config: MetricsStoreConfig) {

	private val cache = ConcurrentHashMap<String, MutableList<Metrics>>()

	init {
		checkConfig()
	}

	private fun checkConfig() {
		if (config.maxSize <= 0) {
			throw OpsException("存储最大条数必须 > 0")
		}
	}

	fun getMetrics(source: String): List<Metrics>? {
		return cache[source]
	}

	fun store(source: String, metrics: Metrics) {
		val data = cache.getOrPut(source, { mutableListOf() })
		data.add(metrics)
		if (data.size > config.maxSize) {
			data.removeFirst()
		}
	}
}

/**
 * @param maxSize 单个source最大存储数量
 */
@Serializable
data class MetricsStoreConfig(val maxSize: Int = 20)
