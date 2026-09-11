package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class responsible for exporting and importing the Room database file.
 */
public class DatabaseBackupManager {

    private static final String DATABASE_NAME = "task_database";

    /**
     * Exports the local database to the provided target Uri chosen by the user.
     */
    public static boolean exportDatabase(Context context, Uri targetUri) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                return false;
            }

            try (InputStream fis = new FileInputStream(dbFile);
                 OutputStream fos = context.getContentResolver().openOutputStream(targetUri)) {

                if (fos == null) return false;

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restores/imports the database from the selected source Uri over the existing local database.
     */
    public static boolean importDatabase(Context context, Uri uri) {
        try {
            // 1. Закрываем текущую базу данных
            try {
                AppDatabase.getInstance(context).close();
            } catch (Exception ignored) {}

            // 2. Сбрасываем статический инстанс Room
            AppDatabase.clearInstance();

            // 3. Получаем путь к файлу БД
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            File dbJournal = new File(dbFile.getPath() + "-journal");

            // 4. Удаляем старый файл базы и его журнал
            if (dbFile.exists()) dbFile.delete();
            if (dbJournal.exists()) dbJournal.delete();

            // 5. Копируем новый файл из выбранного Uri
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(dbFile)) {

                if (inputStream == null) return false;

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}