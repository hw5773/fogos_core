package FogOSStore;

//import android.os.Build;//

//import androidx.annotation.Req//uiresApi;
import FogOSContent.Content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class ContentStore {

    private ArrayList<Content> contents = new ArrayList<Content>();
    private ArrayList fileslist = new ArrayList();
    private String path;


    public ContentStore(String path) throws NoSuchAlgorithmException, IOException {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        this.path = path;
  
        readifExist();

        fileExplorerWithClear(path);

        writeintoFile();
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

//                System.out.println(jsonline);

                JSONObject jsonObject = new JSONObject(jsonline);
                JSONArray jsonArr = jsonObject.getJSONArray("filelist");


                for (int i = 0; i < jsonArr.length(); i++) {

                    JSONObject jsonObj = jsonArr.getJSONObject(i);

                    String myname = jsonObj.getString("name");
                    String mypath = jsonObj.getString("path");
                    Boolean myshared = jsonObj.getBoolean("shared");
                    String myhash = jsonObj.getString("hash");

                    contents.add(new Content(myname,mypath,myshared, myhash));

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
                obj.put("path", contentlist[i].getPath());
                obj.put("shared", Boolean.toString(contentlist[i].isShared()));
                obj.put("hash", contentlist[i].getHash());
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
    private void fileExplorer(String path) throws NoSuchAlgorithmException, IOException {

        File f = new File(path);
        File[] files = f.listFiles();

        if (files == null) {
            return;
        }

        for(int i=0; i<files.length; i++) {
            try {
                File file = files[i];

                if (file.isDirectory()) {
//                    fileslist.add(file);
//                    contents.add(new Content(file.getName(), file.getPath(), false,getHash(file.getAbsolutePath())));
                    fileExplorer(file.getPath());
                }
                else {
                	if(!jsonlistchecker(file)) {
	                    fileslist.add(file);
	                    contents.add(new Content(file.getName(), file.getPath(), false, getHash(file.getAbsolutePath())));
                	}
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                throw e;

            }
        }
        
     
    }
    private Boolean jsonlistchecker(File file) {
    	
    	
    	for(int i=0;i < contents.size();i++)
    	{
       		String str1 = contents.get(i).getPath().replace("\\","");
    		String str2 = file.getPath().replace("\\", "");
    		
    		if (str1.equals(str2))
    		{  			
    			return true;
    		}
    	}
		return false;
    	
    }
    private void fileExplorerWithClear(String path) throws NoSuchAlgorithmException, IOException {
//    	contents.clear();
//    	fileslist.clear();
//    	
    	fileExplorer(path);
    	
    }
    
    private String getHash(String path) throws NoSuchAlgorithmException, IOException {
    	MessageDigest md;
    	String sha1 = "";
    	byte[] content;
        byte[] digest;

    	md = MessageDigest.getInstance("SHA-1");
    	content = Files.readAllBytes(Paths.get("D:\\tmp\\pub.pem"));
    	digest = md.digest(content);

    	return Base64.getEncoder().encodeToString(digest);
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
    
    public void ContentUpdate() throws IOException, NoSuchAlgorithmException {
    	fileExplorerWithClear(this.path);

        writeintoFile();
    }

    public void add(Content content) {
        writeintoFile();
        contents.add(content);
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

    public Content get(String name, String path) {
        int idx = -1;
        for (int i = 0; i < contents.size(); i++) {
            if (name.equals(contents.get(i).getName())) {
                if (path.equals(contents.get(i).getPath()))
                    idx = i;
            }
        }
        if (idx == -1) {
            System.out.println("No Content: " + name + ", "  + path);
        }
        return contents.get(idx);
    }

    public void remove(Content content) {
        contents.remove(content);
    }

}