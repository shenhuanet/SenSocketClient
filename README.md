# SenSocketClient
[![jCenter](https://img.shields.io/badge/core-1.0.0-green.svg) ](https://dl.bintray.com/shenhuanetos/maven/com/shenhua/libs/sensocket-core/1.0.0/)
[![jCenter](https://img.shields.io/badge/tcp-1.0.0-blue.svg) ](https://dl.bintray.com/shenhuanetos/maven/com/shenhua/libs/sensocket-tcp/1.0.0/)
[![jCenter](https://img.shields.io/badge/udp-1.0.0-orange.svg) ](https://dl.bintray.com/shenhuanetos/maven/com/shenhua/libs/sensocket-udp/1.0.0/)
[![Build Status](https://img.shields.io/travis/rust-lang/rust/master.svg)](https://bintray.com/shenhuanetos/maven/SenSocket-Core)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

Sen Socket Client Core for Android develop.Supports TCP/UDP application development based on Java NIO/BIO technology.

![logo](https://raw.githubusercontent.com/shenhuanet/SenSocketClient/master/art/logo.png)

## Usage

Example for only use TCP client,you can change 'sensocket-tcp' to 'sensocket-udp' if only use UDP client.Of course, you can use it together

### Maven build settings 

build.gradle
```gradle
dependencies {
  compile 'com.shenhua.libs:sensocket-tcp:1.0.0'
}
```
or maven
```maven
<dependency>
  <groupId>com.shenhua.libs</groupId>
  <artifactId>sensocket-tcp</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```
or lvy
```lvy
<dependency org='com.shenhua.libs' name='sensocket-tcp' rev='1.0.0'>
  <artifact name='sensocket-tcp' ext='pom' ></artifact>
</dependency>
```
### Code
Kotlin (TCP-NIO)
```kotlin
private var tcpNioClient: TcpNioClient? = null
private var executor: ExecutorService = Executors.newSingleThreadExecutor()

fun connect() {
    executor!!.execute {
        val socketAddress = InetSocketAddress(ip, port)
        if (tcpNioClient == null) {
            tcpNioClient = TcpNioClient(mMessageProcessor, mConnectResultListener)
            tcpNioClient!!.setConnectAddress(arrayOf(TcpAddress(socketAddress.hostName, socketAddress.port)))
        }
        tcpNioClient!!.connect()
    }
}

fun send() {
    executor!!.execute {
        mMessageProcessor.send(tcpNioClient!!, "hello world".toByteArray())
    }
}

override fun disConnect() {
    tcpNioClient?.disconnect()
}

private val mMessageProcessor = object : BaseMessageProcessor() {
    override fun onReceiveMessages(mClient: BaseClient, mQueen: LinkedList<Message>) {
        for (i in mQueen.indices) {
            val message = mQueen[i]
            val s = String(message.data, message.offset, message.length)
            println(s)
            activity.runOnUiThread {
                tvMessage.append("\n$s")
            }
        }
    }
}

private val mConnectResultListener = object : IConnectListener {
    override fun onConnectionSuccess() {
        println("onConnectionSuccess")
    }
    override fun onConnectionFailed() {
        println("onConnectionFailed")
    }
}

override fun onDestroyView() {
    tcpNioClient?.disconnect()
    super.onDestroyView()
}
```

Java (TCP-NIO)
```java
private TcpNioClient tcpNioClient;
private ExecutorService executor = Executors.newSingleThreadExecutor();

public void connect() {
    executor.execute(new Runnable() {
        @Override
        public void run() {
            TcpAddress tcpAddress = new TcpAddress(host, port);
            if (tcpNioClient == null) {
                tcpNioClient = new TcpNioClient(mMessageProcessor, mConnectResultListener);
                tcpNioClient.setConnectAddress(new TcpAddress[]{tcpAddress});
            }
            tcpNioClient.connect();
        }
    });
}

public void send() {
    executor.execute(new Runnable() {
        @Override
        public void run() {
            mMessageProcessor.send(tcpNioClient, "hello world".getBytes());
        }
    });
}

public void disConnect() {
    tcpNioClient.disconnect();
}

private BaseMessageProcessor mMessageProcessor = new BaseMessageProcessor() {
    @Override
    public void onReceiveMessages(BaseClient mClient, LinkedList<Message> mQueen) {
        for (int i = 0; i < mQueen.size(); i++) {
            Message message = mQueen.get(i);
            String msg = new String(message.data, message.offset, message.length);
            System.out.println(msg);
        }
    }
};

private IConnectListener mConnectResultListener = new IConnectListener() {
    @Override
    public void onConnectionSuccess() {
        System.out.println("onConnectionSuccess");
    }
    @Override
    public void onConnectionFailed() {
        System.out.println("onConnectionFailed");
    }
};
```

### Sample Screenshot

![](https://github.com/shenhuanet/SenSocketClient/blob/master/art/001.png)

![](https://github.com/shenhuanet/SenSocketClient/blob/master/art/002.png)

## About Me
CSDN：http://blog.csdn.net/klxh2009<br>
JianShu：http://www.jianshu.com/u/12a81897d5bc

## License

    Copyright 2018 shenhuanet

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.