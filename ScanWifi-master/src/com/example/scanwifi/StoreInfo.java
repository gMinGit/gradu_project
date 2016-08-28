package com.example.scanwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Xml;

public class StoreInfo {
    private List<List<ScanResult>> m_allResult;
    private int m_frequency;
    private long m_time;
    private Calendar m_startTime;
    private String m_position;

    private long m_index;

    private String m_scenery;

    private XmlSerializer m_serializer = Xml.newSerializer();

    private Map<String, String> m_bssid_ssid;// ���mac��ַ��AP����
    private Map<String, List<String>> m_bssid_rssi;// ���mac��ַ�����AP���յ�������RSSIֵ������list��

    String tag = "store";

    public StoreInfo(int fren, long time, Calendar start, String position,
            String scenery) throws IllegalArgumentException,
            IllegalStateException {
        m_allResult = new ArrayList<List<ScanResult>>();
        m_frequency = fren;
        m_time = time;
        m_startTime = start;
        m_position = position;

        m_scenery = scenery;

        m_index = 0;
        m_bssid_ssid = new HashMap<String, String>();
        m_bssid_rssi = new HashMap<String, List<String>>();

    }

    public void addResultToList(List<ScanResult> l) {
        m_index++;
        m_allResult.add(l);
        getApAllRSSI();
    }

    public List<ScanResult> getListWithIndex(int i) {
        return m_allResult.get(i);
    }

    public int getListLen() {
        return m_allResult.size();
    }

    /**
     * 
     * @return the data collected
     * @throws JSONException
     */
    public JSONObject getJSONData() throws JSONException {
        JSONObject json = new JSONObject();
        try {
            json.put("startTime", WriteFile.tranTimeToString(m_startTime));
            json.put("duringTime", m_time);
            json.put("freq", m_frequency);
            json.put("location", m_position);
            json.put("scenery", m_scenery);
            Iterator<String> iter = m_bssid_ssid.keySet().iterator();
            JSONArray rssiJson = new JSONArray();
            while (iter.hasNext()) {
                String sBSSID = (String) iter.next();
                String sSSID = m_bssid_ssid.get(sBSSID);
                JSONObject jsontemp = new JSONObject();
                jsontemp.put("apName", sSSID);
                jsontemp.put("mac", sBSSID);
                JSONArray jsonarraytemp = new JSONArray();
                List<String> thisApRssi = m_bssid_rssi.get(sBSSID);
                Iterator<String> iRssi = thisApRssi.iterator();
                while (iRssi.hasNext()) {
                    jsonarraytemp.put(Integer.valueOf(iRssi.next()));
                }
                jsontemp.put("RSSI", jsonarraytemp);
                rssiJson.put(jsontemp);
            }
            json.put("RSSILists", rssiJson);

        } catch (JSONException e) {
            // TODO: handle exception
            Log.e("StoreInfo#getJSONData()", e.getMessage(), e);
        }
        m_bssid_ssid.clear();
        m_bssid_rssi.clear();
        return json;
    }

