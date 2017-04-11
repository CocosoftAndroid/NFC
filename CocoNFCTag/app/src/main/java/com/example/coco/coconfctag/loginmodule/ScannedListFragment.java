package com.example.coco.coconfctag.loginmodule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coco.coconfctag.R;
import com.example.coco.coconfctag.barcode.BarcodeCaptureActivity;
import com.example.coco.coconfctag.database.DatabaseHandler;
import com.example.coco.coconfctag.listeners.LockNavigationListener;
import com.example.coco.coconfctag.listeners.ScanResultListener;
import com.example.coco.coconfctag.multireadmodule.CartProductAdapter;
import com.example.coco.coconfctag.listeners.QuantityListener;
import com.example.coco.coconfctag.readermodule.ProductItem;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ScannedListFragment extends Fragment implements View.OnClickListener, QuantityListener {

    private TextView mAddCartTxt;
    private static final String LOG_TAG = ScannedListFragment.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private DatabaseHandler mDB;
    private LinearLayoutManager mLManager;
    private RecyclerView mProductRView;
    private CartProductAdapter mProductAdapter;
    private SharedPreferences prefs;
    private QuantityListener mQtyListener;
    private ScanResultListener mScanResultLis;
    private ArrayList<ProductItem> mProductArray;
    private TextView mCountTxtView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProductArray = getArguments().getParcelableArrayList("productarray");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scannedlist, container, false);
        init(view);
        setListeners();
        return view;
    }

    private void setListeners() {
        mAddCartTxt.setOnClickListener(this);
    }

    public void setListener(QuantityListener lis, ScanResultListener scanlis) {
        mQtyListener = lis;
        mScanResultLis = scanlis;

    }

    private void init(View view) {
        mAddCartTxt = (TextView) view.findViewById(R.id.add_cart_txt);
        mDB = new DatabaseHandler(getContext());
        mLManager = new LinearLayoutManager(getContext());
        mProductRView = (RecyclerView) view.findViewById(R.id.rview);
        mProductRView.setLayoutManager(mLManager);
        mProductAdapter = new CartProductAdapter(getContext(), mProductArray, this);
        mProductRView.setAdapter(mProductAdapter);
        prefs = getContext().getSharedPreferences("cocosoft", MODE_PRIVATE);
        Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mCountTxtView = (TextView) toolbar.findViewById(R.id.total_count);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_cart_txt:
                boolean isloggedin = prefs.getBoolean("isloggedin", false);
                if (isloggedin) {
                    changeCount();
                } else {
                    openFrag(1);
                }
                break;
        }
    }


    private void changeCount() {
        int mCount = 0;
        int Total = 0;
        for (int i = 0; i < mProductArray.size(); i++) {
            mCount = mCount + mProductArray.get(i).getCount();
            Total = Total + (mProductArray.get(i).getCount() * mProductArray.get(i).getProductPrice());
        }
        mCountTxtView.setText("" + mCount);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            Log.e(LOG_TAG, "dddddddddd");
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(barcode.displayValue);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (obj != null)
                        mScanResultLis.onScanResult(obj);
                    mProductAdapter.notifyDataSetChanged();
                } else
                    Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onQuantityChange(String id, int quantity) {

        mQtyListener.onQuantityChange(id, quantity);
        mProductAdapter.notifyDataSetChanged();

    }


    private void openFrag(int i) {
        Fragment firstFragment = null;
        switch (i) {
            case 0:
                firstFragment = new HomeFragment();
                break;

            case 1:
                firstFragment = new LoginFragment();
                break;

        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.frame, firstFragment, "h");
        fragmentTransaction.addToBackStack("h");
        fragmentTransaction.commit();
    }
}
