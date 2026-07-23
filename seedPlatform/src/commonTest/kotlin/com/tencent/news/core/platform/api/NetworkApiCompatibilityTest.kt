package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkApiCompatibilityTest {
    @Test
    fun networkMethodsUseLegacyNamesAndPb() {
        val network = RecordingNetwork()
        val builder = NetworkBuilder("", originJsonParser())
        val pbBuilder = PBNetworkBuilder("", { byteArrayOf() }, { it })

        network.jsonPostRequest(builder)
        network.formPostRequest(builder)
        network.getRequest(builder)
        network.sseRequest(builder)
        network.jsonMultiPostRequest(builder)
        network.streamPostRequest(builder)
        network.postPb(pbBuilder)

        assertEquals(
            listOf(
                "jsonPostRequest",
                "formPostRequest",
                "getRequest",
                "sseRequest",
                "jsonMultiPostRequest",
                "streamPostRequest",
                "postPb",
            ),
            network.calls,
        )
    }

    @Test
    fun pbBuilderExecuteDelegatesToAppNetworkPostPb() {
        val originNetwork = QnPlatformLogic.network
        val network = RecordingNetwork()
        QnPlatformLogic.network = network
        try {
            val request = PBNetworkBuilder("", { byteArrayOf() }, { it }).execute()

            assertEquals(DefaultNetworkRequest::class, request::class)
            assertEquals(listOf("postPb"), network.calls)
        } finally {
            QnPlatformLogic.network = originNetwork
        }
    }

    @Test
    fun networkOwnsNetStateListenerApi() {
        val network: INetwork = RecordingNetwork()
        val listener = object : NetStateChangeListener {
            override fun netStateChanged(old: NetState, new: NetState) = Unit
        }

        network.addNetStatusChangeListener(listener)
        network.removeNetStatusChangeListener(listener)
    }
}

private class RecordingNetwork : INetwork {
    val calls = mutableListOf<String>()

    override fun <T> jsonPostRequest(builder: NetworkBuilder<T>) = record("jsonPostRequest")

    override fun <T> formPostRequest(builder: NetworkBuilder<T>) = record("formPostRequest")

    override fun <T> getRequest(builder: NetworkBuilder<T>) = record("getRequest")

    override fun <T> sseRequest(builder: NetworkBuilder<T>) = record("sseRequest")

    override fun <T> jsonMultiPostRequest(builder: NetworkBuilder<T>) = record("jsonMultiPostRequest")

    override fun <T> streamPostRequest(builder: NetworkBuilder<T>) = record("streamPostRequest")

    override fun <T> postPb(builder: PBNetworkBuilder<T>) = record("postPb")

    override fun netState(): NetState = NetState.WIFI

    private fun record(name: String): INetworkRequest {
        calls += name
        return DefaultNetworkRequest()
    }
}
