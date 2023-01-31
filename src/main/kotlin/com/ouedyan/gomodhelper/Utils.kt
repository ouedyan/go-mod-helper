package com.ouedyan.gomodhelper

object Utils {
    fun <T> calcShortNameFromClass(givenClass: Class<T>): String {
        var name = givenClass.name
        var superClass: Class<in T> = givenClass
        while (name.indexOf('$') != -1) {
            superClass = superClass.superclass
            name = superClass.name
        }
        name = name.substring(name.lastIndexOf('.') + 1)

        return name
    }

}
