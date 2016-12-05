package com.caihaozhong.importer;

import android.util.Log;

import com.momchil_atanasov.data.front.parser.IOBJParser;
import com.momchil_atanasov.data.front.parser.OBJModel;
import com.momchil_atanasov.data.front.parser.OBJParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by CaiHaozhong on 2016/11/29.
 */

public class LocalFileImporter extends AsyncObjectImporter {

    public static final String TAG = "LocalFileImporter";

    private String mFileName;

    private OBJModel mModel;

    public LocalFileImporter(){
        super();
    }

    public LocalFileImporter(String fileName){
        super();
        mFileName = fileName;
    }
    public void setFileName(String fileName){
        mFileName = fileName;
    }

    public OBJModel getModel(){
        return mModel;
    }

    protected void importData(){
        try{
            InputStream inputStream = new FileInputStream(mFileName);
            IOBJParser objParser = new OBJParser();
            mModel = objParser.parse(inputStream);
        }catch (FileNotFoundException e){
            Log.e(TAG, "FileNotFoundException");
        }catch (IOException e){
            Log.e(TAG, "IOException");
        }
    }
}
