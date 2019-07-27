package barsotti.alejandro.tf.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class MailUtils {
    public static Intent composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        return intent;
    }

    public static Intent composeEmailWithAttachment(String[] addresses, String subject, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);

        return intent;
    }

    public static Intent composeEmailWithMultipleAttachments(String subject, String body,
                                                             ArrayList<Uri> attachments) {
        Log.d("MailUtils", "composeEmailWithMultipleAttachments: " + attachments);
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

        return intent;
    }
}