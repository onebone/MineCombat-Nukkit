package me.onebone.minecombat.util

import cn.nukkit.Server
import cn.nukkit.level.Position
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PositionDeserializer(private val server: Server): JsonDeserializer<Position> {
	override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Position? {
		val arr = json.asJsonArray
		val x = arr.get(0).asDouble
		val y = arr.get(1).asDouble
		val z = arr.get(2).asDouble
		val levelName = arr.get(3).asString

		val level = server.getLevelByName(levelName)

		return if(level == null) null else Position(x, y, z, level)
	}
}