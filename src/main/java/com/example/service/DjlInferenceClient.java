package com.example.service;

import com.example.util.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * DJL 推理服务 HTTP 客户端
 * 调用 djl-spring-boot-starter-demo 的 /inference 接口
 * DJL 服务端直接从 D:\ny\data_set\input 读取图片，结果写入 D:\ny\data_set\output
 */
public class DjlInferenceClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对指定图片文件进行垃圾检测
     * 调用 DJL 服务的 /inference 接口，DJL 服务端直接从 input 目录读取图片
     *
     * @param fileName 文件名（如 "test.jpg"），位于 D:\ny\data_set\input 目录
     * @return 推理结果
     */
    public static InferenceResult detect(String fileName) throws Exception {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        String urlStr = AppConstants.DJL_INFERENCE_URL + "/inference?file=" + encodedFileName + "&generateOutputImage=true";

        URI uri = new URI(urlStr);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000); // 推理可能较慢，设置60秒超时
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            conn.disconnect();
            throw new RuntimeException("DJL 推理服务返回错误状态: " + responseCode + ", 响应: " + errorResponse);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return mapper.readValue(response.toString(), InferenceResult.class);
    }
}