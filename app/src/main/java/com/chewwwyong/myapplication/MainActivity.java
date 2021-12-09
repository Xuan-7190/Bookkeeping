package com.chewwwyong.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    // 顯示資料庫位置的資訊
    TextView tv_showlocation;

    // 定位用
    private LocationManager mLocationManager;               //宣告定位管理控制
    private ArrayList<UE> UEs = new ArrayList<UE>();   //建立List，屬性為Ues物件
    private boolean getService = false;     //是否已開啟定位服務

    // 放位置的資料庫
    SQLiteDatabase db;

    // 經緯度
    double longitude;
    double latitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_showlocation = findViewById(R.id.tv_showlocation);



        db = openOrCreateDatabase("MyDB.db", MODE_PRIVATE, null);

        String SQL_delete = "DROP TABLE Location;";
        db.execSQL(SQL_delete);

        db.execSQL("CREATE TABLE IF NOT EXISTS Location (Longitude VARCHAR(32), Latitude VARCHAR(32))");


        //取得系統定位服務
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            locationServiceInitial();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            getService = true; //確認開啟定位服務
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }

        findViewById(R.id.btn_register).setOnClickListener(view -> {
            locationServiceInitial();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, LocationChange);
            startActivity(new Intent(MainActivity.this, PeopleDebt.class));
        });

        locationServiceInitial();
    }

    private void locationServiceInitial() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //取得系統定位服務

        //取得定位權限
        //mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

         /*做法一,由程式判斷用GPS_provider
           if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
               location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);  //使用GPS定位座標
         }
         else if ( lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
         { location = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //使用GPS定位座標
         }
         else {}*/
////        // 做法二,由Criteria物件判斷提供最準確的資訊
        Criteria criteria = new Criteria();  //資訊提供者選取標準
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //bestProvider = mLocationManager.getBestProvider(criteria, true);    //選擇精準度最高的提供者
        //bestProvider = LocationManager.NETWORK_PROVIDER;
        //Location location = mLocationManager.getLastKnownLocation(bestProvider);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
//        Location location = mLocationManager.getLastKnownLocation(bestProvider);
//        if ( mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
//        {
//            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //使用GPS定位座標
//            getLocation(location);
//        }
//        else if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //使用GPS定位座標
//            getLocation(location);
//        }
        //Location location = mLocationManager.getLastKnownLocation(bestProvider); //使用GPS定位座標
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //使用GPS定位座標
        getLocation(location);
    }

    private void getLocation(Location mlocation) { //將定位資訊顯示在畫面中
        if (mlocation != null) {
            longitude = mlocation.getLongitude();   //取得經度
            latitude = mlocation.getLatitude();     //取得緯度

            //印出我的座標-經度緯度
            Log.d("TAG", "我的座標 - 經度 : " + mlocation.getLongitude() + "  , 緯度 : " + mlocation.getLatitude() );
            //Toast.makeText(MainActivity.this, "我的座標 - 經度 : " + mlocation.getLongitude() + "  , 緯度 : " + mlocation.getLatitude(), Toast.LENGTH_SHORT).show();
            String SQL_insert = "INSERT INTO Location(Longitude, Latitude) VALUES ("+longitude+", "+latitude+")";
            db.execSQL(SQL_insert);

            Cursor result;
            result = (Cursor) db.rawQuery("SELECT * FROM Location", null);
            result.moveToFirst();
            while (!result.isAfterLast()) {
                //int id=result.getInt(0);
                String tempLongitude = result.getString(0);
                String tempLatitude = result.getString(1);
                // do something useful with these
                Toast.makeText(MainActivity.this, tempLongitude + " " + tempLatitude+"\n", Toast.LENGTH_SHORT).show();
                tv_showlocation.setText(String.valueOf(tv_showlocation.getText()) + tempLongitude + " " + tempLatitude + "\n");
                result.moveToNext();
            }
            result.close();


        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }

    //更新定位Listener
    public LocationListener LocationChange = new LocationListener()
    {
        public void onLocationChanged(Location mLocation)
        {
            getLocation(mLocation);
        }

        public void onProviderDisabled(String provider)
        {
        }

        public void onProviderEnabled(String provider)
        {
        }

        public void onStatusChanged(String provider, int status,Bundle extras)
        {
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mLocationManager.removeUpdates(LocationChange);  //程式結束時停止定位更新
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance)
    {
        if(distance < 1000 ) return String.valueOf((int)distance) + "m" ;
        else return new DecimalFormat("#.00").format(distance/1000) + "km" ;
    }

    //List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
    private void DistanceSort(ArrayList<UE> poi)
    {
        Collections.sort(poi, new Comparator<UE>()
        {
            @Override
            public int compare(UE poi1, UE poi2)
            {
                return poi1.getDistance() < poi2.getDistance() ? -1 : 1 ;
            }
        });
    }

    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2,double latitude2)
    {
        double radLatitude1 = latitude1 * Math.PI / 180;
        double radLatitude2 = latitude2 * Math.PI / 180;
        double l = radLatitude1 - radLatitude2;
        double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
                + Math.cos(radLatitude1) * Math.cos(radLatitude2)
                * Math.pow(Math.sin(p / 2), 2)));
        distance = distance * 6378137.0;
        distance = Math.round(distance * 10000) / 10000;

        return distance ;
    }
}