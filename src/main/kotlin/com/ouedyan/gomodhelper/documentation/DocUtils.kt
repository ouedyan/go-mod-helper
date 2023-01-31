package com.ouedyan.gomodhelper.documentation

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Range
import com.intellij.webSymbols.utils.HtmlMarkdownUtils
import java.io.IOException

object DocUtils {
    const val URL_PREFIX = "https://pkg.go.dev/"

    fun generateHtml(readmeMd: VirtualFile?): String? {
        return if (readmeMd == null) {
            null
        } else {
            try {
                var markdown = loadText(readmeMd)
                if (StringUtil.isEmptyOrSpaces(markdown)) {
                    null
                } else {
                    markdown = removeUselessImageLinks(markdown)
                    // TODO
                    HtmlMarkdownUtils.toHtml(markdown)
                }
            } catch (e: IOException) {
                null
            }
        }
    }

    @Throws(IOException::class)
    fun loadText(file: VirtualFile): String {
        val document = FileDocumentManager.getInstance().getCachedDocument(file)
        return document?.text ?: VfsUtilCore.loadText(file)
    }

    fun removeUselessImageLinks(markdown: String): String {
        val lines = listOf(*StringUtil.splitByLinesDontTrim(markdown))
        val uselessLines = findUselessLineRange(lines)
        val result = mutableListOf<String>()
        for (i in lines.indices) {
            if (uselessLines == null || !uselessLines.isWithin(i)) {
                val line = lines[i]
                val usefulLine = clearUselessLinks(line)
                if (StringUtil.isEmptyOrSpaces(line) || !StringUtil.isEmptyOrSpaces(usefulLine)) {
                    result.add(usefulLine)
                }
            }
        }
        return StringUtil.join(result, "\n")
    }

    fun findUselessLineRange(lines: List<String>): Range<Int>? {
        var range = findImageLinkRange(lines, 0)
        while (range != null) {
            var found = false
            for (i in range.from..range.to) {
                val line = lines[i]
                if (isUselessImageLink(line, TextRange(0, line.length))) {
                    found = true
                    break
                }
            }
            if (found) {
                return range
            }
            range = findImageLinkRange(lines, range.to + 1)
        }
        return null
    }

    fun clearUselessLinks(line: String): String {
        var link = findUselessLink(line, 0)
        return if (link == null) {
            line
        } else {
            var lastLinkEnd = 0
            val result = StringBuilder()
            while (link != null) {
                result.append(line, lastLinkEnd, link.startOffset)
                lastLinkEnd = link.endOffset
                link = findUselessLink(line, lastLinkEnd)
            }
            result.append(line.substring(lastLinkEnd))
            result.toString()
        }
    }

    fun findUselessLink(text: String, startIndex: Int): TextRange? {
        var link = findImageLink(text, startIndex)
        while (link != null && !isUselessImageLink(text, link)) {
            link = findImageLink(text, link.endOffset)
        }
        return link
    }

    fun isUselessImageLink(text: String, link: TextRange) = true

    fun findImageLink(text: String, startIndex: Int): TextRange? {
        var htmlImgRange: TextRange? = null
        val htmlImgStartInd = text.indexOf("<img ", startIndex)
        var mdStartInd: Int
        if (htmlImgStartInd >= 0) {
            mdStartInd = text.indexOf(">", htmlImgStartInd)
            if (mdStartInd != -1) {
                htmlImgRange = TextRange(htmlImgStartInd, mdStartInd + 1)
            }
        }
        mdStartInd = text.indexOf("![", startIndex)
        return if (mdStartInd >= 0 && (htmlImgRange == null || htmlImgRange.startOffset >= mdStartInd)) {
            var titleOpeningInd = mdStartInd + 1
            if (mdStartInd > 0 && text[mdStartInd - 1] == '[') {
                titleOpeningInd = mdStartInd - 1
                mdStartInd = titleOpeningInd
            }
            val titleClosingInd = findClosingInd(text, titleOpeningInd)
            if (titleClosingInd != -1) {
                if (!StringUtil.isChar(text, titleClosingInd + 1, '(')) {
                    return TextRange(mdStartInd, titleClosingInd + 1)
                }
                val urlClosingInd = findClosingInd(text, titleClosingInd + 1)
                if (urlClosingInd != -1) {
                    return TextRange(mdStartInd, urlClosingInd + 1)
                }
            }
            null
        } else {
            htmlImgRange
        }
    }

    fun findClosingInd(text: String, openingInd: Int): Int {
        var squareBrackets = 0
        var parentheses = 0
        var i = openingInd
        var empty = true
        while (i < text.length && (empty || squareBrackets > 0 || parentheses > 0)) {
            when (text[i]) {
                '[' -> {
                    ++squareBrackets
                }

                '(' -> {
                    ++parentheses
                }

                ']' -> {
                    --squareBrackets
                }

                ')' -> {
                    --parentheses
                }
            }
            if (squareBrackets < 0 || parentheses < 0) {
                return -1
            }
            if (empty && squareBrackets == 0 && parentheses == 0) {
                return -1
            }
            ++i
            empty = false
        }
        return if (!empty && squareBrackets == 0 && parentheses == 0) i - 1 else -1
    }

    fun findImageLinkRange(lines: List<String>, startInd: Int): Range<Int>? {
        var rangeStartInd = -1
        var rangeEndInd = -1
        for (i in startInd until lines.size) {
            val line = lines[i]
            if (!StringUtil.isEmptyOrSpaces(line)) {
                if (isImageLinks(line)) {
                    if (rangeStartInd == -1) {
                        rangeStartInd = i
                    }
                    rangeEndInd = i
                } else if (rangeStartInd != -1) {
                    break
                }
            }
        }
        return if (rangeStartInd == -1) null else Range<Int>(rangeStartInd, rangeEndInd)
    }

    fun isImageLinks(line: String): Boolean {
        var i = 0
        var link: TextRange?
        while (i < line.length) {
            while (i < line.length && Character.isWhitespace(line[i])) {
                ++i
            }
            link = findImageLink(line, i)
            if (link == null || link.startOffset != i) {
                break
            }
            i = link.endOffset
        }
        while (i < line.length && Character.isWhitespace(line[i])) {
            ++i
        }
        return i == line.length
    }

}
