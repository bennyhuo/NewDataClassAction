package cn.kotliner.dataclass.entity

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by benny on 6/29/17.
 */
enum class JsonDataType(val value: String) {
    JsonBoolean("Boolean"), JsonInt("Int"), JsonDouble("Double"),
    JsonLong("Long"), JsonString("String"), JsonObject("Any"), JsonArray("Array");

    companion object {
        @JvmStatic
        fun typeOfObject(value: Any?): JsonDataType {
            return when (value) {
                null -> JsonObject
                is Boolean -> JsonBoolean
                is Int -> JsonInt
                is Double -> JsonDouble
                is Long -> JsonLong
                is String -> JsonString
                is JSONObject -> JsonObject
                is JSONArray -> JsonArray
                else -> JsonObject
            }
        }

        @JvmStatic
        fun typeOfString(type: String?): JsonDataType? = type?.let(JsonDataType::valueOf)

        @JvmStatic
        fun isSameDataType(text: String, text2: String): Boolean {
            return isSameDataType(JsonDataType.typeOfString(text), JsonDataType.typeOfString(text2))
        }

        @JvmStatic
        fun isSameDataType(dataType: JsonDataType?, dataType1: JsonDataType?)
                = dataType != null && dataType1 != null && dataType == dataType1
    }
}