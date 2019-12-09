package com.cs125.memefinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int SELECT_ORIGINAL_PIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtils.verifyStoragePermissions(this);
        Button importButton = findViewById(R.id.importButton);
        importButton.setOnClickListener(v -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);//Pick an item fromthe data
            intent.setType("image/*");//从所有图片中进行选择
            startActivityForResult(intent, SELECT_ORIGINAL_PIC);

        });

        PicManage.init(this);
        updateView(PicManage.entryList);

        Button searchButton = findViewById(R.id.searchButton);
        EditText searchBox = findViewById(R.id.searchText);
        searchButton.setOnClickListener(v -> {
            String toSearch = searchBox.getText().toString();
            if (toSearch.length() == 0) {
                updateView(PicManage.entryList);
            } else {
                List<PicEntry> resultList = PicManage.search(toSearch);
                updateView(resultList);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            try {
                Uri imageURI = data.getData();
                String text = "";
                PicManage.addPic(imageURI, text);
                /*String filename = counter + ".jpg";
                FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                thePic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateView(PicManage.entryList);
        }
    }
    public void inflate(PicEntry entry) {
        LinearLayout picLayout = findViewById(R.id.Inflatee);
        View chunkInflate = getLayoutInflater().inflate(R.layout.chunk_inflate, picLayout, false);
        ImageView theImageView = chunkInflate.findViewById(R.id.imageView);
        EditText theTextView = chunkInflate.findViewById(R.id.editText);
        theTextView.setText(entry.fileText);
        Button shareButton = chunkInflate.findViewById(R.id.shareButton);
        Button removeButton = chunkInflate.findViewById(R.id.removeButton);
        Button saveButton = chunkInflate.findViewById(R.id.saveButton);
        theImageView.setImageURI(entry.fileUri);

        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, entry.fileUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Send Picture to"));
        });

        removeButton.setOnClickListener(v -> {
            PicManage.removePic(entry);
            updateView(PicManage.entryList);
        });

        saveButton.setOnClickListener(v -> {
            PicManage.editText(entry ,theTextView.getText().toString());
        });

        picLayout.addView(chunkInflate);
    }
    public void updateView(List<PicEntry> toUpdate) {
        LinearLayout picLayout = findViewById(R.id.Inflatee);
        picLayout.removeAllViews();
        List<PicEntry> temp = new LinkedList<>();
        temp.addAll(toUpdate);
        Collections.reverse(temp);
        for (PicEntry entry : temp) {
            inflate(entry);
        }
    }
}
