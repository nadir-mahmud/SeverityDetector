package com.example.severitydetector;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.List;

public class Utils {
    MainActivity mainActivity;

    public Utils(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public int maxHate(List<Float> outputHate){
        float max = Integer.MIN_VALUE; // Initialize max with the first element
        int hateType = 0;
        int index = -1;

        for (float value : outputHate) {
            index++;
            if (value > max) {
                max = value;
                hateType = index;
            }
        }
        return hateType;
    }

    public void setHateData(List<Float> outputHate, PieChart pieChart, PieChart pieChart2)
    {
        pieChart.clearChart();
        pieChart2.clearChart();


        float personalFloat = outputHate.get(0) * 100;
        int personal = (int) personalFloat/1;

        System.out.println("success");

        float politicalFloat = outputHate.get(1) * 100;
        int political = (int) politicalFloat/1;

        float religiousFloat = outputHate.get(2) * 100;
        int religious = (int) religiousFloat/1;

        float geopoliticalFloat = outputHate.get(3) * 100;
        int geopolitical = (int) geopoliticalFloat/1;

        // Set the percentage of language used

        mainActivity.tvPersonal.setText("Personal " + personal + "%");
        mainActivity.tvPolitical.setText("Political " + political + "%");
        mainActivity.tvReligious.setText("Religious " + religious + "%");
        mainActivity.tvGeopolitical.setText("Geopolitical " + geopolitical + "%");

        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "Personal",
                        personal,
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Political",
                        political,
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Religious",
                        religious,
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        "Geopolitical",
                        geopolitical,
                        Color.parseColor("#29B6F6")));

        // To animate the pie chart
        pieChart.startAnimation();
    }

    public void setSeverityData(List<Float> outputSeverity, PieChart pieChart2)
    {
        pieChart2.setVisibility(View.VISIBLE);

        float lessFloat = outputSeverity.get(0) * 100;
        int less = (int) lessFloat/1;

        System.out.println("success");

        float alarmingFloat = outputSeverity.get(1) * 100;
        int alarming = (int) alarmingFloat/1;

        float veryFloat = outputSeverity.get(2) * 100;
        int very = (int) veryFloat/1;


        // Set the percentage of language used

        mainActivity.tvLess.setText("Less Alarming " + less + "%");
        mainActivity.tvAlarming.setText("Alarming " + alarming + "%");
        mainActivity.tvVery.setText("Very Alarming " + very + "%");


        // Set the data and color to the pie chart
        pieChart2.addPieSlice(
                new PieModel(
                        "Less Alarming",
                        less,
                        Color.parseColor("#FFA726")));
        pieChart2.addPieSlice(
                new PieModel(
                        "Alarming",
                        alarming,
                        Color.parseColor("#66BB6A")));
        pieChart2.addPieSlice(
                new PieModel(
                        "Very Alarming",
                        very,
                        Color.parseColor("#EF5350")));


        // To animate the pie chart
        pieChart2.startAnimation();
    }
}
