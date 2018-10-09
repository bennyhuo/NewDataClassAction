package com.bennyhuo.dataclass.action

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinIcons

/**
 * Created by benny on 6/30/17.
 */
class NewKotlinDataClassAction
    : CreateFileFromTemplateAction("Kotlin Data Class", "Creates new Kotlin Data Class", KotlinFileType.INSTANCE.icon),
        DumbAware{

    override fun getActionName(directory: PsiDirectory?, newName: String?, templateName: String?) = "Kotlin Data Class"

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        //template name - see resources fileTemplates.internal
        builder.addKind("DataClass", KotlinIcons.CLASS,  "Kotlin Data Class")
            .setTitle("Create Kotlin Data Class")
    }

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (super.isAvailable(dataContext)) {
            val ideView = LangDataKeys.IDE_VIEW.getData(dataContext)!!
            val project = PlatformDataKeys.PROJECT.getData(dataContext)!!
            val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
            //This determines when our action should appear. Only when you right click on the source files.
            return ideView.directories.any { projectFileIndex.isInSourceContent(it.virtualFile) }
        }
        return false
    }

    override fun hashCode() = 0

    override fun equals(other: Any?) = other is NewKotlinDataClassAction
}