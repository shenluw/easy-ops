package top.shenluw.ops.alert

import kotlin.properties.Delegates

/**
 * 接收告警事件
 * @author Shenluw
 * created: 2021/2/8 19:54
 */
interface AlertReceiver {
	fun receive(evt: AlertEvent)
}

interface AlertEvent {
	val name: String
	val source: String
	val level: Int
	val timestamp: Long
}

/**
 * @param name 事件名称
 * @param source 来源
 * @param level 事件等级
 * @param timestamp 触发时间
 * @param reason 触发原因
 */
data class SingleAlertEvent(
	override val name: String,
	override val source: String,
	override val level: Int,
	override val timestamp: Long,
	val reason: String
) : AlertEvent

/**
 * 聚合告警
 */
class ComboAlertEvent : AlertEvent {
	override lateinit var name: String
	override lateinit var source: String
	override var level by Delegates.notNull<Int>()
	override var timestamp by Delegates.notNull<Long>()
	lateinit var children: List<AlertEvent>
}