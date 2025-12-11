package com.example.stratify.ml

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.IOException

class SentimentAnalyzer(
    private val context: Context,
    private val modelName: String = "sentiment_model.tflite"
) {

    private var classifier: NLClassifier? = null

    init {
        try {
            // Initialize the classifier from the model file in the assets folder.
            classifier = NLClassifier.createFromFile(context, modelName)
        } catch (e: IOException) {
            // Handle model loading errors, e.g., log the error
            classifier = null
            e.printStackTrace()
        }
    }

    fun classify(text: String): List<Pair<String, Float>> {
        if (classifier == null) {
            // Return a default or empty list if the classifier isn't initialized
            return emptyList()
        }

        // Run inference and return the results.
        // The result is a list of Category objects, each with a label and a score.
        // We convert it to a simple List<Pair<String, Float>> for easier use.
        return classifier?.classify(text)?.map { category ->
            category.label to category.score
        } ?: emptyList()
    }

    fun close() {
        classifier?.close()
        classifier = null
    }

    companion object {
        /**
         * A Composable function that remembers a SentimentAnalyzer instance across recompositions.
         *
         * This function handles the creation and lifecycle of the SentimentAnalyzer, ensuring
         * that the TensorFlow Lite model is loaded only once and that its resources are properly
         * released when the composable is disposed.
         *
         * @return A remembered instance of the [SentimentAnalyzer].
         */
        @Composable
        fun rememberSentimentAnalyzer(): SentimentAnalyzer {
            val context = LocalContext.current
            // remember ensures the analyzer is created only once during the initial composition.
            val analyzer = remember {
                SentimentAnalyzer(context)
            }

            // DisposableEffect is used to clean up resources when the composable is removed
            // from the composition tree (e.g., when navigating away from the screen).
            DisposableEffect(Unit) {
                onDispose {
                    analyzer.close()
                }
            }

            return analyzer
        }
    }
}
