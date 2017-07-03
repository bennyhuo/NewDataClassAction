package cn.kotliner.dataclass.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import javax.swing.JComponent

/**
 * Display simple notification of given type

 * @param project
 * *
 * @param type
 * *
 * @param text
 */
fun JComponent.toast(text: String, type: MessageType = MessageType.ERROR) {
    JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(text, type, null)
            .setFadeoutTime(7500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(this), Balloon.Position.below)
}

/**
 * Display simple notification of given type

 * @param project
 * *
 * @param type
 * *
 * @param text
 */
fun make(project: Project, type: MessageType, text: String) {

    val statusBar = WindowManager.getInstance().getStatusBar(project)

    JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(text, type, null)
            .setFadeoutTime(7500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)
}
