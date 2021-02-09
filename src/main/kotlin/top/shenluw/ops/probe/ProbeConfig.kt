package top.shenluw.ops.probe

import kotlinx.serialization.Serializable

/**
 * 探针配置
 * @author Shenluw
 * created: 2021/2/8 16:15
 */
@Serializable
data class ProbeConfig(val http: List<HttpProbeConfig>? = null)

/**
 * http 探针配置
 */
@Serializable
data class HttpProbeConfig(
	/**
	 * 采集唯一标识
	 */
	val id: String,

	/**
	 * 目标地址
	 */
	val url: String,

	/**
	 * 请求方法 GET POST ...
	 */
	val method: String = "GET",

	/**
	 * 连接超时时间 单位 毫秒
	 */
	val connectTimeout: Int = 6_000,

	/**
	 * 请求超时时间 单位 毫秒
	 */
	val requestTimeout: Int = 6_000,

	/**
	 *探测间隔 单位 毫秒
	 */
	val interval: Int = 10_000,
)
