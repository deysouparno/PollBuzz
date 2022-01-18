package com.triceriasolutions.pollbuzz.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkStatusHelper(context: Context) : LiveData<NetworkStatus>() {

    val validNetworkConnections: ArrayList<Network> = ArrayList()
    var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    fun announceStatus() {
        if (validNetworkConnections.isNotEmpty()) {
            postValue(NetworkStatus.Available)
        } else {
            postValue(NetworkStatus.Unavailable)
        }
    }

    private fun getConnectivityManagerCallback() =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.d("network", "available called")
                super.onAvailable(network)
//                postValue(NetworkStatus.Available)
                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                val hasNetworkConnection =
                    networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        ?: false
                if (hasNetworkConnection) {
                    validNetworkConnections.add(network)
                    announceStatus()
                }
                Log.d("network", validNetworkConnections.toString())
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d("network", "network lost called")
                postValue(NetworkStatus.Unavailable)
                validNetworkConnections.remove(network)
                announceStatus()
                Log.d("network", validNetworkConnections.toString())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d("network", "changed capabilities called")
                super.onCapabilitiesChanged(network, networkCapabilities)
//                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
//                    validNetworkConnections.add(network)
//                } else {
//                    validNetworkConnections.remove(network)
//                }
//                announceStatus()
                Log.d("network", validNetworkConnections.toString())

            }
        }


    override fun onActive() {
        super.onActive()
        connectivityManagerCallback = getConnectivityManagerCallback()
        val networkRequest = NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, connectivityManagerCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }


}

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}