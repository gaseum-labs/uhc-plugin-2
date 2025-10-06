package org.gaseumlabs.uhcplugin

object Reflection {
	fun setFieldValue(target: Any, fieldName: String, value: Any?) {
		val field = target.javaClass.getDeclaredField(fieldName)
		field.isAccessible = true
		field.set(target, value)
	}

	fun setFieldValue(target: Any, subClass: Class<*>, fieldName: String, value: Any?) {
		val field = subClass.getDeclaredField(fieldName)
		field.isAccessible = true
		field.set(target, value)
	}

	fun <T> getFieldValue(target: Any, fieldName: String): T {
		val field = target.javaClass.getDeclaredField(fieldName)
		field.isAccessible = true
		return field.get(target) as T
	}

	fun <C, T> getStaticFieldValue(clazz: Class<C>, fieldName: String): T {
		val field = clazz.getDeclaredField(fieldName)
		field.isAccessible = true
		return field.get(null) as T
	}

	fun getEnumOrdinal(target: Any): Int {
		val clazz = target::class.java
		val ordinalMethod = clazz.getMethod("ordinal")
		return ordinalMethod.invoke(target) as Int
	}
}