package com.example.severitydetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String comment;

    private TextInputEditText inp;

    protected  TextView tvPersonal, tvPolitical, tvReligious, tvGeopolitical;
    protected TextView tvLess, tvAlarming, tvVery;

    List<Integer> inputHate = new ArrayList<>();
    List<Float> outputHate = new ArrayList<>();
    List<Integer> inputSeverity = new ArrayList<>();
    List<Float> outputSeverity = new ArrayList<>();


    HashMap<String, Integer> tokenizerVocabulary = new HashMap<>();

    MappedByteBuffer hateModel, severityModel;

    PieChart pieChart, pieChart2;

    Tokenization severityTokenization;

    Utils utils1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inp = findViewById(R.id.editText);
        Button button = findViewById(R.id.butt_sub);

        tvPersonal = findViewById(R.id.tvPersonal);
        tvPolitical = findViewById(R.id.tvPolitical);
        tvReligious = findViewById(R.id.tvReligious);
        tvGeopolitical = findViewById(R.id.tvGeopolitical);
        pieChart = findViewById(R.id.piechart);

        tvLess = findViewById(R.id.tvLess);
        tvAlarming = findViewById(R.id.tvAlarming);
        tvVery = findViewById(R.id.tvVery);
        pieChart2 = findViewById(R.id.piechart2);

        Tokenization tokenization = new Tokenization();
        severityTokenization = new Tokenization();

        utils1 = new Utils(MainActivity.this);


        // Load the TensorFlow Lite model from res/raw
        try {
            hateModel = FileUtil.loadMappedFile(this, "cnn_model.tflite");
            severityModel = FileUtil.loadMappedFile(this, "severity_cnn.tflite");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TensorFlow Lite model: " + e);
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pieChart2.setVisibility(View.INVISIBLE);

                comment = inp.getText().toString();
                inputHate = tokenization.tokenizeInput(MainActivity.this, comment, "tokenizer_data.json");
                outputHate =  tokenization.performInference(inputHate,hateModel,4);

                if (!outputHate.isEmpty()) {
                    int hateType = utils1.maxHate(outputHate);
                    utils1.setHateData(outputHate,  pieChart, pieChart2);
                    checkHateType(hateType);
                } else {
                    // Handle the case where the list is empty
                    System.out.println("Empty output");
                }

            }
        });

    }

    private void checkHateType(int hateType){
        String hate = "";

        if(hateType == 0){
            hate = "Personal";
        } else if (hateType == 1) {
            hate = "Political";
        } else if (hateType == 2) {
            hate = "Religious";

            inputSeverity = severityTokenization.tokenizeInput(MainActivity.this, comment, "tokenizer_severity.json");
            outputSeverity =  severityTokenization.performInference(inputSeverity,severityModel,3);

            utils1.setSeverityData(outputSeverity, pieChart2);
        }else if (hateType == 3) {
            hate = "Geopolitical";
        }

    }

}

