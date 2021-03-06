package com.example.customermobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customermobile.activity.CarActivity;
import com.example.customermobile.network.HttpConnect;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.customermobile.activity.LoginActivity.ip;


public class Fragment1 extends Fragment {

    TextView textView_carName, textView_carModel, textView_carNum, textView_weatherTemper;
    TextView textView_todayDate, textView_address, textView_weather, textView_possibleDistance;
    TextView textView_fuel, textView_temper, textView_targetTemper, textView_moving;
    ImageView imageView_car, imageView_weather;
    ImageButton imageButton_carLeft, imageButton_carRight, imageButton_startingOn, imageButton_startingOff;
    ImageButton imageButton_doorOn, imageButton_doorOff, imageButton_downTemper, imageButton_upTemper;

    // 버튼 계속 안눌리게 하기 위한 스위치 변수
    int startingSW = 0;
    int doorSW = 0;

    int targetTemper;
    TemperTimer temperTimer;

    // GPS 위치 정보 수신
    private GpsTracker gpsTracker;
    private Context context;
    double lat;
    double lng;

    // 날씨 정보 수신
    String temperature;
    String pty;
    String sky;
    HttpAsyncTaskWeather httpAsyncTaskWeather;
    String weatherResult;

//    public static Fragment1 newInstance() {
//        return new Fragment1();
//    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        ViewGroup viewGroup = null;
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment1,container,false);


        textView_carName = viewGroup.findViewById(R.id.textView_carName);
        textView_carModel = viewGroup.findViewById(R.id.textView_carModel);
        textView_carNum = viewGroup.findViewById(R.id.textView_carNum);
        textView_weatherTemper = viewGroup.findViewById(R.id.textView_weatherTemper);
        textView_todayDate = viewGroup.findViewById(R.id.textView_todayDate);
        textView_address = viewGroup.findViewById(R.id.textView_address);
        textView_weather = viewGroup.findViewById(R.id.textView_weather);
        textView_possibleDistance = viewGroup.findViewById(R.id.textView_possibleDistance);
        textView_fuel = viewGroup.findViewById(R.id.textView_fuel);
        textView_temper = viewGroup.findViewById(R.id.textView_temper);
        textView_targetTemper = viewGroup.findViewById(R.id.textView_targetTemper);
        textView_moving = viewGroup.findViewById(R.id.textView_moving);
        imageView_car = viewGroup.findViewById(R.id.imageView_car);
        imageView_weather = viewGroup.findViewById(R.id.imageView_weather);
        imageButton_carLeft = viewGroup.findViewById(R.id.imageButton_carLeft);
        imageButton_carRight = viewGroup.findViewById(R.id.imageButton_carRight);
        imageButton_startingOn = viewGroup.findViewById(R.id.imageButton_startingOn);
        imageButton_startingOff = viewGroup.findViewById(R.id.imageButton_startingOff);
        imageButton_doorOn = viewGroup.findViewById(R.id.imageButton_doorOn);
        imageButton_doorOff = viewGroup.findViewById(R.id.imageButton_doorOff);
        imageButton_downTemper = viewGroup.findViewById(R.id.imageButton_downTemper);
        imageButton_upTemper = viewGroup.findViewById(R.id.imageButton_upTemper);




        imageButton_carLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               ((CarActivity)getActivity()).clickcarleft();
            }
        }) ;

        imageButton_carRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((CarActivity)getActivity()).clickcarright();
            }
        }) ;


        imageButton_startingOn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(startingSW == 0){
                    CarActivity.carDataTimer.start();
                    startingSW = 1;

                    ((CarActivity)getActivity()).sendfcm("CA00003100000001");

                    imageButton_startingOn.setImageResource(R.drawable.startingon1);
                    imageButton_startingOff.setImageResource(R.drawable.startingoff);

                    ((CarActivity)getActivity()).vibrate(300,3);
                }
            }
        }) ;

        imageButton_startingOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(startingSW == 1){
                    CarActivity.carDataTimer.start();
                    startingSW = 0;

                    ((CarActivity)getActivity()).sendfcm("CA00003100000000");

                    imageButton_startingOff.setImageResource(R.drawable.startingoff1);
                    imageButton_startingOn.setImageResource(R.drawable.startingon);

                    ((CarActivity)getActivity()).vibrate(300,3);
                }

            }
        }) ;

        imageButton_doorOn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(doorSW == 0) {
                    CarActivity.carDataTimer.start();
                    doorSW = 1;

                    ((CarActivity) getActivity()).sendfcm("CA00003300000001");

                    imageButton_doorOn.setImageResource(R.drawable.dooropenimgg);
                    imageButton_doorOff.setImageResource(R.drawable.doorcloseimg);

                    ((CarActivity) getActivity()).vibrate(300, 3);
                }

            }
        }) ;

        imageButton_doorOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(doorSW == 1) {
                    CarActivity.carDataTimer.start();
                    doorSW = 0;

                    ((CarActivity)getActivity()).sendfcm("CA00003300000000");

                    imageButton_doorOn.setImageResource(R.drawable.dooropenimg);
                    imageButton_doorOff.setImageResource(R.drawable.doorcloseimgg);

                    ((CarActivity)getActivity()).vibrate(300,3);
                }

            }
        }) ;


        // 온도 타이머 2초로 세팅하기
        temperTimer = new TemperTimer(2000, 1000);

        imageButton_upTemper.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                CarActivity.carDataTimer.start();

                ((CarActivity)getActivity()).carDataTimer.cancel();

                if(targetTemper >= 30){
                    Toast.makeText(getActivity(),"30도 이하로 설정해주세요!",Toast.LENGTH_SHORT).show();
                }else{
                    targetTemper = targetTemper + 1;
                    textView_targetTemper.setText(String.valueOf(targetTemper));

                    temperTimer.cancel();
                    temperTimer.start();
                }

            }


        }) ;

        imageButton_downTemper.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                ((CarActivity)getActivity()).carDataTimer.cancel();

                if(targetTemper <= 18){
                    Toast.makeText(getActivity(),"18도 이상으로 설정해주세요!",Toast.LENGTH_SHORT).show();
                }else{
                    targetTemper = targetTemper - 1;
                    textView_targetTemper.setText(String.valueOf(targetTemper));

                    temperTimer.cancel();
                    temperTimer.start();
                }

            }
        }) ;


        // GPS 위치 정보 수신
        context = container.getContext();
        getGPS();

        String nx = String.valueOf(Math.round(lat));
        String ny = String.valueOf(Math.round(lng));

        // 날씨 정보 수신
        getWeather(nx, ny);


        return viewGroup;
    }
    /*
     * end onCreate
     */



    // 온도설정을 위한 타이머
    class TemperTimer extends CountDownTimer
    {
        public TemperTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            ((CarActivity)getActivity()).carDataTimer.start();
            ((CarActivity)getActivity()).sendfcm("CA0000210000"+String.valueOf(targetTemper)+"00");;
            Toast t = Toast.makeText(getActivity(),"차량온도가 "+targetTemper+"로 세팅합니다!",Toast.LENGTH_SHORT);
            t.show();
            ((CarActivity)getActivity()).vibrate(500,5);

        }
    }



    // 차 정보를 세팅하는 함수
    public void setCarData(String carname, String carmodel, String carnum, String carimg){

        textView_carName.setText(carname);
        textView_carModel.setText(carmodel);
        textView_carNum.setText(carnum);

        int[] imglist = {R.drawable.car1,R.drawable.car2, R.drawable.car3};


        if(carimg.equals("car1.jpg")){
            imageView_car.setImageResource(imglist[0]);
        }else if(carimg.equals("car2.jpg")){
            imageView_car.setImageResource(imglist[1]);
        }else if(carimg.equals("car3.jpg")){
            imageView_car.setImageResource(imglist[2]);
        }

        if(textView_carName.getText().toString().equals("(null)")){
            textView_carName.setVisibility(View.GONE);
        } else {
            textView_carName.setVisibility(View.VISIBLE);
        }

        Log.d("[TAG]", "setCarData OK"+" "+carname+" "+carmodel+" "+carnum+" "+carimg);

    }

    // 차센서 정보를 세팅하는 함수
    public void setCarSensorData(String moving, int fuel, String starting, String door, int temper, String aircon){

        if(moving.equals("1")){
            textView_moving.setText("주행중");
            textView_moving.setTextColor(Color.GREEN);
        }else{
            textView_moving.setText("정차");
            textView_moving.setTextColor(Color.RED);
        }
        textView_fuel.setText(String.valueOf(fuel/100));
        textView_possibleDistance.setText(String.valueOf(fuel*12/100));
        textView_targetTemper.setText(aircon);
        // 현재 타겟온도 가져오기
        targetTemper = Integer.parseInt(textView_targetTemper.getText().toString());
        Log.d("[TAG]","Here:"+fuel+" "+starting+" "+door+" "+temper);
        if(starting.equals("1")){
            startingSW = 1;
            imageButton_startingOn.setImageResource(R.drawable.startingon1);
            imageButton_startingOff.setImageResource(R.drawable.startingoff);
        }else{
            startingSW = 0;
            imageButton_startingOff.setImageResource(R.drawable.startingoff1);
            imageButton_startingOn.setImageResource(R.drawable.startingon);
        }
        if(door.equals("1")){
            doorSW = 1;
            imageButton_doorOn.setImageResource(R.drawable.dooropenimgg);
            imageButton_doorOff.setImageResource(R.drawable.doorcloseimg);
        }else{
            doorSW = 0;
            imageButton_doorOn.setImageResource(R.drawable.dooropenimg);
            imageButton_doorOff.setImageResource(R.drawable.doorcloseimgg);
        }
        textView_temper.setText(String.valueOf(temper));

        textView_targetTemper.setText(aircon);
        // 현재 타겟온도 가져오기
        targetTemper = Integer.parseInt(textView_targetTemper.getText().toString());
        Log.d("[TAG]", "setCarSensorData OK"+" "+fuel+" "+starting+" "+door+" "+temper+" "+aircon);

    }






