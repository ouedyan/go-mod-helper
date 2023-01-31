package com.ouedyan.gomodhelper.inspections.updateGoModDependencyToLatestVersion

import com.goide.vgo.mod.inspections.VgoUnusedDependencyInspection
import com.goide.vgo.mod.psi.VgoModuleSpec
import com.goide.vgo.mod.psi.VgoRequireDirective
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.ouedyan.gomodhelper.Utils

data class UpdateGoModDependencyToLatestVersionExternalAnnotator @JvmOverloads constructor(val onTheFly: Boolean = true) :
    ExternalAnnotator<InputDependencies?, List<DependencyInfo>?>() {

    companion object {
        private val logger = Logger.getInstance(UpdateGoModDependencyToLatestVersionExternalAnnotator::class.java)

        val INSTANCE_FOR_BATCH_INSPECTION = UpdateGoModDependencyToLatestVersionExternalAnnotator(false)

        private fun getHighlightDisplayKeyByClass(inspectionClass: Class<out LocalInspectionTool?>): HighlightDisplayKey {

            val id = InspectionProfileEntry.getShortName(Utils.calcShortNameFromClass(inspectionClass))

            val key = HighlightDisplayKey.find(id) ?: HighlightDisplayKey(id, id)

            return key
        }

        private fun isToolEnabled(
            project: Project, inspectionClass: Class<out LocalInspectionTool?>, file: PsiFile?
        ): Boolean {
            val manager = InspectionProjectProfileManager.getInstance(project)
            val inspectionProfile = manager.currentProfile
            val key = getHighlightDisplayKeyByClass(inspectionClass)
            val tools = inspectionProfile.getToolsOrNull(key.toString(), project)

            return tools != null && tools.isEnabled(file)
        }

        private fun getSeverityForError(
            file: PsiFile, inspectionClass: Class<out LocalInspectionTool?>
        ): HighlightSeverity {
            val inspectionProjectProfileManager = InspectionProjectProfileManager.getInstance(file.project)
            val highlightDisplayKey = getHighlightDisplayKeyByClass(inspectionClass)
            val severity = inspectionProjectProfileManager.currentProfile.getErrorLevel(
                highlightDisplayKey, file as PsiElement
            ).severity
            return severity
        }
    }

    override fun collectInformation(file: PsiFile): InputDependencies? {
        if (onTheFly) {
            if (!isToolEnabled(file.project, UpdateGoModDependencyToLatestVersionInspection::class.java, file)) {
                return null
            }
        }

        val virtualFile = PsiUtilCore.getVirtualFile(file) ?: return null

        val requireDirectives = PsiTreeUtil.getChildrenOfType(file, VgoRequireDirective::class.java)

        val modules = mutableListOf<VgoModuleSpec>()

        requireDirectives?.forEach { requireDirective ->
            val moduleSpecs = PsiTreeUtil.getChildrenOfType(requireDirective, VgoModuleSpec::class.java)
            moduleSpecs?.forEach { module ->
                if (!VgoUnusedDependencyInspection.hasIndirectComment(module)) {
                    modules.add(module)
                }
            }
        }

        return InputDependencies(file.project, virtualFile, modules)
    }

    override fun doAnnotate(collectedInfo: InputDependencies?): List<DependencyInfo>? {
        if (collectedInfo == null) return null

//        val progress = runReadAction<ProgressIndicator?> {
//            if (!ApplicationManager.getApplication().isDisposed) {
//                if (!collectedInfo.project.isDisposed) ProgressManager.getInstance()
//            }
//
//            null
//        }


        val deps = collectedInfo.moduleSpecs.map { it.identifier.text }

        val output = ExecUtil.execAndGetOutput(
            GeneralCommandLine(
                "go", "list", "-m", "-u", *deps.toTypedArray(),
            ).withWorkDirectory(collectedInfo.goMod.parent.path)
        )
        val depsLines = output.stdoutLines

        if (output.stderr.isNotBlank()) {
            logger.error("outputErrors \n${output.stderr}")
        }

        val latestDeps = depsLines.mapIndexed { index, line ->
            val installedVersion = line.split(' ')[1]
            val newerVersionRegex = "\\[(.+)\\]".toRegex()
            val newerVersionMatch = newerVersionRegex.find(line)
            val newerVersion = newerVersionMatch?.groupValues?.get(1)
            DependencyInfo(
                collectedInfo.moduleSpecs[index], installedVersion, newerVersion
            )
        }

        return latestDeps

    }

    override fun apply(file: PsiFile, annotationResult: List<DependencyInfo>?, holder: AnnotationHolder) {
        annotationResult?.forEach { info ->
            if (!info.newerVersion.isNullOrBlank()) {
                val versionElement = info.moduleSpec.moduleVersion ?: return
                val severity = getSeverityForError(
                    file, UpdateGoModDependencyToLatestVersionInspection::class.java
                )
                holder.newAnnotation(
                    severity,
                    "Newer version of '${
                        info.moduleSpec.identifier.text.split('/').last()
                    }' available: ${info.newerVersion}"
                )
                    .range(versionElement)
                    .newFix(
                        UpdateGoModDependencyToLatestVersionIntentionAction(info, versionElement)
                    )
                    .key(getHighlightDisplayKeyByClass(UpdateGoModDependencyToLatestVersionInspection::class.java))
                    .registerFix()
                    .create()
            }
        }
    }

}
