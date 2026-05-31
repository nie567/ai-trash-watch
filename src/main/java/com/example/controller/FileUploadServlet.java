package com.example.controller;

import com.example.util.AppConfig;
import com.example.util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用文件上传控制器
 * 保存到 upload/ 子目录，返回可通过 /image/ 访问的相对路径
 */
@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload", "/upload/*"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10 * 1024 * 1024,
    maxRequestSize = 20 * 1024 * 1024
)
public class FileUploadServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(
        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp")
    );

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // 读取上传文件
        Part filePart;
        try {
            filePart = req.getPart("file");
        } catch (Exception e) {
            out.write(Result.error("无法读取上传文件").toJson());
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            out.write(Result.error("请选择要上传的文件").toJson());
            return;
        }

        // 校验文件类型
        String contentType = filePart.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            out.write(Result.error("仅支持 jpg/png/gif/bmp/webp 格式图片").toJson());
            return;
        }

        // 获取原始文件名并校验扩展名
        String originalName = getSubmittedFileName(filePart);
        if (originalName == null || originalName.isEmpty()) {
            originalName = "upload_" + System.currentTimeMillis() + ".jpg";
        }
        String lowerName = originalName.toLowerCase();
        boolean validExt = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) { validExt = true; break; }
        }
        if (!validExt) {
            out.write(Result.error("仅支持 jpg/png/gif/bmp/webp 格式图片").toJson());
            return;
        }

        // 生成唯一文件名并保存
        String ext = lowerName.substring(lowerName.lastIndexOf('.'));
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 保存子目录：从 URL 路径获取，默认 rectification
        String subDir = "rectification";
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String dir = pathInfo.substring(1).replaceAll("[^a-zA-Z0-9_-]", "");
            if (!dir.isEmpty()) subDir = dir;
        }

        File uploadRoot = new File(AppConfig.getUploadDir());
        File targetDir = new File(uploadRoot, subDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = new File(targetDir, savedName);
        try (InputStream is = filePart.getInputStream()) {
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("保存上传文件失败", e);
            out.write(Result.error("文件保存失败，请重试").toJson());
            return;
        }

        // 返回可通过 /image/ 访问的路径
        String imageUrl = "upload/" + subDir + "/" + savedName;
        logger.info("文件上传成功: {}", imageUrl);

        out.write(Result.success(imageUrl).toJson());
    }

    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return null;
    }
}
