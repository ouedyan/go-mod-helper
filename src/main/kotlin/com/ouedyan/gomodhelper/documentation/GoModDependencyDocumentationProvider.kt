package com.ouedyan.gomodhelper.documentation

import com.goide.util.GoUtil
import com.goide.vgo.VgoUtil
import com.goide.vgo.mod.psi.VgoModuleSpec
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.ouedyan.gomodhelper.GoUtils
import com.ouedyan.gomodhelper.VgoUtils

class GoModDependencyDocumentationProvider : DocumentationProvider, ExternalDocumentationProvider {
    companion object {
        private fun findReadme(depsRoot: VirtualFile, modulePath: String): VirtualFile? {
            return GoUtils.findFileInModuleDepsRoot(
                depsRoot,
                modulePath,
                Regex("^readme", RegexOption.IGNORE_CASE)
            )

        }

    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return if (VgoUtils.isModuleIdentifier(originalElement)) "<h1>${originalElement?.text}</h1>"
        else null
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return null
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
        if (!VgoUtils.isModuleIdentifier(originalElement)) return null
        return mutableListOf("${DocUtils.URL_PREFIX}${originalElement?.text}")
    }

    override fun getCustomDocumentationElement(
        editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int
    ): PsiElement? {
        return contextElement
    }

    override fun fetchExternalDocumentation(
        project: Project?, element: PsiElement?, docUrls: MutableList<String>?, onHover: Boolean
    ): String? {
        if (project == null || element == null) return null
        if (!VgoUtils.isModuleIdentifier(element)) return null
        val moduleSpec = runReadAction { element.parentOfType<VgoModuleSpec>() }
        val moduleName = moduleSpec?.identifier?.text ?: return null
        val module = GoUtil.module(element)
        val depsRoot = VgoUtil.getDependenciesRoot(project, module) ?: return null
        val modulePath = "$moduleName@${moduleSpec.moduleVersion?.text}"
        val doc = findReadme(depsRoot, modulePath)
        return DocUtils.generateHtml(doc)
    }


    @Deprecated(
        "Deprecated",
        ReplaceWith("VgoUtils.isModuleIdentifier(element)", "com.ouedyan.gomodhelper.VgoUtils"),

        )
    override fun hasDocumentationFor(element: PsiElement?, originalElement: PsiElement?) =
        VgoUtils.isModuleIdentifier(element)

    override fun canPromptToConfigureDocumentation(element: PsiElement?) = false

    override fun promptToConfigureDocumentation(element: PsiElement?) {
    }
}
