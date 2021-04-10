package xyz.hisname.epicpython.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtil {

    fun getFileName(context: Context, uri: Uri): String?{
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use { curse ->
                if (curse != null && curse.moveToFirst()) {
                    result = curse.getString(curse.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut?.plus(1) ?: 0)
            }
        }
        return result
    }

}