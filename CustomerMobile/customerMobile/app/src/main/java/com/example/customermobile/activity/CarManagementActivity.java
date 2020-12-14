package com.example.customermobile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customermobile.R;
import com.example.customermobile.network.HttpConnect;
import com.example.customermobile.vo.CarVO;
import com.example.customermobile.vo.UsersVO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.customermobile.activity.LoginActivity.ip;

public class CarManagementActivity extends AppCompatActivity {

    SharedPreferences sp;
    Button button_logout;
    UsersVO user;

    HttpAsyncTask httpAsyncTask;

    ListView listView_carList;
    ArrayList<CarVO> list;

    ImageButton imageButton_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_management);

        listView_carList = findViewById(R.id.listView_carList);
        list = new ArrayList<>();

        imageButton_back = findViewById(R.id.imageButton_back);

        // 회원정보를 intent로 가져오기
        Intent getintent = getIntent();
        user = null;
        user = (UsersVO) getintent.getSerializableExtra("user");

        sp = getSharedPreferences("user", MODE_PRIVATE);

        // intent 정보가 없을 경우, sp로 회원정보 가져오기
        if (user == null) {
            String userid = sp.getString("userid", "");

            // 자동로그인 정보가 있으면 회원정보 계속 가져오기
            String userpwd = sp.getString("userpwd", "");
            String username = sp.getString("username", "");
            String userphone = sp.getString("userphone", "");
            String strbirth = sp.getString("userbirth", "");
            String usersex = sp.getString("usersex", "");
            String strregdate = sp.getString("userregdate", "");
            String userstate = sp.getString("userstate", "");
            String usersubject = sp.getString("usersubject", "");
            String babypushcheck = sp.getString("babypushcheck", "");
            String accpushcheck = sp.getString("accpushcheck", "");
            String mobiletoken = sp.getString("mobiletoken", "");

            // String 변수를 Date로 변환
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date userbirth = null;
            Date userregdate = null;
            try {
                userbirth = sdf.parse(strbirth);
                userregdate = sdf.parse(strregdate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // sp 정보로 회원 객체 생성
            user = new UsersVO(userid, userpwd, username, userphone, userbirth, usersex, userregdate, userstate, usersubject, babypushcheck, accpushcheck, mobiletoken);

        }

        // 로그아웃 버튼 => 뒤로가기 버튼?
        imageButton_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();

                String url = "http://" + ip + "/webServer/userlogoutimpl.mc";
                url += "?id=" + user.getUserid() + "&destroy=no";
                httpAsyncTask = new HttpAsyncTask();
                httpAsyncTask.execute(url);
            }
        });

        listView_carList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            }
        });

    }// end oncreate


    /*
     HTTP 통신 Code
     */
    class HttpAsyncTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(CarManagementActivity.this);
            progressDialog.setTitle("로그아웃");
            progressDialog.setCancelable(false);

            if (sp.getString("userid", "") == null) {
                progressDialog.show();
            } else {

            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0].toString();
            String result = HttpConnect.getString(url);
            return result;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            String result = s.trim();
            if (result.equals("logoutsuccess")) {
                // 로그아웃
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (result.equals("destroy")) {
                // destroy
            } else if (result.equals("logoutfail")) {
                // 로그아웃 실패: Exception
                AlertDialog.Builder builder = new AlertDialog.Builder(CarManagementActivity.this);
                builder.setTitle("로그아웃에 실패하였습니다.");
                builder.setMessage("다시 시도해 주십시오.");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                builder.show();
            }
        }
    }
    // End HTTP 통신 Code


    @Override
    protected void onDestroy() {
        String url = "http://" + ip + "/webServer/userlogoutimpl.mc";
        String destroy = "yes";

        url += "?id=" + user.getUserid() + "&destroy=" + destroy;
        httpAsyncTask = new HttpAsyncTask();
        httpAsyncTask.execute(url);
        super.onDestroy();
    }

}