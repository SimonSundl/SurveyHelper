package at.fh.surveyhelper.data

import com.google.firebase.Timestamp

//simple data class for surveys
data class Collection(val title: String = "", val link:String = "", val category: String = "", val dueTime: Timestamp? = null, val creationTime: Timestamp? = null, val user: String= "") {
}