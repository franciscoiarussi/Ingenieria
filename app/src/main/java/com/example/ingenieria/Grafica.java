package com.example.ingenieria;

import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Grafica extends AppCompatActivity {
        private LineChart lineChart;
        private LineDataSet lineDataSet;
        public static ArrayList<JSONObject> arrayPulso;

        protected void onCreate(Bundle savedInstanceState) {
            //grafica
            super.onCreate(savedInstanceState);
            setContentView(R.layout.grafica);
            lineChart = findViewById(R.id.lineChart);
            // Creamos un set de datos
            ArrayList<Entry> lineEntries = new ArrayList<Entry>();
            for (int i = 0; i < 11; i++) {
                float y = 0;
                try {
                    y = (int) arrayPulso.get(i).getInt("Pulso");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lineEntries.add(new Entry((float) i, (float) y));
            }
            // Unimos los datos al data set
            lineDataSet = new LineDataSet(lineEntries, "grafica");
            // Asociamos al gráfico
            LineData lineData = new LineData();
            lineData.addDataSet(lineDataSet);
            lineChart.setData(lineData);
        } }