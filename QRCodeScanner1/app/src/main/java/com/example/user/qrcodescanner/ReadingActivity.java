package com.example.user.qrcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

public class ReadingActivity extends AppCompatActivity {

    private static final String TAG = "ReadingActivity";

    private static final int TAKE_PICTURE_REQUEST = 1234;
    private final        int GALLERY_REQUEST      = 22131;

    private Button scan_btn;
    private Button exit_btn;

    private String    resultOfScan;
    private ImageView uiResultOfScanIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create called");
        setContentView(R.layout.activity_reading);

        scan_btn = (Button) findViewById(R.id.scan_btn);
        uiResultOfScanIv = (ImageView) findViewById(R.id.result_of_scan);
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanningQrCode(ReadingActivity.this);
            }
        });
        exit_btn = (Button) findViewById(R.id.exit_btn);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.open_gallery).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                startActivity(new Intent(ReadingActivity.this, GalleryActivity.class));
            }
        });
        findViewById(R.id.open_camera).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(getScanFolder(), "camera_photo.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            }
        });
    }

    private void startScanningQrCode(Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Наведите камеру на код");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    private void saveAsQrCodeImageToGallery(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            saveToGallery(bitmap, text);
            uiResultOfScanIv.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void saveToGallery(Bitmap bitmap, String title) {
        File file = getScanFolder();
        if (!file.exists()) {
            file.mkdirs();//if not, create it
        }
        String timeStamp = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        File imageFile = new File(file.getPath() + "/image " + timeStamp + ".jpg");
        writeBitmapToFile(bitmap, imageFile);
    }

    @NonNull public static File getScanFolder() {
        return new File(Environment.getExternalStorageDirectory(), "Scan_Results");
    }

    private void writeBitmapToFile(Bitmap bitmap, File imageFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Отмена сканирования", Toast.LENGTH_LONG).show();
            } else {
                resultOfScan = result.getContents();
                Toast.makeText(this, resultOfScan, Toast.LENGTH_LONG).show();
                saveAsQrCodeImageToGallery(resultOfScan);
                boolean isValidUrl = Patterns.WEB_URL.matcher(resultOfScan).matches();
                if (isValidUrl) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(resultOfScan));
                    startActivity(i);
                } else {
                    startScanningQrCode(this);
                }
            }
        } else {
            if (requestCode == GALLERY_REQUEST) {

            } else if (requestCode == TAKE_PICTURE_REQUEST) {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Фотография сохранена", Toast.LENGTH_SHORT).show();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Отмена съемки", Toast.LENGTH_SHORT).show();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}