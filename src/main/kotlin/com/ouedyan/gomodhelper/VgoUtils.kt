package com.ouedyan.gomodhelper

import com.goide.vgo.mod.psi.VgoModuleSpec
import com.goide.vgo.mod.psi.VgoTypes
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType

object VgoUtils {
    fun isModuleIdentifier(element: PsiElement?): Boolean {
        return runReadAction {
            val parent = element?.parent
            parent is VgoModuleSpec && element.elementType == VgoTypes.IDENTIFIER && PsiManager.getInstance(
                parent.project
            ).areElementsEquivalent(parent.identifier, element)
        }
    }

    fun isModuleVersion(element: PsiElement?): Boolean {
        return runReadAction {
            val parent = element?.parent
            parent is VgoModuleSpec && element.elementType == VgoTypes.IDENTIFIER && PsiManager.getInstance(
                parent.project
            ).areElementsEquivalent(parent.moduleVersion, element)
        }
    }
}
