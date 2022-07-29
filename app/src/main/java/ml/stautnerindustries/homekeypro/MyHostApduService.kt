package ml.stautnerindustries.homekeypro

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore

class MyHostApduService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d("nfc", "processCommandApdu:" + commandApdu?.joinToString(" ") { "%02x".format(it) })
        if (commandApdu != null && commandApdu.size > 1 && commandApdu.get(1) == 0xA4.toByte()) {
            Log.d("nfc", "Selected")
            return "7000".encodeToByteArray();
        }
        val kp = getKeyPair()

        Log.d("nfc" , "PublicKey: "+kp.public)
        return "9000".encodeToByteArray()
    }

    private fun getKeyPair(): KeyPair? {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        var keyEntry = ks.getEntry("HomeKeyPro",null)
        if(keyEntry == null){
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                "HomeKeyPro",
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                build()
            }
            kpg.initialize(parameterSpec)
            var keyPair = kpg.generateKeyPair()
            ks.se
        }

        return
    }

    override fun onDeactivated(reason: Int) {
        //TODO("Not yet implemented")
        Log.d("nfc", "Reason: " + reason)
    }

}