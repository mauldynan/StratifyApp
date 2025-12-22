package com.example.stratify

import android.content.Context
import com.google.gson.Gson
import java.util.Locale

// ==========================================
// 1. DATA MODEL (Wadah untuk file JSON)
// ==========================================
data class StratifyModel(
    val stem_map: Map<String, String>,  // Peta kata dasar (misal: "belanja" -> "belanja")
    val stopwords: List<String>,        // Kata yang dibuang (misal: "dan", "yang")
    val vocab: Map<String, Int>,        // Kamus kata dan ID-nya
    val idf: List<Double>,              // Bobot kekuatan kata
    val nb: NaiveBayesData              // Data probabilitas Naive Bayes
)

data class NaiveBayesData(
    val class_log_prior: List<Double>,        // Probabilitas awal (Negatif vs Positif)
    val feature_log_prob: List<List<Double>>, // Probabilitas tiap kata per kelas
    val classes: List<Int>                    // [0, 1]
)

// ==========================================
// 2. OTAK AI (Logika Prediksi)
// ==========================================
class SentimentAnalyzer(private val context: Context) {

    private var model: StratifyModel? = null

    init {
        loadModel()
    }

    // Fungsi membaca file JSON dari folder assets
    private fun loadModel() {
        try {
            // Membaca text dari file stratify_complete_model.json
            val jsonString = context.assets.open("stratify_complete_model.json")
                .bufferedReader()
                .use { it.readText() }

            // Mengubah text JSON menjadi Objek Kotlin
            model = Gson().fromJson(jsonString, StratifyModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // FUNGSI UTAMA: Menerima teks -> Mengembalikan "Positif" / "Negatif"
    fun predict(text: String): String {
        // Cek apakah model berhasil dimuat
        val m = model ?: return "Error: Model JSON tidak ditemukan/gagal load"

        // --- TAHAP 1: BERSIH-BERSIH (Preprocessing) ---
        // 1. Ubah ke huruf kecil & buang simbol aneh
        val cleanText = text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z\\s]"), "")

        // 2. Pecah kalimat menjadi kata-kata (Tokenizing)
        val tokens = cleanText.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }

        // 3. Buang Stopwords & Lakukan Stemming (Cari kata dasar)
        val processedTokens = tokens.mapNotNull { word ->
            if (m.stopwords.contains(word)) {
                null // Buang kata ini (misal: "yang", "di")
            } else {
                // Jika ada di kamus stemming, pakai kata dasarnya. Jika tidak, pakai kata asli.
                m.stem_map[word] ?: word
            }
        }

        // --- TAHAP 2: HITUNG FREKUENSI (TF) ---
        // Menghitung berapa kali setiap kata muncul di ulasan ini
        val termFrequencies = mutableMapOf<Int, Double>()
        for (word in processedTokens) {
            val vocabIndex = m.vocab[word]
            // Jika kata dikenal oleh model (ada di vocab), kita hitung
            if (vocabIndex != null) {
                termFrequencies[vocabIndex] = (termFrequencies[vocabIndex] ?: 0.0) + 1.0
            }
        }

        // --- TAHAP 3: HITUNG SKOR NAIVE BAYES ---
        // Class 0 = Negatif
        // Class 1 = Positif
        var scoreNeg = m.nb.class_log_prior[0]
        var scorePos = m.nb.class_log_prior[1]

        for ((index, count) in termFrequencies) {
            val idf = m.idf[index] // Ambil bobot IDF kata tersebut

            // Rumus: Skor Lama + (Frekuensi * IDF * Probabilitas Kata di Kelas Tersebut)
            scoreNeg += count * idf * m.nb.feature_log_prob[0][index]
            scorePos += count * idf * m.nb.feature_log_prob[1][index]
        }

        // --- TAHAP 4: KEPUTUSAN AKHIR ---
        // Bandingkan mana skor yang lebih besar
        return if (scorePos > scoreNeg) {
            "Positif"
        } else {
            "Negatif"
        }
    }
}