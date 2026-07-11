package com.gptgongjakso.naverwriterhelper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gptgongjakso.naverwriterhelper.store.AutomationLogStore
import com.gptgongjakso.naverwriterhelper.store.SessionRepository

/**
 * 안전 감시 (지시서 12). v1.0.0 신규.
 *
 * 자동 입력 중 아래 상황에서 진행을 일시정지한다.
 *  - 화면 꺼짐(ACTION_SCREEN_OFF) : 이 모듈이 브로드캐스트로 감지 → 상태머신 pause.
 *  - 앱 전환 / 전화 수신(다른 앱이 전면으로) :
 *    [NaverAccessibilityService] 가 자동 입력 각 단계에서 현재 화면이 허용된 네이버 패키지인지
 *    확인하고, 아니면 즉시 중단하므로 별도 권한(READ_PHONE_STATE 등) 없이도 안전하게 멈춘다.
 *
 * 권한 최소화 원칙에 따라 통화 상태 리스너(READ_PHONE_STATE)는 사용하지 않는다.
 */
class SafetyMonitor(private val context: Context) {

    private var registered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                onScreenOff()
            }
        }
    }

    fun start() {
        if (registered) return
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        runCatching { context.registerReceiver(screenReceiver, filter) }
            .onSuccess { registered = true }
    }

    fun stop() {
        if (!registered) return
        runCatching { context.unregisterReceiver(screenReceiver) }
        registered = false
    }

    private fun onScreenOff() {
        val sm = SessionRepository.pipeline
        if (!sm.current.isHalting) {
            sm.pause("화면 꺼짐")
            AutomationLogStore.add("화면이 꺼져 자동 진행을 일시정지했습니다.")
            SessionRepository.notifyChanged()
        }
    }
}
