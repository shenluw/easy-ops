package top.shenluw.ops.probe

import okhttp3.OkHttpClient
import okhttp3.Request
import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.Metrics
import top.shenluw.ops.MetricsNames
import top.shenluw.ops.MetricsType
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * @author Shenluw
 * created: 2021/2/8 17:16
 */
class HttpProbe(private val config: HttpProbeConfig) : Probe, KSlf4jLogger {

	override var transport: MetricsTransport? = null

	private var scheduled: ScheduledExecutorService? = null


	private val client: OkHttpClient = OkHttpClient.Builder()
		.connectTimeout(config.connectTimeout.toLong(), TimeUnit.MILLISECONDS)
		.readTimeout(config.requestTimeout.toLong(), TimeUnit.MILLISECONDS)
		.writeTimeout(config.requestTimeout.toLong(), TimeUnit.MILLISECONDS)
		.callTimeout(config.requestTimeout.toLong(), TimeUnit.MILLISECONDS)
		.retryOnConnectionFailure(false)
		.build()

	private val metricsGroup = "http_" + config.url

	private var reqParams = Request.Builder()
		.url(config.url)
		.method(config.method, null)
		.build()

	@Synchronized
	override fun start() {
		scheduled = Executors.newSingleThreadScheduledExecutor()
		scheduled?.scheduleWithFixedDelay(
			{ request() },
			config.interval.toLong(),
			config.interval.toLong(),
			TimeUnit.MILLISECONDS
		)
	}

	@Synchronized
	override fun stop() {
		scheduled?.shutdown()
		scheduled = null
	}


	private fun request() {
		try {
			val response = client.newCall(reqParams).execute()

			val ts = System.currentTimeMillis()
			transport?.apply {
				transport(
					metricsGroup,
					Metrics(MetricsNames.HTTP_STATUS_CODE, MetricsType.NUMBER, response.code, ts),
					config.url
				)
				response.body?.string()?.apply {
					transport(metricsGroup, Metrics(MetricsNames.HTTP_BODY, MetricsType.STRING, this, ts), config.url)
				}
			}
		} catch (e: Exception) {
			log.warn("请求失败 {}: {}", config.method, config.url, e.message)
			val ts = System.currentTimeMillis()
			transport?.apply {
				transport(metricsGroup, Metrics(MetricsNames.HTTP_EXCEPTION, MetricsType.ANY, e, ts), config.url)
			}
		}
	}

}