//    // FCM 발신
//    // 데이터를 Push Message에 넣어서 보내는 send 함수(제어)
//    public void sendFcm(String control, String input) { // String control, String input 으로 변경하기 !
//        System.out.println("phone Send Start...");
//        URL url = null;
//        try {
//            url = new URL("https://fcm.googleapis.com/fcm/send");
//        } catch (MalformedURLException e) {
//            System.out.println("Error while creating Firebase URL | MalformedURLException");
//            e.printStackTrace();
//        }
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) url.openConnection();
//        } catch (IOException e) {
//            System.out.println("Error while createing connection with Firebase URL | IOException");
//            e.printStackTrace();
//        }
//        conn.setUseCaches(false);
//        conn.setDoInput(true);
//        conn.setDoOutput(true);
//        conn.setRequestProperty("Content-Type", "application/json");
//
//        // set my firebase server key
//        conn.setRequestProperty("Authorization", "key="
//// 세미프로젝트 키 + "AAAAK89FyMY:APA91bGxNwkQC6S_QQAKbn3COepWgndhyyjynT8ZvIEarTaGpEfMA1SPFo-ReN8b9uO21R1OfSOpNhfYbQaeohKP_sKzsgVTxu7K5tmzcjEfHzlgXRFrB1r0uqhfxLp4p836lbKw_iaN");
//                + "AAAAeDPCqVw:APA91bH08TNojrp8rdBiVAsIcwTeK5k6ITDZ4q8k5t-FRdEEQiRbFb5I46TAt-0NDg7xQsf9MxTZ7muyKtEeK__IygsotH3G4c4_e--VdDXRub-6H_mL9qetJu7fA-1XR9ip0xG-Q-4i");
//
//        // create notification message into JSON format
//        JSONObject message = new JSONObject();
//        try {
//            message.put("to", "/topics/car");
//            message.put("priority", "high");
//
//            JSONObject notification = new JSONObject();
//            notification.put("title", "HyunDai");
//            notification.put("body", "자동차 상태 변경");
//            message.put("notification", notification);
//
//            JSONObject data = new JSONObject();
//            data.put("control", control); // 이 부분 변경하며 temp, door, power 등 조절
//            data.put("data", input);
//            message.put("data", data);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//            System.out.println("FCM 전송:" + message.toString());
//            out.write(message.toString());
//            out.flush();
//            conn.getInputStream();
//            System.out.println("OK...............");
//
//        } catch (IOException e) {
//            System.out.println("Error while writing outputstream to firebase sending to ManageApp | IOException");
//            e.printStackTrace();
//        }
//
//        System.out.println("phone Send End...");
//    }

    public void getGPS(){
        gpsTracker = new GpsTracker(context);

        lat = gpsTracker.getLatitude();
        lng = gpsTracker.getLongitude();

        String address = getCurrentAddress(lat, lng);
        if(address.contains("동")){
            textView_address.setText(address.substring(5, address.lastIndexOf("동")+1));
        }else if(address.contains("가")){
            textView_address.setText(address.substring(5, address.lastIndexOf("가")+1));
        }

        Toast.makeText(context, "현재위치 \n위도 " + lat + "\n경도 " + lng, Toast.LENGTH_LONG).show();
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(context, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(context, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(context, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    public void getWeather(String nx, String ny) {
        String urlstr = "http://" + ip + "/webServer/weather.mc";
        String xyUrl = "?nx=" + nx + "&ny=" + ny;


        Log.d("[Weather]", urlstr+xyUrl);

        // AsyncTask를 통해 HttpURLConnection 수행.
        httpAsyncTaskWeather = new HttpAsyncTaskWeather();
        httpAsyncTaskWeather.execute(urlstr+xyUrl);

    }


    class HttpAsyncTaskWeather extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
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
            weatherResult = s.trim();

            int tempStartIndex = weatherResult.indexOf(":")+1;
            int tempEndIndex = weatherResult.lastIndexOf("p");
            int ptyIndex = weatherResult.indexOf("y")+2;
            int skyIndex = weatherResult.length()-1;

            temperature = weatherResult.substring(tempStartIndex, tempEndIndex);
            pty = weatherResult.substring(ptyIndex, ptyIndex+1);
            sky = weatherResult.substring(skyIndex);

            Log.d("[Weather]","Temp: "+temperature+" pty: "+pty+" sky"+sky);

            Log.d("[Weather]",temperature);
            Log.d("[Weather]",pty);
            Log.d("[Weather]",sky);
            textView_weatherTemper.setText(temperature);

            if(pty.equals("0")){ //(0) 비 없음  / 맑음(1), 구름많음(3), 흐림(4)
                if(sky.equals("1")){
                    textView_weather.setText("맑음");
                    imageView_weather.setBackground(null);
                    imageView_weather.setImageResource(R.drawable.sun);
                    Log.d("[Weather]","0, 1 Set");
                }else if(sky.equals("3")){
                    textView_weather.setText("구름많음");
                    imageView_weather.setBackground(null);
                    imageView_weather.setImageResource(R.drawable.cloudsun);
                    Log.d("[Weather]","0, 3 Set");
                }else if(sky.equals("4")){
                    textView_weather.setText("흐림");
                    imageView_weather.setBackground(null);
                    imageView_weather.setImageResource(R.drawable.cloud);
                    Log.d("[Weather]","0, 4 Set");
                }
            }else if(pty.equals("1") || pty.equals("5")){ // 비, 빗방울
                textView_weather.setText("비");
                imageView_weather.setBackground(null);
                imageView_weather.setImageResource(R.drawable.umbrella);
                Log.d("[Weather]","1, 5 Set");
            }else if(pty.equals("2") || pty.equals("6")){ // 비, 눈 동반 or 빗방울/눈날림 동반
                textView_weather.setText("비, 눈");
                imageView_weather.setBackground(null);
                imageView_weather.setImageResource(R.drawable.cloudrain);
                Log.d("[Weather]","2, 6 Set");
            }else if(pty.equals("3") || pty.equals("7")){ // 눈, 눈날림
                textView_weather.setText("눈");
                imageView_weather.setBackground(null);
                imageView_weather.setImageResource(R.drawable.snowflake);
                Log.d("[Weather]","3, 7 Set");
            }else if(pty.equals("4")){ // 소나기
                textView_weather.setText("소나기");
                imageView_weather.setBackground(null);
                imageView_weather.setImageResource(R.drawable.cloudsunrain);
                Log.d("[Weather]","4 Set");
            }
        }
    }

}