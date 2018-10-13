package com.bennyhuo.dataclass.action

import com.bennyhuo.dataclass.common.Icons
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinIcons
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

/**
 * Created by benny on 6/30/17.
 */
class UpdateDataClassAction
    : CreateFileFromTemplateAction("Update Data Class", "Update this Data Class", Icons.DATA_CLASS_ICON),
        DumbAware {

    private var ktFile: KtFile? = null

    override fun getActionName(directory: PsiDirectory?, newName: String?, templateName: String?) = "Kotlin Data Class"

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.ktFile(ktFile)
    }

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (super.isAvailable(dataContext)) {
            val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext)
            if (psiFile?.language == KotlinLanguage.INSTANCE) {
                return (psiFile as KtFile).children.any { it is KtClass &&  it.isData() && it.name == it.containingKtFile.virtualFile.nameWithoutExtension }
                        .also {
                            ktFile = psiFile
                        }
            }
        }
        return false
    }

    override fun hashCode() = 0

    override fun equals(other: Any?) = other is UpdateDataClassAction
}