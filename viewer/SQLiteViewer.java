package viewer;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.Set;

public class SQLiteViewer extends JFrame {

    private String filename;
    private final String DB_PATH = "jdbc:sqlite:";

    public SQLiteViewer() {

        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        // Primary window configurations
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLayout(new GridBagLayout());
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("SQLite Viewer");




        // Component definitions

        Dimension buttonSize = new Dimension(80, 50 );
        //int largeComponentWidth = getWidth() - 80;

        JTextField filenameField = new JTextField();
        filenameField.setName("FileNameTextField");
        filenameField.setPreferredSize(new Dimension(550, 50));


        JButton openFileButton = new JButton("Open");
        openFileButton.setName("OpenFileButton");
        openFileButton.setPreferredSize(buttonSize);


        JComboBox<String> tablesComboBox = new JComboBox<>();
        tablesComboBox.setName("TablesComboBox");
        tablesComboBox.setPreferredSize(new Dimension(550, 50));


        JTextArea queryTextArea = new JTextArea();
        queryTextArea.setName("QueryTextArea");
        queryTextArea.setPreferredSize(new Dimension(550, 200));
        queryTextArea.setEnabled(false);


        JButton executeButton = new JButton();
        executeButton.setName("ExecuteQueryButton");
        executeButton.setText("Execute");
        executeButton.setPreferredSize(buttonSize);
        executeButton.setEnabled(false);


        JTable table = new JTable();
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        table.setName("Table");
        table.setPreferredSize(new Dimension(550, 400));
        tableModel.addColumn("contact_id");
        tableModel.addColumn("first_name");
        tableModel.addColumn("last_name");
        tableModel.addColumn("email");
        tableModel.addColumn("phone");


        // GridBag layout for components
        GridBagConstraints gbc = new GridBagConstraints();

        //gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(filenameField, gbc);

        gbc.gridx = 1;
        add(openFileButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(tablesComboBox, gbc);

        gbc.gridx = 1;
        add(executeButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(queryTextArea, gbc);

        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(table, gbc);

        setVisible(true);


        // Add functionality to components


        openFileButton.addActionListener(e -> {
            tablesComboBox.removeAllItems();
            filename = filenameField.getText().trim();

            // Check if file exists
            try {
                new File(filename);
            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(new Frame(), "File doesn't exist!");
            }

            // Connect to DB
            try (Connection conn = DriverManager.getConnection(DB_PATH + filename)) {
                Statement stmt = conn.createStatement();

                String checkForEmptyDatabase = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%';";
                ResultSet numberOfTables = stmt.executeQuery(checkForEmptyDatabase);
                int number = numberOfTables.getInt(1);

                // If DB has no tables, wrong file name
                if (number == 0) {
                    JOptionPane.showMessageDialog(new Frame(), "Wrong file name!");
                    queryTextArea.setEnabled(false);
                    executeButton.setEnabled(false);
                    return;
                }

                String query = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';";
                ResultSet rs = stmt.executeQuery(query);

                queryTextArea.setEnabled(true);
                executeButton.setEnabled(true);

                // Get all tables from DB
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    tablesComboBox.addItem(tableName);
                }


            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                JOptionPane.showMessageDialog(new Frame(), "SQL Error!");
                queryTextArea.setEnabled(false);
                executeButton.setEnabled(false);
            }
        });

        // Gives a default Select all query when a table is selected
        tablesComboBox.addActionListener(e -> {
            String sqlSelectAll = "SELECT * FROM %s;";
            String tableName;
            if (tablesComboBox.getSelectedItem() != null) {
                tableName = tablesComboBox.getSelectedItem().toString();
                String fullQuery = String.format(sqlSelectAll, tableName);
                queryTextArea.setText(fullQuery);
            }
        });


        // Gets all rows from the table and adds them to the table
        executeButton.addActionListener(e -> {

            String sqlQuery = queryTextArea.getText().trim();

            if (!sqlQuery.equals("")) {
                try (Connection conn = DriverManager.getConnection(DB_PATH + filename)) {

                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlQuery);

                    while (rs.next()) {
                        tableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(new Frame(), "SQL Error!");
                }
            }
        });
    }
}

