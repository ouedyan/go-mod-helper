package com.ouedyan.gomodhelper.inspections.updateGoModDependencyToLatestVersion

import com.goide.GoNotifications
import com.goide.sdk.GoSdkService
import com.goide.util.GoExecutor
import com.goide.util.GoHistoryProcessListener
import com.goide.util.GoUtil
import com.goide.vgo.VgoStatusTracker
import com.goide.vgo.VgoUtil
import com.goide.vgo.mod.quickfix.VgoSyncDependencyFix
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications.Bus
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class UpdateGoModDependencyToLatestVersionIntentionAction(
    private val info: DependencyInfo,
    private val versionElement: PsiElement
) : IntentionAction, LowPriorityAction {
    override fun startInWriteAction() = true

    override fun getText() =
        "Update '${info.moduleSpec.identifier.text}' to the latest version: ${info.newerVersion}"

    override fun getFamilyName() = this.text

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val module = GoUtil.module(versionElement) ?: return
        val vgoModulePair =
            VgoUtil.findModuleAndDependencyOfFile(project, module, versionElement.containingFile.virtualFile) ?: return
        val moduleRoot = vgoModulePair.first.root

        if (moduleRoot.isValid) {
            val sdk = GoSdkService.getInstance(project).getSdk(module)
            if (sdk.isValid) {
                val moduleIdentifier = info.moduleSpec.identifier.text
                VgoStatusTracker.startExplicitModuleCacheUpdate(module)
                GoExecutor.`in`(module)
                    .disablePty()
                    .withPresentableName("go get $moduleIdentifier@latest")
                    .withParameters("get", "$moduleIdentifier@latest")
                    .withWorkDirectory(moduleRoot.path)
                    .withPrintingOutputAsStatus()
                    .showNotifications(false, false)
                    .executeWithProgress(
                        true,
                        true,
                        GoHistoryProcessListener(),
                        "go.get.$moduleIdentifier@latest",
                        fun(result) {
                            VgoStatusTracker.finishExplicitModuleCacheUpdate(module)
                            if (result.status == GoExecutor.ExecutionResult.Status.SUCCEEDED) {
                                val notification = GoNotifications.getToolsIntegrationGroup().createNotification(
                                    "$moduleIdentifier updated to ${info.newerVersion}", NotificationType.INFORMATION
                                )
                                Bus.notifyAndHide(notification, project)

                                VgoSyncDependencyFix.syncModules(project, module, moduleRoot, false, true)
                            }
                        })
            }
        }
    }
}
