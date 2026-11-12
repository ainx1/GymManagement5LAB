package com.example.gymmanagement

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.*
// S4 - CSV файл в App-specific storage
// S5 - CSV файл в Shared storage
// S10 - Бинарный файл в App-specific storage
class GymStorageManager(private val context: Context) {

    // S4: CSV-файл во внутреннем хранилище (App-specific storage)
    // /data/user/0/com.example.gymmanagement/files/members.csv
    private val appSpecificCsvFile = File(context.filesDir, "members.csv")

    // (Update / Write)
    fun saveMembersToInternalCsv(members: List<GymMember>) {
        val writer = appSpecificCsvFile.bufferedWriter()
        writer.use { out ->
            // Заголовок CSV
            out.write("fullName;age;phone;durationMonths;category;discountPercent;totalPrice;trainer;branch\n")
            for (m in members) {
                out.write("${m.fullName};${m.age};${m.phone};${m.durationMonths};${m.category};${m.discountPercent};${m.totalPrice};${m.trainer};${m.branch}\n")
            }
        }
    }
        //    val writer = appSpecificCsvFile.bufferedWriter()
        //    try {
        //        writer.write("fullName;age;...\n")
        //    } finally {
        //        writer.close()
        //    }

    // Чтение (Read)
    fun readMembersFromInternalCsv(): MutableList<GymMember> {
        val list = mutableListOf<GymMember>()
        if (!appSpecificCsvFile.exists()) return list

        appSpecificCsvFile.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line -> // Пропускаем заголовок
                val parts = line.split(";")
                if (parts.size >= 9) {
                    list.add(
                        GymMember(
                            fullName = parts[0],
                            age = parts[1].toIntOrNull() ?: 18,
                            phone = parts[2],
                            durationMonths = parts[3].toIntOrNull() ?: 1,
                            category = parts[4],
                            discountPercent = parts[5].toIntOrNull() ?: 0,
                            totalPrice = parts[6].toDoubleOrNull() ?: 0.0,
                            trainer = parts[7],
                            branch = parts[8]
                        )
                    )
                }
            }
        }
        return list
    }

    // Удаление файла (Delete)
    fun deleteInternalCsv(): Boolean {
        return if (appSpecificCsvFile.exists()) appSpecificCsvFile.delete() else false
    }

    fun hasInternalCsv(): Boolean = appSpecificCsvFile.exists()


    // S10: Бинарный файл во внутреннем хранилище (App-specific storage)
    // Файл: /data/user/0/com.example.gymmanagement/files/gym_backup.bin

    private val binaryBackupFile = File(context.filesDir, "gym_backup.bin")

    // Запись бинарного файла (Write / Backup)
    fun saveBinaryBackup(members: List<GymMember>): Boolean {
        return try {
            ObjectOutputStream(FileOutputStream(binaryBackupFile)).use { oos ->
                oos.writeObject(ArrayList(members))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Чтение бинарного файла (Read / Restore)
    @Suppress("UNCHECKED_CAST")
    fun readBinaryBackup(): MutableList<GymMember>? {
        if (!binaryBackupFile.exists()) return null
        return try {
            ObjectInputStream(FileInputStream(binaryBackupFile)).use { ois ->
                ois.readObject() as? ArrayList<GymMember>
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Удаление бинарного файла (Delete)
    fun deleteBinaryBackup(): Boolean {
        return if (binaryBackupFile.exists()) binaryBackupFile.delete() else false
    }

    // S5: CSV-файл в общем хранилище (Shared storage -> папка Downloads)
    private val sharedFileName = "gym_shared_export.csv"

    // 1. Запись (Экспорт)
    fun exportToSharedCsv(members: List<GymMember>): Boolean {
        val csvContent = buildString {
            append("ФИО;Возраст;Телефон;Срок(мес);Категория;Скидка(%);Цена(руб);Тренер;Филиал\n")
            for (m in members) {
                append("${m.fullName};${m.age};${m.phone};${m.durationMonths};${m.category};${m.discountPercent};${m.totalPrice};${m.trainer};${m.branch}\n")
            }
        }
        return try {
            val resolver = context.contentResolver // доступ к общим данным только через резолвер
            deleteSharedCsv() // чтоб не было дубликата

            val contentValues = ContentValues().apply { //Ключ — Значение
                put(MediaStore.MediaColumns.DISPLAY_NAME, sharedFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. Чтение (Импорт)
    fun readFromSharedCsv(): MutableList<GymMember>? {
        return try {
            val lines = mutableListOf<String>()
            val resolver = context.contentResolver
            val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME)
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(sharedFileName)

            resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    val uri = android.content.ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        id
                    )
                    resolver.openInputStream(uri)?.bufferedReader()?.useLines { lines.addAll(it) }
                }
            }
            if (lines.isEmpty()) return null

            val resultList = mutableListOf<GymMember>()
            lines.drop(1).forEach { line ->
                val p = line.split(";")
                if (p.size >= 9) {
                    resultList.add(
                        GymMember(
                            p[0],
                            p[1].toIntOrNull() ?: 18,
                            p[2],
                            p[3].toIntOrNull() ?: 1,
                            p[4],
                            p[5].toIntOrNull() ?: 0,
                            p[6].toDoubleOrNull() ?: 0.0,
                            p[7],
                            p[8]
                        )
                    )
                }
            }
            resultList
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. Удаление
    fun deleteSharedCsv(): Boolean {
        return try {
            val resolver = context.contentResolver
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(sharedFileName)
            resolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}