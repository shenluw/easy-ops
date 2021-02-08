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

enum class MetricsType {
	NUMBER, STRING, ANY
}

/**
 * 采集指标 字段
 */
object MetricsNames {
	/** http */
	val HTTP_STATUS_CODE = "http_status_code"
	val HTTP_BODY = "http_body"

	/* http 请求出现异常 */
	val HTTP_EXCEPTION = "http_exception"
}