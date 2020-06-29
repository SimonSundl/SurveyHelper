package at.fh.surveyhelper.accountactivities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import at.fh.surveyhelper.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private val TAG = "ForgotPasswordActivity"
    //UI elements
    private var etEmail: EditText? = null
    private var btnSubmit: Button? = null
    private var mProgressBar: ProgressBar? = null
    //Firebase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)
        initialize()
    }

    private fun initialize() {
        etEmail = findViewById<View>(R.id.et_email) as EditText
        btnSubmit = findViewById<View>(R.id.btn_submit) as Button
        mAuth = FirebaseAuth.getInstance()
        btnSubmit!!.setOnClickListener { sendPasswordResetEmail() }
        val layout: RelativeLayout = findViewById(R.id.forgotpw_layout)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.INVISIBLE
    }

    /*
    Takes the edit Text value (E-Mail user)
    and proceeds to send an email with which the
    user can reset his password if succesful
     */
    private fun sendPasswordResetEmail() {
        val email = etEmail?.text.toString()
        if (!TextUtils.isEmpty(email)) {
            mProgressBar?.visibility = View.VISIBLE
            mAuth!!
                .sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    mProgressBar?.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        val message = "Email sent."
                        Log.d(TAG, message)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        updateUI()
                    } else {
                        Log.w(TAG, "Exception" + task.exception!!.message)
                        Toast.makeText(this, "No user found with this email.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        }
    }

    //new intent which starts the login activity and closes current activity
    private fun updateUI() {
        mProgressBar?.visibility = View.VISIBLE
        val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        updateUI()
    }
}