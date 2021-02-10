package top.shenluw.ops

import kotlinx.serialization.Serializable
import org.jeasy.rules.api.Fact
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * @author Shenluw
 * created: 2021/2/9 17:41
 */
class MetricsStore(private val config: MetricsStoreConfig) {


	/**
	 * 按照写入顺序排放
	 * id : {  metricName: [ value ] }
	 */
	private val cache = ConcurrentHashMap<String, MutableMap<String, Deque<Metrics>>>()


	/**
	 * 计算属性存储
	 * 同一id下不存在重复指标
	 */
	private val computeCache = ConcurrentHashMap<String, Deque<ComputeMetrics>>()

	init {
		checkConfig()
	}

	private fun checkConfig() {
		if (config.maxSize <= 0) {
			throw OpsException("存储最大条数必须 > 0")
		}
	}

	/**
	 * 获取指标数据
	 * @param count 获取数量
	 * @param  reverse false 从头开始 true 从尾开始
	 */
	fun getMetrics(id: String, metrics: String, count: Int = 2, reverse: Boolean = false): List<Metrics> {
		val data = cache[id] ?: return emptyList()

		val deque = data[metrics]
		if (deque.isNullOrEmpty()) {
			return emptyList()
		}
		val ret = mutableListOf<Metrics>()

		val iter = if (reverse) {
			deque.descendingIterator()
		} else {
			deque.iterator()
		}

		for (i in 0 until count) {
			if (iter.hasNext()) {
				ret.add(iter.next())
			}
		}
		return ret
	}

	fun store(id: String, metrics: Metrics) {
		val data = cache.getOrPut(id, {
			ConcurrentHashMap<String, Deque<Metrics>>()
		})

		val deque = data.getOrPut(metrics.name, { ConcurrentLinkedDeque() })
		deque.add(metrics)
		if (deque.size > config.maxSize) {
			deque.pollFirst()
		}
	}

	fun storeCompute(id: String, metrics: ComputeMetrics) {
		computeCache.getOrPut(id, { ConcurrentLinkedDeque() })
			.add(metrics)
	}

	fun getComputeMetrics(id: String, metrics: String): ComputeMetrics? {
		val data = computeCache[id]
		if (data != null) {
			return data.find { it.name == metrics }
		}
		return null
	}

	/**
	 * 获取最新值断面
	 */
	fun getFacts(): List<Fact<FactData>> {
		if (cache.isEmpty()) {
			return emptyList()
		}
		val ret = mutableListOf<Fact<FactData>>()
		for (entry in cache.entries) {
			val id = entry.key
			for (vs in entry.value.entries) {
				val metrics = vs.value.peekLast()
				ret.add(Fact(metrics.name + "_" + id, FactData(id, metrics.name, metrics.value)))
			}
		}
		return ret
	}

	/**
	 * 获取最新值断面
	 */
	fun getComputeFacts(): List<Fact<FactData>> {
		if (computeCache.isEmpty()) {
			return emptyList()
		}
		val ret = mutableListOf<Fact<FactData>>()
		for (entry in computeCache.entries) {
			val id = entry.key
			val metrics = entry.value.peekLast()
			ret.add(Fact(metrics.name + "_" + id, FactData(id, metrics.name, metrics.value)))
		}
		return ret
	}


	data class FactData(val id: String, val name: String, val value: Any)
}

/**
 * @param maxSize 单个source最大存储数量
 */
@Serializable
data class MetricsStoreConfig(val maxSize: Int = 20)
