package com.gptgongjakso.naverwriterhelper.selector

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * 네이버 화면 요소 선택자 규칙 (지시서 17). v1.0.0 신규 · 기초 구현.
 *
 * 접근성 서비스가 제목/본문/태그/사진 입력칸을 찾을 때 참고할 힌트 키워드를
 * 외부 JSON(selector_rules.json)으로 관리해, 네이버 UI 변경 시 앱 재빌드 없이
 * 규칙만 교체할 수 있도록 한다.
 *
 * 우선순위: 앱 내부 저장소(files/selector_rules.json) → 없으면 내장 기본값.
 * (원격 자동 다운로드는 INTERNET 권한 미보유 정책상 제공하지 않으며,
 *  사용자가 파일을 교체하는 수동 갱신만 지원한다.)
 */
object SelectorRules {

    private const val FILE_NAME = "selector_rules.json"

    data class Rules(
        val titleHints: List<String>,
        val bodyHints: List<String>,
        val tagHints: List<String>,
        val photoHints: List<String>,
        val version: String
    )

    /** 내장 기본 규칙 (v0.1.1 접근성 로직과 동일한 힌트) */
    val defaults = Rules(
        titleHints = listOf("제목", "title", "subject"),
        bodyHints = listOf("본문", "내용", "content", "body"),
        tagHints = listOf("태그", "tag"),
        photoHints = listOf("사진", "이미지", "photo", "image"),
        version = "builtin-1.0.0"
    )

    /** 현재 적용 규칙을 로드한다. 파싱 실패/파일 없음 시 기본값. */
    fun load(context: Context): Rules {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return defaults
        return runCatching {
            val obj = JSONObject(file.readText())
            Rules(
                titleHints = strList(obj, "title_hints", defaults.titleHints),
                bodyHints = strList(obj, "body_hints", defaults.bodyHints),
                tagHints = strList(obj, "tag_hints", defaults.tagHints),
                photoHints = strList(obj, "photo_hints", defaults.photoHints),
                version = obj.optString("version", "custom")
            )
        }.getOrDefault(defaults)
    }

    /** 사용자 갱신: 전달받은 JSON 문자열을 검증 후 저장. 실패 시 false. */
    fun updateFromJson(context: Context, json: String): Boolean = runCatching {
        JSONObject(json) // 파싱 검증
        File(context.filesDir, FILE_NAME).writeText(json)
        true
    }.getOrDefault(false)

    private fun strList(obj: JSONObject, key: String, fallback: List<String>): List<String> {
        val arr = obj.optJSONArray(key) ?: return fallback
        val out = ArrayList<String>()
        for (i in 0 until arr.length()) arr.optString(i)?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        return if (out.isEmpty()) fallback else out
    }
}
