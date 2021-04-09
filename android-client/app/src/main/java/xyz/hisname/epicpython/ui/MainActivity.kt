package xyz.hisname.epicpython.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.ActivityMainBinding
import xyz.hisname.epicpython.ui.dashboard.DashboardFragment

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportFragmentManager.commit {
            replace(R.id.fragment_container, DashboardFragment())
        }
    }
}