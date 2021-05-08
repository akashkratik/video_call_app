package com.example.appyhightest2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        MobileAds.initialize(this@LoginActivity)
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        btn_login.setOnClickListener {

            when {
                TextUtils.isEmpty(login_email.text.toString().trim(){it <= ' '}) -> {
                    Toast.makeText(this@LoginActivity, "Please Enter email", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(login_password.text.toString().trim(){it <= ' '}) -> {
                    Toast.makeText(this@LoginActivity, "Please Enter Password", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val email:String = login_email.text.toString().trim(){ it <= ' '}
                    val password: String = login_password.text.toString().trim(){ it <= ' '}

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this@LoginActivity, "Logged in Succesfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, GetStarted::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        }else {
                            Toast.makeText(this@LoginActivity, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


        }
    }
}