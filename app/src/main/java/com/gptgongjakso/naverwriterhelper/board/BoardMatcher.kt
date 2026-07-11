package com.gptgongjakso.naverwriterhelper.board

import com.gptgongjakso.naverwriterhelper.model.BoardProfile

/**
 * naver_category → 게시판 프로필 매칭 (지시서 10). v1.0.0 신규.
 *
 * 규칙:
 *  1) 표시 이름 정확 일치 우선
 *  2) 별칭(aliases) 일치
 *  3) 찾지 못하면 null → 임의 선택 금지(사용자 선택 유도)
 *
 * 비교는 공백 제거 + 소문자로 관대하게 처리하되, 부분일치는 하지 않는다(오선택 방지).
 * 순수 로직 → 단위 테스트 가능.
 */
object BoardMatcher {

    fun match(category: String?, profiles: List<BoardProfile>): BoardProfile? {
        if (category.isNullOrBlank()) return null
        val key = normalize(category)

        // 1) 정확 일치
        profiles.firstOrNull { normalize(it.displayName) == key }?.let { return it }

        // 2) 별칭 일치
        profiles.firstOrNull { p -> p.aliases.any { normalize(it) == key } }?.let { return it }

        // 3) 없음
        return null
    }

    private fun normalize(s: String): String =
        s.trim().replace(Regex("\\s+"), "").lowercase()
}
