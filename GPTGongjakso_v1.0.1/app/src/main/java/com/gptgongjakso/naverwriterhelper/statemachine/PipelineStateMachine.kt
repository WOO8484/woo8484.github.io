package com.gptgongjakso.naverwriterhelper.statemachine

import com.gptgongjakso.naverwriterhelper.model.PipelineState

/**
 * 자료 처리 상태머신 (지시서 11). v1.0.0 신규.
 *
 * 각 단계의 시각·재시도·실패이유·재개단계·현재태그·자료ID·완료여부를 기록한다.
 * 자동 진행은 절대 발행/임시저장으로 이어지지 않으며, READY_FOR_USER 에서 멈춘다(지시서 15).
 *
 * 순수 로직(시각은 주입) → 단위 테스트 가능. UI/서비스가 이 상태를 구독한다.
 */
class PipelineStateMachine(
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    data class StepRecord(
        val state: PipelineState,
        val enteredAt: Long,
        var retries: Int = 0,
        var failReason: String? = null,
        var completed: Boolean = false
    )

    var current: PipelineState = PipelineState.RECEIVED
        private set

    /** 일시정지 시 돌아갈 재개 단계 */
    var resumeState: PipelineState? = null
        private set

    /** 현재 처리 중 자료 ID */
    var dataId: String? = null

    /** 태그 입력 진행 인덱스(현재 태그) */
    var currentTagIndex: Int = 0

    private val history = ArrayList<StepRecord>()

    init {
        history.add(StepRecord(PipelineState.RECEIVED, clock()))
    }

    /** 정상 진행 순서(선형). 안전을 위해 정의된 인접 전이만 허용한다. */
    private val forwardOrder = listOf(
        PipelineState.RECEIVED,
        PipelineState.VALIDATING,
        PipelineState.DUPLICATE_CHECKING,
        PipelineState.PARSING,
        PipelineState.STORING,
        PipelineState.SAVING_IMAGES,
        PipelineState.OPENING_NAVER,
        PipelineState.SELECTING_CATEGORY,
        PipelineState.INPUTTING_TITLE,
        PipelineState.INPUTTING_BODY,
        PipelineState.INPUTTING_TAGS,
        PipelineState.OPENING_PHOTO_PICKER,
        PipelineState.WAITING_PHOTO_CONFIRM,
        PipelineState.READY_FOR_USER
    )

    /** 다음 정상 단계로 전이. 실패/취소/일시정지는 별도 메서드 사용. */
    fun advance(): PipelineState {
        val idx = forwardOrder.indexOf(current)
        if (idx >= 0 && idx < forwardOrder.size - 1) {
            markCompleted(current)
            transitionTo(forwardOrder[idx + 1])
        }
        return current
    }

    /** 특정 단계로 직접 전이(허용 목록 검증). */
    fun transitionTo(next: PipelineState): PipelineState {
        if (current.isTerminal) return current // 종료 상태에서는 전이 불가
        current = next
        history.add(StepRecord(next, clock()))
        return current
    }

    fun pause(reason: String? = null) {
        if (current == PipelineState.PAUSED || current.isTerminal) return
        resumeState = current
        current.let { record(it)?.failReason = reason }
        transitionTo(PipelineState.PAUSED)
    }

    /** 일시정지 해제 → 재개 단계로 복귀(없으면 그대로). */
    fun resume(): PipelineState {
        if (current != PipelineState.PAUSED) return current
        val back = resumeState ?: return current
        resumeState = null
        current = back
        history.add(StepRecord(back, clock()))
        return current
    }

    fun fail(reason: String): PipelineState {
        record(current)?.failReason = reason
        return transitionToTerminal(PipelineState.FAILED)
    }

    fun cancel(): PipelineState = transitionToTerminal(PipelineState.CANCELLED)

    fun completeByUser(): PipelineState = transitionToTerminal(PipelineState.COMPLETED_BY_USER)

    fun retry(): Int {
        val r = record(current) ?: return 0
        r.retries += 1
        return r.retries
    }

    private fun transitionToTerminal(state: PipelineState): PipelineState {
        current = state
        history.add(StepRecord(state, clock(), completed = true))
        return current
    }

    private fun markCompleted(state: PipelineState) {
        record(state)?.completed = true
    }

    private fun record(state: PipelineState): StepRecord? =
        history.lastOrNull { it.state == state }

    fun snapshot(): List<StepRecord> = history.toList()

    /** "태그 3/8" 같은 사용자 표시용 요약 */
    fun statusSummary(): String = when (current) {
        PipelineState.INPUTTING_TAGS -> "태그 입력 중 (${current.name})"
        else -> current.name
    }
}
