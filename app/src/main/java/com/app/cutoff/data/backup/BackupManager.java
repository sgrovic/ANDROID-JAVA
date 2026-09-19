package com.app.cutoff.data.backup;

import androidx.annotation.Nullable;

import com.app.cutoff.data.database.entity.BillEntity;
import com.app.cutoff.data.database.entity.CutoffEntity;
import com.app.cutoff.data.database.entity.SalaryEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts the app's data (salary, fixed-bill templates, cutoffs and their
 * bills/incentives) to and from a single JSON document, and back.
 *
 * The file intentionally keeps each cutoff's own "id" and each bill's own
 * "sourceBillId" as they were at export time, even though those ids will
 * not survive a re-import (Room re-assigns primary keys on insert).
 * BackupRepository uses these original ids purely to rebuild the
 * cutoff <-> bill and template <-> snapshot relationships after the new
 * rows get their new ids -- see BackupPayload / CutoffBackupGroup below.
 */
public final class BackupManager {

    public static final int CURRENT_VERSION = 3;

    private static final String KEY_VERSION = "version";
    private static final String KEY_EXPORTED_AT = "exportedAtEpochMillis";
    private static final String KEY_SALARY = "salary";
    private static final String KEY_FIRST_TO_FIFTEENTH = "firstToFifteenth";
    private static final String KEY_SIXTEENTH_TO_END = "sixteenthToEnd";
    private static final String KEY_FIXED_BILL_TEMPLATES = "fixedBillTemplates";
    private static final String KEY_CUTOFFS = "cutoffs";
    private static final String KEY_CUTOFF_BILLS = "bills";

    private static final String KEY_BILL_ID = "id";
    private static final String KEY_BILL_TYPE = "type";
    private static final String KEY_BILL_NAME = "name";
    private static final String KEY_BILL_AMOUNT = "amount";
    private static final String KEY_BILL_BANK = "bank";
    private static final String KEY_BILL_ICON_KEY = "iconKey";
    private static final String KEY_BILL_ACTIVE = "active";
    private static final String KEY_BILL_PAID = "paid";
    private static final String KEY_BILL_SORT_ORDER = "sortOrder";
    private static final String KEY_BILL_SOURCE_BILL_ID = "sourceBillId";
    private static final String KEY_BILL_RECURRENCE_SCHEDULE = "recurrenceSchedule";

    private static final String KEY_CUTOFF_ID = "id";
    private static final String KEY_CUTOFF_PERIOD_START = "periodStartEpochDay";
    private static final String KEY_CUTOFF_PERIOD_END = "periodEndEpochDay";
    private static final String KEY_CUTOFF_SALARY_SNAPSHOT = "salarySnapshot";

    private BackupManager() {
    }

    // ---- Export: entities -> JSON ----

    public static JSONObject toJson(@Nullable SalaryEntity salary, List<BillEntity> allBills,
                                     List<CutoffEntity> allCutoffs) throws JSONException {
        JSONObject root = new JSONObject();
        root.put(KEY_VERSION, CURRENT_VERSION);
        root.put(KEY_EXPORTED_AT, System.currentTimeMillis());

        if (salary != null) {
            JSONObject salaryJson = new JSONObject();
            salaryJson.put(KEY_FIRST_TO_FIFTEENTH, salary.getFirstToFifteenth());
            salaryJson.put(KEY_SIXTEENTH_TO_END, salary.getSixteenthToEnd());
            root.put(KEY_SALARY, salaryJson);
        }

        Map<Long, List<BillEntity>> billsByCutoffId = new HashMap<>();
        JSONArray templatesJson = new JSONArray();
        for (BillEntity bill : allBills) {
            if (BillEntity.TYPE_FIXED.equals(bill.getType()) && bill.isTemplate()) {
                templatesJson.put(billToJson(bill));
            } else if (bill.getCutoffId() != null) {
                List<BillEntity> bucket = billsByCutoffId.get(bill.getCutoffId());
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    billsByCutoffId.put(bill.getCutoffId(), bucket);
                }
                bucket.add(bill);
            }
        }
        root.put(KEY_FIXED_BILL_TEMPLATES, templatesJson);

        JSONArray cutoffsJson = new JSONArray();
        for (CutoffEntity cutoff : allCutoffs) {
            JSONObject cutoffJson = new JSONObject();
            cutoffJson.put(KEY_CUTOFF_ID, cutoff.getId());
            cutoffJson.put(KEY_CUTOFF_PERIOD_START, cutoff.getPeriodStartEpochDay());
            cutoffJson.put(KEY_CUTOFF_PERIOD_END, cutoff.getPeriodEndEpochDay());
            cutoffJson.put(KEY_CUTOFF_SALARY_SNAPSHOT, cutoff.getSalarySnapshot());

            JSONArray billsJson = new JSONArray();
            List<BillEntity> bills = billsByCutoffId.get(cutoff.getId());
            if (bills != null) {
                for (BillEntity bill : bills) {
                    billsJson.put(billToJson(bill));
                }
            }
            cutoffJson.put(KEY_CUTOFF_BILLS, billsJson);

            cutoffsJson.put(cutoffJson);
        }
        root.put(KEY_CUTOFFS, cutoffsJson);

