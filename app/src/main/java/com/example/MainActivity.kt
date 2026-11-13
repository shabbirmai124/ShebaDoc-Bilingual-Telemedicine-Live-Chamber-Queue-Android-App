/**
 * ShebaDoc (সেবাডক) - Bangladesh Online Doctor Appointment & Telemedicine App
 * Made by Md Shabbir Mia
 */
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.data.model.AppLanguage
import com.example.ui.AuthScreen
import com.example.ui.ShebaMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var currentLanguageCode by rememberSaveable { mutableStateOf(AppLanguage.BN.code) }
            val currentLang = if (currentLanguageCode == AppLanguage.BN.code) AppLanguage.BN else AppLanguage.EN

            MyApplicationTheme(darkTheme = isDarkTheme) {
                ShebaAppRoot(
                    lang = currentLang,
                    onToggleLanguage = {
                        currentLanguageCode = if (currentLang == AppLanguage.BN) {
                            AppLanguage.EN.code
                        } else {
                            AppLanguage.BN.code
                        }
                    },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun ShebaAppRoot(
    lang: AppLanguage,
    onToggleLanguage: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestMode by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isGuestMode = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null && !isGuestMode) {
        AuthScreen(
            lang = lang,
            onToggleLanguage = onToggleLanguage,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onAuthSuccess = {
                isGuestMode = false
                currentUser = Firebase.auth.currentUser
            },
            onContinueAsGuest = {
                isGuestMode = true
            }
        )
    } else {
        val uid = user?.uid
        key(uid ?: "guest_preview") {
            ShebaMainScreen(
                currentUserId = uid,
                isGuestMode = (user == null),
                lang = lang,
                onToggleLanguage = onToggleLanguage,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onRequestSignIn = { isGuestMode = false },
                onSignedOut = {
                    isGuestMode = false
                    currentUser = null
                }
            )
        }
    }
}
