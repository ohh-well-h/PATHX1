package com.example.pathx01.data.dictionary

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import android.content.SharedPreferences
import com.example.pathx01.utils.UserPreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import android.widget.Toast

data class DictionaryEntry(
    val word: String,
    val meaning: String
)

private val exampleDictionary = listOf(
    DictionaryEntry("apple", "A round fruit with red or green skin and a whitish interior"),
    DictionaryEntry("banana", "A long, yellow fruit with soft, sweet flesh and a peel"),
    DictionaryEntry("cat", "A small, domesticated carnivorous mammal with soft fur and retractable claws"),
    DictionaryEntry("dog", "A domesticated carnivorous mammal with a barking or howling voice"),
    DictionaryEntry("run", "To move at a speed faster than a walk, never having both or all the feet on the ground at the same time")
)

private fun shouldUseExampleData(context: Context): Boolean {
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    // Only load example dictionary if the explicit flag is set by user action
    return prefs.getBoolean("use_example_dictionary", false)
}

private fun getSearchHistory(context: Context): MutableList<String> {
    val prefs = context.getSharedPreferences("dictionary_history", Context.MODE_PRIVATE)
    val str = prefs.getString("history", null)
    return if (str != null) str.split('|').filter { it.isNotBlank() }.toMutableList() else mutableListOf()
}

private fun saveSearchHistory(context: Context, history: List<String>) {
    val prefs = context.getSharedPreferences("dictionary_history", Context.MODE_PRIVATE)
    prefs.edit().putString("history", history.joinToString("|")).apply()
}

private fun clearSearchHistory(context: Context) {
    val prefs = context.getSharedPreferences("dictionary_history", Context.MODE_PRIVATE)
    prefs.edit().putString("history", "").apply()
}

private val wordnetIndexFiles = listOf(
    "index.noun", "index.verb", "index.adj", "index.adv"
)
private val wordnetDataFiles = listOf(
    "data.noun", "data.verb", "data.adj", "data.adv"
)

private fun wordnetAssetsAvailable(context: Context): Boolean {
    return try {
        val assets = context.assets.list("wordnet")
        val checkIdx = wordnetIndexFiles.all { it in (assets ?: emptyArray()) }
        val checkData = wordnetDataFiles.all { it in (assets ?: emptyArray()) }
        Log.d("DictionaryRepository", "Assets present: ${assets?.joinToString()}")
        if (!checkIdx) Log.w("DictionaryRepository", "Missing index files: ${wordnetIndexFiles.filter { it !in (assets ?: emptyArray()) }}")
        if (!checkData) Log.w("DictionaryRepository", "Missing data files: ${wordnetDataFiles.filter { it !in (assets ?: emptyArray()) }}")
        checkIdx && checkData
    } catch (e: Exception) {
        Log.e("DictionaryRepository", "Error listing assets for wordnet", e)
        false
    }
}

private data class WordNetIndexEntry(val lemma: String, val pos: String, val synsetOffsets: List<Int>)
private data class WordNetDef(val word: String, val pos: String, val gloss: String)

// Parse the index file for a POS, just returning word->offsets(synsets)
private fun parseWordNetIndex(context: Context, file: String): Map<String, List<Int>> {
    val result = mutableMapOf<String, List<Int>>()
    try {
        context.assets.open("wordnet/$file").bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
            for (line in lines) {
                if (line.startsWith(' ')) continue
                if (line.startsWith("  ")) continue
                if (line.isBlank() || line.startsWith("  ") || line.startsWith(' ')) continue
                if (line.startsWith("  ")) continue
                if (line.startsWith("  ")) continue
                if (line.startsWith(' ')) continue
                if (line.startsWith("  ")) continue
                if (line.isBlank() || line.startsWith("  ")) continue
                if (line.startsWith(' ')) continue
                if (line.startsWith("  ")) continue
                if (line.isBlank() || line.startsWith("  ")) continue
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size < 6) continue
                val lemma = parts[0].replace('_', ' ')
                val synsetCountIdx = 2
                val synsetCnt = parts[synsetCountIdx].toIntOrNull() ?: continue
                val pCnt = 3
                val ptrs = parts[pCnt].toIntOrNull() ?: continue
                val offsetList = parts.drop(4 + ptrs + 2).take(synsetCnt).mapNotNull { it.toIntOrNull() }
                result[lemma.lowercase()] = offsetList
            }
        }
        Log.d("DictionaryRepository", "Parsed index $file, unique words: ${result.size}")
    } catch (e: Exception) {
        Log.e("DictionaryRepository", "Error parsing index $file", e)
    }
    return result
}

