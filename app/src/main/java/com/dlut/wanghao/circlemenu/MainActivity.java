package com.dlut.wanghao.circlemenu;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dlut.wanghao.circlemenu.CircleMenuGroup.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CircleMenuGroup circleMenuGroup;
    private List<CircleMenuItem> mMenuItems = new ArrayList<CircleMenuItem>();
    private String[] mItemTexts = new String[] { "安全中心 ", "特色服务", "投资理财",
            "转账汇款", "我的账户", "信用卡" };
    private int[] mItemImgs = new int[] { R.drawable.home_mbank_1_normal,
            R.drawable.home_mbank_2_normal, R.drawable.home_mbank_3_normal,
            R.drawable.home_mbank_4_normal, R.drawable.home_mbank_5_normal,
            R.drawable.home_mbank_6_normal };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initData(mItemTexts, mItemImgs);

        //中心视图
        View centerView = LayoutInflater.from(this).inflate(R.layout.circle_menu_item_center,null,false);
        centerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "you click centerView",
                        Toast.LENGTH_SHORT).show();
            }
        });
        circleMenuGroup = (CircleMenuGroup)findViewById(R.id.circlemenu);
        circleMenuGroup.setAdapter(new CircleMenuAdapter(mMenuItems));
        circleMenuGroup.setCenterView(centerView);
        circleMenuGroup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos) {
                Toast.makeText(MainActivity.this, mItemTexts[pos],
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData(String[] mItemTexts, int[] mItemImgs) {
        if (mItemImgs==null && mItemTexts==null){
            throw new IllegalArgumentException("文本和图片不能为空");
        }
        int count = mItemImgs==null ? mItemTexts.length: mItemImgs.length;
        if (mItemImgs!=null && mItemTexts!=null){
            count = Math.min(mItemImgs.length,mItemTexts.length);
        }

        for (int i=0;i<count;i++){
            mMenuItems.add(new CircleMenuItem(mItemImgs[i],mItemTexts[i]));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
