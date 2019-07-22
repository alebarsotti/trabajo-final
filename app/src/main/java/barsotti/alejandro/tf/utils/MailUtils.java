package barsotti.alejandro.tf.utils;

import android.content.Intent;
import android.net.Uri;

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

        //TODO: Enviar lista de uris correspondiente a todas las capturas tomadas.
//        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

//        ArrayList<Uri> uris = new ArrayList<>();
//        uris.add(attachment);
//        intent.putExtra(Intent.EXTRA_STREAM, uris);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);

        return intent;
    }
}
