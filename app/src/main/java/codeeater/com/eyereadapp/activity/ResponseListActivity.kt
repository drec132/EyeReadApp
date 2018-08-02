package codeeater.com.eyereadapp.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import codeeater.com.eyereadapp.R
import codeeater.com.eyereadapp.adapter.ResponseListAdapter
import codeeater.com.eyereadapp.model.Response
import com.google.firebase.database.*
import java.util.*


class ResponseListActivity : AppCompatActivity() {

    private var mDatabaseRef: DatabaseReference? = null
    private var imgList: MutableList<Response>? = null
    private var lv: ListView? = null
    private var adapter: ResponseListAdapter? = null
    private var progressDialog: ProgressDialog? = null
    private var tts: TextToSpeech? = null
    private var toRead: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.ENGLISH
                tts!!.setSpeechRate(0.9f)
                tts!!.setPitch(0.8f)
            }
        })

        imgList = ArrayList()
        lv = findViewById(R.id.listViewImage)
        //Show progress dialog during list image loading
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait loading list image...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        val mn = MainActivity()
        mDatabaseRef = FirebaseDatabase.getInstance()
                .getReference(MainActivity.FB_DATABASE_PATH)
                .child(mn.giveUser())

        mDatabaseRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressDialog!!.dismiss()

                //Fetch image data from firebase database
                dataSnapshot.children
                        .map {
                            //Response class require default constructor
                            it.getValue(Response::class.java)
                        }
                        .forEach { imgList!!.add(it!!) }

                //Init adapter
                adapter = ResponseListAdapter(this@ResponseListActivity, R.layout.image_item, imgList!!)
                //Set adapter for listview
                lv!!.adapter = adapter

                lv!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    toRead = "The Result is " + (imgList as ArrayList<Response>)[position].response
                    tts!!.stop()
                    tts!!.speak(toRead, TextToSpeech.QUEUE_ADD,null)

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog!!.dismiss()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        tts!!.stop()
    }

    override fun onStart() {
        super.onStart()
        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.ENGLISH
                tts!!.setSpeechRate(0.9f)
                tts!!.setPitch(0.8f)
            }
        })
    }
}