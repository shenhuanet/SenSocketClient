package com.shenhua.sensocketclient

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_socket.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by shenhua on 2018/4/11.
 * @author shenhua
 *         Email shenhuanet@126.com
 */
abstract class BaseFragment : Fragment() {

    var executor: ExecutorService? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_socket, container, false);
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnConnect.setOnClickListener { connect() }
        btnSend.setOnClickListener { send() }
        btnDisConnect.setOnClickListener { disConnect() }
        executor = Executors.newSingleThreadExecutor()
    }

    abstract fun connect()

    abstract fun send()

    abstract fun disConnect()
}