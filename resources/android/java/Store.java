package app.financeiro.pessoal;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Guarda as movimentações detectadas até você importar ou descartar. */
class Store {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("fin_imports", 0); }

    static synchronized JSONArray all(Context c) {
        try { return new JSONArray(sp(c).getString("items", "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    static synchronized void put(Context c, JSONArray a) { sp(c).edit().putString("items", a.toString()).apply(); }

    static synchronized boolean dup(Context c, String k, long ts) {
        JSONArray a = all(c);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && k.equals(o.optString("k")) && Math.abs(ts - o.optLong("ts")) < 120000) return true;
        }
        return false;
    }

    static synchronized void add(Context c, JSONObject it) {
        JSONArray a = all(c), b = new JSONArray();
        b.put(it);
        for (int i = 0; i < a.length() && b.length() < 50; i++) b.put(a.opt(i));
        put(c, b);
    }

    static synchronized JSONObject get(Context c, String id) {
        JSONArray a = all(c);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && id.equals(o.optString("id"))) return o;
        }
        return null;
    }

    static synchronized void remove(Context c, String id) {
        JSONArray a = all(c), b = new JSONArray();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && !id.equals(o.optString("id"))) b.put(o);
        }
        put(c, b);
    }
}
