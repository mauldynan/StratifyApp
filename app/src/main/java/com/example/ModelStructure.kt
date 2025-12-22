package com.example.stratify.ml

import com.google.gson.annotations.SerializedName

// Struktur file stratify_complete_model.json
data class StratifyModel(
    @SerializedName("stem_map") val stemMap: Map<String, String>,
    val stopwords: List<String>,
    val vocab: Map<String, Int>,
    val idf: List<Double>,
    val nb: NaiveBayesData
)

data class NaiveBayesData(
    @SerializedName("class_log_prior") val classLogPrior: List<Double>,     // Prior probability
    @SerializedName("feature_log_prob") val featureLogProb: List<List<Double>>, // Bobot kata per kelas
    val classes: List<Int>                 // [0, 1]
)
