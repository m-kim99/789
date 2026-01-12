package helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MediaHelper {
    public static String getFileTypeFromGallary(Context context, Uri uri) {
        String[] columns = {MediaStore.Images.Media.MIME_TYPE};
        Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);
        cursor.moveToFirst();
        String mimeType = cursor.getString(cursor.getColumnIndex(columns[0]));
        cursor.close();
        return mimeType;
    }

    public static Bitmap resizeBitmap(Context context, Uri uri, double ratio) {
        InputStream input = null;

        try {
            input = context.getContentResolver().openInputStream(uri);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        options.inDither = true;// optional
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional

        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);

        try {
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0)
            return 1;
        else
            return k;
    }

    public static void saveBitmap(Bitmap bitmap, File outFile) {
        OutputStream stream = null;

        try {
            stream = new FileOutputStream(outFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        try {
            stream.flush();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringFromBitmap(Bitmap bitmapPicture) {
        /*
         * This functions converts Bitmap picture to a string which can be
         * JSONified.
         * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public static Bitmap getBitmapFromString(String stringPicture) {
        /*
         * This Function converts the String back to Bitmap
         * */
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}
