package com.kc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kc.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询所有启用的角色
     */
    List<SysRole> listEnabledRoles();

    /**
     * 为角色分配菜单权限
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 获取所有菜单(树形结构)
     */
    List<MenuTreeNode> getMenuTree();

    /**
     * 获取用户的角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 为用户分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 菜单树节点DTO
     */
    class MenuTreeNode {
        private Long id;
        private Long parentId;
        private String menuName;
        private String menuCode;
        private Integer menuType;
        private Boolean checked;

        public MenuTreeNode() {}

        public MenuTreeNode(Long id, Long parentId, String menuName, String menuCode, Integer menuType) {
            this.id = id;
            this.parentId = parentId;
            this.menuName = menuName;
            this.menuCode = menuCode;
            this.menuType = menuType;
            this.checked = false;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getMenuName() { return menuName; }
        public void setMenuName(String menuName) { this.menuName = menuName; }
        public String getMenuCode() { return menuCode; }
        public void setMenuCode(String menuCode) { this.menuCode = menuCode; }
        public Integer getMenuType() { return menuType; }
        public void setMenuType(Integer menuType) { this.menuType = menuType; }
        public Boolean getChecked() { return checked; }
        public void setChecked(Boolean checked) { this.checked = checked; }
    }
}
