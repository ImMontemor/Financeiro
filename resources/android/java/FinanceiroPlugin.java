package app.financeiro.pessoal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONObject;

/** Ponte entre o app (HTML) e o código Android. */
@CapacitorPlugin(name = "Financeiro")
public class FinanceiroPlugin extends Plugin {
    static String launchId = null;

    boolean listenerOn() {
        String s = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        return s != null && s.contains(getContext().getPackageName());
    }

    boolean notifOn() {
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    void open(Intent i) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
    }

    @PluginMethod
    public void status(PluginCall c) {
        JSObject o = new JSObject();
        o.put("listener", listenerOn());
        o.put("notif", notifOn());
        c.resolve(o);
    }

    @PluginMethod
    public void openListenerSettings(PluginCall c) {
        open(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        c.resolve();
    }

    @PluginMethod
    public void openAppSettings(PluginCall c) {
        open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName())));
        c.resolve();
    }

    @PluginMethod
    public void requestNotif(PluginCall c) {
        if (Build.VERSION.SDK_INT >= 33)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 77);
        c.resolve();
    }

    @PluginMethod
    public void getLaunch(PluginCall c) {
        JSObject o = new JSObject();
        if (launchId != null) {
            JSONObject it = Store.get(getContext(), launchId);
            if (it != null) o.put("item", it);
            launchId = null;
        }
        c.resolve(o);
    }

    @PluginMethod
    public void getPending(PluginCall c) {
        JSObject o = new JSObject();
        o.put("items", Store.all(getContext()));
        c.resolve(o);
    }

    @PluginMethod
    public void remove(PluginCall c) {
        String id = c.getString("id");
        if (id != null) Store.remove(getContext(), id);
        c.resolve();
    }
}
