package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URLEncoder;

public class WhatsAppHelper {

    public static void sendMessage(Context context,
                                   String mobile,
                                   String message) {

        try {

            String url =
                    "https://wa.me/91"
                            + mobile
                            + "?text="
                            + URLEncoder.encode(message,"UTF-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse(url));

            intent.setPackage("com.whatsapp");

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}