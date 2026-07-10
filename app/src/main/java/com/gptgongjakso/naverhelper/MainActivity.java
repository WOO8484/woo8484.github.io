package com.gptgongjakso.naverhelper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {
    private static final int REQUEST_ZIP = 1001;
    private TextView statusText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);

        findViewById(R.id.selectZipButton).setOnClickListener(v -> selectZip());
        findViewById(R.id.accessibilityButton).setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        findViewById(R.id.openNaverButton).setOnClickListener(v -> openNaverWriter());
        startButton.setOnClickListener(v -> {
            if (AutomationStore.title.isEmpty() || AutomationStore.content.isEmpty()) {
                Toast.makeText(this, "먼저 정상 자료 ZIP을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            AutomationStore.stopped = false;
            AutomationStore.armed = true;
            statusText.setText("자동입력 대기 중입니다. 네이버 글쓰기 화면으로 이동하세요.");
            openNaverWriter();
        });
        findViewById(R.id.stopButton).setOnClickListener(v -> {
            AutomationStore.stopped = true;
            AutomationStore.armed = false;
            statusText.setText("자동입력을 중지했습니다.");
        });
    }

    private void selectZip() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        try {
            startActivityForResult(intent, REQUEST_ZIP);
        } catch (ActivityNotFoundException e) {
            intent.setType("application/octet-stream");
            startActivityForResult(intent, REQUEST_ZIP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ZIP && resultCode == RESULT_OK && data != null && data.getData() != null) {
            readPackage(data.getData());
        }
    }

    private void readPackage(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ZipInputStream zip = new ZipInputStream(input, StandardCharsets.UTF_8)) {
            Map<String, byte[]> files = new HashMap<>();
            ZipEntry entry;
            int imageCount = 0;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().replace('\\', '/');
                byte[] bytes = readAll(zip);
                files.put(name, bytes);
                String lower = name.toLowerCase();
                if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) imageCount++;
            }
            String metadataName = findBySuffix(files, "naver_metadata.json");
            String contentName = findBySuffix(files, "naver_content.txt");
            if (metadataName == null || contentName == null) throw new IllegalArgumentException("naver_metadata.json 또는 naver_content.txt가 없습니다.");
            JSONObject meta = new JSONObject(new String(files.get(metadataName), StandardCharsets.UTF_8));
            String title = meta.optString("title", "").trim();
            String category = meta.optString("naver_category", "").trim();
            String publishMode = meta.optString("publish_mode", "manual_confirm");
            boolean allowAutoPublish = meta.optBoolean("allow_auto_publish", false);
            String content = new String(files.get(contentName), StandardCharsets.UTF_8).trim();
            if (title.isEmpty() || content.isEmpty()) throw new IllegalArgumentException("제목 또는 본문이 비어 있습니다.");
            if (allowAutoPublish || !"manual_confirm".equals(publishMode)) throw new IllegalArgumentException("자동 발행이 허용된 패키지는 사용할 수 없습니다.");
            AutomationStore.title = title;
            AutomationStore.content = content;
            AutomationStore.category = category;
            AutomationStore.stopped = true;
            AutomationStore.armed = false;
            startButton.setEnabled(true);
            JSONArray tags = meta.optJSONArray("naver_tags");
            int tagCount = tags == null ? 0 : tags.length();
            statusText.setText("자료 확인 완료\n제목: " + title + "\n게시판: " + (category.isEmpty() ? "미지정" : category) + "\n태그: " + tagCount + "개\n이미지: " + imageCount + "개\n발행 방식: 사용자 최종 확인");
        } catch (Exception e) {
            AutomationStore.title = "";
            AutomationStore.content = "";
            startButton.setEnabled(false);
            statusText.setText("ZIP 확인 실패: " + e.getMessage());
        }
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        return out.toByteArray();
    }

    private static String findBySuffix(Map<String, byte[]> files, String suffix) {
        for (String name : files.keySet()) if (name.equals(suffix) || name.endsWith("/" + suffix)) return name;
        return null;
    }

    private void openNaverWriter() {
        Uri writer = Uri.parse("https://blog.naver.com/PostWriteForm.naver");
        Intent intent = new Intent(Intent.ACTION_VIEW, writer);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "웹 브라우저를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
