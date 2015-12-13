package lic.ce.s2t;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.EditText;

/**
 * Created by Guillermo on 13/12/2015.
 */
public class exports extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.exports_activity_instances);
        Button ok = (Button) findViewById(R.id.OkButton);
        Button cancel = (Button) findViewById(R.id.CancelButton);

        ok.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // guardar el archivo
                EditText file_route=(EditText) findViewById(R.id.editText);
                File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        file_route.getText().toString());

                FileOutputStream out=null;
                try{
                    out=new FileOutputStream(file.getAbsolutePath());

                    setContentView(R.layout.activity_instances);
                    ListView List=(ListView) findViewById(R.id.listView);
                    ListAdapter adapter1=List.getAdapter();

                    Intent i=getIntent();
                    String X=i.getStringExtra("text");
                    MessageBox(X);
                    out.write(X.getBytes("UTF-8"));
                    out.close();
                }catch(Exception E){
                    MessageBox(E.getMessage());
                }
                //MessageBox(file.getPath());
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }
    public void MessageBox(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}

