package codeeater.com.eyereadapp.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import codeeater.com.eyereadapp.R
import codeeater.com.eyereadapp.fragment.VisionFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    // [START] Firebase Authentication Instance
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    // [END]
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container)
        mViewPager!!.offscreenPageLimit = 1
        mViewPager!!.adapter = mSectionsPagerAdapter
    }

    // [START]create an handler if the it's offline user or firebase user
    fun giveUser(): String? {
        val user = mAuth.currentUser
        return user?.uid
    }
    // [END]

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /**
     * A [FragmentStatePagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return VisionFragment()
        }

        override fun getCount(): Int {
            return 1
        }

        override fun getItemPosition(`object`: Any?): Int {
            return PagerAdapter.POSITION_NONE
        }
    }
    companion object {
        val TAG = MainActivity::class.java.simpleName!!
        val FB_STORAGE_PATH = "image/"
        val FB_DATABASE_PATH = "EyereadTest"
    }
}
