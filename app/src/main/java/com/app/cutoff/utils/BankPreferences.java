package com.app.cutoff.utils;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
public final class BankPreferences {
    private static final String PREFS = "bank_templates", KEY_BANKS = "banks", KEY_ACTIVE = "active";
    private BankPreferences() {}
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    public static List<String> getAll(Context c) {
        Set<String> stored = prefs(c).getStringSet(KEY_BANKS, null);
        if (stored == null) { LinkedHashSet<String> d = new LinkedHashSet<>(Arrays.asList(Constants.BILL_BANKS)); prefs(c).edit().putStringSet(KEY_BANKS, d).putStringSet(KEY_ACTIVE, d).apply(); return new ArrayList<>(d); }
        return new ArrayList<>(stored);
    }
    public static List<String> getActive(Context c) {
        List<String> all = getAll(c); Set<String> active = prefs(c).getStringSet(KEY_ACTIVE, null); List<String> result = new ArrayList<>();
        for (String bank : all) if (active == null || active.contains(bank)) result.add(bank);
        return result.isEmpty() ? new ArrayList<>(all) : result;
    }
    public static void setActive(Context c, String bank, boolean enabled) {
        getAll(c); Set<String> values = new LinkedHashSet<>(prefs(c).getStringSet(KEY_ACTIVE, new LinkedHashSet<>()));
        if (enabled) values.add(bank); else values.remove(bank); if (values.isEmpty()) values.add(bank);
        prefs(c).edit().putStringSet(KEY_ACTIVE, values).apply();
    }
    public static boolean add(Context c, String raw) {
        String bank = raw == null ? "" : raw.trim().toUpperCase(java.util.Locale.US); if (bank.isEmpty()) return false;
        List<String> all = getAll(c); if (all.contains(bank)) return false; all.add(bank); LinkedHashSet<String> stored = new LinkedHashSet<>(all);
        Set<String> active = new LinkedHashSet<>(prefs(c).getStringSet(KEY_ACTIVE, stored)); active.add(bank);
        prefs(c).edit().putStringSet(KEY_BANKS, stored).putStringSet(KEY_ACTIVE, active).apply(); return true;
    }
    public static void remove(Context c, String bank) { List<String> all = getAll(c); all.remove(bank); Set<String> active = new LinkedHashSet<>(prefs(c).getStringSet(KEY_ACTIVE, new LinkedHashSet<>(all))); active.remove(bank); prefs(c).edit().putStringSet(KEY_BANKS, new LinkedHashSet<>(all)).putStringSet(KEY_ACTIVE, active).apply(); }
}
