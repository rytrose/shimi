package mr1.core.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpConnection {
    public static String getRequest(String address, List nameValuePairs) {
        Throwable th;
        StringBuffer stringBuffer = new StringBuffer("");
        BufferedReader bufferedReader = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(address));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(httpClient.execute(httpPost).getEntity().getContent()));
            try {
                for (String readLine = bufferedReader2.readLine(); readLine != null; readLine = bufferedReader2.readLine()) {
                    stringBuffer.append(readLine);
                    stringBuffer.append("\n");
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                        bufferedReader = bufferedReader2;
                    } catch (IOException e) {
                        bufferedReader = bufferedReader2;
                    }
                }
            } catch (Exception e2) {
                bufferedReader = bufferedReader2;
            } catch (Throwable th2) {
                th = th2;
                bufferedReader = bufferedReader2;
            }
        } catch (Exception e3) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e4) {
                }
            }
            return stringBuffer.toString();
        } catch (Throwable th3) {
            th = th3;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
        return stringBuffer.toString();
    }
}
