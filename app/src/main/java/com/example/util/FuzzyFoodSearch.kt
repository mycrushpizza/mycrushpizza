package com.example.util

import com.example.data.model.MenuItem
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Typo-tolerant and phonetic/Hinglish-aware food search engine for My Crush Pizza – Hajipur.
 * Handles spelling mistakes (e.g. "piza", "paner", "magi", "chiken", "birani", "bargr", "chomin",
 * "sandwitch", "kulad", "margrita", "watr", "spong", "partha", "manchuryan") using:
 * 1. Exact & substring matching
 * 2. Phonetic & Hinglish alias normalization
 * 3. Character bigram Dice similarity
 * 4. Bounded Levenshtein edit distance & subsequence matching
 */
object FuzzyFoodSearch {

    private val commonFoodAliases: Map<String, String> = mapOf(
        "piza" to "pizza",
        "pijja" to "pizza",
        "piiza" to "pizza",
        "pizaa" to "pizza",
        "paner" to "paneer",
        "pneer" to "paneer",
        "panir" to "paneer",
        "panner" to "paneer",
        "magi" to "maggi",
        "maggie" to "maggi",
        "meggi" to "maggi",
        "megi" to "maggi",
        "chiken" to "chicken",
        "chikn" to "chicken",
        "chikan" to "chicken",
        "cikn" to "chicken",
        "birani" to "biryani",
        "biriyani" to "biryani",
        "briyani" to "biryani",
        "biryani" to "biryani",
        "bargr" to "burger",
        "bargar" to "burger",
        "burgar" to "burger",
        "berger" to "burger",
        "chomin" to "chowmin",
        "chaumin" to "chowmin",
        "chowmein" to "chowmin",
        "choumin" to "chowmin",
        "chawmin" to "chowmin",
        "noodle" to "noodles",
        "nudels" to "noodles",
        "noodels" to "noodles",
        "sandwitch" to "sandwich",
        "sanwich" to "sandwich",
        "sendwich" to "sandwich",
        "sndwich" to "sandwich",
        "kulad" to "kulhad",
        "kullad" to "kulhad",
        "kuld" to "kulhad",
        "kulhar" to "kulhad",
        "margrita" to "margherita",
        "margarita" to "margherita",
        "margreta" to "margherita",
        "cheez" to "cheese",
        "chese" to "cheese",
        "chiz" to "cheese",
        "chees" to "cheese",
        "partha" to "paratha",
        "pratha" to "paratha",
        "parantha" to "paratha",
        "nan" to "naan",
        "manchuryan" to "manchurian",
        "manchuran" to "manchurian",
        "manchurian" to "manchurian",
        "rasgula" to "rasgulla",
        "rasgulla" to "sponges",
        "spong" to "sponges",
        "sponge" to "sponges",
        "watr" to "water",
        "minral" to "mineral",
        "colddrink" to "coldrink",
        "cold" to "coldrink",
        "drink" to "coldrink",
        "shak" to "shake",
        "shek" to "shake",
        "kitkat" to "kit kat",
        "bnana" to "banana",
        "banan" to "banana",
        "garlik" to "garlic",
        "bred" to "bread",
        "lavacake" to "lava",
        "chokolate" to "chocolate",
        "choclate" to "chocolate",
        "mushrom" to "mushroom",
        "masroom" to "mushroom",
        "mashroom" to "mushroom",
        "kabab" to "kabab",
        "kebab" to "kabab",
        "tikka" to "tikka",
        "tika" to "tikka",
        "tandoi" to "tandoori",
        "tanduri" to "tandoori",
        "omlet" to "omlet",
        "omelet" to "omlet",
        "omelette" to "omlet",
        "pulaav" to "pulao",
        "pulav" to "pulao",
        "raita" to "raita",
        "rayta" to "raita",
        "papar" to "papad"
    )

    const val CATEGORY_REGULAR_PIZZA_89 = "Regular Pizza ₹89"
    const val CATEGORY_MENU_99 = "Menu ₹99"

