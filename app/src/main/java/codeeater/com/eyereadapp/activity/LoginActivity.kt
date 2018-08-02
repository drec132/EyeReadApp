package codeeater.com.eyereadapp.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import codeeater.com.eyereadapp.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var registerTextView: TextView? = null
    private var anonymousTextView: TextView? = null

    /* A dialog that is presented until the Firebase authentication finished. */
    private var mAuthProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = ProgressDialog(this)
        mAuthProgressDialog!!.setTitle("Loading")
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        registerTextView = findViewById(R.id.registerTextView)
        anonymousTextView = findViewById(R.id.anonymousTextView)

        mAuth = FirebaseAuth.getInstance()

        // [START] Signing in with Email and Password
        btnLogin!!.setOnClickListener {
            try {
                if (etEmail!!.text.isEmpty()) {
                    etEmail!!.error = "Enter your email"
                }
                if (etPassword!!.text.isEmpty()) {
                    etPassword!!.error = "Enter your password"
                } else {
                    mAuthProgressDialog!!.setMessage("Signing in...")
                    mAuthProgressDialog!!.setCancelable(false)
                    mAuthProgressDialog!!.show()
                    val email = etEmail!!.text.toString().trim()
                    val password = etPassword!!.text.toString().trim()
                    mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(MainActivity.TAG, "signInWithEmail:success")
                            startActivity(Intent(this, MainActivity::class.java))
                            etEmail!!.text.clear()
                            etPassword!!.text.clear()
                            finish()
                        } else {
                            Log.w(MainActivity.TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                        mAuthProgressDialog!!.hide()
                    }
                }
            } catch (e: Exception) {
                Log.w(MainActivity.TAG, e.message)
            }
        }
        // [END]
        // When both of the TextView in the activitiy_login is clicked
        // [START] Go To RegisterActivity
        registerTextView!!.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            etEmail!!.text.clear()
            etPassword!!.text.clear()
        }
        // [END]
        // [START] Signing as Anonymous
        anonymousTextView!!.setOnClickListener {
            mAuthProgressDialog!!.setMessage("Authenticating as Anonymous...")
            mAuthProgressDialog!!.setCancelable(false)
            mAuthProgressDialog!!.show()
            // calling the signInAnonymously()
            mAuth!!.signInAnonymously()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(this, AnonymousActivity::class.java))
                            mAuthProgressDialog!!.hide()
                            finish()
                        }
                    }
        }
        // [END]
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth!!.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}