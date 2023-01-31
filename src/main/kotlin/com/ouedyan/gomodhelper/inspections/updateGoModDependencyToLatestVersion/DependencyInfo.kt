package com.ouedyan.gomodhelper.inspections.updateGoModDependencyToLatestVersion

import com.goide.vgo.mod.psi.VgoModuleSpec

data class DependencyInfo(val moduleSpec: VgoModuleSpec, val installedVersion: String, val newerVersion: String?)
