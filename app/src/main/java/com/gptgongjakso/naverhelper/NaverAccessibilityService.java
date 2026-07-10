package com.gptgongjakso.naverhelper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class NaverAccessibilityService extends AccessibilityService {
    private boolean titleDone = false;
    private boolean contentDone = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!AutomationStore.armed || AutomationStore.stopped) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        try {
            List<AccessibilityNodeInfo> editable = new ArrayList<>();
            collectEditable(root, editable);
            if (!titleDone && !editable.isEmpty()) {
                if (setText(editable.get(0), AutomationStore.title)) titleDone = true;
            }
            if (titleDone && !contentDone && editable.size() >= 2) {
                if (setText(editable.get(1), AutomationStore.content)) {
                    contentDone = true;
                    AutomationStore.armed = false;
                }
            }
        } finally {
            root.recycle();
        }
    }

    private void collectEditable(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> out) {
        if (node == null || out.size() >= 10) return;
        if (node.isEditable() && node.isVisibleToUser() && node.isEnabled()) out.add(AccessibilityNodeInfo.obtain(node));
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectEditable(child, out);
                child.recycle();
            }
        }
    }

    private boolean setText(AccessibilityNodeInfo node, String text) {
        if (text == null || text.isEmpty()) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    @Override
    public void onInterrupt() {
        AutomationStore.stopped = true;
        AutomationStore.armed = false;
    }

    @Override
    protected void onServiceConnected() {
        titleDone = false;
        contentDone = false;
    }
}
