package com.gptgongjakso.naverwriterhelper.model

/**
 * 자료 처리 상태머신 상태 (지시서 11). v1.0.0 신규.
 *
 * 각 단계의 시각·재시도·실패이유·재개단계·현재태그·자료ID·완료여부는
 * [com.gptgongjakso.naverwriterhelper.statemachine.PipelineStateMachine] 이 관리한다.
 */
enum class PipelineState {
    RECEIVED,
    VALIDATING,
    DUPLICATE_CHECKING,
    PARSING,
    STORING,
    SAVING_IMAGES,
    OPENING_NAVER,
    SELECTING_CATEGORY,
    INPUTTING_TITLE,
    INPUTTING_BODY,
    INPUTTING_TAGS,
    OPENING_PHOTO_PICKER,
    WAITING_PHOTO_CONFIRM,
    READY_FOR_USER,
    PAUSED,
    FAILED,
    CANCELLED,
    COMPLETED_BY_USER;

    /** 종료 상태(더 이상 진행하지 않음) 여부 */
    val isTerminal: Boolean
        get() = this == FAILED || this == CANCELLED || this == COMPLETED_BY_USER

    /** 사용자 확인 대기/발행 준비 등 자동 진행을 멈추는 상태 여부 */
    val isHalting: Boolean
        get() = this == READY_FOR_USER || this == PAUSED || this == WAITING_PHOTO_CONFIRM || isTerminal
}
