package be.greifmatthias.htf.Helpers;

import android.content.Context;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DataHelper {
    private static DataHelper INSTANCE;

    private Context _context;

    public static DataHelper getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new DataHelper(context);
        }

        return INSTANCE;
    }

    public static DataHelper getInstance(){
        return INSTANCE;
    }

    private DataHelper(Context context){
        this._context = context;
    }

    private File getFilesDir(){
        return this._context.getFilesDir();
    }

    public void write(String filename, List<String> data){
//        Create file
        File f = new File(getFilesDir(), filename);

//        Prepare data
        StringBuilder sb = new StringBuilder();
        for(String d : data){
            sb.append(d);
            sb.append("\n\r");
        }

        try {
            FileOutputStream os = _context.openFileOutput(filename, Context.MODE_PRIVATE);
            os.write(sb.toString().getBytes());

            os.close();
        } catch (FileNotFoundException e) { } catch (IOException e) { }
    }

    public List<String> read(String filename){
        List<String> output = new ArrayList<>();

        try {
//            Open file
            FileInputStream is = _context.openFileInput(filename);

//            Read file
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null){
                if(!line.isEmpty()) {
                    output.add(line);
                }
            }

            is.close();
        } catch (FileNotFoundException e) { } catch (IOException e) { }
        finally {
            return output;
        }
    }

    public static String getFromURL(String url) throws IOException, JSONException {
        URL targeturl = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(targeturl.openStream()));

        String inputLine;
        String output = "";

        while ((inputLine = in.readLine()) != null) {
            output += inputLine;
        }
        in.close();

        return output;
    }
}