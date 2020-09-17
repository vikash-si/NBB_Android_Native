package com.webviewexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings.LOAD_DEFAULT
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web.*


class WebActivity : AppCompatActivity() {

    companion object {
        const val PAGE_URL = "pageUrl"
        const val MAX_PROGRESS = 100

        fun newIntent(context: Context, pageUrl: String): Intent {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(PAGE_URL, pageUrl)
            return intent
        }
    }

    private lateinit var pageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        // get pageUrl from String
        pageUrl = intent.getStringExtra(PAGE_URL)
            ?: throw IllegalStateException("field $PAGE_URL missing in Intent")

        initWebView()
        setWebClient()
        handlePullToRefresh()
        loadUrl(pageUrl)
    }

    private fun handlePullToRefresh() {

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = LOAD_DEFAULT
        webView.settings.setAppCacheEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
            override fun onLoadResource(view: WebView?, url: String?) {
                //Fun Part will be here
                webView.loadUrl("javascript:(function() { " +
                        "var head = document.getElementById('header-text-nav-wrap').style.display='none'; " +
                        "})()")

                webView.loadUrl("javascript:(function() { " +
                        "var head = document.getElementsByClassName('news-bar')[0].style.display='none'; " +
                        "})()");
            }
        }

    }

    private fun setWebClient() {
        webView.webChromeClient = object : WebChromeClient() {

            private var mCustomView: View? = null
            private var mCustomViewCallback: CustomViewCallback? = null
            protected var mFullscreenContainer: FrameLayout? = null
            private var mOriginalOrientation = 0
            private var mOriginalSystemUiVisibility = 0

            override fun getDefaultVideoPoster(): Bitmap? {
                return if (mCustomView == null) {
                    null
                } else BitmapFactory.decodeResource(
                    applicationContext.resources,
                    2130837573
                )
            }

            override fun onHideCustomView() {
                (window.decorView as FrameLayout).removeView(mCustomView)
                mCustomView = null
                window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
                requestedOrientation = mOriginalOrientation
                mCustomViewCallback!!.onCustomViewHidden()
                mCustomViewCallback = null
            }
            override fun onShowCustomView(
                paramView: View?,
                paramCustomViewCallback: CustomViewCallback?
            ) {
                if (mCustomView != null) {
                    onHideCustomView()
                    return
                }
                mCustomView = paramView
                mOriginalSystemUiVisibility =
                    window.decorView.systemUiVisibility
                mOriginalOrientation = requestedOrientation
                mCustomViewCallback = paramCustomViewCallback
                (window.decorView as FrameLayout).addView(
                    mCustomView,
                    FrameLayout.LayoutParams(-1, -1)
                )
                window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }

            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress < MAX_PROGRESS && progressBar.visibility == ProgressBar.GONE) {
                    progressBar.visibility = ProgressBar.VISIBLE
                }

                if (newProgress == MAX_PROGRESS) {
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    private fun loadUrl(pageUrl: String) {
        webView.loadUrl(pageUrl)
    }
}
