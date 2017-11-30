package com.example.julius.colabmap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout mActivityContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivityContent =findViewById(R.id.main_activity_content);
    }
    public void OnButtonClick(View v){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new ColaborativeMapFragment();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        mActivityContent.setVisibility(View.GONE);
    }
}
