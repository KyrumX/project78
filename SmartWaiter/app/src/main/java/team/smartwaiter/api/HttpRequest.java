package team.smartwaiter.api;

import android.os.AsyncTask;

abstract class HttpRequest extends AsyncTask<String, Void, String> {
    protected static final String REQUEST_AGENT = "Mozilla/5.0";
    protected static final String REQUEST_ACCEPT = "application/json";
    protected static final int READ_TIMEOUT = 15000;
    protected static final int CONNECTION_TIMEOUT = 15000;
}
