package com.gptgongjakso.naverwriterhelper.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.gptgongjakso.naverwriterhelper.helper.ClipboardInputHelper
import com.gptgongjakso.naverwriterhelper.store.AutomationLogStore
import com.gptgongjakso.naverwriterhelper.store.SessionRepository

/**
 * 네이버 글쓰기 화면 입력을 보조하는 접근성 서비스. v0.1.1 검증 로직 그대로 이식.
 *
 * 설계 원칙(변경 없음):
 *  - 발행/임시저장 버튼은 절대 누르지 않는다. (탐색 대상에서 제외)
 *  - 모든 자동 입력은 실패해도 앱이 죽지 않고 "클립보드 복사 + 안내" 로 fallback.
 *  - 자동 조작 전 현재 화면이 허용된 네이버 블로그 앱 패키지인지 공통 검사.
 *    네이버 블로그 앱이 아니면 어떤 노드 조작도 하지 않고 클립보드 fallback 으로 전환.
 *    (→ 앱 전환/전화 수신 시 다른 앱이 전면으로 오면 자동으로 중단됨: 지시서 12 안전)
 *
 * v1.0.0 추가:
 *  - 단계별 타임아웃(지시서 15): 태그 1개 입력이 제한 시간 내 확정되지 않으면 중단·fallback.
 *    (게시판/제목/본문/사진 단계 타임아웃은 오케스트레이터(FloatingControlService)가 사용)
 */
class NaverAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NaverAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        /** 네이버 블로그 앱 화면이 아닐 때의 공통 안내 문구 */
        private const val MSG_NOT_NAVER =
            "네이버 블로그 앱 화면이 아니므로 자동 입력하지 않았습니다. 클립보드 복사로 전환합니다."

        /** fallback 클립보드 조건부 삭제 지연(2분) */
        private const val FALLBACK_CLEAR_DELAY = 120_000L

        /** 자동 붙여넣기 성공 후 클립보드 조건부 삭제 지연(3초) */
        private const val SUCCESS_CLEAR_DELAY = 3_000L

        // ---- 단계별 타임아웃 (지시서 15) ----
        const val TIMEOUT_CATEGORY_MS = 10_000L
        const val TIMEOUT_TITLE_MS = 10_000L
        const val TIMEOUT_BODY_MS = 20_000L
        const val TIMEOUT_TAG_MS = 5_000L
        const val TIMEOUT_PHOTO_MS = 10_000L

        /** 태그 사이 대기(IME 확정 안정화) */
        private const val TAG_STEP_DELAY = 500L
    }

    /** 접근성 조작을 허용하는 패키지 (네이버 블로그 앱 1개만, v1.0.1) */
    private val allowedPackages = setOf(
        "com.nhn.android.blog"
    )

    /** 사진 첨부 탐색 시 절대 클릭하면 안 되는 단어 */
    private val forbiddenClickWords = listOf(
        "발행", "등록", "완료", "게시", "공개", "저장", "임시저장", "예약", "확인"
    )

    /** 최근 접근성 이벤트의 패키지명(패키지 판별 보조) */
    @Volatile
    private var lastEventPackage: String? = null

    private val handler = Handler(Looper.getMainLooper())

    /** 현재 태그 스텝의 타임아웃 감시 토큰 */
    private var tagTimeoutRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AutomationLogStore.add("접근성 서비스 연결됨")
        SessionRepository.notifyChanged()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 온디맨드 방식이라 이벤트로 조작하지 않지만, 패키지 판별용으로만 기록한다.
        event?.packageName?.toString()?.let { lastEventPackage = it }
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        AutomationLogStore.add("접근성 서비스 해제됨")
        SessionRepository.notifyChanged()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    // ======================= 보안 공통 검사 =======================

    /** 현재 활성 화면이 허용된 네이버 패키지인지 */
    private fun isAllowedNaverPackage(): Boolean {
        val current = rootInActiveWindow?.packageName?.toString()
        if (current != null) return current in allowedPackages
        // 활성 창 패키지를 못 읽으면 최근 이벤트 패키지로 보조 판별
        val fallback = lastEventPackage
        return fallback != null && fallback in allowedPackages
    }

    /** 현재 화면이 글쓰기(편집) 화면으로 볼 만한 신호가 있는지 (과도하게 엄격하지 않게) */
    private fun isLikelyWriteScreen(): Boolean {
        val root = rootInActiveWindow ?: return false
        var editableCount = 0
        var hasWriteSignal = false
        traverse(root) { node ->
            if (node.isEditable) editableCount++
            val hint = node.hintText?.toString() ?: ""
            val text = node.text?.toString() ?: ""
            val vid = node.viewIdResourceName ?: ""
            if (listOf("제목", "본문", "태그", "내용", "글쓰기", "쓰기").any { hint.contains(it) || text.contains(it) } ||
                listOf("title", "content", "body", "tag", "write", "post", "subject").any { vid.contains(it, ignoreCase = true) }
            ) {
                hasWriteSignal = true
            }
        }
        return editableCount >= 1 || hasWriteSignal
    }

    // ======================= 공개 액션 =======================

    /** 제목 자동 입력. 허용 패키지/편집칸 확인 후 진행, 아니면 클립보드 fallback. */
    fun inputTitle(context: Context): Boolean {
        val data = SessionRepository.postData ?: run {
            AutomationLogStore.add("제목 입력 실패 · 불러온 자료 없음")
            return false
        }
        if (!isAllowedNaverPackage()) {
            ClipboardInputHelper.copyWithAutoClear(context, "제목", data.title, FALLBACK_CLEAR_DELAY)
            AutomationLogStore.add("제목 · $MSG_NOT_NAVER")
            return false
        }
        val node = focusedEditable() ?: if (isLikelyWriteScreen()) topMostEditable() else null
        val ok = node != null && setNodeText(node, data.title)
        if (ok) {
            AutomationLogStore.add("제목 입력 완료")
        } else {
            ClipboardInputHelper.copyWithAutoClear(context, "제목", data.title, FALLBACK_CLEAR_DELAY)
            AutomationLogStore.add("제목 자동 입력 실패 · 클립보드 복사됨(입력칸에 붙여넣기)")
        }
        return ok
    }

    /** 본문 자동 입력(클립보드 붙여넣기 우선). 허용 패키지/편집칸 확인 후 진행. */
    fun inputBody(context: Context): Boolean {
        val data = SessionRepository.postData ?: run {
            AutomationLogStore.add("본문 입력 실패 · 불러온 자료 없음")
            return false
        }
        if (!isAllowedNaverPackage()) {
            ClipboardInputHelper.copyWithAutoClear(context, "본문", data.body, FALLBACK_CLEAR_DELAY)
            AutomationLogStore.add("본문 · $MSG_NOT_NAVER")
            return false
        }
        // 붙여넣기용으로 클립보드에 복사
        ClipboardInputHelper.copy(context, "본문", data.body)
        val node = focusedEditable() ?: if (isLikelyWriteScreen()) largestEditable() else null
        val ok = node != null && pasteToNode(node)
        if (ok) {
            AutomationLogStore.add("본문 입력 완료")
            ClipboardInputHelper.scheduleClear(context, data.body, SUCCESS_CLEAR_DELAY)
        } else {
            AutomationLogStore.add("본문 자동 입력 실패 · 클립보드 복사됨(입력칸 길게 눌러 붙여넣기)")
            ClipboardInputHelper.scheduleClear(context, data.body, FALLBACK_CLEAR_DELAY)
        }
        return ok
    }

    /**
     * 태그 전체를 "1개씩" 자동 입력한다. (절대 한 번에 붙여넣지 않음)
     * 각 태그: 입력 → IME_ENTER(확정) → 대기 → 다음.
     * v1.0.0: 각 태그 스텝에 타임아웃(TIMEOUT_TAG_MS) 감시 추가.
     */
    fun autoInputAllTags(
        context: Context,
        onEach: (progress: String) -> Unit = {},
        onFinish: (completed: Boolean) -> Unit = {}
    ) {
        val controller = SessionRepository.tagController
        if (controller.isEmpty()) {
            AutomationLogStore.add("태그 입력 실패 · 태그 없음")
            onFinish(false)
            return
        }
        if (!isAllowedNaverPackage()) {
            controller.current()?.let { ClipboardInputHelper.copyWithAutoClear(context, "태그", it, FALLBACK_CLEAR_DELAY) }
            AutomationLogStore.add("태그 · $MSG_NOT_NAVER")
            onFinish(false)
            return
        }
        stepInputTag(context, onEach, onFinish)
    }

    private fun stepInputTag(
        context: Context,
        onEach: (String) -> Unit,
        onFinish: (Boolean) -> Unit
    ) {
        val controller = SessionRepository.tagController
        if (!controller.hasNext()) {
            cancelTagTimeout()
            AutomationLogStore.add("태그 ${controller.progressText()} 입력 완료 · 전체 완료")
            SessionRepository.notifyChanged()
            onFinish(true)
            return
        }

        // 진행 중 화면이 바뀌어 네이버가 아니게 되면 즉시 중단(안전)
        if (!isAllowedNaverPackage()) {
            cancelTagTimeout()
            controller.current()?.let { ClipboardInputHelper.copyWithAutoClear(context, "태그", it, FALLBACK_CLEAR_DELAY) }
            AutomationLogStore.add("태그 · $MSG_NOT_NAVER")
            onFinish(false)
            return
        }

        val tag = controller.current() ?: run { onFinish(true); return }

        // v1.0.0: 태그 스텝 타임아웃 감시 시작 — 제한 시간 내 다음 스텝으로 넘어가지 못하면 중단.
        armTagTimeout(context, onFinish)

        // 글쓰기 불확실 시 포커스된 편집칸이 있을 때만 입력
        val field = focusedEditable() ?: if (isLikelyWriteScreen()) findTagField() else null

        val typed = field != null && setNodeText(field, tag)
        val entered = typed && field != null && imeEnter(field)

        if (typed && entered) {
            cancelTagTimeout()
            controller.markCurrentDone()
            AutomationLogStore.add("태그 ${controller.progressText()} 입력 완료") // 태그 내용은 로그에 남기지 않음
            SessionRepository.notifyChanged()
            onEach(controller.progressText())
            handler.postDelayed({ stepInputTag(context, onEach, onFinish) }, TAG_STEP_DELAY)
        } else {
            cancelTagTimeout()
            ClipboardInputHelper.copyWithAutoClear(context, "태그", tag, FALLBACK_CLEAR_DELAY)
            AutomationLogStore.add("태그 자동 입력 실패 · 현재 태그를 클립보드에 복사(붙여넣고 Enter)")
            onFinish(false)
        }
    }

    /** 태그 스텝 타임아웃을 건다. 시간 내 해제되지 않으면 안전하게 중단. */
    private fun armTagTimeout(context: Context, onFinish: (Boolean) -> Unit) {
        cancelTagTimeout()
        val r = Runnable {
            val controller = SessionRepository.tagController
            controller.current()?.let { ClipboardInputHelper.copyWithAutoClear(context, "태그", it, FALLBACK_CLEAR_DELAY) }
            AutomationLogStore.add("태그 입력 시간 초과 · 자동 진행을 중단하고 현재 태그를 복사했습니다.")
            onFinish(false)
        }
        tagTimeoutRunnable = r
        handler.postDelayed(r, TIMEOUT_TAG_MS)
    }

    private fun cancelTagTimeout() {
        tagTimeoutRunnable?.let { handler.removeCallbacks(it) }
        tagTimeoutRunnable = null
    }

    /** 다음 태그 1개를 클립보드에 복사하고 커서를 넘긴다(수동 입력 모드). */
    fun copyNextTag(context: Context): Boolean {
        val controller = SessionRepository.tagController
        val tag = controller.current() ?: run {
            AutomationLogStore.add("복사할 다음 태그 없음")
            return false
        }
        ClipboardInputHelper.copyWithAutoClear(context, "태그", tag, FALLBACK_CLEAR_DELAY)
        controller.markCurrentDone()
        AutomationLogStore.add("다음 태그 복사 (${controller.progressText()})") // 태그 내용 미기록
        SessionRepository.notifyChanged()
        return true
    }

    /** 방금(현재 커서 직전) 태그를 다시 클립보드에 복사. */
    fun copyCurrentTagAgain(context: Context): Boolean {
        val controller = SessionRepository.tagController
        val idx = (controller.cursor - 1).coerceAtLeast(0)
        val tag = controller.snapshot().getOrNull(idx) ?: controller.current() ?: run {
            AutomationLogStore.add("다시 복사할 태그 없음")
            return false
        }
        ClipboardInputHelper.copyWithAutoClear(context, "태그", tag, FALLBACK_CLEAR_DELAY)
        AutomationLogStore.add("현재 태그 다시 복사") // 태그 내용 미기록
        return true
    }

    /**
     * 사진 첨부 버튼을 안전하게 찾아 클릭 시도(없으면 안내만).
     * 발행/등록/저장 계열 버튼은 절대 누르지 않는다.
     */
    fun openPhotoAttach(context: Context): Boolean {
        if (!isAllowedNaverPackage()) {
            AutomationLogStore.add("사진 첨부 · $MSG_NOT_NAVER")
            return false
        }
        if (!isLikelyWriteScreen()) {
            AutomationLogStore.add("사진 버튼을 찾지 못했습니다. 네이버 화면에서 직접 눌러주세요.")
            return false
        }
        val root = rootInActiveWindow ?: run {
            AutomationLogStore.add("사진 버튼을 찾지 못했습니다. 네이버 화면에서 직접 눌러주세요.")
            return false
        }

        val candidates = listOf("사진", "이미지", "photo", "image")
        for (word in candidates) {
            val matched = findNodeByText(root, word) ?: continue
            val clickable = clickableAncestor(matched) ?: continue
            // 발행/등록/저장 계열이면 클릭 금지
            if (hasForbiddenWordNear(clickable) || hasForbiddenWordNear(matched)) continue
            if (clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                AutomationLogStore.add("사진 첨부 화면 이동 시도 · GPT공작소 앨범에서 이미지를 선택하세요")
                return true
            }
        }
        AutomationLogStore.add("사진 버튼을 찾지 못했습니다. 네이버 화면에서 직접 눌러주세요.")
        return false
    }

    // ======================= 노드 탐색/조작 (v0.1.1 유지) =======================

    private fun setNodeText(node: AccessibilityNodeInfo, text: String): Boolean {
        return runCatching {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }.getOrDefault(false)
    }

    private fun pasteToNode(node: AccessibilityNodeInfo): Boolean {
        return runCatching {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }.getOrDefault(false)
    }

    /** IME 확정(Enter). API 30+ ACTION_IME_ENTER 사용. */
    private fun imeEnter(node: AccessibilityNodeInfo): Boolean {
        return runCatching {
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
        }.getOrDefault(false)
    }

    private fun focusedEditable(): AccessibilityNodeInfo? {
        val node = findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return null
        return if (node.isEditable) node else null
    }

    private fun collectEditable(): List<AccessibilityNodeInfo> {
        val root = rootInActiveWindow ?: return emptyList()
        val list = ArrayList<AccessibilityNodeInfo>()
        traverse(root) { if (it.isEditable) list.add(it) }
        return list
    }

    private fun topMostEditable(): AccessibilityNodeInfo? =
        collectEditable().minByOrNull { boundsOf(it).top }

    private fun largestEditable(): AccessibilityNodeInfo? =
        collectEditable().maxByOrNull { boundsOf(it).height() }

    private fun findTagField(): AccessibilityNodeInfo? {
        val editable = collectEditable()
        editable.firstOrNull { node ->
            val t = (node.text?.toString() ?: "")
            val h = (node.hintText?.toString() ?: "")
            t.contains("태그") || h.contains("태그")
        }?.let { return it }
        return editable.maxByOrNull { boundsOf(it).top }
    }

    /** 텍스트/설명에 word 가 포함된 첫 노드 */
    private fun findNodeByText(root: AccessibilityNodeInfo, word: String): AccessibilityNodeInfo? {
        var found: AccessibilityNodeInfo? = null
        traverse(root) { node ->
            if (found != null) return@traverse
            val text = (node.text?.toString() ?: "")
            val desc = (node.contentDescription?.toString() ?: "")
            if (text.contains(word, ignoreCase = true) || desc.contains(word, ignoreCase = true)) {
                found = node
            }
        }
        return found
    }

    /** 노드 자신과 가까운 상위 노드에 금지 단어가 있으면 true */
    private fun hasForbiddenWordNear(node: AccessibilityNodeInfo?): Boolean {
        var cur = node
        var depth = 0
        while (cur != null && depth < 4) {
            val text = (cur.text?.toString() ?: "")
            val desc = (cur.contentDescription?.toString() ?: "")
            val hint = (cur.hintText?.toString() ?: "")
            if (forbiddenClickWords.any { text.contains(it) || desc.contains(it) || hint.contains(it) }) {
                return true
            }
            cur = cur.parent
            depth++
        }
        return false
    }

    private fun clickableAncestor(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var cur = node
        var depth = 0
        while (cur != null && depth < 6) {
            if (cur.isClickable) return cur
            cur = cur.parent
            depth++
        }
        return null
    }

    private fun boundsOf(node: AccessibilityNodeInfo): Rect {
        val r = Rect()
        node.getBoundsInScreen(r)
        return r
    }

    private fun traverse(node: AccessibilityNodeInfo?, action: (AccessibilityNodeInfo) -> Unit) {
        node ?: return
        action(node)
        for (i in 0 until node.childCount) {
            traverse(node.getChild(i), action)
        }
    }
}
