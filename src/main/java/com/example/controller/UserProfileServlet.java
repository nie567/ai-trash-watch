package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import com.example.util.AppConstants;
import com.example.util.AppContext;
import com.example.util.BusinessException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户个人中心控制器
 */
@WebServlet(name = "UserProfileServlet", urlPatterns = {
    "/user/profile",
    "/user/profile/edit",
    "/user/password"
})
public class UserProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileServlet.class);

    
    private static final long serialVersionUID = 1L;
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        userService = AppContext.get().getUserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getServletPath();
        
        try {
            switch (action) {
                case "/user/password":
                    showPasswordForm(request, response);
                    break;
                case "/user/profile/edit":
                    showEditForm(request, response);
                    break;
                case "/user/profile":
                default:
                    showProfile(request, response);
                    break;
            }
        } catch (BusinessException e) {
            handleBusinessException(request, response, e);
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getServletPath();
        
        try {
            if ("/user/password".equals(action)) {
                doChangePassword(request, response);
            } else if ("/user/profile/edit".equals(action)) {
                doUpdateProfile(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/user/profile");
            }
        } catch (BusinessException e) {
            handleBusinessException(request, response, e);
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }
    
    /**
     * 显示个人信息页
     */
    private void showProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_USER);
        
        // 重新从数据库获取最新数据
        User user = userService.getUserById(loginUser.getId());
        request.setAttribute("user", user);
        
        request.getRequestDispatcher("/WEB-INF/jsp/user/profile.jsp").forward(request, response);
    }
    
    /**
     * 显示编辑表单
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_USER);
        
        User user = userService.getUserById(loginUser.getId());
        request.setAttribute("user", user);
        
        request.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(request, response);
    }
    
    /**
     * 显示修改密码表单
     */
    private void showPasswordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(request, response);
    }
    
    /**
     * 更新个人信息
     */
    private void doUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_USER);
        
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        
        userService.updateProfile(loginUser.getId(), email, phone);
        
        // 更新Session中的用户信息
        User updatedUser = userService.getUserById(loginUser.getId());
        session.setAttribute(AppConstants.SESSION_USER, updatedUser);
        
        // AJAX 请求返回 JSON，传统请求 302 重定向
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":200,\"message\":\"个人资料更新成功\"}");
        } else {
            // PRG模式
            response.sendRedirect(request.getContextPath() + "/user/profile?success=updated");
        }
    }
    
    /**
     * 修改密码
     */
    private void doChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_USER);
        
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // 验证新密码确认
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的新密码不一致");
        }
        
        // 验证密码强度（与 BCryptUtil.checkStrength 保持一致）
        if (newPassword.length() < 8) {
            throw new BusinessException(400, "新密码长度不能少于8位");
        }
        
        userService.changePassword(loginUser.getId(), oldPassword, newPassword);
        
        // AJAX 请求返回 JSON，传统请求 302 重定向
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":200,\"message\":\"密码修改成功\"}");
        } else {
            // PRG模式
            response.sendRedirect(request.getContextPath() + "/user/password?success=changed");
        }
    }
    
    /**
     * 处理业务异常 — AJAX 请求返回 JSON，传统请求 forward 到 JSP
     */
    private void handleBusinessException(HttpServletRequest request, HttpServletResponse response,
            BusinessException e) throws ServletException, IOException {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(e.getCode() >= 400 && e.getCode() < 500 ? e.getCode() : 400);
            response.getWriter().write("{\"code\":" + e.getCode() + ",\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            return;
        }
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_USER);
        request.setAttribute(AppConstants.REQUEST_ERROR, e.getMessage());
        request.setAttribute("user", userService.getUserById(loginUser.getId()));
        
        String action = request.getServletPath();
        if ("/user/password".equals(action)) {
            request.getRequestDispatcher("/WEB-INF/jsp/user/password.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/jsp/user/profile-edit.jsp").forward(request, response);
        }
    }
    
    /**
     * 处理系统异常
     */
    private void handleException(HttpServletRequest request, HttpServletResponse response, Exception e)
            throws ServletException, IOException {
        logger.error("unexpected error", e);
        request.setAttribute(AppConstants.REQUEST_ERROR, "系统错误: " + e.getMessage());
        request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
    }
}