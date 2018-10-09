package com.bennyhuo.dataclass.common

import com.bennyhuo.dataclass.config.Constant
import java.util.*
import java.util.regex.Pattern

/**
 * Created by dim on 2015/8/21.
 */
object CheckUtil {
    private val sPattern = Pattern.compile("^\\d+")

    private val keyWordList = listOf(
            "package",
            "as",
            "type",
            "class",
            "this",
            "val",
            "var",
            "fun",
            "extension",
            "for",
            "null",
            "typeof",
            "new",
            "true",
            "false",
            "is",
            "in",
            "throw",
            "return",
            "break",
            "continue",
            "object",
            "if",
            "else",
            "while",
            "do",
            "when",
            "out",
            "ref",
            "try",
            "where",
            "by",
            "get",
            "set",
            "import",
            "final",
            "abstract",
            "enum",
            "open",
            "annotation",
            "override",
            "private",
            "public",
            "internal",
            "protected",
            "catch",
            "finally"
    )
    private val simpleTypeList = listOf(
            "String",
            "Boolean",
            "Float",
            "Int",
            "Long",
            "Double",
            "Short",
            "List",
            "Map"
    )
    private val declareClassNameList = HashSet<String>()
    private val declareFieldNameList = HashSet<String>()

    fun cleanDeclareData() {
        declareClassNameList.clear()
        declareFieldNameList.clear()
    }


    fun containsDeclareClassName(name: String): Boolean {
        return declareClassNameList.contains(name)
    }

    fun addDeclareClassName(name: String) {
        declareClassNameList.add(name.replace(".java", ""))
    }

    fun removeDeclareClassName(name: String) {
        declareClassNameList.remove(name)
    }

    fun containsDeclareFieldName(name: String): Boolean {
        return declareFieldNameList.contains(name)
    }

    fun addDeclareFieldName(name: String) {
        declareFieldNameList.add(name)
    }

    fun removeDeclareFieldName(name: String) {
        declareFieldNameList.remove(name)
    }

    fun checkSimpleType(s: String): Boolean {
        return simpleTypeList.contains(s)
    }

    fun checkKeyWord(key: String): Boolean {
        return keyWordList.contains(key)
    }

    fun formatIdentifier(arg: String): String {
        return arg.replace("-".toRegex(), "")
                .let(sPattern::matcher)
                .find()
                .yes {
                    Constant.DEFAULT_PREFIX + arg
                }
                .otherwise {
                    arg
                }
                .let(CheckUtil::checkKeyWord)
                .yes {
                    "`$arg`"
                }
                .otherwise {
                    arg
                }
    }
}
