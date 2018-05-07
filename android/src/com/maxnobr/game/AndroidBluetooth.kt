package com.maxnobr.game

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import com.badlogic.gdx.Gdx

class AndroidBluetooth(private var activity:Activity) : Bluetooth{

    // Intent request codes
    private val REQUEST_CONNECT_DEVICE_SECURE = 1
    private val REQUEST_CONNECT_DEVICE_INSECURE = 2
    private val REQUEST_ENABLE_BT = 3
    private val REQUEST_DURATION = 100

    private var player:MultiPlayer? = null

    private var mConnectedDeviceName: String? = null
    private var mOutStringBuffer: StringBuffer? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mChatService: BluetoothChatService? = null

    override fun logBlue(msg: String) {
        Gdx.app.log("androidTag",msg)
    }

    override fun setMulti(multi:MultiPlayer) {
        player = multi
    }

    override fun canBlue(): Boolean {
        return mBluetoothAdapter != null
    }

    fun onCreate(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null)
            logBlue("there is no BluetoothAdapter")
        else
            logBlue("BluetoothAdapter Found !")
    }

    fun onStart() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (mBluetoothAdapter != null && !mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat()
        }
    }

    fun onDestroy() {
        mChatService?.stop()
    }

    fun onResume() {
        if (mChatService != null) {
            if (mChatService!!.state == BluetoothChatService.STATE_NONE) {
                mChatService?.start()
            }
        }
    }

    private fun setupChat() {
        logBlue("setupChat()")
        mChatService = BluetoothChatService(mHandler)
        mOutStringBuffer = StringBuffer("")
    }

    private fun ensureDiscoverable() {
        logBlue("discoverable !")
        if (mBluetoothAdapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_DURATION)
            activity.startActivityForResult(discoverableIntent,REQUEST_ENABLE_BT)
        }
    }

    override fun sendMessage(message: String) {
        if (mChatService?.state == BluetoothChatService.STATE_CONNECTED) {
            if (message.isNotEmpty()) {
                val send = message.toByteArray()
                mChatService?.write(send)
                mOutStringBuffer?.setLength(0)
            }
        }
        else
            logBlue("can't send message : we are not connected !")
    }

    private fun connectDevice(data: Intent, secure: Boolean) {
        val address = data.extras!!
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)

        logBlue("connecting to: $address")
        val device = mBluetoothAdapter?.getRemoteDevice(address)
        mChatService?.connect(device, secure)
    }

    private var mHandler = Handler{
        when(it.what) {
            Constants.MESSAGE_STATE_CHANGE -> {
                val status = when(it.arg1){
                    BluetoothChatService.STATE_CONNECTED -> "connected"
                    BluetoothChatService.STATE_CONNECTING -> "connecting"
                    BluetoothChatService.STATE_LISTEN -> "listening"
                    BluetoothChatService.STATE_NONE -> "none"
                    else -> "unknown status"
                }
                logBlue("changing status to: $status")
            }
            Constants.MESSAGE_WRITE -> {
                val writeBuf = it.obj as ByteArray
                // construct a string from the buffer
                val writeMessage = String(writeBuf)
                logBlue("just wrote: $writeMessage")
            }
            Constants.MESSAGE_READ -> {
                val readBuf = it.obj as ByteArray
                val readMessage = String(readBuf, 0, it.arg1)
                logBlue("just read: $readMessage")
                player?.receive(readMessage)
            }
            Constants.MESSAGE_DEVICE_NAME -> {
                mConnectedDeviceName = it.data.getString(Constants.DEVICE_NAME)
                logBlue("Connected to $mConnectedDeviceName")
            }
            Constants.MESSAGE_TOAST -> {
                logBlue(it.data.getString(Constants.TOAST))
            }
            else -> {}
        }
        true
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logBlue("onActivityResult!")
        logBlue("onActivityResult in launcher req: $requestCode result: $resultCode data: $data")
        when (requestCode) {
            REQUEST_CONNECT_DEVICE_SECURE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    connectDevice(data, true)
                }
            REQUEST_CONNECT_DEVICE_INSECURE ->
                if (resultCode == Activity.RESULT_OK  && data != null) {
                    connectDevice(data, false)
                }
            REQUEST_ENABLE_BT ->
                if (resultCode == REQUEST_DURATION) {
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occurred
                    logBlue("BT not enabled")
                }
        }
    }

    override fun receiveBtn(id : Int){
        when(id){
            1 ->{
                logBlue("opening intent for secure connection")
                val serverIntent = Intent(activity, DeviceListActivity::class.java)
                activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)
            }
            2 ->{
                logBlue("opening intent for insecure connection")
                val serverIntent = Intent(activity, DeviceListActivity::class.java)
                activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE)
            }
            3 -> ensureDiscoverable()
        }
    }
}