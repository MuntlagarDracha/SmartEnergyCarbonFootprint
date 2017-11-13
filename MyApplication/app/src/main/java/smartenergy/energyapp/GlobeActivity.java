package smartenergy.energyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GlobeActivity extends AppCompatActivity {

    Button btnMain, btnGlobe, btnCalender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_globe);


        btnMain = (Button) findViewById(R.id.btnMiddle);
        btnCalender = (Button) findViewById(R.id.btnLeft);
        btnGlobe = (Button) findViewById(R.id.btnRight);

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
                //do nothing

            }
        });

        btnCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), CalenderActivity.class);
                startActivity(intent);
            }
        });

    }
}
