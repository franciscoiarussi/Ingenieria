package com.example.ingenieria;

import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class Grafica extends AppCompatActivity {
        private LineChart lineChart;
        private LineDataSet lineDataSet;

        protected void onCreate(Bundle savedInstanceState) {
            //grafica
            super.onCreate(savedInstanceState);
            setContentView(R.layout.grafica);
            lineChart = findViewById(R.id.lineChart);
            // Creamos un set de datos
            ArrayList<Entry> lineEntries = new ArrayList<Entry>();
            for (int i = 0; i < 11; i++) {
                float y = (int) (Math.random() * 8) + 1;
                lineEntries.add(new Entry((float) i, (float) y));
            }
            // Unimos los datos al data set
            lineDataSet = new LineDataSet(lineEntries, "grafica");
            // Asociamos al gráfico
            LineData lineData = new LineData();
            lineData.addDataSet(lineDataSet);
            lineChart.setData(lineData);
        } }