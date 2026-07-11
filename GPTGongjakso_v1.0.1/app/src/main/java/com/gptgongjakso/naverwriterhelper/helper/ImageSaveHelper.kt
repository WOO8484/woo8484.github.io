package com.gptgongjakso.naverwriterhelper.helper

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.gptgongjakso.naverwriterhelper.model.NaverPostData
import com.gptgongjakso.naverwriterhelper.model.ParsedImage

/**
 * 파싱된 이미지를 Android 갤러리(사진)에 저장한다. v0.1.1 이식.
 * 앨범명: GPT공작소  (Pictures/GPT공작소)
 *
 * Android 10(API 29)+ scoped storage / MediaStore 사용 → 별도 저장 권한 불필요.
 * 이미지 장수는 자료에 있는 만큼 모두 저장한다(가변 지원, 지시서 7).
 */
object ImageSaveHelper {

    /** 갤러리에 보일 앨범(폴더) 이름 */
    const val ALBUM_NAME = "GPT공작소"

    data class SaveResult(
        val total: Int,
        val success: Int,
        val failed: Int,
        val savedUris: List<Uri>
    ) {
        val allSuccess: Boolean get() = failed == 0 && total > 0
    }

    /**
     * 자료의 모든 이미지를 저장한다. (IO 스레드에서 호출 권장)
     */
    fun saveAll(context: Context, data: NaverPostData): SaveResult {
        var success = 0
        var failed = 0
        val uris = ArrayList<Uri>()

        for (image in data.images) {
            val uri = runCatching { saveOne(context, image) }.getOrNull()
            if (uri != null) {
                success++
                uris.add(uri)
            } else {
                failed++
            }
        }
        return SaveResult(total = data.images.size, success = success, failed = failed, savedUris = uris)
    }

    private fun saveOne(context: Context, image: ParsedImage): Uri {
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, image.saveName)
            put(MediaStore.Images.Media.MIME_TYPE, image.mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("MediaStore insert 실패: ${image.saveName}")

        try {
            resolver.openOutputStream(uri)?.use { out ->
                out.write(image.bytes)
                out.flush()
            } ?: throw IllegalStateException("OutputStream 열기 실패: ${image.saveName}")

            // 저장 완료 → 갤러리에 노출
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Exception) {
            // 실패 시 불완전 항목 정리
            runCatching { resolver.delete(uri, null, null) }
            throw e
        }
        return uri
    }
}
