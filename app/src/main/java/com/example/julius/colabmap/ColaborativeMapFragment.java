package com.example.julius.colabmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * Created by julius on 22/11/2017.
 */

public class ColaborativeMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
         GoogleMap.OnMarkerClickListener, View.OnClickListener{
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private GoogleMap mGoogleMap;
    private Marker mLastUserMarker;
    private StorageReference mPhotoStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMarkersDatabase;
    private BottomSheetBehavior behavior;
    private int bottomSheetHeight = 0;

    private boolean bPhotoAssigned;
    private ImageView mArrowIcon;
    private ImageView mPhoto;
    private Button mCancelButton;
    private Button mSubmitButton;
    private EditText mEditText;
    private ProgressBar mImageProgressBar;

    private HashMap<Marker, String> mMapByMarker;
    private HashMap<String, Marker> mMapById;
    private HashMap<String, CustomIssueMarkerData> mIssueMapByID;
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
        View bottomSheet = mainView.findViewById(R.id.bottom_sheet);
        mArrowIcon = mainView.findViewById(R.id.iv_arrow_icon);
        mPhoto = mainView.findViewById(R.id.iv_photo);
        mCancelButton = mainView.findViewById(R.id.btn_cancel);
        mSubmitButton = mainView.findViewById(R.id.btn_submit);

        mSubmitButton.setEnabled(false);
        mEditText = mainView.findViewById(R.id.ev_issue_text);
        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (!mSubmitButton.isEnabled() && mLastUserMarker!=null && bPhotoAssigned){
                    mSubmitButton.setEnabled(true);
                }
                if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    behavior.setPeekHeight(bottomSheetHeight);
                    mArrowIcon.setRotation(0);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED){
                    mArrowIcon.setRotation(180);
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mArrowIcon.setRotation((180*slideOffset));
            }
        });

        mMapFragment.getMapAsync(this);
        return mainView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhoto.setOnClickListener(this);
        mArrowIcon.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                bottomSheetHeight = view.getHeight();
                behavior.setPeekHeight(bottomSheetHeight);
                view.removeOnLayoutChangeListener(this);
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mEditText.getText().toString().length()==0){
                    mSubmitButton.setEnabled(false);
                }
                else if (mLastUserMarker!=null && bPhotoAssigned){
                    mSubmitButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mCancelButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(54.966188824496314,-1.6294744983315468), new LatLng(54.988969287815934,-1.6047086566686632)));
        mGoogleMap.setMinZoomPreference(15.f);
        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                System.out.println(mGoogleMap.getProjection().getVisibleRegion().latLngBounds);
            }
        });
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);

        mDatabase = FirebaseDatabase.getInstance();
        mMarkersDatabase = mDatabase.getReference("Markers");
        mPhotoStorage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://colabmap-186718.appspot.com/photos");

        mMapByMarker = new HashMap<>();
        mMapById = new HashMap<>();
        mIssueMapByID = new HashMap<>();
        mMarkersDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CustomIssueMarkerData data = dataSnapshot.getValue(CustomIssueMarkerData.class);
                    Marker newMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(data.lat, data.lon))
                            .draggable(true)
                            .visible(true));

                    mMapByMarker.put(newMarker, data.id);
                    mMapById.put(data.id, newMarker);
                    mIssueMapByID.put(data.id, data);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                CustomIssueMarkerData removedMarkerData = dataSnapshot.getValue(CustomIssueMarkerData.class);
                String id= removedMarkerData.id;
                Marker marker = mMapById.get(id);
                mMapByMarker.remove(marker);
                mMapById.remove(id);
                mIssueMapByID.remove(removedMarkerData.id);
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
    private void sendImage(String name){
        mPhoto.setDrawingCacheEnabled(true);
        mPhoto.buildDrawingCache();
        Bitmap bitmap = mPhoto.getDrawingCache();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = mPhotoStorage.child(name + ".png");
        UploadTask task = ref.putBytes(data);
        mPhoto.destroyDrawingCache();
        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(getContext(), "Issue submited!", Toast.LENGTH_SHORT).show();
            }
        });
        mPhoto.setImageBitmap(null);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mLastUserMarker != null) {
            mLastUserMarker.remove();
        }
        mLastUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(false).visible(true));
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        String key = mMapByMarker.get(marker);
        if (key==null)
            return true;
        View customView = getLayoutInflater().inflate(R.layout.issue_info_layout, null, false);
        TextView issueText = customView.findViewById(R.id.tv_issue_description);
        ImageView issueImage = customView.findViewById(R.id.iv_info_window_photo);
        Button dismissButton = customView.findViewById(R.id.btn_dismiss);
        mImageProgressBar = customView.findViewById(R.id.pg_image_progress);
        CustomIssueMarkerData data = mIssueMapByID.get(key);
        issueText.setText(data.description);
        final PopupWindow window = new PopupWindow(customView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        setImage(issueImage, key);
        window.showAsDropDown(getView());
        return true;
    }
    private void setImage(final ImageView img, String key){
        StorageReference ref = mPhotoStorage.child(key+".png");
        mImageProgressBar.setVisibility(View.VISIBLE);
        ref.getBytes(2048*2048).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                mImageProgressBar.setVisibility(View.INVISIBLE);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(Bitmap.createScaledBitmap(bmp, img.getWidth(),
                        img.getHeight(), false));
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bPhotoAssigned = true;
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPhoto.setImageBitmap(imageBitmap);
            if (!mSubmitButton.isEnabled() && mLastUserMarker!=null && bPhotoAssigned && mEditText.getText().toString().length()!=0){
                mSubmitButton.setEnabled(true);
            }
        }

    }
    private void onSubmitIssue(){
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        DatabaseReference pushedMarker = mMarkersDatabase.push();
        String key = pushedMarker.getKey();
        sendImage(key);
        CustomIssueMarkerData data = new CustomIssueMarkerData(mLastUserMarker.getPosition().latitude, mLastUserMarker.getPosition().longitude, mEditText.getText().toString(), key+".png", key);
        pushedMarker.setValue(data);
        mEditText.setText("");
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancel:
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.btn_submit:
                onSubmitIssue();
                break;
            case R.id.iv_photo:
                dispatchTakePictureIntent();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
