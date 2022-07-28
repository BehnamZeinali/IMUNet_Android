package edu.usf.imunet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.usf.imunet.R;

public class SelectActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select2);
    }

    public void startNewActivity(View view) {
        Intent intent = null;
        if (view.getId() == R.id.button_training){
            intent = new Intent(this, MainActivity.class);
        }else if(view.getId() == R.id.button_upload){
            intent = new Intent(this, ActivitySelect.class);
        } else if(view.getId() == R.id.button_comparison){
        intent = new Intent(this, ComparisonActivity.class);
    }
        startActivity(intent);
    }
}