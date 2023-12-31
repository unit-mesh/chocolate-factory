package org.changelog

fun join(parts: List<String>, joiner: String): String {
    return parts
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(joiner)
}

val nomatchRegex = Regex("(?!.*)")

object RegexParser {
    /**
     * Make the regexes used to parse a commit.
     * @param options
     * @returns Regexes.
     */
    fun getParserRegexes(options: ParserOptions): ParserRegexes {
        val notes = getNotesRegex(options.noteKeywords, options.notesPattern)
        val referenceParts = getReferencePartsRegex(options.issuePrefixes, options.issuePrefixesCaseSensitive)
        val references = getReferencesRegex(options.referenceActions)

        return ParserRegexes(
            notes = notes,
            referenceParts = referenceParts,
            references = references,
            mentions = Regex("@([\\w-]+)")
        )
    }

    private fun getNotesRegex(noteKeywords: List<String>?, notesPattern: ((String) -> Regex)?): Regex {
        val nomatchRegex = Regex("(?!.*)")

        if (noteKeywords == null) {
            return nomatchRegex
        }

        val noteKeywordsSelection = join(noteKeywords, "|")

        if (notesPattern == null) {
            return Regex("^[\\s|*]*($noteKeywordsSelection)[:\\s]+(.*)", setOf(RegexOption.IGNORE_CASE))
        }

        return notesPattern(noteKeywordsSelection)!!
    }

    private fun getReferencePartsRegex(issuePrefixes: List<String>?, issuePrefixesCaseSensitive: Boolean?): Regex {
        if (issuePrefixes == null) {
            return nomatchRegex
        }

        val flags = when (issuePrefixesCaseSensitive) {
            true -> setOf(RegexOption.MULTILINE)
            else -> setOf(RegexOption.IGNORE_CASE)
        }

        return Regex("(?:.*?)??\\s*([\\w-\\.\\/]*?)??(${join(issuePrefixes, "|")})([\\w-]*\\d+)", flags)
    }

    private fun getReferencesRegex(referenceActions: List<String>?): Regex {
        if (referenceActions == null) {
            // matches everything
            return Regex("()(.+)", setOf(RegexOption.IGNORE_CASE))
        }

        val joinedKeywords = join(referenceActions, "|")

        return Regex("($joinedKeywords)(?:\\s+(.*?))(?=(?:$joinedKeywords)|$)", setOf(RegexOption.IGNORE_CASE))
    }
}