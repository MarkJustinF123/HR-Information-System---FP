import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter.SortKey;

import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.util.regex.Pattern;

public class HRManagementSystem extends JFrame {

    // Components
    private JTabbedPane tabbedPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    // Form Components
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtPosition, txtSalary;
    private JComboBox<String> cmbDepartment;

    public HRManagementSystem() {
        setTitle("HR Information System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        JPanel viewPanel = createViewPanel();
        tabbedPane.addTab("View Employee List", viewPanel);

        JPanel addPanel = createAddPanel();
        tabbedPane.addTab("Add New Entry", addPanel);

        add(tabbedPane);

        loadData("");
    }

    // TAB 1 - VIEW PANEL 
    private JPanel createViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Search by Name");
        JButton btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(255, 100, 100));
        btnDelete.setForeground(Color.WHITE);

        searchPanel.add(new JLabel("Search Name: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnDelete);

        String[] columns = {"ID", "Full Name", "Position", "Department", "Salary", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);

        // STATIC COLUMNS
        table.getTableHeader().setReorderingAllowed(false);

        // CUSTOM SORTER
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        sorter.setComparator(0, (a, b) -> Integer.compare((int) a, (int) b));
        sorter.setComparator(4, (a, b) -> Double.compare((double) a, (double) b));

        sorter.setSortsOnUpdates(false);
        table.setRowSorter(sorter);

        // TRIPLE-CLICK SORTING
        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                var keys = sorter.getSortKeys();

                if (!keys.isEmpty() && keys.get(0).getColumn() == col) {
                    SortOrder order = keys.get(0).getSortOrder();

                    if (order == SortOrder.ASCENDING) {
                        sorter.setSortKeys(java.util.List.of(new SortKey(col, SortOrder.DESCENDING)));
                    } else if (order == SortOrder.DESCENDING) {
                        sorter.setSortKeys(null);
                    } else {
                        sorter.setSortKeys(java.util.List.of(new SortKey(col, SortOrder.ASCENDING)));
                    }
                } else {
                    sorter.setSortKeys(java.util.List.of(new SortKey(col, SortOrder.ASCENDING)));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        btnSearch.addActionListener(e -> performSearch());
        btnDelete.addActionListener(e -> deleteEmployee());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // TAB 2 - ADD PANEL
    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtPosition = new JTextField(20);
        txtSalary = new JTextField(20);
        cmbDepartment = new JComboBox<>();
        
        loadDepartments();

        addToGrid(panel, new JLabel("First Name:"), 0, 0, gbc);
        addToGrid(panel, txtFirstName, 1, 0, gbc);

        addToGrid(panel, new JLabel("Last Name:"), 0, 1, gbc);
        addToGrid(panel, txtLastName, 1, 1, gbc);

        addToGrid(panel, new JLabel("Email:"), 0, 2, gbc);
        addToGrid(panel, txtEmail, 1, 2, gbc);

        addToGrid(panel, new JLabel("Phone:"), 0, 3, gbc);
        addToGrid(panel, txtPhone, 1, 3, gbc);

        addToGrid(panel, new JLabel("Position (Type Manual):"), 0, 4, gbc);
        addToGrid(panel, txtPosition, 1, 4, gbc);

        addToGrid(panel, new JLabel("Department:"), 0, 5, gbc);
        addToGrid(panel, cmbDepartment, 1, 5, gbc);

        addToGrid(panel, new JLabel("Salary Amount:"), 0, 6, gbc);
        addToGrid(panel, txtSalary, 1, 6, gbc);

        JButton btnSave = new JButton("Save Entry");
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 7;
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> saveEmployee());

        return panel;
    }

    private void addToGrid(JPanel p, Component c, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        p.add(c, gbc);
    }

    // UPDATED SEARCH 
    private void performSearch() {
        String query = searchField.getText().trim();

        try {
            if (Pattern.compile("[0-9]").matcher(query).find()) {
                throw new IllegalArgumentException("Invalid Input: Use Name Only (No Numbers).");
            }

            int rowCount = loadData(query);

            if (rowCount == 0) {
                JOptionPane.showMessageDialog(this, "No entries found.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Search Error", JOptionPane.ERROR_MESSAGE);
            searchField.setText("");
        }
    }

    private void deleteEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int empId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete ID: " + empId + "?");
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                String sql = "DELETE FROM PersonalInfo WHERE employee_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, empId);
                pstmt.executeUpdate();
                
                loadData("");
                JOptionPane.showMessageDialog(this, "Deleted Successfully.");
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveEmployee() {
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            String positionInput = txtPosition.getText().trim();
            int posId = getOrCreatePositionId(conn, positionInput);

            String deptName = (String) cmbDepartment.getSelectedItem();
            int deptId = getDeptIdByName(conn, deptName);

            String sqlInfo = "INSERT INTO PersonalInfo (first_name, last_name, email, phone_number, dept_id, position_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING employee_id";
            PreparedStatement pstmtInfo = conn.prepareStatement(sqlInfo);
            pstmtInfo.setString(1, txtFirstName.getText());
            pstmtInfo.setString(2, txtLastName.getText());
            pstmtInfo.setString(3, txtEmail.getText());
            pstmtInfo.setString(4, txtPhone.getText());
            pstmtInfo.setInt(5, deptId);
            pstmtInfo.setInt(6, posId);
            
            ResultSet rs = pstmtInfo.executeQuery();
            int newEmpId = 0;
            if (rs.next()) newEmpId = rs.getInt(1);

            String sqlSalary = "INSERT INTO Salary (employee_id, amount) VALUES (?, ?)";
            PreparedStatement pstmtSalary = conn.prepareStatement(sqlSalary);
            pstmtSalary.setInt(1, newEmpId);
            pstmtSalary.setDouble(2, Double.parseDouble(txtSalary.getText()));
            pstmtSalary.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Employee Added Successfully!");
            
            txtFirstName.setText(""); txtLastName.setText(""); txtEmail.setText(""); 
            txtPhone.setText(""); txtPosition.setText(""); txtSalary.setText("");
            loadData("");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Saving: " + e.getMessage());
        }
    }

    private int getOrCreatePositionId(Connection conn, String title) throws SQLException {
        String checkSql = "SELECT position_id FROM Positions WHERE title ILIKE ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, title);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) return rs.getInt("position_id");

        String insertSql = "INSERT INTO Positions (title, base_salary_range) VALUES (?, 'TBD') RETURNING position_id";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
        insertStmt.setString(1, title);
        ResultSet rs2 = insertStmt.executeQuery();
        if (rs2.next()) return rs2.getInt(1);

        return 1;
    }

    private int getDeptIdByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT dept_id FROM Department WHERE dept_name = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 1;
    }

