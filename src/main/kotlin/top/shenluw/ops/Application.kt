package top.shenluw.ops

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Shenluw
 * created: 2021/2/8 12:52
 */
private val log: Logger = LoggerFactory.getLogger("Application")

fun main() {
	log.info("准备运行")
	val context = Context()
	context.load()
	context.start()
	log.info("运行中")
}