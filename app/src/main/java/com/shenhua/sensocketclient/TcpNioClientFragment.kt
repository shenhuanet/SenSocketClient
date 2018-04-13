package com.shenhua.sensocketclient

import android.os.Bundle
import android.view.View
import com.shenhua.libs.sensocketcore.BaseClient
import com.shenhua.libs.sensocketcore.BaseMessageProcessor
import com.shenhua.libs.sensocketcore.IConnectListener
import com.shenhua.libs.sensocketcore.message.Message
import com.shenhua.libs.sensockettcp.TcpAddress
import com.shenhua.libs.sensockettcp.nio.TcpNioClient
import kotlinx.android.synthetic.main.fragment_socket.*
import java.net.InetSocketAddress
import java.util.*

/**
 * Created by shenhua on 2018/4/11.
 * @author shenhua
 *         Email shenhuanet@126.com
 */
class TcpNioClientFragment : BaseFragment() {

    private var tcpNioClient: TcpNioClient? = null

    override fun connect() {
        executor!!.execute {
            val socketAddress = InetSocketAddress(etHost.text.toString(), etPort.text.toString().toInt())
            if (tcpNioClient == null) {
                tcpNioClient = TcpNioClient(mMessageProcessor, mConnectResultListener)
                tcpNioClient!!.setConnectAddress(arrayOf(TcpAddress(socketAddress.hostName, socketAddress.port)))
            }
            tcpNioClient!!.connect()
        }
    }

    override fun send() {
        executor!!.execute {
            mMessageProcessor.send(tcpNioClient!!, etSend.text.toString().toByteArray())
        }
    }

    override fun disConnect() {
        tcpNioClient?.disconnect()
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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.title = getString(R.string.app_name) + "-Tcp Nio"
    }

    override fun onDestroyView() {
        tcpNioClient?.disconnect()
        super.onDestroyView()
    }
}