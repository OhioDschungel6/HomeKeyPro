package ml.stautnerindustries.homekeypro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var textView = findViewById<TextView>(R.id.pk)
        val keyPair = MyHostApduService.createOrGetKey()
        val publicKey = keyPair.certificate.publicKey.encoded?.joinToString(" ") { "%02x".format(it) }
        var text = "Public key:\n$publicKey"
        textView.setText(text)
    }
}