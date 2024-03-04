package com.jk.printpdfdemo;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ParcelFileDescriptorUtils {

    public static File convertParcelFileDescriptorToFile(
            Context context, ParcelFileDescriptor pfd, String fileName) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            // Create a temporary file
            File tempFile = new File(context.getCacheDir(), fileName);

            // Create InputStream from ParcelFileDescriptor
            fileInputStream = new FileInputStream(pfd.getFileDescriptor());

            // Create OutputStream to the temporary file
            fileOutputStream = new FileOutputStream(tempFile);

            // Copy data from InputStream to OutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
