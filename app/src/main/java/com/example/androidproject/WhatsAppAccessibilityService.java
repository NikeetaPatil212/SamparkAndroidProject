package com.example.androidproject;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class WhatsAppAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (getRootInActiveWindow() == null) return;

        // Create a root node to search the UI
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        // 1. Try finding by Resource ID (most common)
        // IDs: com.whatsapp:id/send (Regular) or com.whatsapp.w4b:id/send (Business)
        List<AccessibilityNodeInfo> sendButtons = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");

        // 2. Fallback: Try finding by content description (if ID changed)
        if (sendButtons == null || sendButtons.isEmpty()) {
            sendButtons = rootNode.findAccessibilityNodeInfosByText("Send");
        }

        if (sendButtons != null && !sendButtons.isEmpty()) {
            AccessibilityNodeInfo sendButton = sendButtons.get(0);
            if (sendButton.isVisibleToUser() && sendButton.isEnabled()) {
                sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            sendButton.recycle();
        }

        rootNode.recycle();
    }

    @Override
    public void onInterrupt() {
        // Handle service interruption
    }
}