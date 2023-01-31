package com.ouedyan.gomodhelper

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.vfs.VirtualFile

object GoUtils {
    fun findFileInModuleDepsRoot(depsRoot: VirtualFile, modulePath: String, regex: Regex): VirtualFile? {
        return runReadAction {
            val moduleDirectory = depsRoot.findFileByRelativePath(modulePath)
            moduleDirectory?.children?.forEach {
                if (regex.containsMatchIn(it.name))
                    return@runReadAction it
            }
            null
        }
    }
}
