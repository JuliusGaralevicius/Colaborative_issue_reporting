package com.example.julius.colabmap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMarkerDatabase;
    private ConstraintLayout mActivityContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMarkerDatabase = mFirebaseDatabase.getReference("markers");
        mActivityContent =(ConstraintLayout) findViewById(R.id.main_activity_content);
    }
    public void OnButtonClick(View v){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new ColaborativeMapFragment();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        mActivityContent.setVisibility(View.GONE);
    }
}
