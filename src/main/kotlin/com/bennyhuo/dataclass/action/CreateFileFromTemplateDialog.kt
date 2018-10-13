/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bennyhuo.dataclass.action

import com.bennyhuo.dataclass.common.format
import com.bennyhuo.dataclass.common.toDate
import com.bennyhuo.dataclass.common.yes
import com.bennyhuo.dataclass.ds.DataSourceType
import com.bennyhuo.dataclass.logic.JsonParser
import com.bennyhuo.dataclass.ui.toast
import com.intellij.ide.actions.ElementCreator
import com.intellij.ide.actions.TemplateKindCombo
import com.intellij.lang.LangBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons
import com.intellij.util.SystemProperties
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.json.JSONArray
import org.json.JSONObject
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * @author peter
 */
class CreateFileFromTemplateDialog private constructor(private val project: Project) : DialogWrapper(project, true) {
    private lateinit var nameField: JTextField
    private lateinit var kindCombo: TemplateKindCombo
    private lateinit var myPanel: JPanel
    private lateinit var myUpDownHint: JLabel
    private lateinit var kindLabel: JLabel
    private lateinit var nameLabel: JLabel
    private lateinit var dataSourceInput: JTextArea
    private lateinit var formatJson: JButton

    private var myCreator: ElementCreator? = null
    private var myInputValidator: InputValidator? = null

    private var ktFile: KtFile? = null
        set(value) {
            field = value
            if(value == null) {
                title = "Create Kotlin Data Class"
            } else {
                nameField.text = value.virtualFile.nameWithoutExtension
                nameField.isEditable = false
                title = "Update Kotlin Data Class"
            }
        }

    private val enteredName: String
        get() {
            return nameField.text.let { text ->
                val trimmed = text.trim { it <= ' ' }
                if (text.length != trimmed.length) {
                    nameField.text = trimmed
                }
                trimmed
            }
        }

    init {
        kindLabel.labelFor = kindCombo
        kindCombo.registerUpDownHint(nameField)

        DataSourceType.values().forEach {
            //template name - see resources fileTemplates.internal
            kindCombo.addItem(it.presentableName, it.icon, it.templateName)
        }

        myUpDownHint.icon = PlatformIcons.UP_DOWN_ARROWS
        setTemplateKindComponentsVisible(false)

        formatJson.addActionListener { formatJson() }

        dataSourceInput.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                //在最后一个字符换行，可触发 ok 操作
                if (e.keyCode == KeyEvent.VK_ENTER && dataSourceInput.caretPosition == dataSourceInput.text.length) {
                    doOKAction()
                } else {
                    super.keyReleased(e)
                }
            }
        })
        init()
    }

    private fun formatJson(): Boolean {
        try {
            var json = dataSourceInput.text
            json = json.trim { it <= ' ' }
            if (json.startsWith("{")) {
                val jsonObject = JSONObject(json)
                val formatJson = jsonObject.toString(4)
                dataSourceInput.text = formatJson
            } else if (json.startsWith("[")) {
                val jsonArray = JSONArray(json)
                val formatJson = jsonArray.toString(4)
                dataSourceInput.text = formatJson
            } else {
                return false
            }
            return true
        } catch (e: Exception) {
            dataSourceInput.toast("Invalid Json Text: ${e.message}");
        }

        return false
    }

    override fun doValidate(): ValidationInfo? {
        myInputValidator?.let { validator ->
            enteredName.let { text ->
                if (!validator.canClose(text)) {
                    val errorText = (validator as? InputValidatorEx)?.getErrorText(text)
                            ?: LangBundle.message("incorrect.name")
                    return ValidationInfo(errorText, nameField)
                }
            }
        }
        return super.doValidate()
    }

    override fun createCenterPanel() = myPanel

    override fun doOKAction() {
        //必须输入正确的 json 字符串才行
        if (!formatJson()) {
            return
        }
        myCreator?.tryCreate(enteredName)?.isNotEmpty()?.yes {
            super.doOKAction()
        }
    }

    override fun getPreferredFocusedComponent() = nameField

    fun setTemplateKindComponentsVisible(flag: Boolean) {
        kindCombo.isVisible = flag
        kindLabel.isVisible = flag
        myUpDownHint.isVisible = flag
    }

    interface FileCreator<T> {

        fun createFile(name: String, templateName: String): T?

        fun getActionName(name: String, templateName: String): String
    }

    class Builder(private val myDialog: CreateFileFromTemplateDialog, private val myProject: Project) {

        fun ktFile(ktFile: KtFile?): Builder{
            myDialog.ktFile = ktFile
            return this
        }


        fun title(title: String): Builder {
            myDialog.title = title
            return this
        }

        fun addKind(name: String, icon: Icon?, templateName: String): Builder {
            myDialog.kindCombo.addItem(name, icon, templateName)
            if (myDialog.kindCombo.comboBox.itemCount > 1) {
                myDialog.setTemplateKindComponentsVisible(true)
            }
            return this
        }

        fun setValidator(validator: InputValidator): Builder {
            myDialog.myInputValidator = validator
            return this
        }

        fun <T : PsiElement> show(errorTitle: String, selectedItem: String?, creator: FileCreator<T>): T? {
            val created = Ref.create<T>(null)
            myDialog.kindCombo.setSelectedName(selectedItem)
            myDialog.myCreator = object : ElementCreator(myProject, errorTitle) {

                override fun create(fileName: String): Array<PsiElement> {
                    (myDialog.ktFile?:creator.createFile(myDialog.enteredName, myDialog.kindCombo.selectedName))?.let { element ->
                        val ktFile = element as KtFile
                        val json = myDialog.dataSourceInput.text

                        val ktClass = ktFile.children.firstOrNull { it is KtClass && it.name == fileName }?.let { existsKtClass ->
                            println("Delete original element: $existsKtClass")

                            val detachedKtClass = KtPsiFactory(myProject).createClass("""
                                /**
                                 * Updated by ${SystemProperties.getUserName()} on ${System.currentTimeMillis().toDate().format("yyyy-MM-dd")}
                                 */
                                data class $fileName()
                            """.trimIndent())

                            (ktFile.addBefore(detachedKtClass, existsKtClass) as KtClass).also {
                                existsKtClass.delete()
                            }
                        } ?: run {
                            val detachedKtClass = KtPsiFactory(myProject).createClass("""
                                /**
                                 * Created by ${SystemProperties.getUserName()} on ${System.currentTimeMillis().toDate().format("yyyy-MM-dd")}
                                 */
                                data class $fileName()
                            """.trimIndent())
                            ktFile.add(detachedKtClass) as KtClass
                        }

                        JsonParser(json, ktClass).parse()
                        created.set(element as T)
                        return arrayOf(element)
                    }
                    return PsiElement.EMPTY_ARRAY
                }

                override fun getActionName(newName: String): String {
                    return creator.getActionName(newName, myDialog.kindCombo.getSelectedName())
                }
            }

            myDialog.show()
            if (myDialog.exitCode == DialogWrapper.OK_EXIT_CODE) {
                return created.get()
            }
            return null
        }

        val customProperties: Map<String, String>? = null
    }

    companion object {
        fun createDialog(project: Project): Builder {
            val dialog = CreateFileFromTemplateDialog(project)
            return Builder(dialog, project)
        }
    }
}
