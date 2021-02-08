package top.shenluw.ops.probe

import java.io.Serializable

/**
 * 探针配置
 * @author Shenluw
 * created: 2021/2/8 16:15
 */
class ProbeConfig {
	var http: List<HttpProbeConfig>? = null

	override fun toString(): String {
		return "ProbeConfig(http=$http)"
	}
}

/**
 * http 探针配置
 */
class HttpProbeConfig {
	/**
	 * 目标地址
	 */
	lateinit var url: String

	/**
	 * 请求方法 GET POST ...
	 */
	var method: String = "GET"

	/**
	 * 连接超时时间 单位 毫秒
	 */
	var connectTimeout: Int = 6_000

	/**
	 * 请求超时时间 单位 毫秒
	 */
	var requestTimeout: Int = 6_000

	/**
	 *探测间隔 单位 毫秒
	 */
	var interval: Int = 10_000

	override fun toString(): String {
		return "HttpProbeConfig(url='$url', method=$method, connectTimeout=$connectTimeout, requestTimeout=$requestTimeout, interval=$interval)"
	}
}
