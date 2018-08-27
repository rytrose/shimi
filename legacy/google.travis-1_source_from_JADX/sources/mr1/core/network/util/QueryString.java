package mr1.core.network.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class QueryString {
    private String query = "";

    public QueryString(String name, String value) {
        encode(name, value);
    }

    public void add(String name, String value) {
        if (!this.query.equals("")) {
            this.query += "&";
        }
        encode(name, value);
    }

    private void encode(String name, String value) {
        try {
            this.query += URLEncoder.encode(name, "UTF-8");
            this.query += "=";
            this.query += URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Broken VM does not support UTF-8");
        }
    }

    public String getQuery() {
        return this.query;
    }

    public String toString() {
        return getQuery();
    }
}
