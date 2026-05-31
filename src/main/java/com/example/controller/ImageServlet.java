package com.example.controller;

import com.example.util.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片访问Servlet
 * 提供对本地图片文件的HTTP访问
 */
@WebServlet(name = "ImageServlet", urlPatterns = {"/image/*"})
public class ImageServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ImageServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少图片路径");
            return;
        }

        // 去掉开头的斜杠
        String imagePath = pathInfo.substring(1);

        // 选定允许访问的根目录（白名单）+ 请求的文件名
        File baseDir;
        String fileName;
        if (imagePath.startsWith("output/")) {
            baseDir = new File(AppConfig.getDjlOutputDir());
            fileName = imagePath.substring("output/".length());
        } else if (imagePath.startsWith("upload/")) {
            baseDir = new File(AppConfig.getUploadDir());
            fileName = imagePath.substring("upload/".length());
        } else if (imagePath.startsWith("input/")) {
            baseDir = new File(AppConfig.getDjlInputDir());
            fileName = imagePath.substring("input/".length());
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的图片路径格式");
            return;
        }

        // 文件名只允许：字母/数字/下划线/点/短横，拒绝路径分隔符与相对路径片段
        if (!isSafeFileName(fileName)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "非法文件名");
            return;
        }

        // 用 canonical path 比对父目录前缀，彻底阻断路径遍历、符号链接逃逸
        File canonicalBase = baseDir.getCanonicalFile();
        File targetFile = new File(canonicalBase, fileName).getCanonicalFile();
        if (!targetFile.getPath().startsWith(canonicalBase.getPath() + File.separator)
                && !targetFile.getPath().equals(canonicalBase.getPath())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "非法路径");
            return;
        }

        if (!targetFile.exists() || !targetFile.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "图片文件不存在");
            return;
        }

        // 仅允许图片扩展名
        String lowerName = targetFile.getName().toLowerCase();
        String contentType;
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            contentType = "image/png";
        } else if (lowerName.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (lowerName.endsWith(".bmp")) {
            contentType = "image/bmp";
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "不支持的文件类型");
            return;
        }

        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        Files.copy(targetFile.toPath(), resp.getOutputStream());
    }

    /**
     * 安全校验文件名：拒绝路径分隔符、空字节、相对路径片段。
     * 允许中文等 Unicode 字符（用户上传的文件可能包含中文）。
     */
    private boolean isSafeFileName(String name) {
        if (name == null || name.isEmpty()) return false;
        if (name.equals(".") || name.equals("..")) return false;
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) return false;
        if (name.indexOf('\0') >= 0) return false;
        return true;
    }
}
