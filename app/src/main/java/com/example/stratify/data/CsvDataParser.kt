package com.example.stratify.data

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * A helper object to parse CSV files from the app's assets folder.
 */
object CsvDataParser {

    /**
     * Reads a CSV file from the assets folder, extracts data from a specific column,
     * and returns it as a list of strings.
     *
     * @param context The application context to access assets.
     * @param fileName The name of the CSV file in the assets folder.
     * @param columnName The name of the column from which to extract data.
     * @return A list of strings containing the data from the specified column.
     *         Returns an empty list if the file or column is not found or an error occurs.
     */
    fun getReviewsFromCsv(
        context: Context,
        fileName: String = "cleaned_marketPlacesData.csv",
        columnName: String = "data_clean_content"
    ): List<String> {
        val reviews = mutableListOf<String>()
        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val header = reader.readLine().split(',')
                    val columnIndex = header.indexOf(columnName)

                    if (columnIndex == -1) {
                        // Column not found
                        return emptyList()
                    }

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val row = line!!.split(',')
                        if (row.size > columnIndex) {
                            reviews.add(row[columnIndex])
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle file reading errors, e.g., by logging or returning an empty list
        }
        return reviews
    }
}
