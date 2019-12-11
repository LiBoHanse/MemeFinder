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

import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    public static final int SELECT_ORIGINAL_PIC = 1;

    public static List<PicEntry> theList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtils.verifyStoragePermissions(this);
        Button importButton = findViewById(R.id.importButton);
        importButton.setOnClickListener(v -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);//Pick an item from the data
            intent.setType("image/*");//specify type
            startActivityForResult(intent, SELECT_ORIGINAL_PIC);

        });

        PicManage.init(this);

        Button searchButton = findViewById(R.id.searchButton);
        EditText searchBox = findViewById(R.id.searchText);
        searchButton.setOnClickListener(v -> {
            String toSearch = searchBox.getText().toString();
            if (toSearch.length() == 0) {
                theList = PicManage.entryList;
                updateView(theList);
            } else {
                theList = PicManage.search(toSearch);
                updateView(theList);
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle image being sent
            }
        }

        theList = PicManage.entryList;
        updateView(theList);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            try {
                Uri imageURI = data.getData();
                EditText searchText = findViewById(R.id.searchText);
                String text = searchText.getText().toString();
                PicManage.addPic(imageURI, text);
            } catch (Exception e) {
                e.printStackTrace();
            }
            theList = PicManage.entryList;
            updateView(theList);
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
        LinearLayout buttonGroup = chunkInflate.findViewById(R.id.buttonGroup);
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
            theList.remove(entry);
            updateView(theList);
        });

        saveButton.setOnClickListener(v -> {
            PicManage.editText(entry ,theTextView.getText().toString());
        });

        buttonGroup.setVisibility(View.INVISIBLE);
        theTextView.setEnabled(false);

        theImageView.setOnClickListener(v -> {
            switch(buttonGroup.getVisibility()) {
                case View.VISIBLE:
                    buttonGroup.setVisibility(View.INVISIBLE);
                    PicManage.editText(entry ,theTextView.getText().toString());
                    theTextView.setEnabled(false);
                    break;
                case View.INVISIBLE:
                    buttonGroup.setVisibility(View.VISIBLE);
                    theTextView.setEnabled(true);
                    break;
            }
        });

        picLayout.addView(chunkInflate);
    }
    public void updateView(List<PicEntry> toUpdate) {
        LinearLayout picLayout = findViewById(R.id.Inflatee);
        ListIterator<PicEntry> iterator = toUpdate.listIterator(toUpdate.size());
        picLayout.removeAllViews();
        while (iterator.hasPrevious()) {
            inflate(iterator.previous());
        }
    }

    public void handleSendImage(Intent intent) {
        Uri imageURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            String text = "";
            PicManage.addPic(imageURI, text);
        }
    }
}
