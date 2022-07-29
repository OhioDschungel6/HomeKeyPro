package ml.stautnerindustries.homekeypro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("nfc", "MainActivity")
//        startService(Intent(this,MyHostApduService::class.java))
    }
}