    fun isPizzaItem(item: MenuItem): Boolean {
        if (item.id.startsWith("starter_")) return false
        return item.supportsMultipleSizes() ||
            item.title.contains("Pizza", ignoreCase = true) ||
            item.id.startsWith("teaser_") ||
            item.id.startsWith("veg_") ||
            item.id.startsWith("cheese_") ||
            item.id.startsWith("special_")
    }

    fun filterAndRank(
        items: List<MenuItem>,
        selectedCategory: String,
        rawQuery: String,
        vegOnly: Boolean
    ): List<MenuItem> {
        val vegFiltered = if (vegOnly) items.filter { it.isVeg } else items
        val trimmedQuery = rawQuery.trim()

        if (trimmedQuery.isEmpty()) {
            return when {
                selectedCategory.equals("All", ignoreCase = true) -> vegFiltered
                selectedCategory.equals(CATEGORY_REGULAR_PIZZA_89, ignoreCase = true) -> {
                    vegFiltered
                        .filter { isPizzaItem(it) && it.priceRegularInr >= 89 }
                        .sortedWith(
                            compareBy<MenuItem> { it.priceRegularInr }
                                .thenBy { it.title.lowercase() }
                        )
                }
                selectedCategory.equals(CATEGORY_MENU_99, ignoreCase = true) -> {
                    vegFiltered
                        .filter { it.priceRegularInr <= 99 }
                        .sortedWith(
                            compareBy<MenuItem> { it.priceRegularInr }
                                .thenBy { it.title.lowercase() }
                        )
                }
                selectedCategory.equals("Kulhad Pizza", ignoreCase = true) -> {
                    vegFiltered.filter {
                        it.id.contains("kulhad_pizza", ignoreCase = true) ||
                            it.title.contains("Kulhad Pizza", ignoreCase = true)
                    }
                }
                else -> vegFiltered.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
        }

        // When searching, always search across all available dishes so any food item is found
        return scoreItems(vegFiltered, trimmedQuery, selectedCategory)
    }

    fun didYouMeanSuggestion(rawQuery: String, matchedItems: List<MenuItem>): String? {
        val clean = normalizeText(rawQuery)
        if (clean.length < 2 || matchedItems.isEmpty()) return null
        val topTitle = matchedItems.first().title
        if (topTitle.contains(rawQuery.trim(), ignoreCase = true)) return null
        return topTitle
    }

    private fun scoreItems(
        items: List<MenuItem>,
        rawQuery: String,
        preferredCategory: String = "All"
    ): List<MenuItem> {
        val cleanQuery = normalizeText(rawQuery)
        if (cleanQuery.isEmpty()) return items

        val queryTokens = cleanQuery.split(" ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { commonFoodAliases[it] ?: it }

        if (queryTokens.isEmpty()) return items

        val scored = mutableListOf<Pair<MenuItem, Int>>()

        for (item in items) {
            var score = scoreMenuItem(item, cleanQuery, queryTokens)
            if (score > 0 && !preferredCategory.equals("All", ignoreCase = true) &&
                item.category.equals(preferredCategory, ignoreCase = true)
            ) {
                score += 20
            }
            if (score > 0) {
                scored += item to score
            }
        }

        return scored
            .sortedWith(
                compareByDescending<Pair<MenuItem, Int>> { it.second }
                    .thenBy { it.first.title.length }
                    .thenBy { it.first.priceRegularInr }
            )
            .map { it.first }
    }

    private fun scoreMenuItem(
        item: MenuItem,
        cleanFullQuery: String,
        queryTokens: List<String>
    ): Int {
        val titleNorm = normalizeText(item.title)
        val subCatNorm = normalizeText(item.displaySubCategoryLabel())
        val catNorm = normalizeText(item.category)
        val descNorm = normalizeText(item.description)
        val titleWords = titleNorm.split(" ").filter { it.isNotEmpty() }

        // Direct exact / prefix / word-prefix / substring boost so the closest food is ALWAYS #1 at the top
        if (titleNorm == cleanFullQuery) {
            return 1200
        }
        if (titleNorm.startsWith(cleanFullQuery)) {
            return 1000
        }
        if (titleWords.any { it.startsWith(cleanFullQuery) }) {
            return 900
        }
        if (titleNorm.contains(cleanFullQuery)) {
            return 850
        }
        val metaWords = (subCatNorm.split(" ") + catNorm.split(" ") + descNorm.split(" "))
            .filter { it.length >= 2 }
            .distinct()
        val allWords = (titleWords + metaWords).distinct()

        val titlePhoneticWords = titleWords.map { phoneticKey(it) }
        val allPhoneticWords = allWords.map { phoneticKey(it) }

        var totalScore = 0
        var matchedTokenCount = 0

        for (rawToken in queryTokens) {
            val token = commonFoodAliases[rawToken] ?: rawToken
            val tokenPhonetic = phoneticKey(token)
            var bestTokenScore = 0

            // 1. Exact or substring match in title
            if (titleNorm.contains(token)) {
                bestTokenScore = max(bestTokenScore, if (titleWords.any { it.startsWith(token) }) 320 else 260)
            }

            // 2. Exact or substring match in category / subcategory / description
            if (bestTokenScore < 200 && (subCatNorm.contains(token) || catNorm.contains(token) || descNorm.contains(token))) {
                bestTokenScore = max(bestTokenScore, 190)
            }

            // 3. Phonetic match in title or metadata
            if (bestTokenScore < 220 && tokenPhonetic.length >= 2) {
                if (titlePhoneticWords.any { it.contains(tokenPhonetic) || tokenPhonetic.contains(it) }) {
                    bestTokenScore = max(bestTokenScore, 225)
                } else if (allPhoneticWords.any { it.contains(tokenPhonetic) }) {
                    bestTokenScore = max(bestTokenScore, 155)
                }
            }

            // 4. Word-by-word fuzzy edit distance & bigram similarity against title words
            for (word in titleWords) {
                val fuzzy = computeWordFuzzyScore(token, tokenPhonetic, word)
                if (fuzzy > 0) {
                    bestTokenScore = max(bestTokenScore, fuzzy + 60) // Title boost
                }
            }

            // 5. Word-by-word fuzzy edit distance against category / subcategory / description words
            if (bestTokenScore == 0) {
                for (word in metaWords) {
                    val fuzzy = computeWordFuzzyScore(token, tokenPhonetic, word)
                    if (fuzzy > 0) {
                        bestTokenScore = max(bestTokenScore, fuzzy)
                    }
                }
            }

            if (bestTokenScore > 0) {
                matchedTokenCount++
                totalScore += bestTokenScore
            }
        }

        // Require all tokens to match if 1-2 tokens, or at least ceil(70%) of tokens for longer queries
        val minRequiredTokens = if (queryTokens.size <= 2) queryTokens.size else (queryTokens.size - 1)
        if (matchedTokenCount < minRequiredTokens) {
            return 0
        }

        return totalScore
    }

    private fun computeWordFuzzyScore(
        queryToken: String,
        queryPhonetic: String,
        candidateWord: String
    ): Int {
        if (queryToken.length <= 1 || candidateWord.length <= 1) return 0

        // Prefix match (e.g. "marg" -> "margherita")
        if (candidateWord.startsWith(queryToken) || queryToken.startsWith(candidateWord)) {
            val minLen = min(queryToken.length, candidateWord.length)
            if (minLen >= 3) return 170
        }

        // Phonetic equality or close phonetic edit distance
        val candPhonetic = phoneticKey(candidateWord)
        if (queryPhonetic.length >= 3 && candPhonetic == queryPhonetic) {
            return 185
        }
        if (queryPhonetic.length >= 3 && candPhonetic.length >= 3) {
            val phonDist = levenshteinDistance(queryPhonetic, candPhonetic)
            if (phonDist <= 1) return 150
        }

        // Levenshtein edit distance on normalized word
        val maxLen = max(queryToken.length, candidateWord.length)
        val lenDiff = abs(queryToken.length - candidateWord.length)
        if (lenDiff <= 3) {
            val dist = levenshteinDistance(queryToken, candidateWord)
            val allowedDist = when {
                maxLen <= 3 -> 1
                maxLen <= 6 -> 2
                else -> 3
            }
            if (dist <= allowedDist) {
                // Higher score for smaller edit distance
                return (160 - dist * 35).coerceAtLeast(50)
            }

            // Also check prefix of candidateWord if queryToken is shorter with a typo (e.g., "margr" vs "margherita")
            if (queryToken.length in 4..candidateWord.length) {
                val prefixCandidate = candidateWord.take(queryToken.length)
                val prefixDist = levenshteinDistance(queryToken, prefixCandidate)
                if (prefixDist <= 1) {
                    return 135
                }
            }
        }

        // Bigram Dice similarity for transposed or jumbled letters
        if (queryToken.length >= 3 && candidateWord.length >= 3) {
            val dice = bigramDiceSimilarity(queryToken, candidateWord)
            if (dice >= 0.50f) {
                return (dice * 150).toInt()
            }
        }

        // Ordered character subsequence match when first letter matches (e.g. "mrgta" -> "margherita", "brgr" -> "burger")
        if (queryToken.length >= 3 &&
            queryToken.first() == candidateWord.first() &&
            isSubsequence(queryToken, candidateWord) &&
            queryToken.length * 2 >= candidateWord.length
        ) {
            return 95
        }

        return 0
    }

    private fun normalizeText(input: String): String {
        return input
            .lowercase()
            .replace("-", " ")
            .replace("/", " ")
            .replace("&", " and ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Produces a phonetic skeleton so Hinglish/Indian food spelling variations map together.
     */
    private fun phoneticKey(word: String): String {
        if (word.isEmpty()) return ""
        var s = word.lowercase()
        s = s.replace("ph", "f")
            .replace("chh", "ch")
            .replace("sh", "s")
            .replace("ck", "k")
            .replace("qu", "k")
            .replace("v", "w")
            .replace("z", "j")
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("aa", "a")
            .replace("ou", "au")
            .replace("ow", "au")

        // Collapse consecutive duplicate characters ("pizzza" -> "piza", "maggi" -> "magi")
        val collapsed = StringBuilder()
        for (ch in s) {
            if (collapsed.isEmpty() || collapsed.last() != ch) {
                collapsed.append(ch)
            }
        }
        return collapsed.toString()
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var prev = IntArray(b.length + 1) { it }
        var curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            val ca = a[i - 1]
            for (j in 1..b.length) {
                val cost = if (ca == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,      // insertion
                    prev[j] + 1,          // deletion
                    prev[j - 1] + cost    // substitution
                )
                // Damerau transposition of adjacent characters
                if (i > 1 && j > 1 && ca == b[j - 2] && a[i - 2] == b[j - 1]) {
                    curr[j] = min(curr[j], prev[j - 1])
                }
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[b.length]
    }

    private fun bigramDiceSimilarity(a: String, b: String): Float {
        if (a.length < 2 || b.length < 2) return 0f
        val bigramsA = (0 until a.length - 1).map { a.substring(it, it + 2) }.toSet()
        val bigramsB = (0 until b.length - 1).map { b.substring(it, it + 2) }.toSet()
        val intersection = bigramsA.intersect(bigramsB).size
        return (2f * intersection) / (bigramsA.size + bigramsB.size).toFloat()
    }

    private fun isSubsequence(sub: String, full: String): Boolean {
        var i = 0
        var j = 0
        while (i < sub.length && j < full.length) {
            if (sub[i] == full[j]) i++
            j++
        }
        return i == sub.length
    }
}
