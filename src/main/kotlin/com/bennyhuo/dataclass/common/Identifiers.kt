package com.bennyhuo.dataclass.common

/**
 * Created by benny on 10/10/18.
 */
object Identifiers {
    /**
     * Match an identifier. @see <a href="https://stackoverflow.com/questions/35578567/java-regex-for-identifiers-letters-digits-and-underscores"></a>
     */
    private val identifierPattern = Regex("(?:\\b[_a-zA-Z]|\\B\\\$)[_\$a-zA-Z0-9]*")

    /**
     * @see <a href="https://kotlinlang.org/docs/reference/keyword-reference.html">Hard Keywords</a>
     */
    private val hardKeyWordList = listOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "for",
            "false",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "val",
            "var",
            "when",
            "while"
    )

    fun isValidIdentifier(key: String) = key !in hardKeyWordList && identifierPattern.matches(key)

    /**
     * @return word If valid identifier otherwise `$word`
     */
    fun formatIdentifier(word: String) = word.let(Identifiers::isValidIdentifier)
            .yes { "`$word`" }
            .otherwise { word }
}
