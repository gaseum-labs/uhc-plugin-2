package org.gaseumlabs.uhcplugin.util

class JSONWrite() {
	data class Scope(val type: Int, var hasElement: Boolean)

	val scopeStack = ArrayList<Scope>()

	var buffer: StringBuilder = StringBuilder()

	inline fun obj(inside: (JSONWrite) -> Unit): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		scopeStack.add(Scope(TYPE_OBJECT, false))
		buffer.append('{')
		inside(this)
		buffer.append('}')
		scopeStack.removeLast()
		scope?.hasElement = true
		return this
	}

	inline fun array(inside: (JSONWrite) -> Unit): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		scopeStack.add(Scope(TYPE_ARRAY, false))
		buffer.append('[')
		inside(this)
		buffer.append(']')
		scopeStack.removeLast()
		scope?.hasElement = true
		return this
	}

	private fun createProperty(name: String) = "\"${name}\":"
	private fun createString(value: String) = "\"${value.replace("\"", "\\\"")}\""

	fun property(name: String): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.hasElement == true) buffer.append(',')
		buffer.append(createProperty(name))
		scope?.hasElement = true
		return this
	}

	fun value(value: String): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		buffer.append(createString(value))
		scope?.hasElement = true
		return this
	}

	fun value(value: Number): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		buffer.append(value.toString())
		scope?.hasElement = true
		return this
	}

	fun value(value: Boolean): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		buffer.append(value.toString())
		scope?.hasElement = true
		return this
	}

	fun json(json: String): JSONWrite {
		val scope = scopeStack.lastOrNull()
		if (scope?.type == TYPE_ARRAY && scope.hasElement) buffer.append(',')
		buffer.append(json)
		scope?.hasElement = true
		return this
	}

	override fun toString(): String {
		return buffer.toString()
	}

	companion object {
		const val TYPE_OBJECT: Int = 0
		const val TYPE_ARRAY: Int = 1
	}
}