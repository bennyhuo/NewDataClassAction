//package com.bennyhuo.dataclass.action
//
//import com.bennyhuo.dataclass.logic.JsonParser
//import com.intellij.ide.actions.CreateFileFromTemplateDialog
//import com.intellij.ide.actions.ElementCreator
//import com.intellij.openapi.project.Project
//import com.intellij.psi.PsiElement
//import org.jetbrains.kotlin.asJava.classes.KtLightClass
//import org.jetbrains.kotlin.psi.KtClass
//import org.jetbrains.kotlin.psi.KtFile
//
//class DataClassCreator(project: Project, errorTitle: String, val fileCreator: CreateFileFromTemplateDialog.FileCreator<>) : ElementCreator(project, errorTitle) {
//
//    @Throws(Exception::class)
//    override fun create(newName: String): Array<PsiElement> {
//        creator.createFile(myDialog.enteredName, myDialog.kindCombo.selectedName)?.let { element ->
//            val ktFile = element as KtFile
//            val json = myDialog.dataSourceInput.text
//            val ktClass = (ktFile.classes[0] as KtLightClass).kotlinOrigin as KtClass
//            JsonParser(json, ktClass).parse()
//            created.set(element)
//            return arrayOf(element)
//        }
//        return PsiElement.EMPTY_ARRAY
//    }
//
//    override fun getActionName(newName: String): String {
//        return creator.getActionName(newName, myDialog.kindCombo.getSelectedName())
//    }
//}