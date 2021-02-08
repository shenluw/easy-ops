package top.shenluw.ops.alert

/**
 * @author Shenluw
 * created: 2021/2/8 17:13
 */
class AlertConfig {
	var enable = true

	/**
	 * 延迟触发
	 * 会合并短时间内的告警信息
	 */
	var delayTriggerTime = 3_000

	var rules: List<AlertMetricsRule>? = null
}

class AlertMetricsRule {
	/**
	 * 规则数据指标
	 */
	lateinit var metrics: String

	/**
	 * 规则描述
	 */
	var desc: String? = null

	/**
	 * 对比值
	 */
	lateinit var value: Any

	/**
	 * 比较类型
	 */
	lateinit var condition: ConditionType

	/**
	 * 告警等级
	 */
	var level: Int = 1
}

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