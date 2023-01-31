package com.ouedyan.gomodhelper.inspections.updateGoModDependencyToLatestVersion

import com.goide.vgo.mod.psi.VgoModuleSpec
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class InputDependencies(val project: Project, val goMod: VirtualFile, val moduleSpecs: List<VgoModuleSpec>)
