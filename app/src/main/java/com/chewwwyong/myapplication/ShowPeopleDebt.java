package com.chewwwyong.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ShowPeopleDebt extends AppCompatActivity implements View.OnClickListener {

    TextView tv_showdebt, tv_showname;
    Button btn_clear;

    Spinner sp_debtlist;
    ArrayList<String> People = new ArrayList<>();
    ArrayAdapter adapter;

    // 顯示資料
    SQLiteDatabase db;
    // 計算總金額
    int debt_sum = 0;
    // 記錄選單選到誰
    String name_record = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_people_debt);

        tv_showdebt = findViewById(R.id.tv_showdebt);
        tv_showname = findViewById(R.id.tv_showname);
        btn_clear = findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(this);

        sp_debtlist = findViewById(R.id.sp_debtlist);

        // 開啟資料庫
        db = openOrCreateDatabase("MyDB.db", MODE_PRIVATE, null);

        // 從資料庫抓資料插入到 People ArrayList
        Cursor result;
        result = (Cursor) db.rawQuery("SELECT * FROM People", null);

        // 判斷要找的資料庫是不是空的
        if(result.getCount()!=0)
        {
            result.moveToFirst();
            while (!result.isAfterLast()) {
                String tempPeople = result.getString(0);
                // 判斷資料庫的人是否有加到ArrayList
                if (!People.contains(tempPeople)){
                    People.add(tempPeople);
                }
                result.moveToNext();
            }
            result.close();
        }

        adapter = new ArrayAdapter(ShowPeopleDebt.this, android.R.layout.simple_spinner_dropdown_item, People);
        //adapter.setDropDownViewResource(android.R.layout.);
        sp_debtlist.setAdapter(adapter);

        sp_debtlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(view.getContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                tv_showdebt.setText(adapterView.getSelectedItem().toString());

                debt_sum = 0;
                Cursor result;
                result = (Cursor) db.rawQuery("SELECT * FROM PeopleDebt WHERE Name='" + adapterView.getSelectedItem().toString() + "'", null);
                result.moveToFirst();
                while (!result.isAfterLast()) {
                    //String tempName = result.getString(0);
                    String tempAmount = result.getString(1);
                    // do something useful with these
                    debt_sum += Integer.valueOf(tempAmount);
                    result.moveToNext();
                }
                result.close();
                //tv_showname.setText(adapterView.getSelectedItem().toString());
                name_record = adapterView.getSelectedItem().toString();
                tv_showdebt.setText(name_record + " " + String.valueOf(debt_sum));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_clear:
                String SQL_delete = "DELETE FROM PeopleDebt WHERE Name = '" + name_record + "';";
                db.execSQL(SQL_delete);

                String SQL_insert = "INSERT INTO PeopleDebt (Name, Money) VALUES ('" + name_record + "', 0);";
                db.execSQL(SQL_insert);

                // 秀出已經還完錢的數值
                debt_sum = 0;
                Cursor result;
                result = (Cursor) db.rawQuery("SELECT * FROM PeopleDebt WHERE Name='" + name_record + "'", null);
                result.moveToFirst();
                while (!result.isAfterLast()) {
                    //int id=result.getInt(0);
                    //String tempName = result.getString(0);
                    String tempAmount = result.getString(1);
                    // do something useful with these
                    debt_sum += Integer.valueOf(tempAmount);
                    result.moveToNext();
                }
                result.close();
                //tv_showname.setText(String.valueOf(tv_showname.getText()));
                tv_showdebt.setText(name_record + " " + String.valueOf(debt_sum));
                break;
        }
    }
}