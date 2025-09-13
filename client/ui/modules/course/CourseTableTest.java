package client.ui.modules.course;

import javax.swing.*;
import java.awt.*;

/**
 * CourseTablePanel测试类
 * 用于测试课程表格的显示和功能
 */
public class  CourseTableTest {
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("课程表格测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);
            frame.setLocationRelativeTo(null);
            
            // 创建课程表格面板
            CourseTablePanel courseTablePanel = new CourseTablePanel();
            
            // 添加搜索面板
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("搜索");
            JButton refreshButton = new JButton("刷新");
            
            searchButton.addActionListener(e -> {
                String searchText = searchField.getText().trim();
                if (!searchText.isEmpty()) {
                    courseTablePanel.searchByCourseName(searchText);
                } else {
                    courseTablePanel.refreshData();
                }
            });
            
            refreshButton.addActionListener(e -> courseTablePanel.refreshData());
            
            searchPanel.add(new JLabel("搜索课程:"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            searchPanel.add(refreshButton);
            
            // 添加状态标签
            JLabel statusLabel = new JLabel("课程总数: 0");
            searchPanel.add(statusLabel);
            
            // 更新状态标签
            SwingUtilities.invokeLater(() -> {
                int courseCount = courseTablePanel.getCourseTable().getRowCount();
                statusLabel.setText("课程总数: " + courseCount);
            });
            
            // 设置布局
            frame.setLayout(new BorderLayout());
            frame.add(searchPanel, BorderLayout.NORTH);
            frame.add(courseTablePanel, BorderLayout.CENTER);
            
            // 添加表格选择监听器
            courseTablePanel.getCourseTable().getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = courseTablePanel.getCourseTable().getSelectedRow();
                    if (selectedRow >= 0) {
                        System.out.println("选中第 " + (selectedRow + 1) + " 行");
                        // 可以在这里添加更多选择处理逻辑
                    }
                }
            });
            
            frame.setVisible(true);
        });
    }
}