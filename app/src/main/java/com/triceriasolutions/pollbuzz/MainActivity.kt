package com.triceriasolutions.pollbuzz

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.triceriasolutions.pollbuzz.databinding.ActivityMainBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingActivity
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PollBuzz)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.darkBar.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        )



        lifecycleScope.launchWhenStarted {
            delay(1000)
            startActivity(Intent(this@MainActivity, LandingActivity::class.java))
            finish()
        }


    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }


//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}