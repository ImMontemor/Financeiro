package app.financeiro.pessoal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Lê as notificações dos apps de banco e cria o aviso "Importar". */
public class BankNotificationService extends NotificationListenerService {
    static final Set<String> PK = new HashSet<>(Arrays.asList(
        "com.nu.production", "br.com.intermedium", "com.itau", "br.com.itau.iti", "com.bradesco",
        "br.com.bb.android", "br.com.gabba.Caixa", "br.gov.caixa.tem", "com.santander.app",
        "com.c6bank.app", "com.mercadopago.wallet", "com.picpay", "br.com.neon",
        "br.com.uol.ps.myaccount", "br.com.original.bank", "com.google.android.apps.walletnfc"));
    static final Pattern BR = Pattern.compile("(nubank|itau|bradesco|santander|banco do brasil|caixa|mercado pago|picpay|pagbank|pagseguro|sicredi|sicoob|banrisul|safra|btg|neon|original|will bank|google wallet|\\bc6\\b|\\binter\\b|\\bnext\\b|\\bxp\\b)");

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pkg = sbn.getPackageName();
            if (pkg.equals(getPackageName())) return;
            Notification n = sbn.getNotification();
            if (n == null || (n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            String label = label(pkg);
            if (!PK.contains(pkg) && !BR.matcher(Parser.norm(label)).find()) return;
            Bundle e = n.extras;
            CharSequence t = e.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence b = e.getCharSequence(Notification.EXTRA_BIG_TEXT);
            CharSequence x = e.getCharSequence(Notification.EXTRA_TEXT);
            String title = t == null ? "" : t.toString();
            CharSequence body = b != null ? b : x;
            String text = body == null ? "" : body.toString();
            Parser.R r = Parser.parse(title, text);
            if (r == null) return;
            long ts = System.currentTimeMillis();
            String k = Integer.toHexString((pkg + title + text).hashCode());
            if (Store.dup(this, k, ts)) return;
            JSONObject it = new JSONObject();
            it.put("id", k + "-" + ts);
            it.put("k", k);
            it.put("ts", ts);
            it.put("tipo", r.tipo);
            it.put("v", r.v);
            it.put("n", r.n);
            it.put("banco", label);
            Store.add(this, it);
            post(it);
        } catch (Exception ex) { /* ignora notificações que não deu para ler */ }
    }

    String label(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception e) { return pkg; }
    }

    void post(JSONObject it) {
        String id = it.optString("id");
        boolean r = "r".equals(it.optString("tipo"));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel("imp", "Importar movimentações", NotificationManager.IMPORTANCE_HIGH));
        Intent i = new Intent(this, MainActivity.class).putExtra("imp_id", id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, id.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String v = String.format(new Locale("pt", "BR"), "R$ %,.2f", it.optDouble("v"));
        String n = it.optString("n");
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, "imp")
            .setSmallIcon(R.drawable.ic_stat_import)
            .setContentTitle((r ? "Receita de " : "Despesa de ") + v + " \u00b7 " + it.optString("banco"))
            .setContentText(n)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(n))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Importar", pi);
        nm.notify(id.hashCode(), b.build());
    }
}
