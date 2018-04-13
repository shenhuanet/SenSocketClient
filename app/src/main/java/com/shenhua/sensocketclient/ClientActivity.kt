package com.shenhua.sensocketclient

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

/**
 * Created by shenhua on 2018/4/11.
 * @author shenhua
 *         Email shenhuanet@126.com
 */
class ClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        val fragment =
                when (intent.getStringExtra("fragment")) {
                    "TcpNioClientFragment" -> TcpNioClientFragment()
                    "TcpBioClientFragment" -> TcpBioClientFragment()
                    "UdpNioClientFragment" -> UdpNioClientFragment()
                    "UdpBioClientFragment" -> UdpBioClientFragment()
                    else -> Fragment()
                }
        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
    }
}