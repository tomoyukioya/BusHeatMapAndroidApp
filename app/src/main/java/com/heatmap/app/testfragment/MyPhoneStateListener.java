package com.heatmap.app.testfragment;

import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.CellLocation;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Ohya on 2017/11/01.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    Context mContext;
    TelephonyManager mTelephonyManager=null;

    private final int LISTEN = PhoneStateListener.LISTEN_CELL_INFO;

    public MyPhoneStateListener(Context context)
    {
        mContext = context;
    }

    public void Start()
    {
        if(mTelephonyManager==null)
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(this, LISTEN);
    }

    public void Stop()
    {
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onCellInfoChanged(java.util.List<android.telephony.CellInfo> cellInfoList) {
        if(cellInfoList==null) {
//            Toast.makeText(mContext,"onCellInfoChanged with null", Toast.LENGTH_LONG).show();
            Log.v("Trace", "Entering MainActivity::onCellInfoChanged with null");
            cellInfoList = mTelephonyManager.getAllCellInfo();
        }else
        {
//            Toast.makeText(mContext,"onCellInfoChanged", Toast.LENGTH_LONG).show();
            Log.v("Trace", "Entering MainActivity::onCellInfoChanged");
        }
        int ci = 0;
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                if(!cellInfoLte.isRegistered()) continue;
                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                ci = cellIdentityLte.getCi();
                Log.v("Info", "Cell ID is " + ci);

                // ControlPanelFragmentの表示を更新
                ControlPanelFragment frag = (ControlPanelFragment)
                        ((MainActivity)mContext).getSupportFragmentManager().findFragmentById(R.id.fragment_control_panel);
                if(frag!=null) {
                    frag.updateCellId(ci);
                }

                break;
            }
        }
    }
}
