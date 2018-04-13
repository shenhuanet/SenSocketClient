package com.shenhua.sensocketclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shenhua.libs.sensocketcore.BaseClient
import com.shenhua.libs.sensocketcore.BaseMessageProcessor
import com.shenhua.libs.sensocketcore.IConnectListener
import com.shenhua.libs.sensocketcore.message.Message
import com.shenhua.libs.sensocketudp.UdpAddress
import com.shenhua.libs.sensocketudp.bio.UdpBioClient
import kotlinx.android.synthetic.main.fragment_socket.*
import java.net.InetSocketAddress
import java.util.*

/**
 * Created by shenhua on 2018/4/11.
 * @author shenhua
 *         Email shenhuanet@126.com
 */
class UdpBioClientFragment : BaseFragment() {

    private var udpBioClient: UdpBioClient? = null

    override fun connect() {
        executor!!.execute {
            val socketAddress = InetSocketAddress(etHost.text.toString(), etPort.text.toString().toInt())
            if (udpBioClient == null) {
                udpBioClient = UdpBioClient(mMessageProcessor, mConnectResultListener)
                udpBioClient!!.setConnectAddress(arrayOf(UdpAddress(socketAddress.hostName, socketAddress.port)))
            }
            udpBioClient!!.connect()
        }
    }

    override fun send() {
        executor!!.execute {
            mMessageProcessor.send(udpBioClient!!, etSend.text.toString().toByteArray())
        }
    }

    override fun disConnect() {
        udpBioClient?.disconnect()
    }

    private val mMessageProcessor = object : BaseMessageProcessor() {
        override fun onReceiveMessages(mClient: BaseClient, mQueen: LinkedList<Message>) {
            for (i in mQueen.indices) {
                val message = mQueen[i]
                // 此处接收的message.data需要按实际进行编码或进制转换
                val s = String(message.data, message.offset, message.length)
                println("--- $s")
                activity.runOnUiThread {
                    tvMessage.append("\n$s")
                }
            }
        }
    }

    private val mConnectResultListener = object : IConnectListener {
        override fun onConnectionSuccess() {
            activity.runOnUiThread { tvMessage.append("\n 连接成功") }
        }

        override fun onConnectionFailed() {
            activity.runOnUiThread { tvMessage.append("\n 连接失败") }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_socket, container, false);
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.app_name) + "-Udp Bio"
    }

    override fun onDestroyView() {
        udpBioClient?.disconnect()
        super.onDestroyView()
    }
}