// Parse the data file: map offset to (definition, pos)
private fun parseWordNetDataFile(context: Context, file: String): Map<Int, Pair<String, String>> {
    val map = mutableMapOf<Int, Pair<String, String>>()
    try {
        context.assets.open("wordnet/$file").bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
            for (line in lines) {
                if (line.startsWith(' ')) continue
                if (line.isBlank() || line.startsWith(' ')) continue
                if (line.startsWith(' ')) continue
                if (line.startsWith('"')) continue
                if (line.isBlank()) continue
                if (line.startsWith(' ')) continue
                if (line.startsWith('"')) continue
                if (line.isBlank()) continue
                if (line.startsWith(' ')) continue
                if (line.isBlank()) continue
                if (line.startsWith(' ')) continue
                if (line.isBlank()) continue
                if (line.startsWith(' ')) continue
                if (line.isBlank()) continue
                val codeMatch = Regex("^(\\d+)").find(line.trim()) ?: continue
                val offset = codeMatch.value.toIntOrNull() ?: continue
                val pipeIdx = line.indexOf("|")
                val gloss = if (pipeIdx > 0) line.substring(pipeIdx + 1).trim() else ""
                val fields = line.trim().split(Regex("\\s+"))
                val pos = when (file.removePrefix("data.")) {
                    "noun" -> "n"
                    "verb" -> "v"
                    "adj" -> "a"
                    "adv" -> "r"
                    else -> "?"
                }
                map[offset] = Pair(gloss, pos)
            }
        }
        Log.d("DictionaryRepository", "Parsed data file $file, entries: ${map.size}")
    } catch (e: Exception) {
        Log.e("DictionaryRepository", "Error parsing data file $file", e)
    }
    return map
}

private fun parseWordNetDictionary(context: Context): List<DictionaryEntry> {
    if (!wordnetAssetsAvailable(context)) return emptyList()
    val indexEntries = wordnetIndexFiles.flatMap { idxFile ->
        try { parseWordNetIndex(context, idxFile).map { Triple(it.key, idxFile.removePrefix("index."), it.value) } }
        catch (_: Exception) { emptyList() }
    }
    val dataMaps = wordnetDataFiles.associateBy(
        { it.removePrefix("data.") },
        { parseWordNetDataFile(context, it) }
    )
    val result = mutableListOf<DictionaryEntry>()
    for ((lemma, pos, offsets) in indexEntries) {
        for (offset in offsets) {
            val def = dataMaps[pos]?.get(offset)?.first ?: continue
            if (def.isNotBlank())
                result.add(DictionaryEntry(lemma, def))
        }
    }
    // Deduplicate by lemma, prefer first definition
    return result.distinctBy { it.word.lowercase() }
}

class DictionaryRepository(private val context: Context) {
    @Volatile
    private var cachedEntries: List<DictionaryEntry>? = null
    @Volatile
    private var cachedSearchHistory: MutableList<String>? = null

    private fun getIndexFile(): File = File(context.filesDir, "gcide_dict.json")

    private fun loadFromCache(): List<DictionaryEntry>? {
        val f = getIndexFile()
        return if (f.exists()) {
            runCatching { kotlinx.serialization.json.Json.decodeFromString<List<DictionaryEntry>>(f.readText()) }.getOrNull()
        } else null
    }
    private fun saveToCache(list: List<DictionaryEntry>) {
        val f = getIndexFile()
        runCatching { f.writeText(kotlinx.serialization.json.Json.encodeToString(list)) }
    }

    suspend fun size(): Int = withContext(Dispatchers.IO) { loadEntries().size }

