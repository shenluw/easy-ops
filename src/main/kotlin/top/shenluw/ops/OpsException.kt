package top.shenluw.ops

import java.lang.RuntimeException

/**
 * @author Shenluw
 * created: 2021/2/8 19:02
 */
class OpsException : RuntimeException {

	constructor(message: String?) : super(message)
	constructor(message: String?, cause: Throwable?) : super(message, cause)
}