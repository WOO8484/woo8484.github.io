package com.gptgongjakso.naverwriterhelper.store

import com.gptgongjakso.naverwriterhelper.board.BoardMatcher
import com.gptgongjakso.naverwriterhelper.board.BoardProfileRepository
import com.gptgongjakso.naverwriterhelper.model.BoardProfile
import com.gptgongjakso.naverwriterhelper.model.DuplicateCheckResult
import com.gptgongjakso.naverwriterhelper.model.NaverPostData
import com.gptgongjakso.naverwriterhelper.service.TagInputController
import com.gptgongjakso.naverwriterhelper.statemachine.PipelineStateMachine
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 앱 전역 세션 상태. v0.1.1 이식 + v1.0.0 확장.
 * MainActivity / FloatingControlService / NaverAccessibilityService 가 공유한다.
 * (외부 전송 없음. 순수 메모리 보관)
 *
 * v1.0.0 확장:
 *  - pipeline: 처리 상태머신(지시서 11)
 *  - selectedBoard: 자동 매칭된 게시판 프로필(지시서 10)
 *  - lastDuplicateResult: 최근 중복 검사 결과(지시서 18)
 */
object SessionRepository {

    @Volatile
    var postData: NaverPostData? = null
        private set

    /** 태그 1개씩 입력 진행 상태 (v0.1.1) */
    val tagController = TagInputController()

    /** 처리 상태머신 (v1.0.0) */
    @Volatile
    var pipeline = PipelineStateMachine()
        private set

    /** 자동 매칭된 게시판 프로필 (없으면 사용자 선택 필요) */
    @Volatile
    var selectedBoard: BoardProfile? = null

    /** 최근 중복 검사 결과 */
    @Volatile
    var lastDuplicateResult: DuplicateCheckResult? = null

    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    fun setPostData(data: NaverPostData) {
        postData = data
        tagController.reset(data.tags)
        // 새 자료 = 상태머신 초기화
        pipeline = PipelineStateMachine()
        pipeline.dataId = data.zipSha256.take(12)
        // 게시판 자동 매칭(임의 선택 아님: 실패 시 null)
        selectedBoard = BoardMatcher.match(data.metadata.naverCategory, BoardProfileRepository.defaults)
        lastDuplicateResult = null
        notifyChanged()
    }

    fun clear() {
        postData = null
        tagController.reset(emptyList())
        pipeline = PipelineStateMachine()
        selectedBoard = null
        lastDuplicateResult = null
        notifyChanged()
    }

    fun hasData(): Boolean = postData != null

    /** 적용할 게시판(매칭 실패 시 일반 프로필로 검증 기준만 제공) */
    fun effectiveBoard(): BoardProfile = selectedBoard ?: BoardProfileRepository.fallback

    fun addListener(l: () -> Unit) { listeners.addIfAbsent(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }

    fun notifyChanged() {
        listeners.forEach { runCatching { it() } }
    }
}
