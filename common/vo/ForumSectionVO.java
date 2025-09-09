package common.vo;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 论坛分区（板块）值对象
 */
public class ForumSectionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sectionId;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status; // 0-禁用, 1-启用
    private Timestamp createdTime;

    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Timestamp getCreatedTime() { return createdTime; }
    public void setCreatedTime(Timestamp createdTime) { this.createdTime = createdTime; }
}


