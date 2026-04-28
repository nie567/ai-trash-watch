package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import com.example.util.AppConstants;
import com.example.util.BusinessException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户个人中心控制器
 */
@WebServlet(name = "UserProfileServlet", urlPatterns = {
    "/user/profile",
    "/user/profile/edit",
    "/user/password"
})
public class UserProfileServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        userService = new UserService();
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
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_LOGIN_USER);
        
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
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_LOGIN_USER);
        
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
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_LOGIN_USER);
        
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        
        userService.updateProfile(loginUser.getId(), email, phone);
        
        // 更新Session中的用户信息
        User updatedUser = userService.getUserById(loginUser.getId());
        session.setAttribute(AppConstants.SESSION_LOGIN_USER, updatedUser);
        
        // PRG模式
        response.sendRedirect(request.getContextPath() + "/user/profile?success=updated");
    }
    
    /**
     * 修改密码
     */
    private void doChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_LOGIN_USER);
        
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // 验证新密码确认
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的新密码不一致");
        }
        
        // 验证密码强度（简单校验）
        if (newPassword.length() < 6) {
            throw new BusinessException(400, "新密码长度不能少于6位");
        }
        
        userService.changePassword(loginUser.getId(), oldPassword, newPassword);
        
        // PRG模式
        response.sendRedirect(request.getContextPath() + "/user/password?success=changed");
    }
    
    /**
     * 处理业务异常
     */
    private void handleBusinessException(HttpServletRequest request, HttpServletResponse response,
            BusinessException e) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute(AppConstants.SESSION_LOGIN_USER);
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
        e.printStackTrace();
        request.setAttribute(AppConstants.REQUEST_ERROR, "系统错误: " + e.getMessage());
        request.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(request, response);
    }
}