package edu.usf.imunet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import edu.usf.imunet.Quaternion.Quaternion;

import edu.usf.imunet.R;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ComparisonActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private Interpreter tfLite_1;
    private Interpreter tfLite_2;
    private AssetManager assetManager ;

    private Handler mainHandler = new Handler();
    private float tango_rt_w;
    private float tango_rt_x;
    private float tango_rt_y;
    private float tango_rt_z;

    private float game_rv_w;
    private float game_rv_x;
    private float game_rv_y;
    private float game_rv_z;

    private float init_pos_x;
    private float init_pos_y;
    private ScatterChart mChart ;

    private Button btn_track;

    private Thread trackingThread_1;
    private Thread trackingThread_2;


    private static float number = (float) 1000000000.00;
    int i = 0;
    int j_1 = 0;
    int j_2 = 0;
    private ScatterDataSet gt_position;
    private ScatterDataSet es_position_1;
    private ScatterDataSet es_position_2;
    ScatterData data = null;

    private String model_name;
    Button btn_stop;
    private Interpreter.Options options ;


    private long execution_mean_1 = 0;
    private long execution_mean_2 = 0;
    private String[] models = new String[]{"ResNet.tflite" , "MobileNet.tflite" , "MobileNetV2.tflite" , "MnasNet.tflite" , "EfficientNet.tflite" , "IMUNet.tflite"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        mChart = findViewById(R.id.chart_view);
        btn_track = findViewById(R.id.btn_tracking);
        btn_stop = findViewById(R.id.btn_stop);
        btn_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               trackThread_1();
               trackThread_2();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_track.setEnabled(false);
        btn_track.setActivated(false);

        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);


        XAxis x_axis = mChart.getXAxis();
        YAxis y_axis_right = mChart.getAxisRight();
        YAxis y_axis_left = mChart.getAxisLeft();

        float xMin = -40;
        float xMax = 30;
        float yMin =-5;
        float yMax = 65;

        Spinner spinnerModels = findViewById(R.id.model_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.model_comparisons, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerModels.setAdapter(adapter);
        spinnerModels.setOnItemSelectedListener(this);



        x_axis.setAxisMaximum(xMax);
        x_axis.setAxisMinimum(xMin);

        y_axis_right.setAxisMaximum(yMax);
        y_axis_right.setAxisMinimum(yMin);

        y_axis_left.setAxisMaximum(yMax);
        y_axis_left.setAxisMinimum(yMin);

        String title = "Behnam_8_Tango";
        this.setTitle(title);


        FlexDelegate delegate = new FlexDelegate();
        options = new Interpreter.Options().addDelegate(delegate);

        assetManager = getAssets();
        try{
            MappedByteBuffer node_model = loadModelFile("IMUNet.tflite") ;
            //MappedByteBuffer node_model = loadModelFile("first_tango_model.tflite")
            tfLite_1 = new Interpreter( node_model , options);
            model_name = "IMUNet.tflite";
        } catch (Exception ex){
            ex.printStackTrace();
        }

        readThread();

    }

    private MappedByteBuffer loadModelFile(String chosen) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(chosen);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        try{
            MappedByteBuffer node_model = loadModelFile(models[i]) ;
            //MappedByteBuffer node_model = loadModelFile("first_tango_model.tflite")
            tfLite_2 = new Interpreter( node_model , options);
            Toast.makeText(this , models[i] + " loaded" , Toast.LENGTH_SHORT).show();
            model_name = models[i];
            btn_track.setEnabled(true);
        } catch (Exception ex){
            Toast.makeText(this , "Loading Error" , Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }




    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {


    }

    class ExampleRunnable implements Runnable{
        int seconds;
        ExampleRunnable(int seconds){
            this.seconds = seconds;
        }
        @Override
        public void run() {
            try {
                readPntData();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    float[][][] input_1 , input_2;
    float[][] output_1 , output_2;
    class TrackingRunnable_1 implements Runnable{
        @Override
        public void run() {
            trackPntData_1();
        }
    }


    class TrackingRunnable_2 implements Runnable{
        @Override
        public void run() {
            trackPntData_2();
        }
    }


    private void trackPntData_1() {

        Quaternion init_rotor = new Quaternion(tango_rt_x, tango_rt_y, tango_rt_z , tango_rt_w);
        Quaternion game_rv = new Quaternion(game_rv_x, game_rv_y, game_rv_z , game_rv_w);
        game_rv.conjugate();
        init_rotor.multiplyByQuat(game_rv);
        input_1 = new float[1][6][200];
        double last_time = samples.get(0).getTime();
        double time_sum = 0;
        float dts = 0 ;
        output_1 = new float[1][2];
        float previous_pos_x = 0;
        float previous_pos_y = 0;
        execution_mean_1  = 0;

        for (j_1 = 0; j_1 < samples.size() ; j_1++){

            SensorSample sSample = samples.get(j_1);

            if (j_1>0){
                double temp = sSample.getTime() - last_time;
                time_sum = time_sum + temp;
                double dts_ = time_sum/j_1;
                dts = Float.valueOf((float) dts_);
                last_time = sSample.getTime();

            }

            Quaternion ori = new Quaternion(init_rotor.getX(),init_rotor.getY(),init_rotor.getZ(),init_rotor.getW());
            game_rv = new Quaternion(sSample.getOri_x(), sSample.getOri_y(),
                    sSample.getOri_z()  , sSample.getOri_w());
            ori.multiplyByQuat(game_rv);
            Quaternion ori_conj = new Quaternion(-ori.getX(),-ori.getY(),-ori.getZ(),ori.getW());
            Quaternion q_gyro = new Quaternion(sSample.getGyro_x() , sSample.getGyro_y()
                    , sSample.getGyro_z() , 0);
            Quaternion oriented_gyro = new Quaternion(ori.getX(),ori.getY(),ori.getZ(),ori.getW());
            oriented_gyro.multiplyByQuat(q_gyro);
            oriented_gyro.multiplyByQuat(ori_conj);
            Quaternion q_acc = new Quaternion(sSample.getAcc_x() , sSample.getAcc_y()
                    , sSample.getAcc_z() , 0);
            Quaternion oriented_acc =  new Quaternion(ori.getX(),ori.getY(),ori.getZ(),ori.getW());

            oriented_acc.multiplyByQuat(q_acc);
            oriented_acc.multiplyByQuat(ori_conj);



            if (j_1 < 200){
                input_1[0][0][j_1] = oriented_gyro.getX();
                input_1[0][1][j_1] = oriented_gyro.getY();
                input_1[0][2][j_1] = oriented_gyro.getZ();

                input_1[0][3][j_1] = oriented_acc.getX();
                input_1[0][4][j_1] = oriented_acc.getY();
                input_1[0][5][j_1] = oriented_acc.getZ();
            }else {
                for (int k = 0 ; k < 199 ; k++){
                    input_1[0][0][k] = input_1[0][0][k+1];
                    input_1[0][1][k] = input_1[0][1][k+1];
                    input_1[0][2][k] = input_1[0][2][k+1];

                    input_1[0][3][k] = input_1[0][3][k+1];
                    input_1[0][4][k] = input_1[0][4][k+1];
                    input_1[0][5][k] = input_1[0][5][k+1];
                }
                input_1[0][0][199] = oriented_gyro.getX();
                input_1[0][1][199] = oriented_gyro.getY();
                input_1[0][2][199] = oriented_gyro.getZ();

                input_1[0][3][199] = oriented_acc.getX();
                input_1[0][4][199] = oriented_acc.getY();
                input_1[0][5][199] = oriented_acc.getZ();
            }



            if (j_1 > 198)
            {
                long start = System.nanoTime();
                tfLite_1.run(input_1, output_1);
                long end = System.nanoTime();
                execution_mean_1 = (execution_mean_1 + (end-start))/2;
                // execution_mean = end-start;
                float non_integrated_x = output_1[0][0]*dts;
                float non_integrated_y = output_1[0][1]*dts;
                if(j_1==199){
                    previous_pos_x = non_integrated_x;
                    previous_pos_y = non_integrated_y ;
                }else{
                    previous_pos_x = non_integrated_x + previous_pos_x;
                    previous_pos_y = non_integrated_y + previous_pos_y;
                }


                // es_values.add(new Entry(previous_pos_x + init_pos_x,
                //         previous_pos_y + init_pos_y));





                es_position_1.addEntry(new Entry(previous_pos_x + init_pos_x,previous_pos_y + init_pos_y));




                //mChart.setData(data_);






                //float dd = previous_pos_x + init_pos_x;
                /*if (j%500 == 0){
                    Log.d(TAG, "TF_out: " + tf_output[0][0] + " Iteration: " + j);
                }*/
                //
                // data.notifyDataChanged();
                //mChart.setData(data);

                /*// Show the data
                txt_show.post(new Runnable() {
                    @Override
                    public void run() {
                        txt_show.setText(String.valueOf(tf_output[0][0]));
                    }
                });*/
            }
        }

        //trackingThread.stop();
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowTimeDialog(ComparisonActivity.this, execution_mean_1);
            }
        });*/




    }


    private void trackPntData_2() {

        Quaternion init_rotor = new Quaternion(tango_rt_x, tango_rt_y, tango_rt_z , tango_rt_w);
        Quaternion game_rv = new Quaternion(game_rv_x, game_rv_y, game_rv_z , game_rv_w);
        game_rv.conjugate();
        init_rotor.multiplyByQuat(game_rv);
        input_2 = new float[1][6][200];
        double last_time = samples.get(0).getTime();
        double time_sum = 0;
        float dts = 0 ;
        output_2 = new float[1][2];
        float previous_pos_x = 0;
        float previous_pos_y = 0;
        execution_mean_2  = 0;

        for (j_2 = 0; j_2 < samples.size() ; j_2++){

            SensorSample sSample = samples.get(j_2);

            if (j_2>0){
                double temp = sSample.getTime() - last_time;
                time_sum = time_sum + temp;
                double dts_ = time_sum/j_2;
                dts = Float.valueOf((float) dts_);
                last_time = sSample.getTime();

            }

            Quaternion ori = new Quaternion(init_rotor.getX(),init_rotor.getY(),init_rotor.getZ(),init_rotor.getW());
            game_rv = new Quaternion(sSample.getOri_x(), sSample.getOri_y(),
                    sSample.getOri_z()  , sSample.getOri_w());
            ori.multiplyByQuat(game_rv);
            Quaternion ori_conj = new Quaternion(-ori.getX(),-ori.getY(),-ori.getZ(),ori.getW());
            Quaternion q_gyro = new Quaternion(sSample.getGyro_x() , sSample.getGyro_y()
                    , sSample.getGyro_z() , 0);
            Quaternion oriented_gyro = new Quaternion(ori.getX(),ori.getY(),ori.getZ(),ori.getW());
            oriented_gyro.multiplyByQuat(q_gyro);
            oriented_gyro.multiplyByQuat(ori_conj);
            Quaternion q_acc = new Quaternion(sSample.getAcc_x() , sSample.getAcc_y()
                    , sSample.getAcc_z() , 0);
            Quaternion oriented_acc =  new Quaternion(ori.getX(),ori.getY(),ori.getZ(),ori.getW());

            oriented_acc.multiplyByQuat(q_acc);
            oriented_acc.multiplyByQuat(ori_conj);



            if (j_2 < 200){
                input_2[0][0][j_2] = oriented_gyro.getX();
                input_2[0][1][j_2] = oriented_gyro.getY();
                input_2[0][2][j_2] = oriented_gyro.getZ();

                input_2[0][3][j_2] = oriented_acc.getX();
                input_2[0][4][j_2] = oriented_acc.getY();
                input_2[0][5][j_2] = oriented_acc.getZ();
            }else {
                for (int k = 0 ; k < 199 ; k++){
                    input_2[0][0][k] = input_2[0][0][k+1];
                    input_2[0][1][k] = input_2[0][1][k+1];
                    input_2[0][2][k] = input_2[0][2][k+1];

                    input_2[0][3][k] = input_2[0][3][k+1];
                    input_2[0][4][k] = input_2[0][4][k+1];
                    input_2[0][5][k] = input_2[0][5][k+1];
                }
                input_2[0][0][199] = oriented_gyro.getX();
                input_2[0][1][199] = oriented_gyro.getY();
                input_2[0][2][199] = oriented_gyro.getZ();

                input_2[0][3][199] = oriented_acc.getX();
                input_2[0][4][199] = oriented_acc.getY();
                input_2[0][5][199] = oriented_acc.getZ();
            }



            if (j_2 > 198)
            {
                long start = System.nanoTime();
                tfLite_2.run(input_2, output_2);
                long end = System.nanoTime();
                execution_mean_2 = (execution_mean_2 + (end-start))/2;
                // execution_mean = end-start;
                float non_integrated_x = output_2[0][0]*dts;
                float non_integrated_y = output_2[0][1]*dts;
                if(j_2==199){
                    previous_pos_x = non_integrated_x;
                    previous_pos_y = non_integrated_y ;
                }else{
                    previous_pos_x = non_integrated_x + previous_pos_x;
                    previous_pos_y = non_integrated_y + previous_pos_y;
                }


                // es_values.add(new Entry(previous_pos_x + init_pos_x,
                //         previous_pos_y + init_pos_y));




                es_position_2.addEntry(new Entry(previous_pos_x + init_pos_x,
                        previous_pos_y + init_pos_y));




                //mChart.setData(data_);

                if (j_2%10 == 0){
                    data.notifyDataChanged();
                    mChart.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized(mChart){
                                Log.d(TAG, "TF_out model_2\n" );
                                mChart.notify();
                                mChart.invalidate();
                                // mChart.notifyDataSetChanged();
                                //dataSets.notifyAll();
                            }
                        }
                    });
                    //mChart.setData(data_);
                }




                //float dd = previous_pos_x + init_pos_x;
                /*if (j%500 == 0){
                    Log.d(TAG, "TF_out: " + tf_output[0][0] + " Iteration: " + j);
                }*/
                //
                // data.notifyDataChanged();
                //mChart.setData(data);

                /*// Show the data
                txt_show.post(new Runnable() {
                    @Override
                    public void run() {
                        txt_show.setText(String.valueOf(tf_output[0][0]));
                    }
                });*/
            }
        }

        //trackingThread.stop();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowTimeDialog(ComparisonActivity.this, execution_mean_1, execution_mean_2);
            }
        });

    }
    public void trackThread_1(){

        TrackingRunnable_1 runnable = new TrackingRunnable_1();
        trackingThread_1 = new Thread(runnable);
        trackingThread_1.start();


    }

    public void trackThread_2(){

        TrackingRunnable_2 runnable = new TrackingRunnable_2();
        trackingThread_2 = new Thread(runnable);
        trackingThread_2.start();
    }


    public void readThread(){
        ExampleRunnable runnable = new ExampleRunnable(10);
        new Thread(runnable).start();

    }
    private ArrayList<SensorSample> samples = new ArrayList<>();
    private void readPntData() throws IOException {
        InputStream is =  getResources().openRawResource(R.raw.data);

        SensorSample sensorSample;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName(("UTF-8")))
        );






        es_position_1 = new ScatterDataSet(null , "IMUNet");
        es_position_1.setScatterShapeSize(4f);
        es_position_1.setColor(Color.RED);

        es_position_2 = new ScatterDataSet(null , "Model");
        es_position_2.setScatterShapeSize(6f);
        es_position_2.setColor(Color.GREEN);

        gt_position = new ScatterDataSet(null , "True Position");
        gt_position.setScatterShapeSize(4f);
        String line = "";
        reader.readLine();
        i = 0;
        while ( (line =  reader.readLine()) != null){



            String[] token = line.split(",");
            sensorSample = new SensorSample();

            String time = (token[1]);
            double  tt = Double.parseDouble(time);
            double ttt = tt/1000000000;
            sensorSample.setTime(ttt);
            sensorSample.setGyro_x(Float.parseFloat(token[2]));
            sensorSample.setGyro_y(Float.parseFloat(token[3]));
            sensorSample.setGyro_z(Float.parseFloat(token[4]));

            sensorSample.setAcc_x(Float.parseFloat(token[5]));
            sensorSample.setAcc_y(Float.parseFloat(token[6]));
            sensorSample.setAcc_z(Float.parseFloat(token[7]));

            sensorSample.setPos_x(Float.parseFloat(token[17]));
            sensorSample.setPos_y(Float.parseFloat(token[18]));

            sensorSample.setOri_w(Float.parseFloat(token[24]));
            sensorSample.setOri_x(Float.parseFloat(token[25]));
            sensorSample.setOri_y(Float.parseFloat(token[26]));
            sensorSample.setOri_z(Float.parseFloat(token[27]));





            if (i == 0){
                tango_rt_w = Float.parseFloat(token[20]);
                tango_rt_x = Float.parseFloat(token[21]);
                tango_rt_y = Float.parseFloat(token[22]);
                tango_rt_z = Float.parseFloat(token[23]);
                game_rv_w = Float.parseFloat(token[24]);
                game_rv_x = Float.parseFloat(token[25]);
                game_rv_y = Float.parseFloat(token[26]);
                game_rv_z = Float.parseFloat(token[27]);
                init_pos_x = Float.parseFloat(token[17]);
                init_pos_y = Float.parseFloat(token[18]);

            }
            samples.add(sensorSample);
            i = i + 1 ;
            /*txt_show.post(new Runnable() {
                @Override
                public void run() {
                    txt_show.setText(String.valueOf(i));
                }
            });*/
            //yValues.add(new Entry(Float.valueOf(token[17]),Float.valueOf(token[18])));
            gt_position.addEntry(new Entry(Float.valueOf(token[17]),Float.valueOf(token[18])));
        }
        es_position_1.addEntry(new Entry(Float.valueOf(init_pos_x),Float.valueOf(init_pos_y)));
        es_position_2.addEntry(new Entry(Float.valueOf(init_pos_x),Float.valueOf(init_pos_y)));



        //dataSets = new ArrayList<>();

        //dataSets.add(gt_position);
        data = new ScatterData();
        data.addDataSet(gt_position);
        data.addDataSet(es_position_1);
        data.addDataSet(es_position_2);
        mChart.setData(data);
        mChart.invalidate();


        //data.notifyDataChanged();

    }

    private void ShowTimeDialog (Context context, long execution_time_1 , long execution_time_2){

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Execution time in MilliSec:")
                .setMessage("AMUNet.tflite" + " :" + String.valueOf(execution_time_1)+"\n"+model_name + " :" + String.valueOf(execution_time_2))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action
                .show();

    }



}