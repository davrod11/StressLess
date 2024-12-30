package com.example.stressless.main.profile;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.stressless.R;
import java.util.ArrayList;
import java.util.Set;

public class ConnectionsFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private final ArrayList<String> deviceList = new ArrayList<>();
    private final ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connections, container, false);

        ListView listViewDevices = rootView.findViewById(R.id.list_view_devices);
        devicesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceList);
        listViewDevices.setAdapter(devicesAdapter);

        Button btnRefresh = rootView.findViewById(R.id.btn_refresh);

        listViewDevices.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            BluetoothDevice selectedDevice = bluetoothDevices.get(position);
            connectToDevice(selectedDevice);
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth is not supported on this device.");
            return rootView;
        }

        btnRefresh.setOnClickListener(v -> checkPermissionsAndDiscoverDevices());

        checkPermissionsAndDiscoverDevices();

        return rootView;
    }

    private void checkPermissionsAndDiscoverDevices() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
        } else {
            permissions = new String[]{Manifest.permission.BLUETOOTH};
        }
        if (!hasPermissions(permissions)) {
            requestPermissions(permissions, 1);
            return;
        }
        discoverDevicesSafely();
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void discoverDevicesSafely() {
        try {
            discoverDevices();
        } catch (SecurityException e) {
            showToast("Permission denied. Unable to access Bluetooth devices.");
        }
    }

    private void discoverDevices() {
        if (bluetoothAdapter.isEnabled()) {
            deviceList.clear();
            bluetoothDevices.clear();

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                showToast("Missing permission to get bonded devices.");
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && !pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = (device.getName() != null) ? device.getName() : "Unknown Device";
                    deviceList.add(deviceName + "\n" + device.getAddress());
                    bluetoothDevices.add(device);
                }
            } else {
                showToast("No paired devices found.");
            }
            devicesAdapter.notifyDataSetChanged();
        } else {
            showToast("Please enable Bluetooth to see available devices.");
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                showToast("Missing permission to connect to devices.");
                return;
            }
            showToast("Attempting to connect to: " + device.getName());
            // Replace with actual connection logic
        } catch (Exception e) {
            showToast("Error connecting to device: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                discoverDevicesSafely();
            } else {
                showToast("Bluetooth permission is required to discover devices.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
