package dev.vffuunnyy.push_bridger

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fredporciuncula.phonemoji.PhonemojiTextInputEditText
import com.google.android.material.textfield.TextInputEditText
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class AuthActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("isAuthorized", false)) {
            goToMainActivity()
            return
        }


        val phoneNumberEditText = findViewById<TextInputEditText>(R.id.enter_phone_number)
        val phoneUtils = PhoneNumberUtil.createInstance(this)

        findViewById<Button>(R.id.enter_app_button).setOnClickListener {
            val phoneNumberText = phoneNumberEditText.text.toString()
            val parsed = phoneUtils.parse(phoneNumberText, "BD")
            Log.d("AuthActivity", "Enter app button clicked with Country Code: ${parsed.countryCode}, Phone: ${parsed.nationalNumber}")
            authorize(parsed.countryCode.toString(), parsed.nationalNumber.toString())
        }
    }

    private fun authorize(dialCode: String, phoneNumber: String) {
        val jsonObject = JSONObject().apply {
            put("dial_code", dialCode)
            put("mobile_number", phoneNumber)
        }

        coroutineScope.launch {
            try {
                val response = Utils.sendPostRequest(getString(R.string.server_auth_url), null, jsonObject.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    val responseJson = JSONObject(responseBody)
                    val token = responseJson.optString("token", "")
                    sharedPreferences.edit()
                        .putBoolean("isAuthorized", true)
                        .putString("token", token)
                        .putString("dialCode", dialCode)
                        .putString("phoneNumber", phoneNumber)
                        .apply()

                    goToMainActivity()
                } else {
                    val responseBody = response.body?.string() ?: "{}"
                    val responseJson = JSONObject(responseBody)
                    val message = responseJson.optString("message", "")

                    Log.e("AuthActivity", "Authorization failed: ${response.code}, $message")

                    if (message.isNotEmpty()) {
                        val error = findViewById<TextView>(R.id.enter_phone_number_error)
                        error.text = message
                        error.visibility = TextView.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthActivity", "Authorization error: ${e.message}")

                val error = findViewById<TextView>(R.id.enter_phone_number_error)
                error.text = getString(R.string.server_error)
                error.visibility = TextView.VISIBLE
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
