package com.app.cutoff.data.backup;

/**
 * Thrown when a backup file can't be parsed (malformed JSON, missing
 * required fields) or is a version this build doesn't know how to read.
 * Kept separate from JSONException/IOException so BackupRepository callers
 * can show a clear, specific message to the user.
 */
public class BackupFormatException extends Exception {

    public BackupFormatException(String message) {
        super(message);
    }

    public BackupFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
