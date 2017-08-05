package com.young.jdmall.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.young.jdmall.R;
import com.young.jdmall.bean.CartInfoBean;
import com.young.jdmall.bean.GoodsOrderInfoBean;
import com.young.jdmall.bean.RecommendInfoBean;
import com.young.jdmall.dao.CartDao;
import com.young.jdmall.network.BaseObserver;
import com.young.jdmall.network.RetrofitFactory;
import com.young.jdmall.ui.activity.FillingOrderActivity;
import com.young.jdmall.ui.adapter.GoodsShowAdapter;
import com.young.jdmall.ui.adapter.ShoppingCarFragmentAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;


public class ShopCartFragment extends BaseFragment {

    @BindView(R.id.rv_shopcar)
    RecyclerView mRvShopcar;
    @BindView(R.id.goods_bottom_checkbox)
    CheckBox mGoodsBottomCheckbox;
    @BindView(R.id.pay)
    Button mPay;
    @BindView(R.id.bottom)
    LinearLayout mBottom;
    @BindView(R.id.total_money)
    TextView mTotalMoney;
    private ShoppingCarFragmentAdapter mShoppingCarFragmentAdapter;
    private boolean mIsCheckedAll = false;
    private GoodsShowAdapter mGoodsShowAdapter;
    private int mMoney;
    private CartInfoBean mCartInfoBean;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View shopCarView = inflater.inflate(R.layout.fragment_shop_car, null);
        ButterKnife.bind(this, shopCarView);
        mGoodsShowAdapter = new GoodsShowAdapter(getActivity());
        mShoppingCarFragmentAdapter = new ShoppingCarFragmentAdapter(getActivity());
        initView();
        initData();
        mRvShopcar.setAdapter(mShoppingCarFragmentAdapter);
        initListener();
        return shopCarView;

    }

    private void initListener() {
        mGoodsBottomCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mGoodsBottomCheckbox.setVisibility(View.VISIBLE);
                }else{
                    mGoodsBottomCheckbox.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    private void initView() {
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 2 : 1;
            }
        });
        mRvShopcar.setLayoutManager(manager);
        Log.d("shopcar", "fragment创建布局,添加适配器");

    }

    private void initData() {
        List<GoodsOrderInfoBean> goodsOrderInfoBeen = CartDao.queryAll();
        String sku = "";
        for (GoodsOrderInfoBean goodsOrderInfoBean : goodsOrderInfoBeen) {
            sku += goodsOrderInfoBean.toString()+"|";
        }
        Observable<CartInfoBean> cartInfoBeanObservable = RetrofitFactory.getInstance().listCart(sku);
        cartInfoBeanObservable.compose(compose(this.<CartInfoBean>bindToLifecycle())).subscribe(new BaseObserver<CartInfoBean>(getActivity()) {
            @Override
            protected void onHandleSuccess(CartInfoBean cartInfoBean) {
                mCartInfoBean = cartInfoBean;
                if (mCartInfoBean != null) {
                    mBottom.setVisibility(View.VISIBLE);
                }
                mShoppingCarFragmentAdapter.setList(mCartInfoBean);
                Log.d("data", "cartInfoBean==============" + mCartInfoBean.toString());
            }
        });

        Observable<RecommendInfoBean> recommendInfoBeanObservable = RetrofitFactory.getInstance().listRecommend(1, 10, "saleDown");
        recommendInfoBeanObservable.compose(compose(this.<RecommendInfoBean>bindToLifecycle())).subscribe(new BaseObserver<RecommendInfoBean>(getActivity()) {
            @Override
            protected void onHandleSuccess(RecommendInfoBean recommendInfoBean) {
                mShoppingCarFragmentAdapter.setData(recommendInfoBean.getProductList());
                Log.d("data", "recommendInfoBean=============" + recommendInfoBean.getProductList().toString());

            }
        });


    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    /*//更新全选与不选的价格
    public void setTotalPrice(String price){
        mTotalMoney.setText(PriceFormater.format(price));
    }*/
    //更新单个条目选择的价格
    public void setPrice(int price){
        int currentPrice = Integer.parseInt(mTotalMoney.getText().toString().trim());
        mTotalMoney.setText(currentPrice+price+"");
    }
    public void setFirstPrice(int price){
        Log.d("luoyou", "price"+price);
        int currentPrice = Integer.parseInt(mTotalMoney.getText().toString().trim());
        int newPrice = Math.abs(currentPrice+price);
        Log.d("luoyou", "newprice"+newPrice);
        if (currentPrice<0){
            currentPrice = 0;
        }
        mTotalMoney.setText(newPrice+"");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.goods_bottom_checkbox, R.id.pay, R.id.bottom})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goods_bottom_checkbox:

                mIsCheckedAll = !mIsCheckedAll;

                mGoodsBottomCheckbox.setChecked(mIsCheckedAll);
                //mShoppingCarFragmentAdapter.setAllselect(mIsCheckedAll);
                if (mShoppingCarFragmentAdapter.mGoodsShowAdapter!=null) {
                    mShoppingCarFragmentAdapter.mGoodsShowAdapter.setAllselect(mIsCheckedAll);
                    mShoppingCarFragmentAdapter.mGoodsShowAdapter.notifyDataSetChanged();
                }
                //更新全选与不选的价格
                if (mIsCheckedAll){
                    mTotalMoney.setText(mShoppingCarFragmentAdapter.mGoodsShowAdapter.getTotalMoney()+"");
                }else{
                    mTotalMoney.setText(0+"");
                }

                break;
            case R.id.pay:
                if (mCartInfoBean!=null&&Integer.parseInt(mTotalMoney.getText().toString().trim())>0){
                    Intent intent = new Intent(getActivity(), FillingOrderActivity.class);
                    intent.putExtra("cart", mCartInfoBean);
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity(),"请选中商品",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void setminPrice(int price) {
        int currentPrice = Integer.parseInt(mTotalMoney.getText().toString().trim());
        currentPrice = currentPrice-price;
        mTotalMoney.setText(currentPrice+"");
    }

    public void isAllselect(int totalMoney) {
        int currentPrice = Integer.parseInt(mTotalMoney.getText().toString().trim());
        if (totalMoney==currentPrice){
            mGoodsBottomCheckbox.setChecked(true);
        }else{
            mGoodsBottomCheckbox.setChecked(false);
        }
    }
}
