package smartenergy.energyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CalenderActivity extends AppCompatActivity {

    Button btnMain, btnGlobe, btnCalender, btnMonthly, btnYearly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        btnMain = (Button)findViewById(R.id.btnMiddle);
        btnCalender = (Button)findViewById(R.id.btnLeft);
        btnGlobe = (Button)findViewById(R.id.btnRight);
        btnMonthly = (Button)findViewById(R.id.btnMonthly);
        btnYearly = (Button)findViewById(R.id.btnYearly);

        initButtonsBottom();
        initButtonsCalendar();

    }

    private void initButtonsBottom(){
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);            }
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
                //do nothing
            }
        });

    }

    private void initButtonsCalendar(){
        btnMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transition to table with info about monthly use
                Intent intent = new Intent(view.getContext(), MonthlyFootprintActivity.class);
                startActivity(intent);
            }
        });

        btnYearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transition to table with info about yearly use
                Intent intent = new Intent(view.getContext(), YearlyFootprintActivity.class);
                startActivity(intent);
            }
        });
    }


}
