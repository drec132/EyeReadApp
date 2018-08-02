package codeeater.com.eyereadapp.activity

import android.app.ProgressDialog
import android.nfc.Tag
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import codeeater.com.eyereadapp.R
import codeeater.com.eyereadapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {

    // Create a Database Instance
    private val database = FirebaseDatabase.getInstance()!!
    // Create a Database Reference container
    private var dbRef: DatabaseReference? = null
    // Create a Authentication Reference
    private var mAuth = FirebaseAuth.getInstance()
    // Widgets
    private var btnSignup: Button? = null
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var etAge: EditText? = null
    private var etPassword: EditText? = null
    private var etConfirmPass: EditText? = null
    private var tvLogin: TextView? = null
    private var rdoMale: RadioButton? = null
    private var rdoFemale: RadioButton? = null

    /* A dialog that is presented until the Firebase authentication finished. */
    private var mAuthProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuthProgressDialog = ProgressDialog(this)
        mAuthProgressDialog!!.setTitle("Loading")
        mAuthProgressDialog!!.setMessage("Preparing the data...")
        mAuthProgressDialog!!.setCancelable(false)

        btnSignup = findViewById(R.id.btnSignup)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAge = findViewById(R.id.etAge)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        tvLogin = findViewById(R.id.tvLogin)
        rdoMale = findViewById(R.id.rdoMale)
        rdoFemale = findViewById(R.id.rdoFemale)

        // [START] TextView onclick to the
        tvLogin!!.setOnClickListener {
            finish()
        }
        // [END]

        // [START] Registering the user information
        btnSignup!!.setOnClickListener {
            val name = etName!!.text.toString().trim()
            val email = etEmail!!.text.toString().trim()
            val age = etAge!!.text.toString().trim()
            val password = etPassword!!.text.toString().trim()
            val cPassword = etConfirmPass!!.text.toString().trim()
            val male = rdoMale!!.isChecked
            val female = rdoFemale!!.isChecked

            try {
                if (!hasValidEmail(email)) {
                    etEmail!!.error = "Enter your email"
                }
                if (age.isEmpty()) {
                    etAge!!.error = "You need to enter your age"
                }
                if (cPassword.isEmpty() && password.isEmpty()) {
                    etPassword!!.error = "Your password must be at least 6 characters in length."
                    etConfirmPass!!.error = "You need to confirm your password."
                } else {
                    if (!hasPassword(password) && !hasPassword(cPassword)){
                        etPassword!!.error = "Your password must be at least 6 characters in length."
                        etConfirmPass!!.error = "You need to confirm your password."
                    } else {
                        if (password == cPassword) {
                            mAuthProgressDialog!!.show()
                            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = mAuth.currentUser!!.uid
                                    dbRef = database.getReference(FB_DATABASE_USER_PATH).child(user)

                                    var userUpload = User()

                                    when {
                                        male -> userUpload = User(name,email,age, "male")
                                        female -> userUpload = User(name, email, age, "female")
                                    }

                                    dbRef!!.setValue(userUpload).addOnCompleteListener {
                                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                        mAuth.signOut()
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                                }
                                mAuthProgressDialog!!.hide()
                            }
                        } else {
                            Toast.makeText(this, "Password Mismatch", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.w(MainActivity.TAG, e.message)
            }


        }
        // [END]
    }

    // validating email id
    private fun hasValidEmail(email: String): Boolean {
        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    // validating password with retype password
    private fun hasPassword(pass: String): Boolean {
        return pass!= null && pass.length >= 6
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        val FB_DATABASE_USER_PATH = "EyereadUser"
    }
}
