package com.example.iptvplayer.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iptvplayer.config.AppConfig
import com.example.iptvplayer.databinding.ActivityTokenBinding
import com.example.iptvplayer.repository.PlaylistFetcher
import com.example.iptvplayer.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * First screen shown on app launch. Asks the user for an access token,
 * appends it to your server's base URL, and only proceeds to the channel
 * list if the server confirms the token is valid (i.e. returns real
 * playlist data rather than an error/empty response).
 *
 * All the actual gating logic lives on YOUR server — this screen just
 * calls it and reacts to the response. It does not embed, hide, or ship
 * with any third-party token or endpoint.
 */
class TokenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTokenBinding
    private lateinit var tokenRepository: TokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTokenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenRepository = TokenRepository(applicationContext)

        // If we already have a saved token, try it silently before asking again.
        val savedToken = tokenRepository.getToken()
        if (!savedToken.isNullOrBlank()) {
            binding.tokenInput.setText(savedToken)
            validateAndProceed(savedToken, silent = true)
        }

        binding.submitButton.setOnClickListener {
            val token = binding.tokenInput.text.toString().trim()
            if (token.isBlank()) {
                Toast.makeText(this, "Enter your access token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            validateAndProceed(token, silent = false)
        }
    }

    private fun validateAndProceed(token: String, silent: Boolean) {
        if (AppConfig.PLAYLIST_SERVER_BASE_URL.isBlank()) {
            Toast.makeText(
                this,
                "Server URL not configured. Set AppConfig.PLAYLIST_SERVER_BASE_URL first.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            val separator = if (AppConfig.PLAYLIST_SERVER_BASE_URL.contains("?")) "&" else "?"
            val url = "${AppConfig.PLAYLIST_SERVER_BASE_URL}${separator}token=$token"

            val result = withContext(Dispatchers.IO) {
                runCatching { PlaylistFetcher.fetchText(url) }
            }

            setLoading(false)

            result.onSuccess { body ->
                if (body.isBlank()) {
                    if (!silent) Toast.makeText(this@TokenActivity, "Invalid token or empty playlist", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                tokenRepository.saveToken(token)
                val intent = Intent(this@TokenActivity, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLAYLIST_BODY, body)
                intent.putExtra(MainActivity.EXTRA_PLAYLIST_URL, url)
                startActivity(intent)
                finish()
            }.onFailure { error ->
                if (!silent) {
                    Toast.makeText(
                        this@TokenActivity,
                        "Couldn't verify token: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.submitButton.isEnabled = !loading
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
    }
}
