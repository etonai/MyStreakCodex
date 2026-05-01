package com.pseddev.mystreak.ui.legal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.databinding.FragmentPrivacyPolicyBinding
import java.io.IOException

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
        loadPrivacyPolicy()
    }

    private fun setupWebView() {
        binding.webViewPrivacyPolicy.apply {
            webViewClient = WebViewClient()
            settings.apply {
                javaScriptEnabled = false // Security: disable JavaScript
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                loadWithOverviewMode = true
                useWideViewPort = true
                // Disable cache for fresh content loading
                cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            }
        }
    }

    private fun loadPrivacyPolicy() {
        try {
            // Load HTML file from assets
            binding.webViewPrivacyPolicy.loadUrl("file:///android_asset/legal/privacy-policy.html")
        } catch (e: Exception) {
            // Fallback: show error message
            val errorHtml = """
                <html>
                <body style="font-family: sans-serif; padding: 20px;">
                    <h2>Privacy Policy</h2>
                    <p>Sorry, the privacy policy could not be loaded at this time.</p>
                    <p>Please contact us at PseudonymousEd@gmail.com for privacy-related questions.</p>
                </body>
                </html>
            """.trimIndent()

            binding.webViewPrivacyPolicy.loadData(errorHtml, "text/html", "UTF-8")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}