package com.example.ingenieria;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;

import java.util.ArrayList;

public class GraficaPasos extends AppCompatActivity {
        private BarChart BarChart;

        protected void onCreate(Bundle savedInstanceState) {

                //grafica
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graficapasos);
        BarChart = findViewById(R.id.BarChart);

        // Creamos un set de datos
        ArrayList<BarEntry> lineEntries = new ArrayList<>();
        /*
        for (int i = 0; i < arrayPasos.size(); i++) {
            float y = 0;
            try {
                y = (int) arrayPasos.get(i).getInt("Pasos");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            lineEntries.add(new BarEntry((float) i, (float) y));
        }*/

        lineEntries.add(new BarEntry(1f, 0));
        lineEntries.add(new BarEntry(2f, 500));
        lineEntries.add(new BarEntry(3f, 0));
        lineEntries.add(new BarEntry(4f,670));
        lineEntries.add(new BarEntry(5f,0));
        lineEntries.add(new BarEntry(6f,490));
        lineEntries.add(new BarEntry(7f,0));

        // Unimos los datos al data set
        BarDataSet datos= new BarDataSet(lineEntries, "Grafico de pasos");

        // Asociamos al gráfico
        BarData data = new BarData(datos);

            //separacion entre barras
            data.setBarWidth(0.9f);

            //PONE BARRAS CENTRADAS
            BarChart.setFitBars(true);

        data.addDataSet(datos);
        BarChart.setData(data);

        BarChart.invalidate();
    }
}
