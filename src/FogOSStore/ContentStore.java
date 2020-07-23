package FogOSStore;

//import android.os.Build;//

//import androidx.annotation.Req//uiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import FogOSContent.Content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;

public class ContentStore {

    private ArrayList contents = new ArrayList();
    private ArrayList fileslist = new ArrayList();


    public ContentStore(String path){
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        readifExist();

        fileExplorer(path);

        writeintoFile();
        //}
    }

    private void readifExist(){
        File f = new File("/sdcard/FogOS/ContentStore/content.json");
        String jsonline = "";
        if(f.exists() && !f.isDirectory()) {
            try {
                FileReader rd = new FileReader("/sdcard/FogOS/ContentStore/content.json");
                BufferedReader bufReader = new BufferedReader(rd);

                jsonline = bufReader.readLine();

                System.out.println(jsonline);

                JSONObject jsonObject = new JSONObject(jsonline);
                JSONArray jsonArr = jsonObject.getJSONArray("filelist");


                for (int i = 0; i < jsonArr.length(); i++) {

                    JSONObject jsonObj = jsonArr.getJSONObject(i);

                    String myname = jsonObj.getString("name");
                    String mypath = jsonObj.getString("path");
                    Boolean myshared = jsonObj.getBoolean("shared");
                    contents.add(new Content(myname,mypath,myshared));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException ee) {
                ee.printStackTrace();
            }

        }
        else
            return;
    }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void writeintoFile() {
        File f = new File("/sdcard/FogOS/ContentStore/content.json");
        Content[] contentlist = getContentList();

        JSONArray outjson2 = new JSONArray();
        JSONObject filelist = new JSONObject();
        try {
            for(int i=0;i<contentlist.length;i++) {

                JSONObject obj = new JSONObject();
                obj.put("name", contentlist[i].getName());
                System.out.println(contentlist[i].getName());
                obj.put("path", contentlist[i].getPath());
                System.out.println(contentlist[i].getPath());
                obj.put("shared", Boolean.toString(contentlist[i].isShared()));
                System.out.println(contentlist[i].isShared());
                outjson2.put(obj);

            }


            filelist.put("filelist",outjson2);
            FileWriter fw = new FileWriter(f);
            fw.write(filelist.toString());
            fw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ee) {
            ee.printStackTrace();
        }

    }
    private void fileExplorer(String path){
        File f = new File(path);
        File[] files = f.listFiles();

        for(int i=0; i<files.length; i++) {
            try {
                File file = files[i];

                if (file.isDirectory()) {
                    fileslist.add(file);
                    contents.add(new Content(file.getName(), file.getPath(), true));
                    fileExplorer(file.getPath());
                }
                else {
                    fileslist.add(file);
                    contents.add(new Content(file.getName(),file.getPath(),true));

                }
            }
            catch(Exception e) {
                e.printStackTrace();
                throw e;

            }
        }

    }

    public Content[] getContentList(){

        Content[] rt = new Content[contents.size()];
        for(int i=0; i<contents.size();i++){
            rt[i] = (Content) contents.get(i);
        }

        return rt;
    }

    public File[] getFileList(){
        File[] returnlist = new File[fileslist.size()];
        for(int i=0; i<fileslist.size();i++){
            returnlist[i] = (File) fileslist.get(i);
        }

        return returnlist;
    }
}
