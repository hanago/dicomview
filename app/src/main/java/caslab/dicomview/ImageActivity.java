package caslab.dicomview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ImageActivity extends AppCompatActivity {

    private final String LOG_TAG = SearchDICOMFragment.class.getSimpleName();

    ImageView mImageView;
    PhotoViewAttacher mAttacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);


        Intent mitent = getIntent();
        String id = mitent.getStringExtra("ID");

        mImageView = (ImageView) findViewById(R.id.myImage);

        try {
            // Any implementation of ImageView can be used!
            //mImageView.setImageDrawable(grabImageFromUrl("https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xat1/v/t1.0-9/s720x720/12313746_947420085305555_8553525612570417544_n.jpg?oh=8d64882f5e6cd4b71e492f22daf682ee&oe=57199272"));

            // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
            mAttacher = new PhotoViewAttacher(mImageView);
            Log.d(LOG_TAG, "id="+id);
            new BitmapDownloaderTask().execute(id);


        } catch (Exception e) {
            Log.i(LOG_TAG, e.toString());
        }
    }


    private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

        private final String LOG_TAG = SearchDICOMFragment.class.getSimpleName();

        private static final String PACS_BASE_URL =
                "http://140.116.102.47:8042/";

        @Override
        protected Bitmap doInBackground(String... params) {
            Uri.Builder buildUri = Uri.parse(PACS_BASE_URL).buildUpon();
            buildUri.appendPath("instances")
                    .appendPath(params[0])
                    .appendPath("preview");

            Log.d(LOG_TAG, buildUri.toString());
            return getBitmapFromURL(buildUri.toString());
        }

        private  Bitmap getBitmapFromURL(String imageUrl){
            try{
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            }
            catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
            super.onPostExecute(result);
        }
    }

}
