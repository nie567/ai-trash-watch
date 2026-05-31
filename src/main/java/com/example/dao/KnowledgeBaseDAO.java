package com.example.dao;

import com.example.model.KnowledgeBase;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 知识库数据访问层
 */
public class KnowledgeBaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseDAO.class);


    /**
     * 查询所有知识条目
     */
    public List<KnowledgeBase> findAll() {
        List<KnowledgeBase> list = new ArrayList<>();
        String sql = "SELECT * FROM knowledge_base ORDER BY id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractKnowledgeBase(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有知识条目失败", e);
        }
        return list;
    }

    /**
     * 按垃圾分类类型查询知识条目
     */
    public List<KnowledgeBase> findByType(String garbageType) {
        List<KnowledgeBase> list = new ArrayList<>();
        String sql = "SELECT * FROM knowledge_base WHERE garbage_type = ? ORDER BY id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, garbageType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractKnowledgeBase(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按类型查询知识条目失败, garbageType={}", garbageType, e);
        }
        return list;
    }

    /**
     * 新增知识条目
     */
    public boolean insert(KnowledgeBase kb) {
        String sql = "INSERT INTO knowledge_base (title, garbage_type, content, image_path, create_time) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kb.getTitle());
            ps.setString(2, kb.getGarbageType());
            ps.setString(3, kb.getContent());
            ps.setString(4, kb.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("插入知识条目失败", e);
        }
        return false;
    }

    /**
     * 更新知识条目
     */
    public boolean update(KnowledgeBase kb) {
        String sql = "UPDATE knowledge_base SET title = ?, garbage_type = ?, content = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kb.getTitle());
            ps.setString(2, kb.getGarbageType());
            ps.setString(3, kb.getContent());
            ps.setString(4, kb.getImagePath());
            ps.setLong(5, kb.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("更新知识条目失败, id={}", kb.getId(), e);
        }
        return false;
    }

    /**
     * 删除知识条目
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM knowledge_base WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("删除知识条目失败, id={}", id, e);
        }
        return false;
    }

    /**
     * 从 ResultSet 提取知识库对象
     */
    private KnowledgeBase extractKnowledgeBase(ResultSet rs) throws SQLException {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(rs.getLong("id"));
        kb.setTitle(rs.getString("title"));
        kb.setGarbageType(rs.getString("garbage_type"));
        kb.setContent(rs.getString("content"));
        kb.setImagePath(rs.getString("image_path"));
        kb.setCreateTime(rs.getTimestamp("create_time"));
        return kb;
    }
}
