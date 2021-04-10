package xyz.hisname.epicpython.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.ActivityMainBinding
import xyz.hisname.epicpython.ui.dashboard.DashboardFragment

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val db = Firebase.firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = false
        }
        db.firestoreSettings = settings
        val view = binding.root
        setContentView(view)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, DashboardFragment())
        }
    }
}