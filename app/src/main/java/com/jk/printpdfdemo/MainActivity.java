package com.jk.printpdfdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int IMAGE_REQUEST_ID = 1337;

    private static final int PDF_REQUEST_ID = 1447;
    private PrintManager mgr = null;
    private File pdfDoc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mgr = (PrintManager) getSystemService(PRINT_SERVICE);

        findViewById(R.id.print_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });
        findViewById(R.id.print_bitmap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .setType("image/*");

                startActivityForResult(i, IMAGE_REQUEST_ID);
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PDF_REQUEST_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void printDialog() {
        PrintAttributes printAttributes = new PrintAttributes
                .Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build();
        print("Test PDF",
                new PdfDocumentAdapter(getApplicationContext(), pdfDoc),
                printAttributes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_ID
                && resultCode == Activity.RESULT_OK) {
            try {
                if (data != null && data.getData() != null) {
                    PrintHelper help = new PrintHelper(this);
                    help.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
                    help.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                    help.printBitmap("Photo!", data.getData());
                } else {
                    Toast.makeText(this, "data null", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                Log.e(getClass().getSimpleName(), "Exception printing bitmap",
                        e);
                Toast.makeText(this, "Error " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PDF_REQUEST_ID
                && resultCode == Activity.RESULT_OK) {

            Uri selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                ParcelFileDescriptor pfd = null;
                try {
                    pfd = getContentResolver().openFileDescriptor(selectedFileUri, "r");
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFoundException " + e.getMessage(), e);
                }

                if (pfd != null) {
                    pdfDoc = ParcelFileDescriptorUtils.convertParcelFileDescriptorToFile(
                            this, pfd, "pdffile.pdf");
                    if (pdfDoc != null) {
                        printDialog();
                    } else {
                        Toast.makeText(this, "pdfDoc null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "pfd null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private PrintJob print(String name, PrintDocumentAdapter adapter,
                           PrintAttributes attrs) {
        startService(new Intent(this, PrintJobMonitorService.class));

        return (mgr.print(name, adapter, attrs));
    }
}