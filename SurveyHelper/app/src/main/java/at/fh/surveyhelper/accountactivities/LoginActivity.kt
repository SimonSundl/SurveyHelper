package at.fh.surveyhelper.accountactivities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import at.fh.surveyhelper.mainactivities.MainActivity
import at.fh.surveyhelper.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    //global variables
    private var email: String? = null
    private var password: String? = null
    //UI elements
    private var tvForgotPassword: TextView? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressBar? = null
    //Firebase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login)

        //skip login routine if a logged in user is found
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val i = Intent(this@LoginActivity, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out")
        }

        initialize()

    }

    //initialize variables, ui elements and firebase authentication
    private fun initialize() {
        tvForgotPassword = findViewById<View>(R.id.tv_forgot_password) as TextView
        etEmail = findViewById<View>(R.id.et_email) as EditText
        etPassword = findViewById<View>(R.id.et_password) as EditText
        btnLogin = findViewById<View>(R.id.btn_login) as Button
        btnCreateAccount = findViewById<View>(R.id.btn_register_account) as Button
        val layout: RelativeLayout = findViewById(R.id.login_layout)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()
        tvForgotPassword!!
            .setOnClickListener { startActivity(Intent(this@LoginActivity,
                ForgotPasswordActivity::class.java)) }
        btnCreateAccount!!
            .setOnClickListener { startActivity(
                Intent(this@LoginActivity,
                CreateAccountActivity::class.java)
            ) }
        btnLogin!!.setOnClickListener { loginUser() }
    }

    /*
    Login with E-mail and password via firebase
    OnCompleteListener hides the progress bar and proceeds
    with either succesfull login or failure
     */
    private fun loginUser() {
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar?.visibility = View.VISIBLE
            Log.d(TAG, "Logging in user.")
            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    mProgressBar?.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        updateUI()
                    } else {
                        Log.e(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    //new intent which starts the main activity and closes current activity
    private fun updateUI() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}