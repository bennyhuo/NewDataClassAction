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

package cn.kotliner.dataclass.action

import cn.kotliner.dataclass.common.no
import cn.kotliner.dataclass.common.yes
import cn.kotliner.dataclass.logic.JsonParser
import cn.kotliner.dataclass.ui.toast
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
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.json.JSONArray
import org.json.JSONObject
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * @author peter
 */
class CreateFileFromTemplateDialog protected constructor(private val project: Project) : DialogWrapper(project, true) {
    protected lateinit var nameField: JTextField
    protected lateinit var kindCombo: TemplateKindCombo
    private lateinit var myPanel: JPanel
    private lateinit var myUpDownHint: JLabel
    protected lateinit var kindLabel: JLabel
    protected lateinit var nameLabel: JLabel
    private lateinit var dataSourceInput: JTextArea
    private lateinit var formatJson: JButton

    private var myCreator: ElementCreator? = null
    private var myInputValidator: InputValidator? = null

    init {
        kindLabel.labelFor = kindCombo
        kindCombo.registerUpDownHint(nameField)
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
        myInputValidator?.let {
            val text = nameField.text.trim { it <= ' ' }
            it.canClose(text).no {
                val errorText = (it as? InputValidatorEx)?.getErrorText(text) ?: LangBundle.message("incorrect.name")
                return ValidationInfo(errorText, nameField)
            }
        }
        return super.doValidate()
    }

    private val enteredName: String
        get() {
            return nameField.text.trim { it <= ' ' }.apply { nameField.text = this }
        }

    override fun createCenterPanel(): JComponent? {
        return myPanel
    }

    override fun doOKAction() {
        //必须输入正确的 json 字符串才行
        if (!formatJson()) {
            return
        }
        myCreator?.let {
            it.tryCreate(enteredName).size > 0
        }?.yes {
            super.doOKAction()
        }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return nameField
    }

    fun setTemplateKindComponentsVisible(flag: Boolean) {
        kindCombo.isVisible = flag
        kindLabel.isVisible = flag
        myUpDownHint.isVisible = flag
    }

    interface Builder {
        fun setTitle(title: String): Builder

        fun setValidator(validator: InputValidator): Builder

        fun addKind(kind: String, icon: Icon?, templateName: String): Builder

        fun <T : PsiElement> show(errorTitle: String, selectedItem: String?, creator: FileCreator<T>): T?

        val customProperties: Map<String, String>?
    }

    interface FileCreator<T> {

        fun createFile(name: String, templateName: String): T?

        fun getActionName(name: String, templateName: String): String
    }

    private class BuilderImpl(private val myDialog: CreateFileFromTemplateDialog, private val myProject: Project) : Builder {

        override fun setTitle(title: String): Builder {
            myDialog.title = title
            return this
        }

        override fun addKind(name: String, icon: Icon?, templateName: String): Builder {
            myDialog.kindCombo.addItem(name, icon, templateName)
            if (myDialog.kindCombo.comboBox.itemCount > 1) {
                myDialog.setTemplateKindComponentsVisible(true)
            }
            return this
        }

        override fun setValidator(validator: InputValidator): Builder {
            myDialog.myInputValidator = validator
            return this
        }

        override fun <T : PsiElement> show(errorTitle: String, selectedTemplateName: String?,
                                           creator: FileCreator<T>): T? {
            val created = Ref.create<T>(null)
            myDialog.kindCombo.setSelectedName(selectedTemplateName)
            myDialog.myCreator = object : ElementCreator(myProject, errorTitle) {

                @Throws(Exception::class)
                override fun create(newName: String): Array<PsiElement> {
                    val element = creator.createFile(myDialog.enteredName, myDialog.kindCombo.getSelectedName())
                    val json = myDialog.dataSourceInput.text
                    val ktFile = element as KtFile?
                    val ktClass = (ktFile!!.classes[0] as KtLightClass).kotlinOrigin as KtClass?
                    JsonParser(json, ktClass).parse()
                    created.set(element)
                    if (element != null) {
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

        override val customProperties: Map<String, String>? = null
    }

    companion object {
        fun createDialog(project: Project): Builder {
            val dialog = CreateFileFromTemplateDialog(project)
            return BuilderImpl(dialog, project)
        }
    }
}
