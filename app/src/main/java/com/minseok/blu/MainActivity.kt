package com.minseok.blu

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private var phoneNumberTextView: TextView? = null
    private var phoneNumber: String? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneNumberTextView = findViewById(R.id.phoneNumberTextView)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        } else {
            phoneNumber = getPhoneNumber()
            phoneNumberTextView.setText(phoneNumber)
        }

        setupBluetooth()

        // 블루투스 리더기의 MAC 주소를 사용
        val deviceAddress = "00:11:22:33:44:55" // 실제 블루투스 리더기의 주소로 대체
        try {
            connectToDevice(deviceAddress)
            sendData(phoneNumber)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getPhoneNumber(): String? {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        return telephonyManager.line1Number
    }

    private fun setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // 블루투스가 지원되지 않는 기기입니다.
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            // 블루투스가 비활성화 상태이면 활성화합니다.
            bluetoothAdapter!!.enable()
        }
    }

    @Throws(IOException::class)
    private fun connectToDevice(deviceAddress: String) {
        val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)
        bluetoothSocket =
            device.createRfcommSocketToServiceRecord(UUID.fromString("YOUR_UUID_HERE"))
        bluetoothSocket.connect()
        outputStream = bluetoothSocket.getOutputStream()
    }

    @Throws(IOException::class)
    private fun sendData(data: String?) {
        if (outputStream != null && data != null) {
            outputStream!!.write(data.toByteArray())
        }
    }

    companion object {
        private const val REQUEST_READ_PHONE_STATE = 1
    }


override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phoneNumber = getPhoneNumber()
                phoneNumberTextView!!.text = phoneNumber
            } else {
                phoneNumberTextView!!.text = "Permission required to read phone number."
            }
        }
    }

}
