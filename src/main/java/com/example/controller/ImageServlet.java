package com.example.controller;

import com.example.util.AppConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片访问Servlet
 * 提供对本地图片文件的HTTP访问
 */
@WebServlet(name = "ImageServlet", urlPatterns = {"/image/*"})
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少图片路径");
            return;
        }

        // 移除开头的斜杠
        String imagePath = pathInfo.substring(1);

        // 安全检查：防止路径遍历攻击
        if (imagePath.contains("..") || imagePath.contains(":")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "非法路径");
            return;
        }

        // 构建完整文件路径
        // imagePath格式: output/filename.jpg 或 input/filename.jpg
        File imageFile;
        if (imagePath.startsWith("output/")) {
            String fileName = imagePath.substring("output/".length());
            imageFile = new File(AppConstants.DJL_OUTPUT_DIR, fileName);
        } else if (imagePath.startsWith("input/")) {
            String fileName = imagePath.substring("input/".length());
            imageFile = new File(AppConstants.DJL_INPUT_DIR, fileName);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的图片路径格式");
            return;
        }

        // 检查文件是否存在
        if (!imageFile.exists() || !imageFile.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "图片文件不存在: " + imagePath);
            return;
        }

        // 确定Content-Type
        String fileName = imageFile.getName().toLowerCase();
        String contentType;
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (fileName.endsWith(".bmp")) {
            contentType = "image/bmp";
        } else {
            contentType = "application/octet-stream";
        }

        // 设置响应头
        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "public, max-age=86400"); // 缓存1天

        // 读取并写入图片数据
        Path path = imageFile.toPath();
        Files.copy(path, resp.getOutputStream());
    }
}
