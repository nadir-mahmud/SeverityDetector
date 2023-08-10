package com.example.severitydetector;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Tokenization {



    private MappedByteBuffer loadModelFile(Activity activity, String modelFileName) throws IOException {
        AssetFileDescriptor assetFileDescriptor = activity.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
    }




    // Load tokenizer data function
    public HashMap<String, Integer> loadTokenizerData(Context context, String tokenizerFileName) {
        HashMap<String, Integer> tokenizer = new HashMap<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(tokenizerFileName);
            String tokenizerJson = readInputStream(inputStream);

            System.out.println("Loaded JSON content:");
            System.out.println(tokenizerJson);
            //Toast.makeText(this,0,Toast.LENGTH_SHORT).show();

            JSONObject tokenizerData = new JSONObject(tokenizerJson);

            tokenizer = convertJsonToVocabulary(tokenizerData);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // Handle error
        }
        return tokenizer;

    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public static HashMap<String, Integer> convertJsonToVocabulary(JSONObject tokenizerData) throws JSONException {
        // Convert JSON object to a vocabulary mapping
        HashMap<String, Integer> vocabulary = new HashMap<>();

        JSONObject wordIndexObject = tokenizerData.getJSONObject("word_index");

        Iterator<String> keys = wordIndexObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            int value = wordIndexObject.getInt(key);
            vocabulary.put(key, value);
        }

        return vocabulary;
    }




    public List<Integer> tokenizeInput(Context context, String input, String fileName) {
        String[] words = input.split("\\s+");
        List<Integer> tokenizedInput = new ArrayList<>();
        HashMap<String, Integer> tokeizerVocabulary = new HashMap<>();
        int index = 0;

        tokeizerVocabulary = loadTokenizerData(context, fileName);

        for (String word : words) {
            Integer vocabIndex = tokeizerVocabulary.get(word);

            if (vocabIndex != null) {
                tokenizedInput.add(vocabIndex);
            } else {
                // Handle out-of-vocabulary words if needed
                tokenizedInput.add(index); // Use a specific index for out-of-vocabulary words
            }

            index++;
        }

        return tokenizedInput;
    }


    // Perform inference using tokenized input
    public List<Float> performInference(List<Integer> inputIntArray, MappedByteBuffer tfliteModel, int outputSize) {
        // Convert inputIntArray to ByteBuffer
        ByteBuffer inputBuffer;
        inputBuffer = convertListToByteBuffer(inputIntArray);

        // Allocate buffers for output tensor(s)
        // Define the output size accordingly
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(outputSize * Float.BYTES);
        outputBuffer.order(ByteOrder.nativeOrder());

        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if(compatList.isDelegateSupportedOnThisDevice()){
            // if the device has a supported GPU, add the GPU delegate
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
        } else {
            // if the GPU is not supported, run on 4 threads
            options.setNumThreads(4);
        }
        List<Float> outputFloatArray = new ArrayList<>();

        // Perform inference
        try (Interpreter interpreter = new Interpreter(tfliteModel,options)) {
            interpreter.run(inputBuffer, outputBuffer);
            outputBuffer.rewind();
            outputFloatArray = convertByteBufferToList(outputBuffer);
            //Toast.makeText(this,outputFloatArray.length,Toast.LENGTH_SHORT).show();
            return outputFloatArray;
        }

    }

    private ByteBuffer convertListToByteBuffer(List<Integer> inputIntegerList) {
        int size = inputIntegerList.size();
        ByteBuffer buffer = ByteBuffer.allocateDirect(100 * Integer.BYTES);
        buffer.order(ByteOrder.nativeOrder());

        for (int value : inputIntegerList) {
            buffer.putInt(value);
        }

        buffer.flip();
        return buffer;
    }

    private List<Float> convertByteBufferToList(ByteBuffer buffer) {
        buffer.order(ByteOrder.nativeOrder());

        List<Float> integerList = new ArrayList<>();
        while (buffer.hasRemaining()) {
            float value = buffer.getFloat();
            integerList.add(value);
        }

        return integerList;
    }

}
