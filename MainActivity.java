package com.example.birdsclassifier;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.birdsclassifier.ml.Birds;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btLoadImage;
    TextView result;
    ImageView img;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> mGetContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.imageView);
        result = findViewById(R.id.textView3);
        btLoadImage = findViewById(R.id.button2);
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Bitmap imagebitmap = null;
                try{
                    imagebitmap = UriToBitmap(result);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                img.setImageBitmap(imagebitmap);
                outputGenerator(imagebitmap);
                Log.d("TAG_URI",""+ result);
            }
        });
        btLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });
    }
    private void outputGenerator(Bitmap imageB)
    {
        try {
            Birds model = Birds.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageB);

            // Runs model inference and gets result.
            Birds.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            int index = 0;
            float max = probability.get(0).getScore();

            for(int i = 0; i < probability.size(); i++)
            {
                if(max < probability.get(i).getScore())
                {
                    max = probability.get(i).getScore();
                    index = i;
                }
            }
            Category output = probability.get(index);
            result.setText(output.getLabel());
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }
    private Bitmap UriToBitmap(Uri result) throws IOException {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
    }
}