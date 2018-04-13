package com.shenhua.sensocketclient

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun tcpNio(view: View) {
        startActivity(Intent(this, ClientActivity::class.java)
                .putExtra("fragment", "TcpNioClientFragment"))
    }

    fun tcpBio(view: View) {
        startActivity(Intent(this, ClientActivity::class.java)
                .putExtra("fragment", "TcpBioClientFragment"))
    }

    fun udpNio(view: View) {
        startActivity(Intent(this, ClientActivity::class.java)
                .putExtra("fragment", "UdpNioClientFragment"))
    }

    fun udpBio(view: View) {
        startActivity(Intent(this, ClientActivity::class.java)
                .putExtra("fragment", "UdpBioClientFragment"))
    }

}
