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
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile

/**
 * Created by benny on 6/30/17.
 */
class NewDataClassAction
    : CreateFileFromTemplateAction("New Data Class", "Creates new Kotlin Data Class", Icons.DATA_CLASS_ICON),
        DumbAware {

    private var ktFile: KtFile? = null

    override fun getActionName(directory: PsiDirectory?, newName: String?, templateName: String?) = "Kotlin Data Class"

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.ktFile(ktFile)
    }

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (super.isAvailable(dataContext)) {
            if(CommonDataKeys.PSI_FILE.getData(dataContext) == null){
                val ideView = LangDataKeys.IDE_VIEW.getData(dataContext)!!
                val project = PlatformDataKeys.PROJECT.getData(dataContext)!!
                val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
                //This determines when our action should appear. Only when you right click on the source files.
                return ideView.directories.any { projectFileIndex.isInSourceContent(it.virtualFile) }
            }
        }
        return false
    }

    override fun hashCode() = 0

    override fun equals(other: Any?) = other is NewDataClassAction
}