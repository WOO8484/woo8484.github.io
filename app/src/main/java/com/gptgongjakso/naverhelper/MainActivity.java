package com.gptgongjakso.naverhelper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private Button openNaverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        openNaverButton = findViewById(R.id.openNaverButton);

        findViewById(R.id.selectZipButton).setOnClickListener(v -> selectZip());
        openNaverButton.setOnClickListener(v -> openNaverWriter());
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
            if (metadataName == null || contentName == null) {
                throw new IllegalArgumentException("naver_metadata.json 또는 naver_content.txt가 없습니다.");
            }
            JSONObject meta = new JSONObject(new String(files.get(metadataName), StandardCharsets.UTF_8));
            String title = meta.optString("title", "").trim();
            String category = meta.optString("naver_category", "").trim();
            String publishMode = meta.optString("publish_mode", "manual_confirm");
            boolean allowAutoPublish = meta.optBoolean("allow_auto_publish", false);
            String content = new String(files.get(contentName), StandardCharsets.UTF_8).trim();
            if (title.isEmpty() || content.isEmpty()) {
                throw new IllegalArgumentException("제목 또는 본문이 비어 있습니다.");
            }
            if (allowAutoPublish || !"manual_confirm".equals(publishMode)) {
                throw new IllegalArgumentException("자동 발행이 허용된 패키지는 사용할 수 없습니다.");
            }
            JSONArray tags = meta.optJSONArray("naver_tags");
            int tagCount = tags == null ? 0 : tags.length();
            openNaverButton.setEnabled(true);
            statusText.setText(
                    "설치 및 ZIP 확인 성공\n" +
                    "제목: " + title + "\n" +
                    "게시판: " + (category.isEmpty() ? "미지정" : category) + "\n" +
                    "태그: " + tagCount + "개\n" +
                    "이미지: " + imageCount + "개\n" +
                    "접근성 서비스: 미포함\n" +
                    "자동입력·자동발행: 사용 안 함"
            );
        } catch (Exception e) {
            openNaverButton.setEnabled(false);
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
        for (String name : files.keySet()) {
            if (name.equals(suffix) || name.endsWith("/" + suffix)) return name;
        }
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
