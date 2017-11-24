package com.example.julius.colabmap;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;

/**
 * Created by julius on 22/11/2017.
 */

public class ColaborativeMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener{
    private GoogleMap mGoogleMap;
    private Button mAddIssueButton;
    private Marker mLastUserMarker;
    private EditText mEditText;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mMarkersDatabase;

    View mFragmentView;

    private HashMap<Marker, String> mMapByMarker;
    private HashMap<String, Marker> mMapById;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public ColaborativeMapFragment(){
        super();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.custom_map_layout, container, false);
        mFragmentView = mainView;
        MapFragment mMapFragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mEditText = mainView.findViewById(R.id.tv_input_issue);
        mEditText.setVisibility(View.GONE);

        mAddIssueButton = mainView.findViewById(R.id.btn_add_issue);

        mAddIssueButton.setOnClickListener(this);
        mMapFragment.getMapAsync(this);
        return mainView;
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMarkerDragListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mMarkersDatabase = mDatabase.getReference("Markers");

        mMapByMarker = new HashMap<>();
        mMapById = new HashMap<>();
        mMarkersDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CustomMarkerData data = dataSnapshot.getValue(CustomMarkerData.class);
                    Marker newMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(data.lat, data.lon))
                            .draggable(true)
                            .visible(true));

                    mMapByMarker.put(newMarker, data.id);
                    mMapById.put(data.id, newMarker);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CustomMarkerData removedMarkerData = dataSnapshot.getValue(CustomMarkerData.class);
                String id= removedMarkerData.id;
                Marker marker = mMapById.get(id);
                mMapByMarker.remove(marker);
                mMapById.remove(id);
                marker.remove();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mEditText.setVisibility(View.VISIBLE);
        mEditText.setText("");
        if (mLastUserMarker!=null) {
            mLastUserMarker.remove();
        }
            mLastUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(false).visible(true));
    }

    @Override
    public void onClick(View view) {
        if (mLastUserMarker==null)
            return;
        String issueText = mEditText.getText().toString();
        if (issueText.equals("")){
            Toast.makeText(mFragmentView.getContext(), "Please enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = mMarkersDatabase.push().getKey();

        CustomMarkerData data = new CustomMarkerData(mLastUserMarker.getPosition().latitude, mLastUserMarker.getPosition().longitude, issueText, null, key);
        mMarkersDatabase.child(key).setValue(data);
        mEditText.setVisibility(View.GONE);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        String id = mMapByMarker.get(marker);
        mMarkersDatabase.child(id).removeValue();
        if (mLastUserMarker!=null)
            mLastUserMarker.remove();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
