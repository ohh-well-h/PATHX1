package com.example.pathx01.data.model

import java.time.LocalDate

data class BibleVerse(
    val reference: String, // e.g., "John 3:16"
    val text: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val date: LocalDate = LocalDate.now()
)

// Sample Bible verses for demonstration
object BibleVerseRepository {
    private val verses = listOf(
        BibleVerse(
            reference = "John 3:16",
            text = "For God so loved the world, that he gave his only Son, that whoever believes in him should not perish but have eternal life.",
            book = "John",
            chapter = 3,
            verse = 16
        ),
        BibleVerse(
            reference = "Jeremiah 29:11",
            text = "For I know the plans I have for you, declares the Lord, plans for welfare and not for evil, to give you a future and a hope.",
            book = "Jeremiah",
            chapter = 29,
            verse = 11
        ),
        BibleVerse(
            reference = "Philippians 4:13",
            text = "I can do all things through him who strengthens me.",
            book = "Philippians",
            chapter = 4,
            verse = 13
        ),
        BibleVerse(
            reference = "Proverbs 3:5-6",
            text = "Trust in the Lord with all your heart, and do not lean on your own understanding. In all your ways acknowledge him, and he will make straight your paths.",
            book = "Proverbs",
            chapter = 3,
            verse = 5
        ),
        BibleVerse(
            reference = "Romans 8:28",
            text = "And we know that for those who love God all things work together for good, for those who are called according to his purpose.",
            book = "Romans",
            chapter = 8,
            verse = 28
        ),
        BibleVerse(
            reference = "Isaiah 40:31",
            text = "But they who wait for the Lord shall renew their strength; they shall mount up with wings like eagles; they shall run and not be weary; they shall walk and not faint.",
            book = "Isaiah",
            chapter = 40,
            verse = 31
        ),
        BibleVerse(
            reference = "Matthew 11:28",
            text = "Come to me, all who labor and are heavy laden, and I will give you rest.",
            book = "Matthew",
            chapter = 11,
            verse = 28
        ),
        BibleVerse(
            reference = "2 Corinthians 5:17",
            text = "Therefore, if anyone is in Christ, he is a new creation. The old has passed away; behold, the new has come.",
            book = "2 Corinthians",
            chapter = 5,
            verse = 17
        ),
        BibleVerse(
            reference = "Psalm 23:1-3",
            text = "The Lord is my shepherd; I shall not want. He makes me lie down in green pastures. He leads me beside still waters. He restores my soul.",
            book = "Psalms",
            chapter = 23,
            verse = 1
        ),
        BibleVerse(
            reference = "Galatians 2:20",
            text = "I have been crucified with Christ. It is no longer I who live, but Christ who lives in me. And the life I now live in the flesh I live by faith in the Son of God, who loved me and gave himself for me.",
            book = "Galatians",
            chapter = 2,
            verse = 20
        )
    )
    
    fun getVerseOfTheDay(date: LocalDate = LocalDate.now()): BibleVerse {
        // Use date as seed for consistent daily verse
        val dayOfYear = date.dayOfYear
        val index = dayOfYear % verses.size
        return verses[index].copy(date = date)
    }
    
    fun getRandomVerse(): BibleVerse {
        return verses.random()
    }
}
