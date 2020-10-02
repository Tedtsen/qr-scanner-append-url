package com.example.qrscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edittext.view.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan.setOnClickListener {
            run {
                IntentIntegrator(this@MainActivity).initiateScan();
            }
        }

        btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit URL")
            builder.setMessage("Change the url.")

            var view = layoutInflater.inflate(R.layout.edittext, null);
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

        btnOpenUrl.setOnClickListener {
            val intent = Intent(this.applicationContext, WebViewActivity::class.java)
            intent.putExtra("THE_URL", txtValue.text.toString())
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {

            if (result.contents != null) {
                txtValue.text = result.contents
            } else {
                txtValue.text = "scan failed"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
