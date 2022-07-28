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

public class MainActivity2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private Interpreter tfLite;
    private AssetManager assetManager ;
    private float[][] tf_output = null;
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

    private Thread trackingThread;

    private float[][][] input_data = null;

    private static float number = (float) 1000000000.00;
    int i = 0;
    int j = 0;

    private ScatterDataSet gt_position;
    private ScatterDataSet es_position;
    ScatterData data = null;
    int file ;
    private String model_name;
    Button btn_stop;
    private Interpreter.Options options ;
    private String[] titles = new String[]{"Behnam_3_S10" , "Behnam_5_S10", "Behnam_11_s10" , "Behnam_12_S10","Behnam_13_S10","Behnam_26_S10",
             "Behnam_28_S10", "Behnam_29_S10"};

    private long execution_mean = 0;
    private String[] models = new String[]{"ResNet.tflite" , "MobileNet.tflite" , "MobileNetV2.tflite" , "MnasNet.tflite" , "EfficientNet.tflite" , "IMUNet.tflite"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mChart = findViewById(R.id.chart_view);
        btn_track = findViewById(R.id.btn_tracking);
        btn_stop = findViewById(R.id.btn_stop);
        btn_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackThread();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_track.setEnabled(false);


        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);

        Bundle bundle = getIntent().getExtras();


        XAxis x_axis = mChart.getXAxis();
        YAxis y_axis_right = mChart.getAxisRight();
        YAxis y_axis_left = mChart.getAxisLeft();

        float xMin =Float.valueOf(bundle.getString("minX"));
        float xMax =Float.valueOf(bundle.getString("maxX"));
        float yMin =Float.valueOf(bundle.getString("minY"));
        float yMax =Float.valueOf(bundle.getString("maxY"));

        Spinner spinnerModels = findViewById(R.id.model_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.models, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerModels.setAdapter(adapter);
        spinnerModels.setOnItemSelectedListener(this);



        x_axis.setAxisMaximum(xMax);
        x_axis.setAxisMinimum(xMin);

        y_axis_right.setAxisMaximum(yMax);
        y_axis_right.setAxisMinimum(yMin);

        y_axis_left.setAxisMaximum(yMax);
        y_axis_left.setAxisMinimum(yMin);

        file = Integer.valueOf(bundle.getString("file"));

        String title = titles[file-1];
        this.setTitle(title);


        FlexDelegate delegate = new FlexDelegate();
        options = new Interpreter.Options().addDelegate(delegate);

        assetManager = getAssets();
        try{
            MappedByteBuffer node_model = loadModelFile("MobileNetV2.tflite") ;
            //MappedByteBuffer node_model = loadModelFile("first_tango_model.tflite")
            tfLite = new Interpreter( node_model , options);
            model_name = "MobileNetV2.tflite";
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
            tfLite = new Interpreter( node_model , options);
            Toast.makeText(this , models[i] + " loaded" , Toast.LENGTH_SHORT).show();
            model_name = models[i];
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

    class TrackingRunnable implements Runnable{

        @Override
        public void run() {
            trackPntData();
        }
    }



    private void trackPntData() {

        Quaternion init_rotor = new Quaternion(tango_rt_x, tango_rt_y, tango_rt_z , tango_rt_w);
        Quaternion game_rv = new Quaternion(game_rv_x, game_rv_y, game_rv_z , game_rv_w);
        game_rv.conjugate();
        init_rotor.multiplyByQuat(game_rv);
        input_data = new float[1][6][200];
        double last_time = samples.get(0).getTime();
        double time_sum = 0;
        float dts = 0 ;
        tf_output = new float[1][2];
        float previous_pos_x = 0;
        float previous_pos_y = 0;
        execution_mean  = 0;

        for (j = 0; j < samples.size() ; j++){

            SensorSample sSample = samples.get(j);

            if (j>0){
                double temp = sSample.getTime() - last_time;
                time_sum = time_sum + temp;
                double dts_ = time_sum/j;
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



            if (j < 200){
                input_data[0][0][j] = oriented_gyro.getX();
                input_data[0][1][j] = oriented_gyro.getY();
                input_data[0][2][j] = oriented_gyro.getZ();

                input_data[0][3][j] = oriented_acc.getX();
                input_data[0][4][j] = oriented_acc.getY();
                input_data[0][5][j] = oriented_acc.getZ();
            }else {
                for (int k = 0 ; k < 199 ; k++){
                    input_data[0][0][k] = input_data[0][0][k+1];
                    input_data[0][1][k] = input_data[0][1][k+1];
                    input_data[0][2][k] = input_data[0][2][k+1];

                    input_data[0][3][k] = input_data[0][3][k+1];
                    input_data[0][4][k] = input_data[0][4][k+1];
                    input_data[0][5][k] = input_data[0][5][k+1];
                }
                input_data[0][0][199] = oriented_gyro.getX();
                input_data[0][1][199] = oriented_gyro.getY();
                input_data[0][2][199] = oriented_gyro.getZ();

                input_data[0][3][199] = oriented_acc.getX();
                input_data[0][4][199] = oriented_acc.getY();
                input_data[0][5][199] = oriented_acc.getZ();
            }



            if (j > 198)
            {
                long start = System.nanoTime();
                tfLite.run(input_data, tf_output);
                long end = System.nanoTime();
                execution_mean = (execution_mean + (end-start))/2;
               // execution_mean = end-start;
                float non_integrated_x = tf_output[0][0]*dts;
                float non_integrated_y = tf_output[0][1]*dts;
                if(j==199){
                    previous_pos_x = non_integrated_x;
                    previous_pos_y = non_integrated_y ;
                }else{
                    previous_pos_x = non_integrated_x + previous_pos_x;
                    previous_pos_y = non_integrated_y + previous_pos_y;
                }


                // es_values.add(new Entry(previous_pos_x + init_pos_x,
                //         previous_pos_y + init_pos_y));




                es_position.addEntry(new Entry(previous_pos_x + init_pos_x,
                        previous_pos_y + init_pos_y));




                //mChart.setData(data_);

                if (j%10 == 0){
                    data.notifyDataChanged();
                    mChart.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized(mChart){
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
                ShowTimeDialog(MainActivity2.this, execution_mean);
            }
        });




        }
    public void trackThread(){

        TrackingRunnable runnable = new TrackingRunnable();
        trackingThread = new Thread(runnable);
        trackingThread.start();


    }



    public void readThread(){
        ExampleRunnable runnable = new ExampleRunnable(10);
        new Thread(runnable).start();

    }
    private ArrayList<SensorSample> samples = new ArrayList<>();
    private void readPntData() throws IOException {
        InputStream is ;
        switch (file){
            case 1:
                is = getResources().openRawResource(R.raw.behnam_3__s10);
                break;
            case 2:
                is = getResources().openRawResource(R.raw.behnam_5__s10);
                break;
            case 3:
                is = getResources().openRawResource(R.raw.behnam_11__s10);
                break;
            case 4:
                is = getResources().openRawResource(R.raw.behnam_12__s10);
                break;
            case 5:
                is = getResources().openRawResource(R.raw.behnam_13__s10);
                break;
            case 6:
                is = getResources().openRawResource(R.raw.behnam_26__s10);
                break;
            case 7:
                is = getResources().openRawResource(R.raw.behnam_28__s10);
                break;
            case 8:
                is = getResources().openRawResource(R.raw.behnam_29__s10);
                break;
            default:
                is = getResources().openRawResource(R.raw.behnam_29__s10);


        }

        SensorSample sensorSample;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName(("UTF-8")))
        );



        gt_position = new ScatterDataSet(null , "True Position");
        gt_position.setScatterShapeSize(3f);


        es_position = new ScatterDataSet(null , "Estimated Position");
        es_position.setScatterShapeSize(3f);
        es_position.setColor(Color.RED);

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
        es_position.addEntry(new Entry(Float.valueOf(init_pos_x),Float.valueOf(init_pos_y)));



        //dataSets = new ArrayList<>();

        //dataSets.add(gt_position);
        data = new ScatterData();
        data.addDataSet(gt_position);
        data.addDataSet(es_position);
        mChart.setData(data);
        mChart.invalidate();
        btn_track.post(new Runnable() {
            @Override
            public void run() {
                btn_track.setEnabled(true);
            }
        });

        //data.notifyDataChanged();

    }

    private void ShowTimeDialog (Context context, long execution_time){

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Execution time in MilliSec")
                .setMessage(model_name + " :" + String.valueOf(execution_time))

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