package com.example.controller;

import com.example.service.DjlInferenceClient;
import com.example.service.InferenceResult;
import com.example.service.RuleService;
import com.example.util.AppConfig;
import com.example.util.AppContext;
import com.example.util.AppConstants;
import com.example.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
import java.util.*;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 垃圾分类推理控制器
 * 接收前端请求，调用 DJL 推理服务，返回检测结果
 * 增加规则映射逻辑，返回mappedCategory和recommendedCategory
 * 支持文件上传和本地文件选择两种方式
 */
@WebServlet(name = "InferenceServlet", urlPatterns = {"/inference", "/inference/*"})
@jakarta.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10 * 1024 * 1024,
    maxRequestSize = 20 * 1024 * 1024
)
public class InferenceServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(InferenceServlet.class);
    private static final Semaphore inferenceSemaphore = new Semaphore(3);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".bmp", ".gif");
    private RuleService ruleService;

    @Override
    public void init() throws ServletException {
        ruleService = AppContext.get().getRuleService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 列出 input 目录下的所有图片文件
        List<String> imageFiles = listImageFiles(AppConfig.getDjlInputDir());
        req.setAttribute("imageFiles", imageFiles);
        req.getRequestDispatcher("/WEB-INF/jsp/user/garbage-upload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        // 路由: /inference/detect - 上传检测
        if ("/detect".equals(pathInfo)) {
            doDetect(req, resp);
            return;
        }

        // 默认: 兼容旧接口，直接用fileName参数检测
        doDetect(req, resp);
    }

    /**
     * 检测图片并返回含规则映射的结果
     * 支持两种方式：
     * 1. 文件上传（multipart/form-data，字段名file）
     * 2. 本地文件名（fileName参数）
     */
    private void doDetect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();

        if (!inferenceSemaphore.tryAcquire()) {
            out.write(Result.error(429, "推理服务繁忙，请稍后重试").toJson());
            return;
        }

        String fileName = null;
        boolean isUploadedFile = false;

        try {
            Part filePart = req.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                // 有上传文件，保存到input目录
                String originalName = getSubmittedFileName(filePart);
                if (originalName == null || originalName.isEmpty()) {
                    originalName = "uploaded_" + System.currentTimeMillis() + ".jpg";
                }
                // 确保文件名唯一
                fileName = generateUniqueFileName(originalName);
                File targetFile = new File(AppConfig.getDjlInputDir(), fileName);
                try (InputStream is = filePart.getInputStream()) {
                    Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                isUploadedFile = true;
            }
        } catch (Exception e) {
            // 不是multipart请求，忽略
        }

        // 如果没有上传文件，尝试获取fileName参数
        if (fileName == null) {
            fileName = req.getParameter("fileName");
            if (fileName == null || fileName.trim().isEmpty()) {
                out.write(Result.error("请上传图片或指定图片文件名").toJson());
                return;
            }
            fileName = fileName.trim();

            // 检查文件是否存在于 input 目录
            File inputFile = new File(AppConfig.getDjlInputDir(), fileName);
            if (!inputFile.exists() || !inputFile.isFile()) {
                out.write(Result.error("图片文件不存在: " + fileName).toJson());
                return;
            }
        }

        try {
            InferenceResult result = DjlInferenceClient.detect(fileName);
            if (!result.isSuccess()) {
                out.write(Result.error(result.getMessage()).toJson());
                return;
            }

            // 对每个检测结果进行规则映射
            Set<String> categories = new LinkedHashSet<>();
            if (result.getDetectedObjects() != null) {
                for (InferenceResult.DetectedObject obj : result.getDetectedObjects()) {
                    String mappedCategory = ruleService.mapCategory(obj.getClassName());
                    if (mappedCategory != null) {
                        categories.add(mappedCategory);
                    }
                }
            }

            // 计算推荐类别和isMixed
            String recommendedCategory;
            int isMixed;
            if (categories.size() == 0) {
                recommendedCategory = null;
                isMixed = 0;
            } else if (categories.size() == 1) {
                recommendedCategory = categories.iterator().next();
                isMixed = 0;
            } else {
                recommendedCategory = AppConstants.CATEGORY_MIXED;
                isMixed = 1;
            }

            // 处理输出图片路径
            // 微服务返回的outputReference可能是完整路径，需要提取文件名
            String outputImageName = result.getOutputImageName();
            String outputImageFileName = null;
            if (outputImageName != null && !outputImageName.isEmpty()) {
                // 提取文件名（去掉路径部分）
                outputImageFileName = outputImageName.replace("\\", "/");
                if (outputImageFileName.contains("/")) {
                    outputImageFileName = outputImageFileName.substring(outputImageFileName.lastIndexOf("/") + 1);
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("success", true);
            data.put("imageName", fileName);
            data.put("outputImageName", outputImageFileName);
            data.put("recommendedCategory", recommendedCategory);
            data.put("isMixed", isMixed);

            List<Map<String, Object>> detectedList = new ArrayList<>();
            if (result.getDetectedObjects() != null) {
                for (InferenceResult.DetectedObject obj : result.getDetectedObjects()) {
                    String mappedCat = ruleService.mapCategory(obj.getClassName());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("className", obj.getClassName());
                    item.put("confidence", obj.getConfidence());
                    item.put("mappedCategory", mappedCat);
                    detectedList.add(item);
                }
            }
            data.put("detectedObjects", detectedList);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("code", 200);
            response.put("message", "");
            response.put("data", data);

            out.write(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("unexpected error", e);
            out.write(Result.error("检测服务暂不可用，请稍后重试").toJson());
        } finally {
            inferenceSemaphore.release();
        }
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

    private String generateUniqueFileName(String originalName) {
        // 在文件名前加时间戳确保唯一
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx > 0) {
            return System.currentTimeMillis() + "_" + originalName.substring(0, dotIdx) + originalName.substring(dotIdx);
        }
        return System.currentTimeMillis() + "_" + originalName;
    }

    private List<String> listImageFiles(String dirPath) {
        List<String> imageFiles = new ArrayList<>();
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return imageFiles;
        }

        File[] files = dir.listFiles((d, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : IMAGE_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });

        if (files != null) {
            for (File f : files) {
                imageFiles.add(f.getName());
            }
        }
        return imageFiles;
    }
}
