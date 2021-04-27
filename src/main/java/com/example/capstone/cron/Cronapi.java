package com.example.capstone.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.logging.*;
import org.apache.commons.io.FileUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static javax.swing.text.StyleConstants.Size;
//    @Scheduled(cron="0 4/10 10-22 * * SAT-TUE" )    @Scheduled(cron="0/10 * * * * *" )


@Component
public class Cronapi {
    private final Logger LOGGER = LoggerFactory.getLogger(Job.class.getName());
//    String[] setY = {"33.900000", "34.400000", "34.900000", "35.400000", "35.900000", "36.610000", "37.110000",
//            "37.610000", "38.110000", "38.610000"};
    String[] setY = {"33.900000",  "35.110000", "35.610000", "36.110000", "37.110000", "38.110000", "38.610000"};
    int i = 0;

    boolean flag = false;

    public class Downloader extends Thread {

        public void run() {

            Job test = new Job();

            System.out.println("setY" + i);
            try {
                test.task(setY[i], setY[i + 1]);
                flag = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron="0 4/10 10-22 * * *")
    public void trigger() throws IOException {
        String CronapiStart = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss").format(new Date());
        LOGGER.info("Cronapi start " + CronapiStart);
        int count = 0;
        i = 0;
        for (i = 0; i < 6; i++) {
            Downloader dl = new Downloader();
            dl.start();
            System.out.println("setD" + i);

            for (count= 0;count < 150;count++) {
                try {
                    Thread.sleep(100);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (flag == true) {
                    flag = false;
                    break;
                }
            }
            dl.stop();

        }
        String CronapiEnd = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss").format(new Date());
        LOGGER.info("Cronapi end " + CronapiEnd);

    }

    class Job {
        private final Logger LOGGER = LoggerFactory.getLogger(Job.class.getName());

        public void task(String minY, String maxY) throws IOException {

            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm'.csv'").format(new Date());




            StringBuilder urlBuilder = new StringBuilder("http://openapi.its.go.kr:9080/trafficInfo"); /* URL */
            urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("011fe544e26e43768c9d22244062a537", "UTF-8")); /* 공개키 */
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); /* 도로유형 */
            urlBuilder.append("&" + URLEncoder.encode("routeNo", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); /* 노선번호 */
            urlBuilder.append("&" + URLEncoder.encode("drcType", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); /* 도로방향 */
            urlBuilder.append("&" + URLEncoder.encode("minX", "UTF-8") + "=" + URLEncoder.encode("124.800000", "UTF-8")); /* 최소경도영역 */
            urlBuilder.append("&" + URLEncoder.encode("maxX", "UTF-8") + "=" + URLEncoder.encode("131.870000", "UTF-8")); /* 최대경도영역 */
            urlBuilder.append("&" + URLEncoder.encode("minY", "UTF-8") + "=" + URLEncoder.encode(minY, "UTF-8")); /* 최소위도영역 */
            urlBuilder.append("&" + URLEncoder.encode("maxY", "UTF-8") + "=" + URLEncoder.encode(maxY, "UTF-8")); /* 최대위도영역 */
            urlBuilder.append("&" + URLEncoder.encode("getType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /* 출력타입 */
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = null;
            conn =(HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println("1");

            conn.setRequestProperty("Content-type", "JSON;charset=UTF-8");
            System.out.println("Response code: " + conn.getResponseCode());
            System.out.println("2");

            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"), 131071);
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"), 131071);
            }
            System.out.println("3");

            StringBuilder sb = new StringBuilder();
            String line;

            System.out.println("4");
//                System.out.println("1"+rd.readLine());
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("5");

            rd.close();
            conn.disconnect();
//             System.out.println("1"+sb.toString());
//        System.out.println(rd.sizeof());
            JSONObject output;
            try {
                output = new JSONObject(sb.toString());
                // System.out.println(output.toString());
                System.out.println("str2");
                JSONObject doc = output.getJSONObject("body");
                JSONArray docs = doc.getJSONArray("items");
//                File file = new File("/root/task/S" + minY + "T" + fileName);
                File file = new File("C:\\Users\\Public\\2\\S" + minY + "T" + fileName);
                String csv = CDL.toString(docs);
                LOGGER.info("true");

                String fileEnd = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss").format(new Date());
                LOGGER.info("DL end " + fileEnd);
                FileUtils.writeStringToFile(file, csv);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("end1");

        }
    }
}