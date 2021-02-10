package top.shenluw.ops.probe

import org.junit.jupiter.api.Test
import top.shenluw.ops.log.KSlf4jLogger
import java.util.concurrent.TimeUnit

/**
 * @author Shenluw
 * created: 2021/2/8 18:09
 */
internal class HttpProbeTest : KSlf4jLogger {

	@Test
	fun start() {
		val config = HttpProbeConfig(
			id = "test",
			url = "http://127.0.0.1:10801",
			interval = 1_000
		)

		val probe = HttpProbe(config)
		probe.transport = LogMetricsTransport
		probe.start()
		TimeUnit.SECONDS.sleep(13)
		probe.stop()
	}
}