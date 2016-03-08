package com.aggregate.dusi;

import java.net.URI;
import java.net.URLEncoder;

/**
 * Created by morfeusys on 06.03.16.
 */
public class URIUtils {
    public static URI parse(String url) throws Exception {
        int pos = url.indexOf('?');
        if (pos > -1 && pos != url.length() - 1) {
            StringBuilder sb = new StringBuilder(url.substring(0, pos + 1));
            String query = url.substring(pos + 1, url.length());
            String[] pairs = query.split("&");
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String name = kv[0].trim();
                    String value = URLEncoder.encode(kv[1], "UTF-8");
                    if (i > 0) sb.append("&");
                    sb.append(name).append("=").append(value);
                }
            }
            url = sb.toString();
        }

        return new URI(url);
    }
}
