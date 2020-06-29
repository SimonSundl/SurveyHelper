package at.fh.surveyhelper.mainactivities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.fh.surveyhelper.R
import at.fh.surveyhelper.accountactivities.LoginActivity
import at.fh.surveyhelper.data.Collection
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class MainActivity : AppCompatActivity() {
    //firebase elements
    private var mAuth: FirebaseAuth? = null
    private var mFirestore: FirebaseFirestore? = null
    private var mCollection: CollectionReference? = null
    private var query: Query? = null
    private var adapter: ProductFirestoreRecyclerAdapter? = null
    private var mStorage: FirebaseStorage? = null

    //ui elements
    private var mProgressBar: ProgressBar? = null
    private var mView: RecyclerView? = null

    //adapter needs to be started onStart()
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    //if adapter is still running stop
    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        initialize()
    }

    /*
    create menu with options logout and create new survey
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            R.id.new_survey -> {
                mProgressBar?.visibility = View.VISIBLE
                val intent = Intent(this@MainActivity, CreateSurveyActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //simple logout, returns user to login activity
    private fun logout() {
        mAuth?.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    //initialize variables, ui- and firebase elements
    private fun initialize(){
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mCollection = mFirestore!!.collection("Surveys")
        mView = findViewById<View>(R.id.recycleView) as RecyclerView
        mView!!.layoutManager = LinearLayoutManager(this)
        mStorage = FirebaseStorage.getInstance()
        query = mCollection!!.orderBy("creationTime", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<Collection> = FirestoreRecyclerOptions.Builder<Collection>()
            .setQuery(query!!, Collection::class.java)
            .build()
        adapter = ProductFirestoreRecyclerAdapter(options)
        mView!!.adapter = adapter
        val layout: RelativeLayout = findViewById(R.id.main_view)
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(mProgressBar, params);
        mProgressBar?.visibility = View.INVISIBLE
    }


    /*
    Survey are displayed in a RecyclerView, which needs a ViewHolder for the data
    setSurveyData builds the view for a survey
     */
    private inner class SurveyViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        internal fun setSurveyData(Title: String, Link: String, Category: String, DueDate: com.google.firebase.Timestamp?) {
            val title = view.findViewById<TextView>(R.id.view_title)
            val link = view.findViewById<TextView>(R.id.view_link)
            val category = view.findViewById<TextView>(R.id.view_category)
            val time = view.findViewById<TextView>(R.id.view_time)
            val storageReference = mStorage?.reference?.child("pen.png")

            //download image from storage
            storageReference?.downloadUrl?.addOnSuccessListener { Uri ->
                val imageUrl = Uri.toString()
                val image: ImageView = view.findViewById<ImageView>(R.id.pen_image)
                Glide.with(this@MainActivity)
                    .load(imageUrl)
                    .into(image)
            }

            val calendar = Calendar.getInstance()
            calendar.time = DueDate!!.toDate()
            val year = calendar.get(Calendar.YEAR).toString()
            val month = (calendar.get(Calendar.MONTH) + 1).toString()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()

            val builder = "Evaluation date: $day.$month.$year"

            title.text = Title
            category.text = Category
            time.text = builder

            link.setOnClickListener(){
                openWebview(Link)
            }
        }
    }

   /*
   FirestoreRecyclerAdapter which was initalized with set options (108)
   uses the /data/Collection data class and sends data to ViewHolder (126)
    */
    private inner class ProductFirestoreRecyclerAdapter internal constructor(options: FirestoreRecyclerOptions<Collection>) : FirestoreRecyclerAdapter<Collection, SurveyViewHolder>(options) {
        override fun onBindViewHolder(surveyViewHolder: SurveyViewHolder, position: Int, surveyModel: Collection) {
            surveyViewHolder.setSurveyData(surveyModel.title, surveyModel.link, surveyModel.category, surveyModel.dueTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
            return SurveyViewHolder(view)
        }
    }

    //opens webview to carry out the surveys, url is passed to webview activity as parameter
    fun openWebview(url: String) {
        mProgressBar?.visibility = View.VISIBLE
        val intent = Intent(this@MainActivity, WebViewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("url", url)
        startActivity(intent)
        finish()
    }

    //overriding onBackPressed() since it would close the app abruptly otherwise
    override fun onBackPressed() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("Do you want to Exit?")
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { _, _ -> //if user pressed "yes", then he is allowed to exit from application
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