package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigureActivity extends AppCompatActivity {

    EditText ETaddressIP, ETaddressPHP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        String ip_address = Configure.getIPaddress(this);
        String php_address = Configure.getPhpDirPath(this);

        ETaddressIP = findViewById(R.id.edit_address_ip);
        ETaddressPHP = findViewById(R.id.edit_address_path);

        ETaddressIP.setText(ip_address);
        ETaddressPHP.setText(php_address);

        ETaddressIP.setSelection(ETaddressIP.getText().length());
        ETaddressPHP.setSelection(ETaddressPHP.getText().length());

        Button BTsave = findViewById(R.id.button_save);

        BTsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newAddressIP = ETaddressIP.getText().toString();
                String newAddressPath = ETaddressPHP.getText().toString();

                if(newAddressIP.equalsIgnoreCase(""))
                {
                    ETaddressIP.setError("This cannot be empty");
                }
                else
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("mPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("ip_address", newAddressIP);
                    editor.putString("php_address", newAddressPath);
                    editor.apply();

                    //Toast.makeText(ConfigureActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(ConfigureActivity.this, MainActivity.class));
                    finish();
                }

            }
        });

    }
}