        return root;
    }

    private static JSONObject billToJson(BillEntity bill) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_BILL_ID, bill.getId());
        json.put(KEY_BILL_TYPE, bill.getType());
        json.put(KEY_BILL_NAME, bill.getName());
        json.put(KEY_BILL_AMOUNT, bill.getAmount());
        json.put(KEY_BILL_BANK, bill.getBank());
        json.put(KEY_BILL_ICON_KEY, bill.getIconKey());
        json.put(KEY_BILL_ACTIVE, bill.isActive());
        json.put(KEY_BILL_PAID, bill.isPaid());
        json.put(KEY_BILL_SORT_ORDER, bill.getSortOrder());
        json.put(KEY_BILL_RECURRENCE_SCHEDULE, bill.getRecurrenceSchedule());
        if (bill.getSourceBillId() != null) {
            json.put(KEY_BILL_SOURCE_BILL_ID, bill.getSourceBillId());
        }
        return json;
    }

    // ---- Import: JSON -> entities ----

    public static BackupPayload fromJson(String jsonText) throws BackupFormatException {
        try {
            JSONObject root = new JSONObject(jsonText);

            int version = root.optInt(KEY_VERSION, -1);
            if (version < 1 || version > CURRENT_VERSION) {
                throw new BackupFormatException(
                        "This backup file is from an unsupported version (" + version + ").");
            }

            SalaryEntity salary = null;
            if (root.has(KEY_SALARY) && !root.isNull(KEY_SALARY)) {
                JSONObject salaryJson = root.getJSONObject(KEY_SALARY);
                salary = new SalaryEntity(
                        salaryJson.getDouble(KEY_FIRST_TO_FIFTEENTH),
                        salaryJson.getDouble(KEY_SIXTEENTH_TO_END));
            }

            List<BillEntity> templates = new ArrayList<>();
            JSONArray templatesJson = root.optJSONArray(KEY_FIXED_BILL_TEMPLATES);
            if (templatesJson != null) {
                for (int i = 0; i < templatesJson.length(); i++) {
                    templates.add(billFromJson(templatesJson.getJSONObject(i),
                            /*isTemplate=*/ true, /*cutoffId=*/ null));
                }
            }

            List<CutoffBackupGroup> cutoffGroups = new ArrayList<>();
            JSONArray cutoffsJson = root.optJSONArray(KEY_CUTOFFS);
            if (cutoffsJson != null) {
                for (int i = 0; i < cutoffsJson.length(); i++) {
                    JSONObject cutoffJson = cutoffsJson.getJSONObject(i);

                    long oldCutoffId = cutoffJson.getLong(KEY_CUTOFF_ID);
                    CutoffEntity cutoff = new CutoffEntity(
                            cutoffJson.getLong(KEY_CUTOFF_PERIOD_START),
                            cutoffJson.getLong(KEY_CUTOFF_PERIOD_END),
                            cutoffJson.getDouble(KEY_CUTOFF_SALARY_SNAPSHOT));
                    cutoff.setId(oldCutoffId);

                    List<BillEntity> bills = new ArrayList<>();
                    JSONArray billsJson = cutoffJson.optJSONArray(KEY_CUTOFF_BILLS);
                    if (billsJson != null) {
                        for (int b = 0; b < billsJson.length(); b++) {
                            bills.add(billFromJson(billsJson.getJSONObject(b),
                                    /*isTemplate=*/ false, oldCutoffId));
                        }
                    }

                    cutoffGroups.add(new CutoffBackupGroup(cutoff, bills));
                }
            }

            return new BackupPayload(salary, templates, cutoffGroups);
        } catch (JSONException e) {
            throw new BackupFormatException("This file isn't a valid backup.", e);
        }
    }

    private static BillEntity billFromJson(JSONObject json, boolean isTemplate,
                                            @Nullable Long cutoffId) throws JSONException {
        Long sourceBillId = json.has(KEY_BILL_SOURCE_BILL_ID)
                ? json.getLong(KEY_BILL_SOURCE_BILL_ID) : null;
        String iconKey = json.isNull(KEY_BILL_ICON_KEY) ? null : json.optString(KEY_BILL_ICON_KEY, null);
        String bank = json.optString(KEY_BILL_BANK, com.app.cutoff.utils.Constants.DEFAULT_BILL_BANK);

        BillEntity bill = new BillEntity(
                json.getString(KEY_BILL_TYPE),
                json.getString(KEY_BILL_NAME),
                json.getDouble(KEY_BILL_AMOUNT),
                iconKey,
                isTemplate,
                cutoffId,
                sourceBillId,
                json.optBoolean(KEY_BILL_ACTIVE, false),
                json.optBoolean(KEY_BILL_PAID, false),
                json.optInt(KEY_BILL_SORT_ORDER, 0), bank);
        bill.setId(json.getLong(KEY_BILL_ID));
        bill.setRecurrenceSchedule(json.optString(KEY_BILL_RECURRENCE_SCHEDULE,
                BillEntity.RECURRENCE_BOTH_CUTOFFS));
        return bill;
    }

    /** Everything parsed out of a backup file, still carrying the original (pre-import) ids. */
    public static final class BackupPayload {
        @Nullable public final SalaryEntity salary;
        public final List<BillEntity> fixedBillTemplates;
        public final List<CutoffBackupGroup> cutoffs;

        BackupPayload(@Nullable SalaryEntity salary, List<BillEntity> fixedBillTemplates,
                      List<CutoffBackupGroup> cutoffs) {
            this.salary = salary;
            this.fixedBillTemplates = fixedBillTemplates;
            this.cutoffs = cutoffs;
        }
    }

    /** One cutoff plus the bills/incentives that belonged to it, grouped for re-insertion. */
    public static final class CutoffBackupGroup {
        public final CutoffEntity cutoff;
        public final List<BillEntity> bills;

        CutoffBackupGroup(CutoffEntity cutoff, List<BillEntity> bills) {
            this.cutoff = cutoff;
            this.bills = bills;
        }
    }
}
