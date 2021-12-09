package com.chewwwyong.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PeopleDebt extends AppCompatActivity implements View.OnClickListener {

    EditText edt_name;
    Button btn_add, btn_minus10, btn_minus1, btn_plus1, btn_plus10, btn_confirm, btn_finish, btn_delete;
    TextView tv_sum, tv_sb, tv_people, tv_db, tv_showpeople;
    SeekBar sb_amount;

    Spinner sp_people;
    ArrayList<String> People = new ArrayList<>();
    ArrayAdapter adapter;

    // 計算每次金額
    int sum = 0;
    // 計算總金額
    int debt_sum = 0;
    // 金額上限
    int max_sum = 200;

    // 管理鍵盤的東西？！
    private InputMethodManager imm;

    // 金錢資料庫
    SQLiteDatabase db;

    // 記錄選單選到誰
    String name_record = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_debt);

        db = openOrCreateDatabase("MyDB.db", MODE_PRIVATE, null);

//        String SQL_delete = "DROP TABLE PeopleDebt;";
//        db.execSQL(SQL_delete);

        // 創一張表 紀錄 誰欠多少錢
        String SQL_create_PeopleDebt = "CREATE TABLE IF NOT EXISTS PeopleDebt (Name TEXT, Money Integer);";
        db.execSQL(SQL_create_PeopleDebt);

//        String SQL_delete = "DROP TABLE People;";
//        db.execSQL(SQL_delete);

        // 創一張表放新增的人
        // CREATE TABLE IF NOT EXISTS People (Name TEXT);
        String SQL_create = "CREATE TABLE IF NOT EXISTS People (Name TEXT);";
        db.execSQL(SQL_create);

        edt_name = findViewById(R.id.edt_name);
        edt_name.setText("");

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        btn_minus10 = findViewById(R.id.btn_minus10);
        btn_minus10.setOnClickListener(this);
        btn_minus1 = findViewById(R.id.btn_minus1);
        btn_minus1.setOnClickListener(this);
        btn_plus1 = findViewById(R.id.btn_plus1);
        btn_plus1.setOnClickListener(this);
        btn_plus10 = findViewById(R.id.btn_plus10);
        btn_plus10.setOnClickListener(this);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);
        btn_finish = findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(this);
        btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);

        tv_sum = findViewById(R.id.tv_sum);
        tv_sb = findViewById(R.id.tv_sb);
        tv_people = findViewById(R.id.tv_people);
        tv_db = findViewById(R.id.tv_db);
        tv_showpeople = findViewById(R.id.tv_showpeople);

        sb_amount = findViewById(R.id.sb_amount);
        sb_amount.setMax(max_sum);//設定SeekBar最大值
        sb_amount.setProgress(0);//設定SeekBar拖移初始值
        tv_sb.setText(sb_amount.getProgress() + "/" + sb_amount.getMax());

        sb_amount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_sb.setText(sb_amount.getProgress() + "/" + sb_amount.getMax());
                tv_sum.setText(String.valueOf(sb_amount.getProgress()));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(PeopleDebt.this, "觸碰SeekBar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(PeopleDebt.this, "觸碰SeekBar", Toast.LENGTH_SHORT).show();
            }
        });

        sp_people = findViewById(R.id.sp_people);


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

        adapter = new ArrayAdapter(PeopleDebt.this, android.R.layout.simple_spinner_dropdown_item, People);
        //adapter.setDropDownViewResource(android.R.layout.);
        sp_people.setAdapter(adapter);
        // 下拉式選單
        sp_people.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(view.getContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                //tv_people.setText(adapterView.getSelectedItem().toString());
                name_record = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onClick(View view) {
        sum = Integer.valueOf(String.valueOf(tv_sum.getText()));
        switch (view.getId()){
            case R.id.btn_add:

                if(String.valueOf(edt_name.getText()).trim().length() > 0){
                    //People.add(new String(String.valueOf(edt_name.getText())));

                    // 插入人名
                    // INSERT INTO People (Name) VALUES ('String.valueOf(edt_name.getText())');
                    // 判斷ArrayList是否有包含要加入資料庫的人
                    if (!People.contains(String.valueOf(edt_name.getText()))){
                        String SQL_insert = "INSERT INTO People (Name) VALUES ('" + String.valueOf(edt_name.getText()) + "');";
                        db.execSQL(SQL_insert);
                    }

                    // 從資料庫抓資料插入到 People ArrayList
                    Cursor result;
                    result = (Cursor) db.rawQuery("SELECT * FROM People", null);
                    result.moveToFirst();
                    while (!result.isAfterLast()) {
                        String tempPeople = result.getString(0);
                        // 判斷資料庫的人是否有加到ArrayList
                        if (!People.contains(tempPeople)){
                            People.add(tempPeople);
                        }
                        result.moveToNext();
                        // 偵錯 觀察資料庫裡的人有哪些
                        //tv_showpeople.setText(String.valueOf(tv_showpeople.getText()) + tempPeople + "\n");
                    }
                    result.close();

                    sp_people.setAdapter(adapter);

                    edt_name.setText("");
                    // 送出後鍵盤收起
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                else{

                }
                break;

            case R.id.btn_delete:
                // 從人名資料庫移除
                String SQL_delete = "DELETE FROM People WHERE Name = '" + name_record + "';";
                db.execSQL(SQL_delete);
                // 清除欠錢資料庫的紀錄
                SQL_delete = "DELETE FROM PeopleDebt WHERE Name = '" + name_record + "';";
                db.execSQL(SQL_delete);
                // 從人名ArrayList中清除
                People.remove(name_record);
                // 清空所有記錄的資料
                name_record = "";
                debt_sum = 0;
                tv_db.setText("");
                sum=0;
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                // 重新設定Spinner
                sp_people.setAdapter(adapter);
                break;

            case R.id.btn_minus10:
                sum -= 10;
                if(sum<=0){
                    sum=0;
                }
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                break;

            case R.id.btn_minus1:
                sum -= 1;
                if(sum<=0){
                    sum=0;
                }
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                break;

            case R.id.btn_plus1:
                sum += 1;
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                break;

            case R.id.btn_plus10:
                sum += 10;
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                break;

            case R.id.btn_confirm:

                // 插入每個人的金額
                // INSERT INTO PeopleDebt (Name, Money) VALUES ('name', money);
                String SQL_insert = "INSERT INTO PeopleDebt (Name, Money) VALUES ('" + name_record + "', " + Integer.valueOf(String.valueOf(tv_sum.getText())) + ");";
                db.execSQL(SQL_insert);

                debt_sum = 0;
                Cursor result;
                result = (Cursor) db.rawQuery("SELECT * FROM PeopleDebt WHERE Name='" + name_record + "'", null);
                result.moveToFirst();
                while (!result.isAfterLast()) {
                    String tempName = result.getString(0);
                    String tempAmount = result.getString(1);
                    // do something useful with these
                    debt_sum += Integer.valueOf(tempAmount);
                    result.moveToNext();
                }
                result.close();
                tv_db.setText(name_record + " " + String.valueOf(debt_sum));
                break;

            case R.id.btn_finish:
                tv_db.setText("");
                sum=0;
                tv_sum.setText(String.valueOf(sum));
                sb_amount.setProgress(sum);//設定SeekBar拖移初始值
                startActivity(new Intent(PeopleDebt.this, ShowPeopleDebt.class));
                break;
        }
    }


}