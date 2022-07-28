package edu.usf.imunet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.usf.imunet.R;

public class ActivitySelect extends AppCompatActivity {

    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button btn_6;
    private Button btn_7;
    private Button btn_8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        btn_1 = findViewById(R.id.track_1);
        btn_2 = findViewById(R.id.track_2);
        btn_3 = findViewById(R.id.track_3);
        btn_4 = findViewById(R.id.track_4);
        btn_5 = findViewById(R.id.track_5);
        btn_6 = findViewById(R.id.track_6);
        btn_7 = findViewById(R.id.track_7);
        btn_8 = findViewById(R.id.track_8);

        final Bundle bundle = new Bundle();



        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);

                bundle.putString("minX", String.valueOf(-20));
                bundle.putString("minY", String.valueOf(-5));
                bundle.putString("maxX", String.valueOf(5));
                bundle.putString("maxY", String.valueOf(20));
                bundle.putString("file", "1");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-10));
                bundle.putString("minY", String.valueOf(-5));
                bundle.putString("maxX", String.valueOf(10));
                bundle.putString("maxY", String.valueOf(65));
                bundle.putString("file", "2");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-20));
                bundle.putString("minY", String.valueOf(0));
                bundle.putString("maxX", String.valueOf(10));
                bundle.putString("maxY", String.valueOf(45));
                bundle.putString("file", "3");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-25));
                bundle.putString("minY", String.valueOf(-6));
                bundle.putString("maxX", String.valueOf(5));
                bundle.putString("maxY", String.valueOf(75));
                bundle.putString("file", "4");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-5));
                bundle.putString("minY", String.valueOf(-5));
                bundle.putString("maxX", String.valueOf(25));
                bundle.putString("maxY", String.valueOf(45));
                bundle.putString("file", "5");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-65));
                bundle.putString("minY", String.valueOf(-5));
                bundle.putString("maxX", String.valueOf(5));
                bundle.putString("maxY", String.valueOf(50));
                bundle.putString("file", "6");
                i.putExtras(bundle);
                startActivity(i);

            }
        });
        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-40));
                bundle.putString("minY", String.valueOf(-5));
                bundle.putString("maxX", String.valueOf(20));
                bundle.putString("maxY", String.valueOf(105));
                bundle.putString("file", "7");
                i.putExtras(bundle);
                startActivity(i);

            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivitySelect.this, MainActivity2.class);
                bundle.putString("minX", String.valueOf(-100));
                bundle.putString("minY", String.valueOf(0));
                bundle.putString("maxX", String.valueOf(0));
                bundle.putString("maxY", String.valueOf(250));
                bundle.putString("file", "8");
                i.putExtras(bundle);
                startActivity(i);

            }
        });

    }
}