package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Spinner dropdown = findViewById(R.id.spinner);
        String[] symptomList = new String[]{"Nausea", "Headache", "Diarrhea", "Sore Throat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling Tired"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, symptomList);
        dropdown.setAdapter(adapter);

        Button button = (Button)findViewById(R.id.button5);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(2);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                String symptom = dropdown.getSelectedItem().toString();

                switch(symptom){
                    case "Nausea":
                        Person.setNausea(rating);
                        break;
                    case "Headache":
                        Person.setHeadache(rating);
                        break;
                    case "Diarrhea":
                        Person.setDiarrhea(rating);
                        break;
                    case "Sore Throat":
                        Person.setSoreThroat(rating);
                        break;
                    case "Fever":
                        Person.setFever(rating);
                        break;
                    case "Muscle Ache":
                        Person.setMuscleAche(rating);
                        break;
                    case "Loss of Smell or Taste":
                        Person.setLossOfSmellOrTaste(rating);
                        break;
                    case "Cough":
                        Person.setCough(rating);
                        break;
                    case "Shortness of Breath":
                        Person.setShortnessOfBreath(rating);
                        break;
                    case "Feeling Tired":
                        Person.setFeelingTired(rating);
                        break;
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}