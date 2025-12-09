package com.example.stratify

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

class LanguagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LanguagesScreen(
                        onNavigateUp = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                        onLanguageSelected = { lang -> setLocale(lang) }
                    )
                }
            }
        }
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        val editor = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", lang)
        editor.apply()

        requireActivity().recreate()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    onNavigateUp: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("Settings", Context.MODE_PRIVATE) }
    val savedLang = sharedPreferences.getString("My_Lang", "en") ?: "en"

    var selectedLanguage by remember { mutableStateOf(savedLang) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LanguageOption(
                language = "English",
                langCode = "en",
                isSelected = selectedLanguage == "en",
                onSelected = {
                    selectedLanguage = "en"
                    onLanguageSelected("en")
                }
            )
            Divider()
            LanguageOption(
                language = "Indonesia",
                langCode = "in",
                isSelected = selectedLanguage == "in",
                onSelected = {
                    selectedLanguage = "in"
                    onLanguageSelected("in")
                }
            )
        }
    }
}

@Composable
fun LanguageOption(
    language: String,
    langCode: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = language, style = MaterialTheme.typography.bodyLarge)
    }
}
