package com.example.coco.coconfctag.loginmodule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.MenuPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coco.coconfctag.R;
import com.example.coco.coconfctag.database.DatabaseHandler;
import com.example.coco.coconfctag.listeners.QuantityListener;
import com.example.coco.coconfctag.listeners.WishlistCheckListener;
import com.example.coco.coconfctag.listeners.WishlistListener;
import com.example.coco.coconfctag.multireadmodule.CartProductAdapter;
import com.example.coco.coconfctag.readermodule.ProductItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class WishListFragment extends Fragment implements View.OnClickListener, WishlistListener, WishlistCheckListener {

    private TextView mAddCartTxt;
    private LinearLayoutManager mLManager;
    private RecyclerView mProductRView;
    private WishlistAdapter mWishlistAdapter;
    private ArrayList<ProductItem> mProductArray = new ArrayList<>();
    private TextView mCountTxtView;
    private TextView mTitleTxtView;
    private ImageView mCartImg;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    private DatabaseHandler mDb;
    private ArrayList<WishlistItem> mWishlistArray = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        init(view);
        setListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTitleTxtView.setText("WishList");
    }

    private void setListeners() {
        mAddCartTxt.setOnClickListener(this);

    }


    private void init(View view) {

        mAddCartTxt = (TextView) view.findViewById(R.id.add_cart_txt);
        mLManager = new LinearLayoutManager(getContext());
        mProductRView = (RecyclerView) view.findViewById(R.id.rview);
        mProductRView.setLayoutManager(mLManager);
        mDb = new DatabaseHandler(getContext());
        prefs = getContext().getSharedPreferences("cocosoft", MODE_PRIVATE);
        prefsEditor = prefs.edit();
        String username = prefs.getString("username", null);
        ArrayList<String> productids = mDb.getAllWishList(username);
        if (productids != null) {
            for (int i = 0; i < productids.size(); i++) {
                mProductArray.add(mDb.getProductItem(productids.get(i)));
            }
        }
        mWishlistAdapter = new WishlistAdapter(getContext(), mProductArray, this, this);
        mProductRView.setAdapter(mWishlistAdapter);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mCountTxtView = (TextView) toolbar.findViewById(R.id.total_count);
        mTitleTxtView = (TextView) toolbar.findViewById(R.id.title_txt);
        mCartImg = (ImageView) toolbar.findViewById(R.id.cart_img);
        mCountTxtView.setVisibility(View.GONE);
        mCartImg.setVisibility(View.GONE);

        for (int j = 0; j < mProductArray.size(); j++) {
            mWishlistArray.add(j, new WishlistItem(mProductArray.get(j).getProductId(), false));
        }

    }


    @Override
    public void onClick(View v) {
        boolean isloggedin = prefs.getBoolean("isloggedin", false);
        String username = prefs.getString("username", "");
        switch (v.getId()) {
            case R.id.add_cart_txt:

                if (isloggedin) {
                    Toast.makeText(getContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
                } else {
                    openFrag(1);
                }
                break;

        }
    }


    private void openFrag(int i) {
        Fragment firstFragment = null;
        switch (i) {
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

    @Override
    public void onFavouriteClicked(String productid, boolean isChecked) {
        String username = prefs.getString("username", "");
        mDb.removeWishlist(productid, username);
        for (int i = 0; i < mProductArray.size(); i++) {
            if (mProductArray.get(i).getProductId().equals(productid))
                mProductArray.remove(i);
        }
        mWishlistAdapter.notifyDataSetChanged();

    }

    @Override
    public void onChecked(String productid,boolean isChecked) {
        for (int i = 0; i < mWishlistArray.size(); i++) {
    if(mWishlistArray.get(i).getProductid().equals(productid))
        mWishlistArray.get(i).setChecked(isChecked);
        }
    }


    public class WishlistItem {
        private String productid = "";
        private boolean isChecked = false;

        public WishlistItem(String productid, boolean isChecked) {
            this.productid = productid;
            this.isChecked = isChecked;
        }

        public WishlistItem() {
        }

        public String getProductid() {
            return productid;
        }

        public void setProductid(String productid) {
            this.productid = productid;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
