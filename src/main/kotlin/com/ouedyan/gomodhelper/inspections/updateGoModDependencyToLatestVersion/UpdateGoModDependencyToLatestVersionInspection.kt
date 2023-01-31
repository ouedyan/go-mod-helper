package com.ouedyan.gomodhelper.inspections.updateGoModDependencyToLatestVersion

import com.intellij.codeInspection.ExternalAnnotatorInspectionVisitor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.PairedUnfairLocalInspectionTool
import com.intellij.psi.PsiElementVisitor
import com.ouedyan.gomodhelper.Utils

class UpdateGoModDependencyToLatestVersionInspection : LocalInspectionTool(),
    PairedUnfairLocalInspectionTool {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return ExternalAnnotatorInspectionVisitor(
            holder,
            UpdateGoModDependencyToLatestVersionExternalAnnotator.INSTANCE_FOR_BATCH_INSPECTION,
            isOnTheFly
        )
    }

    override fun getInspectionForBatchShortName(): String {
        return Utils.calcShortNameFromClass(this.javaClass)
    }
}
