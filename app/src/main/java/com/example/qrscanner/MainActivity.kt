package com.example.qrscanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edittext.view.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Whenever app starts up, check storage for previously stored strings
        val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
        val emptyString : String = ""
        val storedApiUrl : String? = sharedPref.getString("API_URL_KEY", emptyString)
        val storedUrl : String? = sharedPref.getString("URL_KEY", emptyString)
        val storedApiJson : String? = sharedPref.getString("API_JSON_KEY", emptyString)
        if (storedApiUrl != "") {editTxtApiUrl.setText(storedApiUrl)}
        if (storedUrl != "") {txtValue.text = storedUrl}
        if (storedApiJson != "") {editTxtApiJson.setText(storedApiJson)}

        editTxtApiUrl.SaveStringAfterTextChanged("API_URL_KEY")
        txtValue.SaveStringAfterTextChanged("URL_KEY")
        editTxtApiJson.SaveStringAfterTextChanged("API_JSON_KEY")

        // Open scanner
        btnScan.setOnClickListener {
            run {
                IntentIntegrator(this@MainActivity).initiateScan()
            }
        }

        // Edit url
        btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit URL")
            builder.setMessage("Change the url.")

            var view = layoutInflater.inflate(R.layout.edittext, null)
            builder.setView(view)
            view.editText.setText(txtValue.text)
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                txtValue.text = view.editText.text
                Toast.makeText(applicationContext, android.R.string.yes, Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
                Toast.makeText(applicationContext, android.R.string.cancel, Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        // Open url in new webview activity
        btnOpenUrl.setOnClickListener {
            val intent = Intent(this.applicationContext, WebViewActivity::class.java)
            intent.putExtra("THE_URL", txtValue.text.toString())
            startActivity(intent)
            Toast.makeText(this, txtValue.text.toString(), Toast.LENGTH_LONG).show()
        }
    }


    // After scanning
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                if (editTxtApiUrl.text.isNotEmpty())
                {
                    // Append result of api call to url
                    GlobalScope.launch {
                        val apiAsyncTask = async { PostApi() }
                        val apiResult = apiAsyncTask.await()
                        withContext(Dispatchers.Main) {
                            txtValue.text = result.contents + "?dtoken=" + apiResult + "&source="
                        }
                    }
                }
                else txtValue.text = result.contents
            } else {
                txtValue.text = "scan failed"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun <T> T.SaveStringAfterTextChanged(key : String){
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
                with (sharedPref.edit()) {
                    putString(key, s.toString())
                    commit()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        if (this is TextView) {
            this.addTextChangedListener(textWatcher)
        }else if (this is EditText){
            this.addTextChangedListener(textWatcher)
        }
    }

    private val client = OkHttpClient()
    private fun PostApi() : String {
        val JSON : MediaType = MediaType.parse("application/json; charset=utf-8")!!
        val body : RequestBody = RequestBody.create(JSON, editTxtApiJson.text.toString())
        val request = Request.Builder()
            .header("Connection", "close")
            .url(editTxtApiUrl.text.toString())
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            val jsonData = response.body()!!.string()
            val jsonObj = JSONObject(jsonData)
            return jsonObj.getString("token")
        }
        catch (ex : Exception) {}

        return ""
    }
}
