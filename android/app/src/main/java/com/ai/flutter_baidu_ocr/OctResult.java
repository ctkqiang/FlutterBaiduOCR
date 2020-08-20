package com.ai.flutter_baidu_ocr;

import java.util.Map;

public class OctResult {
    private String filePath;
    private Map body;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map getBody() {
        return body;
    }

    public void setBody(Map body) {
        this.body = body;
    }
}
