package top.shenluw.ops.alert

import kotlinx.serialization.Serializable

/**
 * @author Shenluw
 * created: 2021/2/8 17:13
 */
@Serializable
data class AlertConfig(
	val enable: Boolean = true,
	/**
	 * 延迟触发
	 * 会合并短时间内的告警信息
	 */
	val delayTriggerTime: Int = 3_000,

	val rules: Map<String, AlertMetricsRule>? = null,
)

@Serializable
data class AlertMetricsRule(
	/**
	 * 规则数据指标
	 */
	val metrics: String,
	/**
	 * 区分来源
	 * 不为空表示取指定采集id数据
	 */
	val source: String? = null,
	/**
	 * 规则描述
	 */
	val desc: String? = null,

	/**
	 * 对比值
	 */
	val value: String,

	/**
	 * 比较类型
	 */
	val condition: ConditionType,

	/**
	 * 告警等级
	 */
	val level: Int = 1
)

enum class ConditionType {
	/**
	 * ==
	 */
	EQ,

	/**
	 * <
	 */
	LESS,

	/**
	 * <=
	 */
	LESS_EQ,

	/**
	 * >
	 */
	GRAN,

	/**
	 * >=
	 */
	GRAN_EQ,

	LIKE
}