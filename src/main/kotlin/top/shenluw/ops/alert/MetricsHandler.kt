package top.shenluw.ops.alert

import org.jeasy.rules.api.RulesEngine
import top.shenluw.ops.*


/**
 * @author Shenluw
 * created: 2021/2/8 21:24
 */
class MetricsHandler(val id: String, private val store: MetricsStore) {

	private val handlers = mutableListOf<Handler>()

	init {
		handlers.add(CountHandler())
		handlers.add(IntervalHandler())
	}

	/**
	 * 数据处理器
	 */
	fun handle(metrics: Metrics) {
		store.store(id, metrics)
		handlers.forEach {
			it.handle(metrics)
		}
	}

	interface Handler {
		fun handle(metrics: Metrics)
	}

	/**
	 * 连续次数统计
	 */
	inner class CountHandler : Handler {

		override fun handle(metrics: Metrics) {
			val millis = System.currentTimeMillis()
			val computeMetricsName = metrics.name + MetricsNames.SPLITERATOR + MetricsNames.INC_COUNT
			var persist = store.getComputeMetrics(id, computeMetricsName)
			if (persist == null) {
				persist =
					ComputeMetrics(computeMetricsName, MetricsType.NUMBER, 1, millis, metrics.value)
				store.storeCompute(id, persist)
			}

			val records = store.getMetrics(id, metrics.name, reverse = true)
			if (records.isEmpty()) {
				return
			}

			val last = records[0]

			if (records.size == 2) {
				val prev = records[1]
				persist.timestamp = millis
				if (last.value != prev.value) {
					persist.value = 1
					persist.origin = metrics.value
				} else {
					persist.value = (persist.value as Int).inc()
				}
			}
		}

	}

	/**
	 * 时间间隔统计
	 */
	inner class IntervalHandler : Handler {
		override fun handle(metrics: Metrics) {
			val millis = System.currentTimeMillis()
			val computeMetricsName = metrics.name + MetricsNames.SPLITERATOR + MetricsNames.INTERVAL
			var persist = store.getComputeMetrics(id, computeMetricsName)
			if (persist == null) {
				persist =
					ComputeMetrics(computeMetricsName, MetricsType.NUMBER, 0, millis, metrics.value)
				store.storeCompute(id, persist)
			}

			val records = store.getMetrics(id, metrics.name, reverse = true)
			if (records.isEmpty()) {
				return
			}

			val last = records[0]

			if (records.size == 2) {
				val prev = records[1]
				persist.timestamp = millis
				persist.origin = metrics.value
				persist.value = prev.timestamp - last.timestamp
			}
		}
	}
}
