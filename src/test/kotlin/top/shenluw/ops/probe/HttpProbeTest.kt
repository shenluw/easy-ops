package top.shenluw.ops.probe

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import top.shenluw.luss.common.log.KSlf4jLogger
import top.shenluw.ops.Metrics
import java.util.concurrent.TimeUnit

/**
 * @author Shenluw
 * created: 2021/2/8 18:09
 */
internal class HttpProbeTest : KSlf4jLogger {

	@Test
	fun start() {
		val config = HttpProbeConfig()
		config.url = "http://127.0.0.1:10801"
		config.interval = 1_000

		val probe = HttpProbe(config)
		probe.transport = object : MetricsTransport {
			override fun transport(metrics: Metrics) {
				log.info("rev: {}", metrics)
			}
		}
		probe.start()
		TimeUnit.SECONDS.sleep(13)
		probe.stop()
	}
}