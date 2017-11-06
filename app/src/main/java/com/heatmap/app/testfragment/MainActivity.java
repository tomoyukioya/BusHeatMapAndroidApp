package com.heatmap.app.testfragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements
        ControlPanelFragment.OnMapModeChangedListener,
        MapViewFragment.OnMapViewInteractionListener
{
    // 定数
    static final int RC_LOCATION_PERMISSIONS = 0x01;
    static final String [] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
    };

    // メンバ変数
    private MyPhoneStateListener mMyPhoneStateListener;
    private ControlPanelFragment mControlPanelFragment;
    private MapViewFragment mMapViewFragment;
    private Context mContext;
    private int mHeatMapMode = LocalConstatnts.InitialHeatMapMode;

    //////////////////////////
    //
    // 初期化
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        // Fragmentを取得
        mControlPanelFragment = (ControlPanelFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_control_panel);
        mMapViewFragment = (MapViewFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapView);

        // 必要な権限を取得
        getPermission();
    }

    //////////////////////////
    //
    // パーミッション取得
    //
    private boolean getPermission()
    {
        boolean permissionDenied = false;
        for(String permission : PERMISSIONS){
            if(ActivityCompat.checkSelfPermission(this, permission)!= PackageManager.PERMISSION_GRANTED)
            {
                permissionDenied = true;
                break;
            }
        }

        if(permissionDenied){
            Log.v("Trace", "not enough permission: start requesting permissions");
            ActivityCompat.requestPermissions(this, PERMISSIONS, RC_LOCATION_PERMISSIONS);
            return false;
        }

        Log.v("Trace", "Permissions comfirmed");
        onPermissionConfirmed();
        return true;
    }

    @Override
    // 許可ダイアログの結果を受け取るコールバックメソッド
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.v("Trace", "Entering onRequestLocationPermissionsResult callback");
        if (requestCode == RC_LOCATION_PERMISSIONS) {
            boolean permissionDenied = false;
            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED)
                {
                    permissionDenied = true;
                    break;
                }
            }

            if (permissionDenied)
            {
                // 権限を取得できなかったのでActivityを終了
                Log.v("Trace", "Permissions not granted, exiting");
                Toast.makeText(this, "必要な権限が取得できませんでした", Toast.LENGTH_LONG).show();
                finish();
            }

            // 権限が取得できた
            onPermissionConfirmed();
        }
    }

    // 権限が取得できた際に行う処理
    private void onPermissionConfirmed()
    {
        mMyPhoneStateListener = new MyPhoneStateListener(this);
        mMyPhoneStateListener.Start();

        mMapViewFragment.onPermissionConfirmed();
    }

    //////////////////////////
    //
    // Activityライフサイクル
    //
    @Override
    protected void onResume() {
        Log.v("Trace", "Entering MainActivity::onResume");
        super.onResume();
        if(mMyPhoneStateListener!=null) mMyPhoneStateListener.Start();
    }

    @Override
    protected void onPause() {
        Log.v("Trace", "Entering MainActivity::onPause");
        super.onPause();
        if(mMyPhoneStateListener!=null) mMyPhoneStateListener.Stop();
    }

    @Override
    protected void onDestroy() {
        Log.v("Trace", "Entering MainActivity::onDestroy");
        super.onDestroy();
    }

    //////////////////////////
    //
    // FragmentからMainActivityへの連携
    //
    public void onMapModeChanged(int id){
        if(id!=mHeatMapMode){
            mHeatMapMode = id;
            mMapViewFragment.onHeatMapModeChanged(id);
        }
    }

    public void onMapViewInteraction(Uri uri){

    }

    // デバッグメッセージ表示
    public void SetZoomLevelText(String message)
    {
        mControlPanelFragment.SetZoomLevelText(message);
    }
    public void SetViewRegionText(String northWest, String southWest)
    {
        mControlPanelFragment.SetViewRegionText(northWest, southWest);
    }
}
