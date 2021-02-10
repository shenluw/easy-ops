package top.shenluw.ops

/**
 *
 * 采集指标
 *
 * @author Shenluw
 * created: 2021/2/8 17:20
 *
 * @param name 指标名词
 * @param type 数据类型
 * @param value 数据
 * @param timestamp 采集时间
 */
data class Metrics(val name: String, val type: MetricsType, val value: Any, val timestamp: Long)

/**
 * 计算指标
 * @param name 指标名词
 * @param type 数据类型
 * @param value 计算结果
 * @param timestamp 最后更新时间
 * @param origin 原始指标值
 */
data class ComputeMetrics(val name: String, val type: MetricsType, var value: Any, var timestamp: Long, var origin: Any)

enum class MetricsType {
	NUMBER, STRING, ANY
}

/**
 * 采集指标 字段
 */
object MetricsNames {
	/**
	 * 组合分割字符
	 */
	const val SPLITERATOR = "_"

	/************** 计算指标 ***************************/

	/**
	 * 连续次数
	 */
	const val INC_COUNT = "inc_count"

	/**
	 * 2次之间间隔
	 */
	const val INTERVAL = "interval"

	/*************** 普通指标 *************************/

	/** http */
	const val HTTP_STATUS_CODE = "http_status_code"
	const val HTTP_BODY = "http_body"

	/* http 请求出现异常 */
	const val HTTP_EXCEPTION = "http_exception"
}