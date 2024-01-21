package forms;

import classes.Course;
import classes.Faculty;
import classes.Programs;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.DataHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class CourseWindow extends JFrame {
    private JPanel CourseWindow;
    private JComboBox facultyCB;
    private JButton addCourseButton;
    private JButton clearButton;
    private JComboBox departmentCB;
    private JTextField courseNameTF;
    private JTextField searchInputField;
    private JButton searchButton;
    private JLabel studentNumberLabel;
    private JTextField courseCodeTF;
    private JLabel studentNameLabel;
    private JLabel departLabel;
    private JComboBox lecturerCB;
    private JTable searchTable;

    Faculty faculty = new Faculty();
    String facultyName = faculty.facultyName;

    Programs programs = new Programs();
    String programName = programs.programName;

    public CourseWindow() {
        setTitle("Add New Course");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(400, 700);
        setLocation(550,250);
        setContentPane(CourseWindow);

        DefaultTableModel searchTableModel = new DefaultTableModel();
        searchTableModel.addColumn("Course ID");
        searchTableModel.addColumn("Course Name");
        searchTableModel.addColumn("Lecturer");
        searchTableModel.addRow(new Object[]{"courseCode", "courseName", "Lecturer"});

        List<List<String>> courses = DataHandler.getAllCourses();
        if (courses != null) {
            for (int i = 0; i < courses.get(0).size(); i++) {
                String courseCode = courses.get(0).get(i);
                String courseName = courses.get(1).get(i);
                String lecturer = courses.get(2).get(i);

                searchTableModel.addRow(new Object[]{courseCode, courseName, lecturer});
            }
        }

        searchTable.setModel(searchTableModel);

        var facultyNames = DataHandler.getFacultyNames();
        for (String name : facultyNames) {
            facultyCB.addItem(name);
        }
        facultyCB.setSelectedIndex(-1);

        if (facultyCB.getSelectedIndex() == -1) {
            departmentCB.setEnabled(false);
            lecturerCB.setEnabled(false);
        }

        if (!departmentCB.isEnabled()) {
            departmentCB.setToolTipText("You have to choose faculty for see departments.");
            lecturerCB.setEnabled(false);
        } else {
            departmentCB.setToolTipText("");
        }

        if (!lecturerCB.isEnabled()) {
            lecturerCB.setToolTipText("You have to choose program for see lecturers.");
        } else {
            lecturerCB.setToolTipText("");
        }

        if (departmentCB.getSelectedIndex() == -1) {
            lecturerCB.setEnabled(false);
        }

        facultyCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (facultyCB.getSelectedIndex() != -1) {
                    departmentCB.setEnabled(true);
                    departmentCB.removeAllItems();

                    var programNames = DataHandler.getProgramNames(facultyCB.getSelectedItem().toString());
                    for (String programName : programNames) {
                        departmentCB.addItem(programName);
                    }

                    departmentCB.setSelectedIndex(-1);
                    lecturerCB.setEnabled(false);
                    lecturerCB.removeAllItems();
                }
            }
        });

        departmentCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lecturerCB.setEnabled(true);
                lecturerCB.removeAllItems();
                lecturerCB.setSelectedIndex(-1);

                if (departmentCB.getSelectedIndex() != -1) {
                    var lecturers = DataHandler.getLecturers(facultyCB.getSelectedItem().toString(), departmentCB.getSelectedItem().toString());
                    for (String lecturer : lecturers) {
                        lecturerCB.addItem(lecturer);
                    }
                }
            }
        });

        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (courseCodeTF.getText() == null || courseNameTF.getText() == null || lecturerCB.getSelectedItem() == null || facultyCB.getSelectedItem() == null || departmentCB.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(null, "Please fill in all blank areas!", "Blank Areas", JOptionPane.WARNING_MESSAGE);
                } else {
                    facultyName = facultyCB.getSelectedItem().toString();
                    programName = departmentCB.getSelectedItem().toString();

                    Course newCourse = new Course();
                    newCourse.courseCode = courseCodeTF.getText();
                    newCourse.courseName = courseNameTF.getText();
                    newCourse.lecturer = lecturerCB.getSelectedItem().toString();

                    var jsonContent = DataHandler.readJsonFile("src/data/Courses.json");

                    for (JsonElement facultyElement : jsonContent) {
                        JsonObject facultyObject = facultyElement.getAsJsonObject();
                        JsonArray facultiesArray = facultyObject.getAsJsonArray("faculties");

                        for (JsonElement faculty : facultiesArray) {
                            JsonObject facultyJson = faculty.getAsJsonObject();
                            String currentFacultyName = facultyJson.get("facultyName").getAsString();

                            if (currentFacultyName.equals(facultyName)) {
                                JsonArray programsArray = facultyJson.getAsJsonArray("programs");

                                for (JsonElement program : programsArray) {
                                    JsonObject programJson = program.getAsJsonObject();
                                    String currentProgramName = programJson.get("programName").getAsString();

                                    if (currentProgramName.equals(programName)) {
                                        JsonArray coursesArray = programJson.getAsJsonArray("courses");
                                        coursesArray.add(new Gson().toJsonTree(newCourse));

                                        DataHandler.saveJsonData("src/data/Courses.json", jsonContent);
                                        JOptionPane.showMessageDialog(null, "Course added successfully!", "Great!", JOptionPane.INFORMATION_MESSAGE);

                                        searchTableModel.setRowCount(0);

                                        List<List<String>> courses = DataHandler.getAllCourses();
                                        if (courses != null) {
                                            for (int i = 0; i < courses.get(0).size(); i++) {
                                                String courseCode = courses.get(0).get(i);
                                                String courseName = courses.get(1).get(i);
                                                String lecturer = courses.get(2).get(i);

                                                searchTableModel.addRow(new Object[]{courseCode, courseName, lecturer});
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                courseCodeTF.setText("");
                courseNameTF.setText("");

                facultyCB.setSelectedIndex(-1);
                departmentCB.setSelectedIndex(-1);
                lecturerCB.setSelectedIndex(-1);
                departmentCB.removeAllItems();
                lecturerCB.removeAllItems();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var givenID = searchInputField.getText();

                if (givenID.isEmpty() || givenID.isBlank()) {
                    JOptionPane.showMessageDialog(null, "No matching courses found!", "Error", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    var results = DataHandler.getCourseByID(givenID);

                    StringBuilder resultMessage = new StringBuilder("Matching results:\n");

                    for (JsonObject result : results) {
                        resultMessage
                                .append("ID: ").append(result.get("courseCode").getAsString()).append("\n")
                                .append("Name: ").append(result.get("courseName").getAsString()).append("\n")
                                .append("Lecturer: ").append(result.get("lecturer").getAsString()).append("\n");
                    }

                    JDialog customDialog = new JDialog();
                    customDialog.setTitle("Search Results");
                    customDialog.setModal(true);
                    customDialog.setSize(300, 200);
                    customDialog.setLocation(600,275);

                    JTextArea textArea = new JTextArea(resultMessage.toString());
                    JScrollPane scrollPane = new JScrollPane(textArea);

                    customDialog.add(scrollPane);
                    textArea.setEditable(false);
                    textArea.setEnabled(false);
                    customDialog.setVisible(true);
                }
            }
        });

        setVisible(true);
        searchInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String searchInput = searchInputField.getText().toLowerCase().trim();

                TableRowSorter<DefaultTableModel> sorting = new TableRowSorter<>(searchTableModel);
                searchTable.setRowSorter(sorting);

                sorting.setRowFilter(RowFilter.regexFilter("(?i).*" + searchInput + ".*"));
            }
        });
    }

    public static void main(String[] args) {
        new CourseWindow();
    }
}
