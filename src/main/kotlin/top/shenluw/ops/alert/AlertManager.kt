package top.shenluw.ops.alert

import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rule
import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngine
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.core.RuleBuilder
import top.shenluw.ops.Metrics
import top.shenluw.ops.MetricsStore
import top.shenluw.ops.OpsException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.math.max


/**
 * @author Shenluw
 * created: 2021/2/8 16:10
 */
class AlertManager(private val config: AlertConfig, private val store: MetricsStore) {

	private val scheduled = Executors.newSingleThreadScheduledExecutor()

	private val receivers = mutableListOf<AlertReceiver>()

	private val delayQueue = ConcurrentLinkedQueue<AlertEvent>()

	private val handlers = ConcurrentHashMap<String, MetricsHandler>()

	private val rulesEngine: RulesEngine

	private val rules: Rules

	init {
		checkConfig()
		rulesEngine = DefaultRulesEngine()
		rules = Rules(
			config.rules?.values?.stream()
				?.map { createRule(it) }
				?.collect(Collectors.toSet())
		)

		if (config.delayTriggerTime > 0) {
			// 错开告警间隔检查
			var delay = config.delayTriggerTime / 100L
			delay = max(100, delay)
			scheduled.scheduleWithFixedDelay({ tryFire() }, delay, delay, TimeUnit.MILLISECONDS)
		}
	}

	private fun createRule(ruleConfig: AlertMetricsRule): Rule {
		val origin = RuleBuilder()
			.name(ruleConfig.metrics)
			.description(ruleConfig.desc)
			.priority(ruleConfig.level)
			.`when` { f ->
				f.iterator().forEach { fact ->
					val factData = fact.value as MetricsStore.FactData
					if (ruleConfig.source == null) {
						// 不检查id
						return@`when` checkCondition(ruleConfig.condition, ruleConfig.value, factData.value)
					} else {
						if (ruleConfig.source == factData.id) {
							return@`when` checkCondition(ruleConfig.condition, ruleConfig.value, factData.value)
						}
					}
				}
				false
			}
			.build()
		return if (ruleConfig.source == null) {
			SourceRule(origin)
		} else {
			SourceRule(origin, ruleConfig.source)
		}
	}

	private fun checkConfig() {
		val rs = config.rules
		if (rs.isNullOrEmpty()) {
			if (config.enable) {
				throw OpsException("告警规则不能为空")
			}
			return
		}
		rs.values.forEach {
			if (it.condition in arrayOf(
					ConditionType.GRAN,
					ConditionType.GRAN_EQ,
					ConditionType.LESS_EQ,
					ConditionType.LESS
				)
			) {
				if (it.value.toFloatOrNull() == null) {
					throw OpsException("规则配置错误：" + it.metrics)
				}
			}
		}
	}

	private fun checkCondition(type: ConditionType, expected: String, actual: Any): Boolean {
		when (type) {
			ConditionType.LIKE -> {
				return actual.toString().contains(expected)
			}
			ConditionType.EQ -> {
				return expected == actual.toString()
			}
			ConditionType.LESS -> {
				return expected.toFloat() > (actual as Number).toFloat()
			}
			ConditionType.LESS_EQ -> {
				return expected.toFloat() >= (actual as Number).toFloat()
			}
			ConditionType.GRAN -> {
				return expected.toFloat() < (actual as Number).toFloat()
			}
			ConditionType.GRAN_EQ -> {
				return expected.toFloat() <= (actual as Number).toFloat()
			}
		}
	}

	fun register(receiver: AlertReceiver) {
		receivers.add(receiver)
	}

	fun receiveMetrics(id: String, metrics: Metrics) {
		handlers.getOrPut(id, { MetricsHandler(id, store) }).handle(metrics)

		val facts = Facts()
		store.getComputeFacts().forEach {
			facts.add(it)
		}
		store.getFacts().forEach {
			facts.add(it)
		}

		// 不做实时告警
		val millis = System.currentTimeMillis()
		rulesEngine.check(rules, facts).forEach { (rule, hit) ->
			if (!hit) {
				return@forEach
			}
			delayQueue.add(
				SingleAlertEvent(
					rule.name,
					(rule as SourceRule).source,
					rule.priority,
					millis,
					rule.description
				)
			)
		}
	}


	private fun tryFire() {
		val millis = System.currentTimeMillis()
		val map = hashMapOf<String, MutableList<AlertEvent>>()
		synchronized(delayQueue) {
			val iterator = delayQueue.iterator()
			while (iterator.hasNext()) {
				val evt = iterator.next()
				if (millis > evt.timestamp + config.delayTriggerTime) {
					iterator.remove()
					// 同一来源未聚合维度
					map.getOrPut(evt.source, { mutableListOf() }).add(evt)
				}
			}
		}

		map.values.forEach {
			if (it.size == 1) {
				fire(it[0])
			} else {
				val combo = ComboAlertEvent()
				combo.name = "聚合告警"
				combo.timestamp = millis

				val children = mutableListOf<AlertEvent>()
				combo.children = children
				var maxLv = 0
				it.forEach { e ->
					children.add(e)
					maxLv = max(maxLv, e.level)
					combo.source = e.source
				}
				combo.level = maxLv

				fire(combo)
			}
		}

	}

	private fun fire(evt: AlertEvent) {
		receivers.forEach {
			it.receive(evt)
		}
	}

	private class SourceRule(val delegate: Rule, val source: String = "any") : Rule {

		override fun compareTo(other: Rule?): Int {
			return delegate.compareTo(other)
		}

		override fun getName(): String {
			return delegate.name
		}

		override fun getDescription(): String {
			return delegate.description
		}

		override fun getPriority(): Int {
			return delegate.priority
		}

		override fun evaluate(facts: Facts?): Boolean {
			return delegate.evaluate(facts)
		}

		override fun execute(facts: Facts?) {
			delegate.evaluate(facts)
		}
	}
}