    suspend fun search(query: String): List<DictionaryEntry> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val entries = loadEntries()
        val q = query.trim().lowercase()
        addToHistory(q)
        val starts = entries.filter { it.word.lowercase().startsWith(q) }
        val contains = entries.filter { it.word.lowercase().contains(q) || it.meaning.lowercase().contains(q) }
        (starts + (contains - starts)).take(50)
    }

    fun getHistory(): List<String> {
        if (cachedSearchHistory == null) {
            cachedSearchHistory = getSearchHistory(context)
        }
        return cachedSearchHistory!!.toList()
    }

    fun addToHistory(word: String) {
        val history = cachedSearchHistory ?: getSearchHistory(context)
        if (word.isNotBlank()) {
            if (history.firstOrNull()?.equals(word, ignoreCase = true) != true) {
                // Dedup and add
                history.removeIf { it.equals(word, ignoreCase = true) }
                history.add(0, word)
                if (history.size > 50) history.removeAt(50)
                cachedSearchHistory = history
                saveSearchHistory(context, history)
            }
        }
    }

    fun clearHistory() {
        cachedSearchHistory = mutableListOf()
        clearSearchHistory(context)
    }

    private fun loadEntries(): List<DictionaryEntry> {
        synchronized(this) {
            if (cachedEntries != null) return cachedEntries!!
            // Prefer WordNet if present
            val wordNetData = parseWordNetDictionary(context)
            Log.d("DictionaryRepository", "Total dictionary entries: ${wordNetData.size}")
            if (wordNetData.isNotEmpty()) {
                cachedEntries = wordNetData
                return wordNetData
            }
            // Example dictionary only if user explicitly enabled
            if (shouldUseExampleData(context)) {
                cachedEntries = exampleDictionary
                return exampleDictionary
            }
            cachedEntries = emptyList()
            return emptyList()
        }
    }

    suspend fun getDictionarySize(): Int = withContext(Dispatchers.IO) { loadEntries().size }

    private fun parseDictionaryText(text: String): List<DictionaryEntry> {
        // Improved parser: handles multiple formats
        // Format 1: "Word - meaning"
        // Format 2: "Word: meaning"
        // Format 3: "Word meaning" (word at start, rest is meaning)
        // Format 4: Line breaks between entries
        
        val result = mutableListOf<DictionaryEntry>()
        val lines = text.lines()
        var currentWord: String? = null
        var currentMeaning = StringBuilder()

        fun flush() {
            val w = currentWord?.trim()?.replace(Regex("[^a-zA-Z ]"), "")
            val m = currentMeaning.toString().trim()
            if (!w.isNullOrBlank() && w.length > 1 && m.isNotBlank() && m.length > 3) {
                result.add(DictionaryEntry(w, m))
            }
            currentWord = null
            currentMeaning = StringBuilder()
        }

        for (raw in lines) {
            val line = raw.trim()
            if (line.isBlank()) {
                // Empty line might be entry separator
                if (currentWord != null) flush()
                continue
            }

            // Try different separator patterns
            val patterns = listOf(
                Regex("^([A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s*-\\s*(.+)$"),  // "word - meaning"
                Regex("^([A-Za-z]+(?:\\s+[A-Za-z]+)*):\\s*(.+)$"),      // "word: meaning"
                Regex("^([A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(.+)$")        // "word meaning" (space separated)
            )

            var matched = false
            for (pattern in patterns) {
                val match = pattern.find(line)
                if (match != null) {
                    flush()
                    currentWord = match.groupValues[1].trim()
                    currentMeaning.append(match.groupValues[2].trim())
                    matched = true
                    break
                }
            }

            if (!matched) {
                // Continuation of meaning (wrapped line)
                if (currentWord != null) {
                    if (currentMeaning.isNotEmpty()) currentMeaning.append(' ')
                    currentMeaning.append(line)
                } else if (line.length > 2 && line[0].isLetter()) {
                    // Might be a word without separator - treat first word as word, rest as meaning
                    val parts = line.split(Regex("\\s+"), limit = 2)
                    if (parts.size == 2) {
                        flush()
                        currentWord = parts[0]
                        currentMeaning.append(parts[1])
                    }
                }
            }
        }
        flush()
        
        // Deduplicate by word keeping the first occurrence
        val distinct = result.distinctBy { it.word.lowercase() }
        Log.d("DictionaryRepository", "Final distinct entries: ${distinct.size}")
        return distinct
    }
}


