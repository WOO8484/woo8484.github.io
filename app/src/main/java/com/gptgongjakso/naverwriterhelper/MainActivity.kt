package com.gptgongjakso.naverwriterhelper

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gptgongjakso.naverwriterhelper.databinding.ActivityMainBinding
import com.gptgongjakso.naverwriterhelper.dedup.DuplicateChecker
import com.gptgongjakso.naverwriterhelper.helper.ClipboardInputHelper
import com.gptgongjakso.naverwriterhelper.helper.ImageSaveHelper
import com.gptgongjakso.naverwriterhelper.helper.NaverLaunchHelper
import com.gptgongjakso.naverwriterhelper.helper.PermissionGuideHelper
import com.gptgongjakso.naverwriterhelper.image.ImageValidator
import com.gptgongjakso.naverwriterhelper.model.DuplicateVerdict
import com.gptgongjakso.naverwriterhelper.model.NaverPostData
import com.gptgongjakso.naverwriterhelper.model.PipelineState
import com.gptgongjakso.naverwriterhelper.model.PostHistoryRecord
import com.gptgongjakso.naverwriterhelper.parser.PackageParser
import com.gptgongjakso.naverwriterhelper.parser.TagNormalizer
import com.gptgongjakso.naverwriterhelper.service.FloatingControlService
import com.gptgongjakso.naverwriterhelper.store.AutomationLogStore
import com.gptgongjakso.naverwriterhelper.store.SessionRepository
import com.gptgongjakso.naverwriterhelper.store.db.HistoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val history by lazy { HistoryStore(applicationContext) }

    private val logListener: () -> Unit = { refreshLog() }
    private val sessionListener: () -> Unit = { refreshData() }

    /** 시험 모드(지시서 16): 이미지 저장/이력 기록을 건너뛰고 검증/미리보기만 수행 */
    private var testMode: Boolean = false

    // 자료 ZIP 선택
    private val openZipLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) loadPackage(uri)
        }

    // 알림 권한 요청(Android 13+)
    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshPermissions()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AutomationLogStore.init(applicationContext)

        binding.btnLoad.setOnClickListener {
            openZipLauncher.launch(
                arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
            )
        }
        binding.btnAccessibility.setOnClickListener {
            PermissionGuideHelper.openAccessibilitySettings(this)
        }
        binding.btnOverlay.setOnClickListener {
            PermissionGuideHelper.openOverlaySettings(this)
        }
        binding.btnNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !PermissionGuideHelper.hasNotificationPermission(this)
            ) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                PermissionGuideHelper.openAppNotificationSettings(this)
            }
        }
        binding.btnSaveImages.setOnClickListener { saveImages() }
        binding.btnOpenNaver.setOnClickListener { openNaver() }
        binding.btnStartFloating.setOnClickListener { startFloating() }

        // 발행 상태 기록(지시서 19) — 사용자가 네이버에서 직접 처리한 결과를 기록
        binding.btnMarkPublished.setOnClickListener { markPublish("발행완료", PipelineState.COMPLETED_BY_USER) }
        binding.btnMarkDraft.setOnClickListener { markPublish("임시저장", PipelineState.READY_FOR_USER) }
        binding.btnMarkCancelled.setOnClickListener { markPublish("취소", PipelineState.CANCELLED) }

        binding.switchTestMode.setOnCheckedChangeListener { _, checked ->
            testMode = checked
            AutomationLogStore.add(if (checked) "시험 모드 ON (이미지 저장/이력 기록 생략)" else "시험 모드 OFF")
        }

        AutomationLogStore.addListener(logListener)
        SessionRepository.addListener(sessionListener)

        refreshData()
        refreshLog()

        // ZIP 공유/열기 인텐트 처리 (지시서 8)
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
        refreshData()
    }

    override fun onDestroy() {
        AutomationLogStore.removeListener(logListener)
        SessionRepository.removeListener(sessionListener)
        super.onDestroy()
    }

    // ---------- 공유/열기 인텐트 → ZIP 로드 (지시서 8) ----------
    private fun handleIncomingIntent(intent: Intent?) {
        intent ?: return
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }
        if (uri != null) {
            AutomationLogStore.add("공유/열기로 자료 ZIP 수신")
            loadPackage(uri)
        }
    }

    // ---------- 자료 불러오기 + 처리 파이프라인 ----------
    private fun loadPackage(uri: Uri) {
        // 새 자료 로드 = 세션 초기화 → 이전에 앱이 복사한 클립보드만 조건부 정리
        ClipboardInputHelper.clearIfOwn(this)
        binding.badgeData.text = getString(R.string.badge_loading)

        lifecycleScope.launch {
            // 파싱(PARSING) — IO 스레드
            val parseResult = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.use { input ->
                        PackageParser.parse(input)
                    } ?: throw IllegalStateException("파일을 열 수 없습니다")
                }
            }

            parseResult.onSuccess { data ->
                SessionRepository.setPostData(data)   // 게시판 자동 매칭 + 상태머신 초기화
                val sm = SessionRepository.pipeline
                sm.transitionTo(PipelineState.VALIDATING)

                // 중복 검사(DUPLICATE_CHECKING)
                sm.transitionTo(PipelineState.DUPLICATE_CHECKING)
                val dup = withContext(Dispatchers.IO) { runDuplicateCheck(data) }
                SessionRepository.lastDuplicateResult = dup
                AutomationLogStore.add("중복 검사 · ${dup.verdict.label} · ${dup.reason}")

                if (dup.verdict == DuplicateVerdict.IDENTICAL && !testMode) {
                    // 동일 글 → 자동 진행 차단(지시서 18). 자료는 보여주되 저장/이미지 자동화는 멈춤.
                    sm.pause("동일 글 감지 · 진행 차단")
                    AutomationLogStore.add("동일 글로 판정되어 자동 진행을 멈췄습니다.")
                    refreshData()
                    toast("이미 처리된 동일 글입니다. 진행이 차단되었습니다.")
                    return@onSuccess
                }

                // 이미지 검증(가변, 지시서 7)
                val board = SessionRepository.effectiveBoard()
                val imgResult = ImageValidator.validate(data.images, board, data.metadata.imageCount)
                AutomationLogStore.add("이미지 검증 · ${imgResult.severity} · ${imgResult.issues.firstOrNull() ?: ""}")

                // 이력 저장(STORING) — 시험 모드가 아니면 기록
                sm.transitionTo(PipelineState.STORING)
                if (!testMode) {
                    withContext(Dispatchers.IO) { saveToHistory(data, dup.verdict.label) }
                }

                sm.transitionTo(PipelineState.READY_FOR_USER)
                AutomationLogStore.add(
                    "자료 준비 완료 · 태그 ${data.tagCount}개 · 이미지 ${data.imageCount}장 · 게시판 ${board.displayName}"
                )
                refreshData()
            }.onFailure { e ->
                SessionRepository.pipeline.fail(e.message ?: "알 수 없는 오류")
                AutomationLogStore.add("자료 불러오기 실패 · ${e.message}")
                binding.badgeData.text = getString(R.string.badge_no_data)
                toast("불러오기 실패: ${e.message}")
                refreshData()
            }
        }
    }

    private fun runDuplicateCheck(data: NaverPostData) =
        DuplicateChecker.check(
            zipSha256 = data.zipSha256,
            bodySha256 = data.bodySha256,
            bodyFingerprint = data.bodyFingerprint,
            title = data.title,
            topicKey = data.metadata.topicKey,
            topicAngle = data.metadata.topicAngle,
            contentVersion = data.metadata.contentVersion,
            postId = data.metadata.postId,
            existing = runCatching { history.all() }.getOrDefault(emptyList()),
            nowMillis = System.currentTimeMillis(),
            windowDays = SessionRepository.effectiveBoard().duplicateWindowDays
        )

    private fun saveToHistory(data: NaverPostData, verdict: String) {
        val postId = data.metadata.postId?.takeIf { it.isNotBlank() }
            ?: "auto-${data.zipSha256.take(16)}"
        val record = PostHistoryRecord(
            postId = postId,
            title = data.title,
            bodyText = data.body,
            boardKey = SessionRepository.effectiveBoard().key,
            topicKey = data.metadata.topicKey,
            topicAngle = data.metadata.topicAngle,
            tags = data.tags,
            createdAt = System.currentTimeMillis(),
            publishStatus = "미확인",
            zipSha256 = data.zipSha256,
            bodySha256 = data.bodySha256,
            bodyFingerprint = data.bodyFingerprint,
            contentVersion = data.metadata.contentVersion,
            lastVerdict = verdict
        )
        runCatching { history.upsert(record) }
    }

    // ---------- 발행 상태 기록 (지시서 19) ----------
    private fun markPublish(status: String, state: PipelineState) {
        val data = SessionRepository.postData ?: run { toast("먼저 자료를 불러오세요"); return }
        val postId = data.metadata.postId?.takeIf { it.isNotBlank() } ?: "auto-${data.zipSha256.take(16)}"
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { runCatching { history.updatePublishStatus(postId, status) } }
            val sm = SessionRepository.pipeline
            when (state) {
                PipelineState.COMPLETED_BY_USER -> sm.completeByUser()
                PipelineState.CANCELLED -> sm.cancel()
                else -> if (!sm.current.isTerminal) sm.transitionTo(state)
            }
            AutomationLogStore.add("발행 상태 기록 · $status")
            SessionRepository.notifyChanged()
            toast("상태 기록: $status")
        }
    }

    // ---------- 이미지 저장 ----------
    private fun saveImages() {
        val data = SessionRepository.postData
        if (data == null) {
            toast("먼저 자료를 불러오세요")
            return
        }
        if (data.images.isEmpty()) {
            binding.badgeImageSave.text = "🔴 저장할 이미지 없음"
            AutomationLogStore.add("이미지 저장 실패 · 이미지 없음")
            return
        }
        SessionRepository.pipeline.let { if (!it.current.isTerminal) it.transitionTo(PipelineState.SAVING_IMAGES) }
        binding.badgeImageSave.text = getString(R.string.badge_saving)
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                ImageSaveHelper.saveAll(this@MainActivity, data)
            }
            val board = SessionRepository.effectiveBoard()
            val missing = (board.imageRecommended - data.imageCount).coerceAtLeast(0)
            val badge = when {
                result.allSuccess && missing == 0 ->
                    "✅ 이미지 ${result.success}장 저장 완료"
                result.allSuccess && missing > 0 ->
                    "🟡 이미지 ${result.success}장 저장 · 권장 대비 ${missing}장 부족"
                result.success > 0 ->
                    "🟡 이미지 ${result.success}장 저장 · ${result.failed}장 실패"
                else ->
                    "🔴 이미지 저장 실패"
            }
            binding.badgeImageSave.text = badge
            AutomationLogStore.add("이미지 저장: 성공 ${result.success} / 실패 ${result.failed} (앨범: ${ImageSaveHelper.ALBUM_NAME})")
        }
    }

    // ---------- 네이버 열기 ----------
    private fun openNaver() {
        SessionRepository.pipeline.let { if (!it.current.isTerminal) it.transitionTo(PipelineState.OPENING_NAVER) }
        when (NaverLaunchHelper.openNaverBlogWrite(this)) {
            NaverLaunchHelper.LaunchResult.OPENED_BLOG_APP ->
                AutomationLogStore.add("네이버 블로그 앱 열기 완료")
            NaverLaunchHelper.LaunchResult.OPENED_NAVER_APP ->
                AutomationLogStore.add("네이버 앱 열기 완료 · 블로그 글쓰기로 이동하세요")
            NaverLaunchHelper.LaunchResult.OPENED_BROWSER ->
                AutomationLogStore.add("브라우저로 네이버 블로그 열기 완료")
            NaverLaunchHelper.LaunchResult.FAILED -> {
                AutomationLogStore.add("네이버 열기 실패 · 직접 글쓰기 화면을 여세요")
                toast("네이버 앱/브라우저를 직접 열어 글쓰기 화면으로 이동하세요")
            }
        }
    }

    // ---------- 플로팅 시작 ----------
    private fun startFloating() {
        if (!PermissionGuideHelper.canDrawOverlays(this)) {
            toast("다른 앱 위에 표시 권한이 필요합니다")
            PermissionGuideHelper.openOverlaySettings(this)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionGuideHelper.hasNotificationPermission(this)
        ) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val intent = Intent(this, FloatingControlService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        toast("플로팅 컨트롤을 시작했습니다")
    }

    // ---------- 화면 갱신 ----------
    private fun refreshData() {
        val data = SessionRepository.postData
        if (data == null) {
            binding.badgeData.text = getString(R.string.badge_no_data)
            binding.txtTitle.text = "제목: -"
            binding.txtBody.text = "본문: -"
            binding.txtTags.text = "태그: -"
            binding.txtImages.text = "이미지: -"
            binding.txtBoard.text = "게시판: -"
            binding.txtDuplicate.text = "중복 검사: -"
            binding.txtImageValidation.text = "이미지 검증: -"
            binding.txtStateStatus.text = "상태: -"
            return
        }
        binding.badgeData.text = getString(R.string.badge_ready)
        binding.txtTitle.text = "제목: ${data.title}"
        binding.txtBody.text = "본문: ${data.bodyLength}자"
        binding.txtTags.text = "태그: ${data.tagCount}개  ${TagNormalizer.toDisplayString(data.tags)}"

        val board = SessionRepository.effectiveBoard()
        val imgResult = ImageValidator.validate(data.images, board, data.metadata.imageCount)
        binding.txtImages.text = "이미지: ${data.imageCount}장 (권장 ${board.imageRecommended})"
        binding.txtBoard.text =
            if (SessionRepository.selectedBoard != null) "게시판: ${board.displayName} (자동 매칭)"
            else "게시판: 미매칭 → 기본(${board.displayName}) 기준 검증"
        binding.txtImageValidation.text = "이미지 검증: ${imgResult.severity} · ${imgResult.issues.joinToString(" / ")}"

        val dup = SessionRepository.lastDuplicateResult
        binding.txtDuplicate.text =
            if (dup != null) "중복 검사: ${dup.verdict.label} · ${dup.reason}" else "중복 검사: 대기"

        binding.txtStateStatus.text = "상태: ${SessionRepository.pipeline.current.name}" +
            if (testMode) " · [시험 모드]" else ""
    }

    private fun refreshPermissions() {
        binding.badgeAccessibility.text =
            if (PermissionGuideHelper.isAccessibilityEnabled(this)) getString(R.string.badge_granted_green)
            else getString(R.string.badge_need)
        binding.badgeOverlay.text =
            if (PermissionGuideHelper.canDrawOverlays(this)) getString(R.string.badge_granted_green)
            else getString(R.string.badge_need)
        binding.badgePhoto.text = getString(R.string.badge_ready_green)
        binding.badgeNotification.text =
            if (PermissionGuideHelper.hasNotificationPermission(this)) getString(R.string.badge_granted_green)
            else getString(R.string.badge_need)
    }

    private fun refreshLog() {
        val logs = AutomationLogStore.recent(6)
        binding.txtLog.text = if (logs.isEmpty()) "아직 로그가 없습니다." else logs.joinToString("\n")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
