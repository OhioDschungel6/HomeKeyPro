package ml.stautnerindustries.homekeypro

import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.nio.ByteBuffer
import java.security.AccessController.getContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.*
import javax.security.auth.x500.X500Principal

class MyHostApduService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d("nfc", "processCommandApdu:" + commandApdu?.joinToString(" ") { "%02x".format(it) })
        if (commandApdu != null && commandApdu.size > 1 && commandApdu.get(1) == 0xA4.toByte()) {
            Log.d("nfc", "Selected")
            val uuid = uuidToByteArray(getUuid())
            Log.d("nfc", "UUID: "+ uuid.joinToString(" ") { "%02x".format(it) })
            return uuid
        }
        if (commandApdu != null) {
            if (commandApdu.get(0) == 0xCA.toByte()) {
                return byteArrayOf(0x1A.toByte())
            }
            if (commandApdu.get(0) == 0x56.toByte()) {
                //GetKey
                Log.d("nfc", "Get key")
                val keyPair = createOrGetKey()
                val publicKey = keyPair.certificate.publicKey.encoded
                Log.d("nfc", "Key: "+ publicKey?.joinToString(" ") { "%02x".format(it) })
                return publicKey
            }
            if (commandApdu.get(0) == 0x4A.toByte()) {
                //Verify
                Log.d("nfc", "Verify")
                val data = commandApdu.sliceArray(1..commandApdu.lastIndex)
                val keyPair = createOrGetKey()
                val signature = Signature.getInstance("SHA256withECDSA").run {
                    initSign(keyPair.privateKey)
                    update(data)
                    sign()
                }
                Log.d("nfc", "Data to sign: "+data.joinToString(" ") { "%02x".format(it) })
                Log.d("nfc", "Signature: "+signature?.joinToString(" ") { "%02x".format(it) })
                return signature
            }
        }
        return "9000".encodeToByteArray()
    }

    override fun onDeactivated(p0: Int) {

    }


    companion object {
        private const val alias = "HomeKeyPro"
        public fun createOrGetKey(): KeyStore.PrivateKeyEntry {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }

            if (ks.getEntry(Companion.alias, null) == null) {
                val kpg =
                    KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

                val parameterSpec =
                    KeyGenParameterSpec.Builder(
                        Companion.alias,
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                    )
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setCertificateSubject(X500Principal("CN=$alias"))
                        .build()

                kpg.initialize(parameterSpec)
                kpg.generateKeyPair()
            }

            val entry = ks.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
            return checkNotNull(entry)
        }

    }

    fun uuidToByteArray(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }

    fun getUuid(): UUID {
        val prefFile = "HomeKeyProPreferences"
        val prefKey = "UUID"
        val pref = getSharedPreferences(prefFile, Context.MODE_PRIVATE)
        return if (pref.contains(prefKey)) {
            UUID.fromString(pref.getString(prefKey, ""))
        } else {
            val uuid = UUID.randomUUID()
            pref.edit().putString("UUID", uuid.toString()).apply()
            uuid
        }
    }

}