    // UPDATED loadData 
    private int loadData(String searchName) {
        tableModel.setRowCount(0);
        int rowCounter = 0;

        String sql = "SELECT p.employee_id, p.first_name, p.last_name, pos.title, d.dept_name, " +
                     "s.amount, p.email, p.phone_number " +
                     "FROM PersonalInfo p " +
                     "JOIN Department d ON p.dept_id = d.dept_id " +
                     "JOIN Positions pos ON p.position_id = pos.position_id " +
                     "JOIN Salary s ON p.employee_id = s.employee_id ";

        if (!searchName.isEmpty()) {
            sql += "WHERE p.first_name ILIKE ? " +
                   "OR p.last_name ILIKE ? " +
                   "OR (p.first_name || ' ' || p.last_name) ILIKE ? ";
        }

        sql += "ORDER BY p.employee_id ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (!searchName.isEmpty()) {
                String wildcard = "%" + searchName + "%";
                pstmt.setString(1, wildcard);
                pstmt.setString(2, wildcard);
                pstmt.setString(3, wildcard);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("employee_id"));
                row.add(rs.getString("first_name") + " " + rs.getString("last_name"));
                row.add(rs.getString("title"));
                row.add(rs.getString("dept_name"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone_number"));
                tableModel.addRow(row);
                rowCounter++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (table.getRowSorter() != null) {
            table.getRowSorter().setSortKeys(null);
        }

        return rowCounter;
    }

    private void loadDepartments() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT dept_name FROM Department")) {
            
            while (rs.next()) {
                cmbDepartment.addItem(rs.getString("dept_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HRManagementSystem().setVisible(true));
    }
}
