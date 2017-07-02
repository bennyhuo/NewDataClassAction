package cn.kotliner.dataclass.action

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
private val log = Logger.getInstance(NewKotlinDataClassAction::class.java)

class NewKotlinDataClassAction: CreateFileFromTemplateAction("Kotlin Data Class",
        "Creates new Kotlin Data Class",
        KotlinFileType.INSTANCE.icon),
        DumbAware{
    override fun getActionName(directory: PsiDirectory?, newName: String?, templateName: String?): String {
        return "Kotlin Data Class"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.addKind("DataClass", KotlinIcons.CLASS,  "Kotlin Data Class")
            .setTitle("Create Kotlin Data Class")
    }

    override fun isAvailable(dataContext: DataContext): Boolean {
        if (super.isAvailable(dataContext)) {
            val ideView = LangDataKeys.IDE_VIEW.getData(dataContext)!!
            val project = PlatformDataKeys.PROJECT.getData(dataContext)!!
            val projectFileIndex = ProjectRootManager.getInstance(project).fileIndex
            return ideView.directories.any { projectFileIndex.isInSourceContent(it.virtualFile) }
        }
        return false
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(obj: Any?): Boolean {
        return obj is NewKotlinDataClassAction
    }

    override fun postProcess(createdElement: PsiFile, templateName: String, customProperties: Map<String, String>?) {
        super.postProcess(createdElement, templateName, customProperties)
//        val ktFile = createdElement as KtFile
//        val ktClass = (ktFile.classes[0] as KtLightClassImpl).kotlinOrigin as KtClass
//        val jsonDialog = JsonDialog(ktClass)
//        jsonDialog.setSize(600, 400)
//        jsonDialog.setLocationRelativeTo(null)
//        jsonDialog.isVisible = true
    }
}