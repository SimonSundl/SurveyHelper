package at.fh.surveyhelper.mainactivities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import at.fh.surveyhelper.R

class WebViewActivity : AppCompatActivity() {

    private lateinit var mWebView: WebView
    private var mProgressBar: ProgressBar? = null

    //surveys are carried out in this webview
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view)

        val layout: RelativeLayout = findViewById(R.id.webview_layout)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.VISIBLE

        val url: String? = intent.getStringExtra("url")
        mWebView = findViewById(R.id.webview)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mProgressBar?.visibility = View.INVISIBLE
            }
        }
        mWebView.run { loadUrl(url) }
    }

    /*
    override onBackPressed to be able to exit the webview with ease
    displaying a dialog though to avoid a user accidentally quitting the survey
     */
    override fun onBackPressed() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("Do you want to go back to the app and leave the survey? (Make sure to submit the survey!)")
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { _, _ -> //if user pressed "yes", then he is allowed to exit from application
                mProgressBar?.visibility = View.VISIBLE
                val intent = Intent(this@WebViewActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialog, _ -> //if user select "No", just cancel this dialog and continue with app
                dialog.cancel()
            })
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}