    public void writeXml() throws IOException {
        File v_xmlfile = new File(Environment.getExternalStorageDirectory()
                .getPath()
                + "SignalStrength/"
                + m_position
                + "_"
                + m_startTime.getTime().toString() + ".xml");
        boolean isExists = v_xmlfile.exists();
        if (!isExists) {
            v_xmlfile.createNewFile();
        } else {
            v_xmlfile.delete();
        }
        Log.v(tag, "json");

        OutputStream v_os = new FileOutputStream(v_xmlfile, true);

        m_serializer.setOutput(v_os, "UTF-8");
        m_serializer.startDocument("UTF-8", true);

        m_serializer.startTag(null, "scan");
        // =====д��ʼʱ�䣬Ƶ�ʣ�����ʱ�䣬����λ��==============
        m_serializer.startTag(null, "startTime");
        m_serializer.text(WriteFile.tranTimeToString(m_startTime));
        m_serializer.endTag(null, "startTime");

        m_serializer.startTag(null, "frequency");
        m_serializer.text(String.valueOf(m_frequency));
        m_serializer.endTag(null, "frequency");

        m_serializer.startTag(null, "duration");
        m_serializer.text(String.valueOf(m_time));
        m_serializer.endTag(null, "duration");

        m_serializer.startTag(null, "position");
        m_serializer.text(m_position);
        m_serializer.endTag(null, "position");

        m_serializer.startTag(null, "allapinfo");
        // ==========дÿ��RSSI��ֵ
        getApAllRSSI();
        Iterator<String> iBssid = m_bssid_ssid.keySet().iterator();
        Log.v(tag, "rssi");
        while (iBssid.hasNext()) {
            String bssid = iBssid.next();

            m_serializer.startTag(null, "ap");
            // ============дAP����========================
            m_serializer.startTag(null, "ssid");
            m_serializer.text(m_bssid_ssid.get(bssid));
            m_serializer.endTag(null, "ssid");

            // ============дAP mac��ַ========================
            m_serializer.startTag(null, "bssid");
            m_serializer.text(bssid);
            m_serializer.endTag(null, "bssid");

            Log.v(tag, "rssi");
            // ============д���AP���յ�������rssiֵ========================
            m_serializer.startTag(null, "rssi");
            List<String> thisApRssi = m_bssid_rssi.get(bssid);
            Iterator<String> iRssi = thisApRssi.iterator();
            while (iRssi.hasNext()) {
                m_serializer.text(iRssi.next() + "\r\n");
            }
            m_serializer.endTag(null, "rssi");

            m_serializer.endTag(null, "ap");
            Log.v(tag, "rssi");
        }
        m_serializer.endTag(null, "allapinfo");
        m_serializer.endTag(null, "scan");
        m_serializer.text("\r\n");
        m_serializer.endDocument();

        v_os.flush();
        v_os.close();
        // }
    }

    // ==========��ÿ��AP��RSSI��list����������AP��mac��ַ����map========
    private void getApAllRSSI() {
        // m_bssid_ssid.clear();
        // m_bssid_rssi.clear();

        Iterator<List<ScanResult>> iList = m_allResult.iterator();

        // ���α�����ͳ�Ʋ���¼�����г��ֵ�AP�����ƺ�MAC��ַ��
        while (iList.hasNext()) {
            List<ScanResult> nextList = iList.next();

            Iterator<ScanResult> iscan = nextList.iterator();

            while (iscan.hasNext()) {
                ScanResult nextScan = iscan.next();

                boolean isHas = m_bssid_ssid.containsKey(nextScan.BSSID);
                if (isHas == false) {// �����������AP������
                    List<String> oneApRssi = new ArrayList<String>();
                    m_bssid_ssid.put(nextScan.BSSID, nextScan.SSID);

                    // oneApRssi.add(String.valueOf(nextScan.level));
                    m_bssid_rssi.put(nextScan.BSSID, oneApRssi);
                    // } else {// ���AP�Ѿ����ֹ���
                    // List<String> thisApRssi =
                    // m_bssid_rssi.get(nextScan.BSSID);
                    // // thisApRssi.add(String.valueOf(nextScan.level));
                }
            }
        }

        // ��һ�εı����ǽ�RSSI��Ϣ���䣬֮����Ҫ��2�α�����������Ϊ��Щ�ε�ɨ�裬��û��ɨ�赽��ЩAP����Ϣ����������������Ŀ����ͬ�����RSSI�б��ƫ�����û�����ƶϡ�
        // ���Ա��α�������Ҫ�ڱ���ÿ��ɨ����ʱ����ɨ�赽��RSSI��Ϣ���䣬��Ҫ��δ��ɨ�赽��AP��ͬ��λ����0�Ա�ʾ��ǰɨ�����ʱδɨ�赽��AP����Ϣ��
        iList = m_allResult.iterator();
        while (iList.hasNext()) {
            List<ScanResult> nextList = iList.next();
            Iterator<ScanResult> iscan = nextList.iterator();

            while (iscan.hasNext()) {
                ScanResult nextScan = iscan.next();
                List<String> thisApRssi = m_bssid_rssi.get(nextScan.BSSID);
                thisApRssi.add(String.valueOf(nextScan.level));
            }

            Iterator<Entry<String, List<String>>> onceResult = m_bssid_rssi
                    .entrySet().iterator();
            while (onceResult.hasNext()) {
                Map.Entry<String, List<String>> entry = (Entry<String, List<String>>) onceResult
                        .next();
                if (entry.getValue().size() == m_index - 1) {
                    entry.getValue().add("0");
                } else if (entry.getValue().size() < m_index - 1) {
                    int i;
                    long len = m_index - entry.getValue().size();
                    Log.v("Ap times",
                            "Ap times not correct. " + String.valueOf(m_index)
                                    + " "
                                    + String.valueOf(entry.getValue().size()));
                    for (i = 0; i < len; i++) {
                        entry.getValue().add(0, "0");
                    }
                    if (entry.getValue().size() != m_index) {
                        Log.w("Ap times",
                                "Ap times not correct. "
                                        + String.valueOf(m_index)
                                        + " "
                                        + String.valueOf(entry.getValue()
                                                .size()));
                    }
                    Log.v(tag, "RSSIList Array Fill " + String.valueOf(i)
                            + " times ");
                    i = 0;
                } else if (entry.getValue().size() != m_index) {
                    Log.w("index", String.valueOf(m_index));
                    Log.w(tag + " AP times",
                            String.valueOf(entry.getValue().size()));
                    Log.w(tag, "RSSIList Array Size not correct!");
                }
            }
        }
        m_allResult.clear();
    }

