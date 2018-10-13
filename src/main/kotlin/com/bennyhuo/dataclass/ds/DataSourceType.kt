package com.bennyhuo.dataclass.ds

import com.intellij.icons.AllIcons
import org.jetbrains.kotlin.idea.KotlinIcons
import javax.swing.Icon

enum class DataSourceType(val presentableName: String, val icon: Icon, val templateName: String = "Kotlin Data Class"){
    JSON("Json", KotlinIcons.CLASS), YAML("Yaml", KotlinIcons.CLASS)
}