package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //<선언부>////////////////////////////////////////////
    private BluetoothAdapter mBTAdapter;            // 블루투스 어댑터
    private Set<BluetoothDevice> mPairedDevices;    // 페어링 가능 기기 목록 담는 자료구조
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;              // 페어링 가능 기기 목록 보여주는 리스트
    private Button mListPairedDevicesBtn;           // 페어링 가능한 기기 목록 보여주기 버튼
    private BluetoothSocket mBTSocket = null;       // bi-directional client-to-client data path

    // 블루투스 모드를 위한 고유 UUID 번호 ("random" unique identifier)
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //블루투스 상태
    private int PAIR_STATUS = 0;

    ///////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //<구현부>////////////////////////////////////////////

        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);

        Intent intent = getIntent();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(blReceiver, filter);

        // 블루투스 어댑터활성화:  Bluetooth를 사용할 수 있는 준비가 된 상태
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();


        //페어링된기기를 표시할 리스트 생성 및 초기화
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);


        // 페어링 가능 기기 목록 제시
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view

        // 페어링된 기기리스트 활성화 버튼
        mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),"paired btn clicked", Toast.LENGTH_SHORT).show();
                listPairedDevices(v);
            }
        });

        //페어링된 기기 리스트에서 기기선택클릭을 감지할 리스너
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        ///////////////////////////////////////////////////
    }

    //<정의부>////////////////////////////////////////////
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //블루투스어댑터의 상태 체크(on/off)
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBTAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    Toast.makeText(getApplicationContext(),"Bluetooth on",Toast.LENGTH_SHORT).show();
                }
                if (mBTAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    Toast.makeText(getApplicationContext(),"Bluetooth offed",Toast.LENGTH_SHORT).show();
                    // The user bluetooth is already disabled.
                    return;
                }
            }
        }
    };


    // 페어링된기기 목록을 보여준다
    @SuppressLint("MissingPermission")
    private void listPairedDevices(View view){
        if(mBTAdapter.isEnabled()) {
            mPairedDevices = mBTAdapter.getBondedDevices();
            // put it's one to the adapter
            mBTArrayAdapter.clear();
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }


    // 페어링 가능 기기 목록에서 연결하고자 하는 기기를 선택했을 때
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 현재 기기의 블루투스가 활성화 되어있지 않음
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            // 현재 기기와 페어링 시도하는 기기 사이의 연결 시도
            // 페어링 시도하는 기기의 MAC 주소를 가져온다 (MAC 주소 : 리스트뷰 상에서 마지막 17글자)
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                @SuppressLint("MissingPermission")
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        Log.i("User Log ::", device.toString());
                        mBTSocket = device.createInsecureRfcommSocketToServiceRecord(new UUID(1,2));
                        Log.i("USER LOG ::",mBTSocket.toString());
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // 블루투스 소켓 연결 활성화 시키기
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            PAIR_STATUS = 0; // 현재 블루투스 페어링이 성공적이지 않은 상태
                            mBTSocket.close();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        PAIR_STATUS = 1; // 현재 블루투스 페어링이 성공적인 상태
                        //Toast.makeText(getBaseContext(),"블루투스 연결에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    };


    // 블루투스 소켓 생성
    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    ///////////////////////////////////////////////////
}
