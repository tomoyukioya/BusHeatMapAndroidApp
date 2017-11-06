package com.heatmap.app.testfragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ControlPanelFragment extends Fragment implements View.OnClickListener {

    // メンバ変数
    private OnMapModeChangedListener mListener;
    private View mView;
    private ControlPanelFragment mFragment;

    //////////////////////////
    //
    // 初期化
    //
    public ControlPanelFragment() {
        // Required empty public constructor
        mFragment = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_control_panel, container, false);

        // ボタンのイベントハンドラ登録
        mView.findViewById(R.id.Button_MeshAdaptive).setOnClickListener(this);
        mView.findViewById(R.id.Button_HeatMap_Weighted).setOnClickListener(this);
        mView.findViewById(R.id.Button_Mesh500m).setOnClickListener(this);
        mView.findViewById(R.id.Button_Mesh1k).setOnClickListener(this);

        // ボタンの初期状態
        mView.findViewById(LocalConstatnts.InitialHeatMapMode).setEnabled(false);
        mListener.onMapModeChanged(LocalConstatnts.InitialHeatMapMode);

        return mView;
    }

    @Override
    // ボタンのイベントハンドラ
    public void onClick(View v) {

        mView.findViewById(R.id.Button_HeatMap_Weighted).setEnabled(true);
        mView.findViewById(R.id.Button_Mesh500m).setEnabled(true);
        mView.findViewById(R.id.Button_Mesh1k).setEnabled(true);
        mView.findViewById(R.id.Button_MeshAdaptive).setEnabled(true);

        int id = v.getId();
        mView.findViewById(id).setEnabled(false);
        mListener.onMapModeChanged(id);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapModeChangedListener) {
            mListener = (OnMapModeChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMapModeChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateCellId(int id)
    {
        TextView textView = (TextView)mView.findViewById(R.id.cellIdText);
        textView.setText(String.valueOf(id));
    }

    // Map表示モードが変更された際に呼ばれるインタフェース
    public interface OnMapModeChangedListener {
        // TODO: Update argument type and name
        void onMapModeChanged(int id);
    }

    // デバッグメッセージ表示
    public void SetZoomLevelText(String message)
    {
        ((TextView)mView.findViewById(R.id.zoomLevelText)).setText(message);
    }
    public void SetViewRegionText(String northWest, String southWest)
    {
        ((TextView)mView.findViewById(R.id.northEastText)).setText(northWest);
        ((TextView)mView.findViewById(R.id.southWestText)).setText(southWest);
    }
}
