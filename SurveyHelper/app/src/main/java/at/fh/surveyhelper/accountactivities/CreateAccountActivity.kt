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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class CreateAccountActivity : AppCompatActivity() {

    private val TAG = "CreateAccountActivity"
    //global variables
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var password: String? = null

    //UI elements
    private var etFirstName: EditText? = null
    private var etLastName: EditText? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressBar? = null

    //Firebase elements
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        initialize()

    }

    //initialize variables, ui- and firebase elements
    private fun initialize() {
        etFirstName = findViewById<View>(R.id.et_first_name) as EditText
        etLastName = findViewById<View>(R.id.et_last_name) as EditText
        etEmail = findViewById<View>(R.id.et_email) as EditText
        etPassword = findViewById<View>(R.id.et_password) as EditText
        btnCreateAccount = findViewById<View>(R.id.btn_register) as Button
        val layout: RelativeLayout = findViewById(R.id.create_account_layout)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.INVISIBLE
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        btnCreateAccount!!.setOnClickListener { createNewAccount() }
    }

    /*
    Creates a user with email and password if the user
    entered data correctly, toast error is displayed otherwise
     */
    private fun createNewAccount() {
        firstName = etFirstName?.text.toString()
        lastName = etLastName?.text.toString()
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
            && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar?.visibility = View.VISIBLE

            mAuth!!
                .createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    mProgressBar?.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val userId = mAuth!!.currentUser!!.uid
                        //Verify Email
                        verifyEmail();
                        //update user profile information
                        val currentUserDb = mDatabaseReference!!.child(userId)
                        currentUserDb.child("firstName").setValue(firstName)
                        currentUserDb.child("lastName").setValue(lastName)
                        updateUserInfoAndUI()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@CreateAccountActivity, "Authentication failed." + task.exception?.message,
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }


    }

    //new intent which starts the login activity and closes current activity
    private fun updateUserInfoAndUI() {
        val intent = Intent(this@CreateAccountActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    /*
    sends an verification email to new user
    *note* verification is no must for entering the application,
           which is intended
     */
    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity,
                        "Verification email sent to " + mUser.email,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this@CreateAccountActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