    public String writeJSON() throws IOException {
        // TODO Auto-generated method stub
        String outpath = "";
        try {
            JSONObject jsonObject = getJSONData();
            if (jsonObject.getJSONArray("RSSILists").length() == 0) {
                Log.e("StoreInfo#writeJSON()",
                        "jsonObject entity RSSILists empty!");
            }
            // ����ֻ���ú�����Ϊ�ָ�������ÿո񣬻��ڹ����ļ���ʱʹ�ļ����пո񣬻���������⡣�����ļ����в�����ð�š�
            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd-HHmmss", Locale.US);
            String date = sDateFormat.format(new java.util.Date());
            File floder = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/SignalStrength/");
            if (!(floder.exists()) && !(floder.isDirectory())) {
                floder.mkdirs();
            }
            File v_jsonFile = new File(Environment
                    .getExternalStorageDirectory().getPath()
                    + "/SignalStrength/"
                    + jsonObject.getString("scenery")
                    + "_"
                    + jsonObject.getString("location")
                    + "_"
                    + date
                    + ".json");
            boolean isExists = v_jsonFile.exists();
            if (!isExists) {
                v_jsonFile.createNewFile();
            } else {
                v_jsonFile.delete();
            }
            Log.v(tag, "json file start writing.");
//            FileWriter ft = new FileWriter(v_jsonFile);
            OutputStreamWriter ot = new OutputStreamWriter(new FileOutputStream(v_jsonFile), "utf-8");
            JsonWriter js = new JsonWriter(ot);
            js.beginObject();
            js.name("startTime").value(jsonObject.getString("startTime"));
            js.name("duringTime").value(jsonObject.getString("duringTime"));
            js.name("scenery").value(jsonObject.getString("scenery"));
            js.name("location").value(jsonObject.getString("location"));
            js.name("freq").value(jsonObject.getString("freq"));

            js.name("RSSILists");
            js.beginArray();

            JSONArray lists = jsonObject.getJSONArray("RSSILists");
            for (int i = 0; i < lists.length(); i++) {
                js.beginObject();

                js.name("apName").value(
                        lists.getJSONObject(i).getString("apName"));
                js.name("mac").value(lists.getJSONObject(i).getString("mac"));
                js.name("RSSI");
                js.beginArray();
                JSONArray list = lists.getJSONObject(i).getJSONArray("RSSI");
                int len = list.length();
                for (int j = 0; j < len; j++) {
                    js.value(Integer.valueOf(list.getString(j)));
                }
                js.endArray();
                js.endObject();
            }
            js.endArray();

            js.endObject();

            outpath = v_jsonFile.getAbsolutePath();

            Log.v("writeJsonFileDone", outpath);

            ot.close();

            js.close();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("JSONException", e.getMessage(), e);
            outpath = "";
        } catch (IOException e) {
            // TODO: handle exception
            Log.e("IOException", e.getMessage(), e);
            outpath = "";
        } catch (NullPointerException e) {
            // TODO: handle exception
            Log.e("NullPointerException", e.getMessage(), e);
            outpath = "";
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Exception", e.getMessage(), e);
            outpath = "";
        }
        return outpath;
    }
}
