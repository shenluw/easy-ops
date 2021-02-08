package top.shenluw.ops.alert

import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngine
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.core.RuleBuilder
import top.shenluw.ops.Metrics
import top.shenluw.ops.OpsException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.math.max


/**
 * @author Shenluw
 * created: 2021/2/8 16:10
 */
class AlertManager(private val config: AlertConfig) {

	private val scheduled = Executors.newSingleThreadScheduledExecutor()

	private val receivers = mutableListOf<AlertReceiver>()

	private val delayQueue = ConcurrentLinkedQueue<AlertEvent>()

	private val handlers = hashMapOf<String, MetricsHandler>()

	private val rulesEngine: RulesEngine

	private val rules: Rules

	init {
		checkConfig()
		rulesEngine = DefaultRulesEngine()
		rules = Rules(
			config.rules?.stream()
				?.map {
					val metrics = it.metrics
					RuleBuilder()
						.name(metrics)
						.description(it.desc)
						.priority(it.level)
						.`when` { f ->
							val type = it.condition
							if (type == ConditionType.LIKE) {
								f.get<String>(metrics).contains(it.value.toString())
							} else if (type == ConditionType.EQ) {
								f.get<String>(metrics) == it.value.toString()
							} else if (type == ConditionType.LESS) {
								f.get<Float>(metrics) < it.value as Float
							} else if (type == ConditionType.LESS_EQ) {
								f.get<Float>(metrics) <= it.value as Float
							} else if (type == ConditionType.GRAN) {
								f.get<Float>(metrics) > it.value as Float
							} else if (type == ConditionType.GRAN_EQ) {
								f.get<Float>(metrics) >= it.value as Float
							}
							false
						}
						.build()
				}?.collect(Collectors.toSet())
		)

		if (config.delayTriggerTime > 0) {
			var delay = config.delayTriggerTime / 100L
			delay = max(100, delay)
			scheduled.scheduleWithFixedDelay({ tryFire() }, delay, delay, TimeUnit.MILLISECONDS)
		}
	}

	private fun checkConfig() {
		val rs = config.rules
		if (rs.isNullOrEmpty()) {
			return
		}
		rs.forEach {
			val condition = it.condition
			if (condition == ConditionType.LESS || condition == ConditionType.GRAN) {
				if (it.value !is Number) {
					throw OpsException("规则配置错误：" + it.metrics)
				}
			} else if (condition == ConditionType.LIKE || condition == ConditionType.EQ) {
				if (it.value !is String) {
					throw OpsException("规则配置错误：" + it.metrics)
				}
			}
		}
	}

	fun register(receiver: AlertReceiver) {
		receivers.add(receiver)
	}

	fun receiveMetrics(group: String, metrics: Metrics, source: String) {
		val key = group + source
		var handler = handlers[key]
		if (handler == null) {
			handler = MetricsHandler(group, source)
			handlers[key] = handler
		}
		val evt = handler.handle(metrics)
		if (evt != null) {
			if (config.delayTriggerTime == 0) {
				delayQueue.add(evt)
				tryFire()
			} else {
				fire(evt)
			}
		}
	}


	private fun tryFire() {
		val millis = System.currentTimeMillis()
		val map = hashMapOf<String, List<AlertEvent>>()
		delayQueue.forEach {
			if (millis > it.timestamp + config.delayTriggerTime) {
				// TODO: 2021/2/8 处理集合 
			}
		}
	}

	private fun fire(evt: AlertEvent) {
		receivers.forEach {
			it.receive(evt)
		}
	}
}