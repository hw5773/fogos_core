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
import java.util.ArrayList;

public class ContentStore {

    private ArrayList<Content> contents = new ArrayList<Content>();
    private ArrayList fileslist = new ArrayList();
    private String path;


    public ContentStore(String path){
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        this.path = path;
        readifExist();

        fileExplorerWithClear(path);

        writeintoFile();
        //}
    }

    private void readifExist(){
    	contents.clear();

        File f = new File(path + "/FogOS/ContentStore/content.json");
        String jsonline = "";
        if(f.exists() && !f.isDirectory()) {
            try {
                FileReader rd = new FileReader(path + "/FogOS/ContentStore/content.json");
                BufferedReader bufReader = new BufferedReader(rd);

                jsonline = bufReader.readLine();

                //System.out.println(jsonline);

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
    	
        File f = new File(path+"/FogOS/ContentStore/content.json");
        if(f.exists())
        {
        	f.delete();
        }
        try {
        f.createNewFile();
        } catch (IOException e){
        	e.printStackTrace();
        }
        Content[] contentlist = getContentList();

        JSONArray outjson2 = new JSONArray();
        JSONObject filelist = new JSONObject();
        try {
            for(int i=0;i<contentlist.length;i++) {

                JSONObject obj = new JSONObject();
                obj.put("name", contentlist[i].getName());
                //System.out.println(contentlist[i].getName());
                obj.put("path", contentlist[i].getPath());
                //System.out.println(contentlist[i].getPath());
                obj.put("shared", Boolean.toString(contentlist[i].isShared()));
                //System.out.println(contentlist[i].isShared());
                outjson2.put(obj);

            }

            filelist.put("filelist",outjson2);
            FileWriter fw = new FileWriter(f,false);
            fw.write(filelist.toString());
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ee) {
            ee.printStackTrace();
        }

    }
    private void fileExplorer(String path){

        File f = new File(path);
        File[] files = f.listFiles();

        if (files == null) {
            return;
        }

        for(int i=0; i<files.length; i++) {
            try {
                File file = files[i];

                if (file.isDirectory()) {
                    fileslist.add(file);
                    contents.add(new Content(file.getName(), file.getPath(), false));
                    fileExplorer(file.getPath());
                }
                else {
                    fileslist.add(file);
                    contents.add(new Content(file.getName(),file.getPath(),false));
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                throw e;

            }
        }
        
     
    }

    private void fileExplorerWithClear(String path) {
    	contents.clear();
    	fileslist.clear();
    	
    	fileExplorer(path);
    	
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
    
    public void ContentUpdate() {
    	fileExplorerWithClear(this.path);

        writeintoFile();
    	
    }

    public void add(String name, String path, Boolean shared) {
        contents.add(new Content(name, path, shared));
        writeintoFile();
    }

    public void add(Content content) {
        contents.add(content);
        writeintoFile();
    }

    public void remove(String name) {
        int removeIdx = -1;
        for (int i = 0; i < contents.size(); i++) {
            if (name.equals(contents.get(i).getName())) {
                removeIdx = i;
            }
        }

        if (removeIdx != -1) {
            contents.remove(removeIdx);
        }

        writeintoFile();
    }

    public Content get(String name) {
        int idx = -1;
        for (int i = 0; i < contents.size(); i++) {
            if (name.equals(contents.get(i).getName())) {
                idx = i;
            }
        }
        return contents.get(idx);
    }

    public void remove(Content content) {
        contents.remove(content);
    }

}
