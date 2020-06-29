package at.fh.surveyhelper.mainactivities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import at.fh.surveyhelper.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateSurveyActivity : AppCompatActivity() {
    private val TAG = "CreateSurveyActivity"
    //UI elements
    private var etTitle: EditText? = null
    private var etLink: EditText? = null
    private var etCategory: EditText? = null
    private var svDatePicker: DatePicker? = null
    private var btnSubmit: Button? = null
    private var mProgressBar: ProgressBar? = null

    //firebase elements
    private var mFirestore: FirebaseFirestore? = null
    private var mCollection: CollectionReference? = null

    //global variables
    private var title: String? = null
    private var link: String? = null
    private var category: String? = null
    private var calendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_survey)

        initialize()
    }

    //initialize variables, ui- and firebase elements
    private fun initialize(){
        etTitle = findViewById<View>(R.id.sv_title) as EditText
        etLink = findViewById<View>(R.id.sv_link) as EditText
        etCategory = findViewById<View>(R.id.sv_category) as EditText
        svDatePicker = findViewById<View>(R.id.sv_datepicker) as DatePicker
        btnSubmit = findViewById<View>(R.id.btn_submit) as Button
        val layout: RelativeLayout = findViewById(R.id.create_survey_layout)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.INVISIBLE
        mFirestore = FirebaseFirestore.getInstance()
        mCollection = mFirestore!!.collection("Surveys")
        btnSubmit!!.setOnClickListener{createSurvey()}
    }

    /*
    takes user input and adds a new survey to collection
    with creating a hashmap and collection reference (59)
     */
    private fun createSurvey(){
        title = etTitle?.text.toString()
        link = etLink?.text.toString()
        category = etCategory?.text.toString()
        calendar = Calendar.getInstance()
        calendar!!.set(svDatePicker!!.year, svDatePicker!!.month, svDatePicker!!.dayOfMonth)
        val user = FirebaseAuth.getInstance().currentUser

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(link) && !TextUtils.isEmpty(category)) {
            //check if link matches url pattern
            if(Patterns.WEB_URL.matcher(link).matches()) {
                if(!link!!.startsWith("www.")&& !link!!.startsWith("http://") && !link!!.startsWith("https://")){
                    link = "www."+ link;
                }
                if(!link!!.startsWith("http://") && !link!!.startsWith("https://")){
                    link = "http://"+ link;
                }
                mProgressBar?.visibility = View.VISIBLE
                Log.d(TAG, "Adding to collection.")
                val surveyData = hashMapOf(
                    "title" to title,
                    "link" to link,
                    "category" to category,
                    "dueTime" to calendar!!.time,
                    "creationTime" to FieldValue.serverTimestamp(),
                    "user" to user.toString()
                )

                mCollection?.add(surveyData)
                updateUI()
            }
            else{
                Toast.makeText(this, "Not a valid link", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    //new intent which starts the main activity and closes current activity
    private fun updateUI(){
        mProgressBar?.visibility = View.VISIBLE
        val intent = Intent(this@CreateSurveyActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    //overwrite on back pressed since there's nothing to go back to
    override fun onBackPressed() {
        updateUI()
    }
}