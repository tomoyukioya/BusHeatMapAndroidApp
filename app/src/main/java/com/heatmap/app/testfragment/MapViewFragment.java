package com.heatmap.app.testfragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapViewFragment extends Fragment
implements OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener,
        HttpResponseAsync.HttpResponseAsyncInterface
{
    // 定数
    private final int DEFAULT_ZOOM = 15;

    // メンバ変数
    private OnMapViewInteractionListener mListener;
    private MapView mMapView;
    private GoogleMap mMap;
    private LocationManager mLocationManager=null; // 前回表示した場所情報を保存
    private HeatmapTileProvider mProvider = null;
    private TileOverlay mOverlay=null;
    private int mHeatMapMode = LocalConstatnts.InitialHeatMapMode;
    private List<Polygon> mPolygons;

    //////////////////////////
    //
    // 初期化
    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Viewのinflate
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        // MapViewの準備
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this); // onMapReadyをcallback

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);

        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        if(!setMyLocationEnabled()) {
            // permission取得中、もしくはpermission拒否
            // 東京駅を表示。指定しないとアフリカが表示される。
            try{
                LatLng tokyoStation = new LatLng(35.681298, 139.766247);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyoStation, DEFAULT_ZOOM));
            } catch(Exception e) {
                // エラー処理
                Log.v("Error", "Unhandled Exception caught in onMapReady: " + e.getMessage());
            }
        }
        else
        {
            // permission取得済み
            try{
                // 前回の現在地を表示
                Criteria criteria = new Criteria();
                String provider = mLocationManager.getBestProvider(criteria, true);
                Location lastLocation = mLocationManager.getLastKnownLocation(provider);
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, DEFAULT_ZOOM));

                // 現在地更新処理起動
                onPermissionConfirmed();

            } catch(SecurityException e) {
                // エラー処理
                Log.v("Error", "SecrityException caught in onMapReady: " + e.getMessage());
            }catch(Exception e) {
                // エラー処理
                Log.v("Error", "Unhandled Exception caught in onMapReady: " + e.getMessage());
            }
        }
    }

    //////////////////////////
    //
    // 権限取得後に行う処理
    //
    public void onPermissionConfirmed()
    {
        requestLocationUpdates();
    }

    //////////////////////////
    //
    // 現在地更新処理
    //
    private void requestLocationUpdates(){
        if (mLocationManager != null) {
            Criteria criteria = new Criteria();
            List<String> providers = mLocationManager.getProviders(true);
            for (String provider : providers) {
                try {
                    Log.v("Status", "calling requestLocationUpdates");
                    // 現在地が取得でき次第、onLocationChangedが呼ばれるように設定
                    mLocationManager.requestLocationUpdates(provider, 0, 0, this);
                } catch (SecurityException e) {
                    Log.v("Error", "requestLocationUpdates failed");
                }
            }
        }
    }

    @Override
    // 現在地が取得出来たら呼ばれる
    // 現在地更新は一回のみ
    public void onLocationChanged(Location location) {
        Log.v("Trace", "Entering MapViewFragment::onLocationChanged");

        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM));
        try {
            if(mLocationManager!=null)
                mLocationManager.removeUpdates(this);
        } catch(SecurityException e) {
            // エラー処理
            Log.v("Error", "SecurityException in onLocationChanged: " + e.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v("Trace", "Entering MapViewFragment::onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v("Trace", "Entering MapViewFragment::onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.v("Status", "Entering MapViewFragment::onStatusChanged");
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.v("Status", "AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("Status", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("Status", "TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    //////////////////////////
    //
    // 現在地ボタン表示
    //
    public boolean setMyLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Log.v("Status", "calling mMap.setMyLocationEnabled(true)");
            if(mMap!=null){
                mMap.setMyLocationEnabled(true);
            }else{
                Log.v("Status", "mMap not ready");
            }
            return true;
        }
        else {
            Log.v("Status", "no location access permission in setMyLocationEnabled");
            return false;
        }
    }

    //////////////////////////
    //
    // Google Mapイベントハンドラ
    //
    private HttpResponseAsync mCurrentTask=null;
    @Override
    // Camera移動が一段落したら、ViewRegion内のHeatMap描画
    public void onCameraIdle() {
        Log.v("Trace", "Entering MapViemCurwFragment::onCameraIdle");

        // Heatmap描画
        VisibleRegion region = mMap.getProjection().getVisibleRegion();
        if(mCurrentTask !=null ){
            mCurrentTask.cancel(true);
        }
        mCurrentTask = new HttpResponseAsync(this);
        mCurrentTask.execute(region);
    }

    // HttpResponseAsyncからのcallback
    // HeatMap描画
    public void OnHttpResponseReady(List<HeatMapData> result)
    {
        VisibleRegion region = mMap.getProjection().getVisibleRegion();

        // デバッグメッセージ表示
        ((MainActivity)getActivity()).SetZoomLevelText(String.valueOf(mMap.getCameraPosition().zoom));
        ((MainActivity)getActivity()).SetViewRegionText(
                String.valueOf(region.latLngBounds.northeast.latitude) + " " + String.valueOf(region.latLngBounds.northeast.longitude),
                String.valueOf(region.latLngBounds.southwest.latitude) + " " + String.valueOf(region.latLngBounds.southwest.longitude));

        Log.v("Trace", "Entering MapViewFragment::OnHttpResponseReady");

        switch(mHeatMapMode)
        {
            case R.id.Button_HeatMap_Weighted:
                // Polygon削除
                if(mPolygons!=null){
                    for(Polygon poly : mPolygons)
                        poly.remove();
                    mPolygons = null;
                }

                List<WeightedLatLng> weightedLatLngList = new ArrayList<WeightedLatLng>();
                for(HeatMapData data : result)
                {
                    weightedLatLngList.add(new WeightedLatLng(new LatLng(data.Latitude, data.Longitude),
                            mHeatMapMode==R.id.Button_HeatMap_Weighted ? data.Intensity : 1));
                }
                if(weightedLatLngList.size()>0){
                    if(mProvider == null)
                        mProvider = new HeatmapTileProvider.Builder().weightedData(weightedLatLngList).build();
                    else
                        mProvider.setWeightedData(weightedLatLngList);
                    if(mOverlay == null) mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    mOverlay.clearTileCache();
                }
                break;

            case R.id.Button_Mesh500m:
            case R.id.Button_Mesh1k:
            case R.id.Button_MeshAdaptive:
                // メッシュサイズ
                int meshSize;
                if(mHeatMapMode==R.id.Button_Mesh500m)
                    meshSize = 500;
                else if(mHeatMapMode==R.id.Button_Mesh1k)
                    meshSize = 1000;
                else{
                    int zoom = (int)mMap.getCameraPosition().zoom;
                    if(zoom >=13)
                        meshSize = 500;
                    else
                        meshSize = (int)(Math.pow(2.0, 13 - zoom)) * 500;
                }

                // HeatMap Overlay削除
                if(mOverlay!=null) mOverlay.remove();
                mOverlay=null;

                // Polygon削除
                if(mPolygons!=null)
                    for(Polygon poly : mPolygons)
                        poly.remove();
                mPolygons = new ArrayList<Polygon>();

                // Polygon作成
                double meshSizeLong = LocalConstatnts.LongitudeForMeter(/*mMap.getCameraPosition().target.latitude*/35.0)*meshSize;
                double meshSizeLat = LocalConstatnts.LatitudeForMeter*meshSize;

                int nBlockInLong = (int)((region.latLngBounds.northeast.longitude - region.latLngBounds.southwest.longitude)
                    / meshSizeLong) + 1;
                int nBlockInLat = (int)((region.latLngBounds.northeast.latitude - region.latLngBounds.southwest.latitude)
                        / meshSizeLat) + 1;

                int[][] heatMapIntensity = new int[nBlockInLat][nBlockInLong];
                int[][] heatMapItem = new int[nBlockInLat][nBlockInLong];
                for(HeatMapData data : result)
                {
                    int indexLat = (int)((data.Latitude - (int)(region.latLngBounds.southwest.latitude / meshSizeLat)*meshSizeLat)/meshSizeLat);
                    int indexLong = (int)((data.Longitude - (int)(region.latLngBounds.southwest.longitude / meshSizeLong)*meshSizeLong)/meshSizeLong);
                    if(0<=indexLat && indexLat <nBlockInLat &&0<=indexLong && indexLong <nBlockInLong ){
                        heatMapIntensity[indexLat][indexLong] += data.Intensity;
                        heatMapItem[indexLat][indexLong] ++;
                    }
                }

                for(int i=0;i<nBlockInLong;i++){
                    double baseLong = (int)(region.latLngBounds.southwest.longitude / meshSizeLong)*meshSizeLong + i * meshSizeLong;
                    for(int j=0;j<nBlockInLat;j++) {
                        double baseLat = (int)(region.latLngBounds.southwest.latitude / meshSizeLat)*meshSizeLat + j * meshSizeLat;

                        Polygon polygon = mMap.addPolygon(
                                new PolygonOptions().add(
                                        new LatLng(baseLat, baseLong),
                                        new LatLng(baseLat, baseLong + meshSizeLong),
                                        new LatLng(baseLat + meshSizeLat, baseLong + meshSizeLong),
                                        new LatLng(baseLat + meshSizeLat, baseLong)));
                        mPolygons.add(polygon);
                        polygon.setStrokeColor(Color.TRANSPARENT);
                        if(heatMapIntensity[j][i]>0)
                            polygon.setFillColor(Color.argb(heatMapIntensity[j][i]/heatMapItem[j][i]/2, 0, 255,0));
                    }
                }

                break;

        }
    }

    @Override
    public void onCameraMoveStarted(int reason)
    {
        Log.v("Trace", "Entering MapViewFragment::onCameraMoveStarted");
    }

    @Override
    public void onResume() {
        Log.v("Trace", "Entering MapViewFragment::onResume");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        Log.v("Trace", "Entering MapViewFragment::onPause");
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        Log.v("Trace", "Entering MapViewFragment::onDestroy");
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.v("Trace", "Entering MapViewFragment::onLowMemory");
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onAttach(Context context) {
        Log.v("Trace", "Entering MapViewFragment::onAttach");
        super.onAttach(context);
        if (context instanceof OnMapViewInteractionListener) {
            mListener = (OnMapViewInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.v("Trace", "Entering MapViewFragment::onDetach");
        super.onDetach();
        mListener = null;
    }

    //////////////////////////
    //
    // HeatMapMode更新処理
    //
    public void onHeatMapModeChanged(int id){
        Log.v("Trace", "Entering MapViewFragment::onHeatMapModeChanged");
        if(mHeatMapMode!=id) {
            mHeatMapMode = id;
            if (mHeatMapMode == R.id.Button_Mesh500m) {
                float minZoom = 12.0f;
                if (mMap.getCameraPosition().zoom < minZoom)
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(minZoom));
                mMap.setMinZoomPreference(minZoom);
            } else if (mHeatMapMode == R.id.Button_Mesh1k) {
                float minZoom = 11.0f;
                if (mMap.getCameraPosition().zoom < minZoom)
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(minZoom));
                mMap.setMinZoomPreference(minZoom);
            }else{
                mMap.resetMinMaxZoomPreference();
            }
            onCameraIdle();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMapViewInteractionListener {
        // TODO: Update argument type and name
        void onMapViewInteraction(Uri uri);
    }
}

