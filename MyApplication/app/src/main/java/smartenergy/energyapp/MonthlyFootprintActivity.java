package smartenergy.energyapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class MonthlyFootprintActivity extends AppCompatActivity {

    String TAG = "MonthlyFootprint";

    Button btnMain, btnGlobe, btnCalender;
    GraphView graph;
    HashMap<Integer, Double> rawData;
    Analyzer analyzer;
    private DBRawHelper dbRawHelper;
    private DBProcessedHelper dbProcessedHelper;
    private DBAggregatedHelper dbAggregatedHelper;

    TextView tvCo2Mon, tvTreeMon, tvMoneyMon, textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_footprint);
        btnMain = (Button) findViewById(R.id.btnMiddle);
        btnCalender = (Button) findViewById(R.id.btnLeft);
        btnGlobe = (Button) findViewById(R.id.btnRight);
        initButtonsBottom();

        tvCo2Mon = (TextView) findViewById(R.id.tvCo2Mon);
        tvMoneyMon = (TextView) findViewById(R.id.tvMoneyMon);
        tvTreeMon = (TextView) findViewById(R.id.tvTreeMon);
        textView2 = (TextView) findViewById(R.id.textView2);

        dbRawHelper = new DBRawHelper(this);
        dbProcessedHelper = new DBProcessedHelper(this);
        dbAggregatedHelper = new DBAggregatedHelper(this);
        analyzer = new Analyzer(dbRawHelper, dbProcessedHelper, dbAggregatedHelper);

        rawData = analyzer.co2Use(TimePeriod.MONTHLY);

        graph = (GraphView) findViewById(R.id.graph);

        //get the Data in a format graphview sort of likes
        int size = rawData.size();
        boolean graphNeedsHelp = false;
        boolean noData = false;
        if (size == 0){
            //we got no data we simply add one small data point and that's it
            size = 1;
            noData = true;
        }
        if (size<=1){
            //we need to help the graph along a bit
            size += 2;
            graphNeedsHelp = true;
        }
        DataPoint data[] = new DataPoint[size];
        DataPoint lineDataAverage[] = new DataPoint[2];
        DataPoint lineDataDesired[] = new DataPoint[2];
        int i = 0;
        Set<Integer> keys = rawData.keySet();
        Log.d(TAG, "onCreate: " + keys);
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        double max = 0; //used for the bounds of the graph this finds the largest value in the graph

        //read the data from the db into Datapoints
        for (Integer key : keys) {
            date.setTime((long) key * 1000);
            cal.setTimeInMillis((long) key * 1000);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "onCreate: dates " + day + " timestamp: " + key);
            double value = rawData.get(key) / 1000;
            DataPoint dp = new DataPoint(day, value); //let's convert it to kg
            Log.d(TAG, "onCreate: data " + value);
            if (max < value) {
                max = value;
            }
            data[i] = dp;
            i++;
            if (graphNeedsHelp){
                day -= 1;
                data[i] = new DataPoint(day, 0.1);
                i++;
                day +=2;
                data[i] = new DataPoint(day, 0.1);
            }
        }

        if (noData){
            data[0]= new DataPoint(0, 0.1);
        }

        final double avgCO2Day = 5.64;
        final double desiredCO2 = 0.8 * avgCO2Day;

        //Data for the lines
        DataPoint ldp = new DataPoint(0, avgCO2Day);
        DataPoint ldpd = new DataPoint(0, desiredCO2);
        lineDataAverage[0] = ldp;
        lineDataDesired[0] = ldpd;
        ldp = new DataPoint(32, avgCO2Day);
        ldpd = new DataPoint(32, desiredCO2);
        lineDataAverage[1] = ldp;
        lineDataDesired[1] = ldpd;



        //make everything into Graphseries
        final BarGraphSeries<DataPoint> series = new BarGraphSeries<>(data); //bars
        LineGraphSeries<DataPoint> lineSeriesAverage = new LineGraphSeries<>(lineDataAverage); //red line
        LineGraphSeries<DataPoint> lineSeriesDesired = new LineGraphSeries<>(lineDataDesired); //orange line

        if (max < avgCO2Day){
            max = avgCO2Day;
        }

        //make the graph look nice and start at 0
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(32);
        graph.getViewport().setScalable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(max + 5);
        graph.getViewport().setScalableY(true);

        //x Axis title
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("day of the month");

        //Graph title
        String month = new DateFormatSymbols().getMonths()[date.getMonth()];
        graph.setTitle("Daily CO\u2082 use in " + month + " in [kg]");
        textView2.setText(month + " consumption");

        //now add everything to the graph
        graph.addSeries(lineSeriesAverage);
        graph.addSeries(lineSeriesDesired);
        if (series == null){
            Log.d(TAG, "onCreate: series is null");
        }
        try {
            graph.addSeries(series);
        }catch (NullPointerException e){
            //do nothing simply do not show the graph
        }

        series.setSpacing(10);

        // styling for the bars
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                if (data.getY() > avgCO2Day) {
                    return Color.RED;
                } else if (data.getY() > desiredCO2) {
                    return Color.rgb(255, 153, 51);
                } else {
                    return series.getColor(); //the graph default blue
                }
            }
        });

        //setting the color of the lines in the graph
        lineSeriesAverage.setColor(Color.RED);
        lineSeriesAverage.setTitle("Higher than Swiss average");
        lineSeriesDesired.setTitle("Higher than Swiss goal");
        series.setTitle("Keep on the good work :-)");
        lineSeriesDesired.setColor(Color.rgb(255, 153, 51));
        //now we need to label the lines
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


        tvTreeMon.setText("" + analyzer.kgsOfWoodUsed(TimePeriod.MONTHLY) + " kg of wood");
        tvMoneyMon.setText("" + analyzer.chfSpent(TimePeriod.MONTHLY) + " CHF on gasoline");
        tvCo2Mon.setText("" + analyzer.kgCo2Emitted(TimePeriod.MONTHLY) + " kg CO\u2082");
    }

    private void initButtonsBottom() {
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        btnGlobe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), GlobeActivity.class);
                startActivity(intent);
            }
        });

        btnCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go back to the main calender activity
                Intent intent = new Intent(view.getContext(), CalenderActivity.class);
                startActivity(intent);
            }
        });

    }
}
