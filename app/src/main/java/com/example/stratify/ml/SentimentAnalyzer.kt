package com.example.stratify.ml

import android.content.Context
import com.google.gson.Gson
import java.util.Locale

class SentimentAnalyzer(private val context: Context) {

    private var model: StratifyModel? = null

    // Load model saat class dibuat
    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            // Membaca file dari folder assets
            val jsonString = context.assets.open("stratify_complete_model.json")
                .bufferedReader().use { it.readText() }
            model = Gson().fromJson(jsonString, StratifyModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fungsi Utama: Prediksi
    fun predict(text: String): String {
        val m = model ?: return "Error: Model not loaded"

        // --- TAHAP 1: PREPROCESSING ---
        // 1. Lowercase & Regex (Hapus simbol selain huruf)
        val cleanText = text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z\\s]"), "")

        // 2. Tokenize (Pecah jadi kata-kata)
        val tokens = cleanText.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        // 3. Stopword Removal & Stemming
        val processedTokens = tokens.mapNotNull { word ->
            if (m.stopwords.contains(word)) {
                null // Buang stopword
            } else {
                // Ubah ke kata dasar jika ada di map, kalau tidak biarkan
                m.stemMap[word] ?: word
            }
        }

        // --- TAHAP 2: VECTORIZATION (TF Count) ---
        // Hitung berapa kali kata muncul di kalimat ini
        val termFrequencies = mutableMapOf<Int, Double>()
        for (word in processedTokens) {
            val vocabIndex = m.vocab[word]
            if (vocabIndex != null) {
                termFrequencies[vocabIndex] = (termFrequencies[vocabIndex] ?: 0.0) + 1.0
            }
        }

        // --- TAHAP 3: NAIVE BAYES CALCULATION ---
        // Skor Awal = Log Prior (Kecenderungan dasar kelas)
        // Class 0 = Negatif, Class 1 = Positif
        var scoreNeg = m.nb.classLogPrior[0]
        var scorePos = m.nb.classLogPrior[1]

        // Tambahkan bobot setiap kata yang ditemukan
        for ((index, count) in termFrequencies) {
            val idf = m.idf[index] // Bobot seberapa unik kata itu

            // Rumus: count * idf * bobot_probabilitas_model
            scoreNeg += count * idf * m.nb.featureLogProb[0][index]
            scorePos += count * idf * m.nb.featureLogProb[1][index]
        }

        // --- TAHAP 4: KEPUTUSAN ---
        return if (scorePos > scoreNeg) {
            "Positif"
        } else {
            "Negatif"
        }
    }
}
