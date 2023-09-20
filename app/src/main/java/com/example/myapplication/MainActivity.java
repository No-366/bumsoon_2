package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Array;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //<선언부>////////////////////////////////////////////
    private BluetoothAdapter mBTAdapter;            // 블루투스 어댑터
    private Set<BluetoothDevice> mPairedDevices;    // 페어링 가능 기기 목록 담는 자료구조
    private Set<BluetoothDevice> mAvailableDevices; // 연결 가능 기기 목록 담는 자료구조
    private ArrayAdapter<String> mBTArrayAdapter;
    private ArrayAdapter<String> mBTArrayAdapter2;
    private ListView mDevicesListView;              // 페어링 가능 기기 목록 보여주는 리스트
    private ListView mDevicesListView2;
    private Button mListPairedDevicesBtn;           // 페어링 가능한 기기 목록 보여주기 버튼
    private Button mListAvailableDevicesBtn;

    ///////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //<구현부>////////////////////////////////////////////

        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        mListAvailableDevicesBtn = (Button) findViewById(R.id.DiscoverBtn);



        Intent intent = getIntent();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(blReceiver, filter);

        // 블루투스 어댑터활성화:  Bluetooth를 사용할 수 있는 준비가 된 상태
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();



        //페어링된기기를 표시할 리스트 생성 및 초기화
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //연결 가능기기 표시 리스트 생성 및 초기화
        mBTArrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);



        // 페어링 가능 기기 목록 제시
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        // 연격 가능 기기 목록 제시
        mDevicesListView2 = (ListView) findViewById(R.id.devicesListView2);
        mDevicesListView2.setAdapter(mBTArrayAdapter2);

        // 페어링된 기기리스트 활성화 버튼
        mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),"paired btn clicked", Toast.LENGTH_SHORT).show();
                listPairedDevices(v);
            }
        });

        // 연결가능한 기기리스트 활성화 버튼
        mListAvailableDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v){
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(),"discover btn clicked", Toast.LENGTH_SHORT).show();
                listAvailableDevices(v);
            }
        });





    ///////////////////////////////////////////////////
    }

    //<정의부>////////////////////////////////////////////
    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter2.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter2.notifyDataSetChanged();
            }




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





    ///////////////////////////////////////////////////
}