package com.gptgongjakso.naverwriterhelper.helper

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * 네이버 블로그 글쓰기 화면 열기.
 *
 * 우선순위:
 *  1) 네이버 블로그 앱 실행
 *  2) 네이버 앱 실행
 *  3) 브라우저로 모바일 블로그 열기
 *  4) 실패 시 직접 열기 안내
 *
 * ※ 이 앱은 네이버 로그인 정보를 저장/입력하지 않는다. 로그인은 사용자가 직접 한다.
 */
object NaverLaunchHelper {

    private const val PKG_BLOG = "com.nhn.android.blog"
    private const val PKG_NAVER = "com.nhn.android.search"

    /** 모바일 네이버 블로그 홈 (여기서 글쓰기 진입) */
    private const val URL_MOBILE_BLOG = "https://m.blog.naver.com/"

    enum class LaunchResult { OPENED_BLOG_APP, OPENED_NAVER_APP, OPENED_BROWSER, FAILED }

    fun openNaverBlogWrite(context: Context): LaunchResult {
        // 1) 네이버 블로그 앱
        launchPackage(context, PKG_BLOG)?.let { return LaunchResult.OPENED_BLOG_APP }
        // 2) 네이버 앱
        launchPackage(context, PKG_NAVER)?.let { return LaunchResult.OPENED_NAVER_APP }
        // 3) 브라우저
        if (openBrowser(context, URL_MOBILE_BLOG)) return LaunchResult.OPENED_BROWSER
        // 4) 실패
        return LaunchResult.FAILED
    }

    private fun launchPackage(context: Context, pkg: String): Unit? {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return null
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent); Unit }.getOrNull()
    }

    private fun openBrowser(context: Context, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }
}
