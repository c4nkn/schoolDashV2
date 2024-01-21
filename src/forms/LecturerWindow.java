package forms;
import classes.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.DataHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LecturerWindow extends JFrame {
    private JPanel LecturerWindow;
    private JLabel studentNumberLabel;
    private JTextField lecturerIdTF;
    private JLabel studentNameLabel;
    private JTextField lecturerNameTF;
    private JLabel surnameLabel;
    private JTextField lecturerSurnameTF;
    private JLabel facultyLabel;
    private JComboBox facultyCB;
    private JComboBox departmentCB;
    private JButton addCourseButton;
    private JTextArea courseTextArea;
    private JButton addLecturerButton;
    private JButton clearButton;
    private JTextField searchInputTF;
    private JButton searchButton;
    private JLabel searchLabel;
    private JTable searchTable;

    Faculty faculty = new Faculty();
    String facultyName = faculty.facultyName;

    Programs programs = new Programs();
    String programName = programs.programName;

    private int newID;
    public List<String> selectedCourses = new ArrayList<String>();

    public LecturerWindow() {
        setTitle("Add New Lecturer");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(400, 700);
        setLocation(550, 250);
        setContentPane(LecturerWindow);

        DefaultTableModel searchTableModel = new DefaultTableModel();
        searchTableModel.addColumn("Lecturer ID");
        searchTableModel.addColumn("Lecturer Name");
        searchTableModel.addColumn("Lecturer Surname");
        searchTableModel.addColumn("Faculty");
        searchTableModel.addColumn("Program");
        searchTableModel.addRow(new Object[]{"ID", "Name", "Surname", "Faculty", "Program"});

        List<JsonObject> lecturers = DataHandler.getAllLecturers();

        if (lecturers != null && !lecturers.isEmpty()) {
            for (JsonObject lecturer : lecturers) {
                String lecturerName = lecturer.get("studentName").getAsString();
                String lecturerSurname = lecturer.get("studentSurname").getAsString();
                String lecturerID = lecturer.get("studentNumber").getAsString();
                String faculty = lecturer.get("faculty").getAsString();
                String program = lecturer.get("program").getAsString();

                String[] lecturerData = {lecturerID, lecturerName, lecturerSurname, faculty, program};
                searchTableModel.addRow(lecturerData);
            }
        }

        searchTable.setModel(searchTableModel);

        newID = createLecturerID();
        lecturerIdTF.setText(String.valueOf(newID));

        var facultyNames = DataHandler.getFacultyNames();
        for (String name : facultyNames) {
            facultyCB.addItem(name);
        }
        facultyCB.setSelectedIndex(-1);

        if (facultyCB.getSelectedIndex() == -1) {
            departmentCB.setEnabled(false);
            addCourseButton.setEnabled(false);
        }

        if (!departmentCB.isEnabled()) {
            departmentCB.setToolTipText("You have to choose faculty for see departments.");
            addCourseButton.setEnabled(false);
        }

        if (departmentCB.getSelectedIndex() == -1) {
            addCourseButton.setEnabled(false);
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
                    addCourseButton.setEnabled(false);
                    courseTextArea.setText("");
                    selectedCourses.clear();
                }
            }
        });

        departmentCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (departmentCB.getSelectedIndex() != -1) {
                    addCourseButton.setEnabled(true);
                    courseTextArea.setText("");
                    selectedCourses.clear();
                }
            }
        });

        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<List<String>> coursesList = DataHandler.getCourses(facultyCB.getSelectedItem().toString(), departmentCB.getSelectedItem().toString());

                List<String> courseCodeList = coursesList.get(0);
                List<String> courseNameList = coursesList.get(1);
                List<String> lecturerList = coursesList.get(2);

                Object[] optionsArray = new Object[courseNameList.size()];
                for (int i = 0; i < courseNameList.size(); i++) {
                    optionsArray[i] = "(" + courseCodeList.get(i) + ") " + courseNameList.get(i);
                }

                Component source = (Component) e.getSource();
                Object selectedOption = JOptionPane.showInputDialog(source,
                        "Select one", "Courses",
                        JOptionPane.QUESTION_MESSAGE, null, optionsArray,
                        optionsArray[0]);

                if (courseTextArea.getText().contains((CharSequence) selectedOption)) {
                    JOptionPane.showMessageDialog(source,
                            "Warning: Option already exists in the text area!",
                            "Duplicate Option", JOptionPane.WARNING_MESSAGE);
                } else {
                    String currentText = courseTextArea.getText();

                    if (!currentText.isEmpty()) {
                        currentText += ", ";
                    }

                    String courseCode = selectedOption.toString().substring(1, selectedOption.toString().indexOf(')'));

                    selectedCourses.add(courseCode);
                    courseTextArea.setText(currentText + selectedOption);
                }
            }
        });

        addLecturerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lecturerNameTF.getText() == null || Objects.equals(lecturerNameTF.getText(), "") || Objects.equals(lecturerSurnameTF.getText(), "") || lecturerSurnameTF.getText() == null || facultyCB.getSelectedItem() == null || departmentCB.getSelectedItem() == null || courseTextArea.getText() == null) {
                    JOptionPane.showMessageDialog(null, "Please fill in all blank areas!", "Blank Areas", JOptionPane.WARNING_MESSAGE);
                } else {
                    facultyName = facultyCB.getSelectedItem().toString();
                    programName = departmentCB.getSelectedItem().toString();

                    var JsonContent = DataHandler.readJsonFile("src/data/Lecturers.json");

                    List<Course> coursesList = new ArrayList<>();

                    for (String courseCode : selectedCourses) {
                        Course newCourse = new Course();
                        newCourse.courseCode = courseCode;
                        newCourse.courseName = getCourseNameFromCode(courseCode);
                        newCourse.lecturer = lecturerNameTF.getText() + " " + lecturerSurnameTF.getText();

                        coursesList.add(newCourse);
                    }

                    Course[] coursesArray = coursesList.toArray(new Course[0]);

                    Lecturer newLecturer = new Lecturer();
                    newLecturer.lecturerID = newID;
                    newLecturer.lecturerName = lecturerNameTF.getText();
                    newLecturer.lecturerSurname = lecturerSurnameTF.getText();
                    newLecturer.faculty = facultyName;
                    newLecturer.program = programName;
                    newLecturer.courses = coursesArray;

                    JsonContent.add(new Gson().toJsonTree(newLecturer));
                    DataHandler.saveJsonData("src/data/Lecturers.json", JsonContent);
                    JOptionPane.showMessageDialog(null, "Lecturer added successfully!", "Nice!", JOptionPane.INFORMATION_MESSAGE);

                    newID = createLecturerID();
                    lecturerIdTF.setText(String.valueOf(newID));

                    searchTableModel.setRowCount(0);

                    List<JsonObject> lecturers = DataHandler.getAllLecturers();

                    if (lecturers != null && !lecturers.isEmpty()) {
                        for (JsonObject lecturer : lecturers) {
                            String lecturerName = lecturer.get("studentName").getAsString();
                            String lecturerSurname = lecturer.get("studentSurname").getAsString();
                            String lecturerID = lecturer.get("studentNumber").getAsString();
                            String faculty = lecturer.get("faculty").getAsString();
                            String program = lecturer.get("program").getAsString();

                            String[] lecturerData = {lecturerID, lecturerName, lecturerSurname, faculty, program};
                            searchTableModel.addRow(lecturerData);
                        }
                    }
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newID = createLecturerID();
                lecturerIdTF.setText(String.valueOf(newID));

                lecturerNameTF.setText("");
                lecturerSurnameTF.setText("");

                facultyCB.setSelectedIndex(-1);

                departmentCB.setSelectedIndex(-1);
                departmentCB.removeAllItems();
                departmentCB.setEnabled(false);

                addCourseButton.setEnabled(false);
                courseTextArea.setText("");
                selectedCourses.clear();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var givenText = searchInputTF.getText();

                if (givenText.isEmpty() || givenText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "You need to provide lecturer name for search!", "Blank area!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    var results = DataHandler.getLecturerByName(givenText);

                    StringBuilder resultMessage = new StringBuilder("Matching results:\n");

                    for (JsonObject result : results) {
                        resultMessage
                                .append("ID: ").append(result.get("lecturerID").getAsString()).append("\n")
                                .append("Name: ").append(result.get("lecturerName").getAsString()).append("\n")
                                .append("Surname: ").append(result.get("lecturerSurname").getAsString()).append("\n")
                                .append("Faculty: ").append(result.get("faculty").getAsString()).append("\n")
                                .append("Program: ").append(result.get("program").getAsString()).append("\n")
                                .append("Courses: ");

                        JsonArray courses = result.getAsJsonArray("courses");
                        for (JsonElement course : courses) {
                            JsonObject courseObject = course.getAsJsonObject();
                            resultMessage
                                    .append("\n  - (").append(courseObject.get("courseCode").getAsString()).append(")")
                                    .append(", ").append(courseObject.get("courseName").getAsString());
                        }
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
        searchInputTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String searchInput = searchInputTF.getText().toLowerCase().trim();

                TableRowSorter<DefaultTableModel> sorting = new TableRowSorter<>(searchTableModel);
                searchTable.setRowSorter(sorting);

                sorting.setRowFilter(RowFilter.regexFilter("(?i).*" + searchInput + ".*"));
            }
        });
    }

    private static int createLecturerID() {
        JsonArray JsonContent = DataHandler.readJsonFile("src/data/Lecturers.json");

        int maxNumber = 0;

        for (JsonElement lecturer : JsonContent) {
            JsonObject lecturerObj = lecturer.getAsJsonObject();
            int lecturerId = lecturerObj.getAsJsonPrimitive("lecturerID").getAsInt();

            if (lecturerId > maxNumber) {
                maxNumber = lecturerId;
            }
        }

        return maxNumber + 1;
    }

    private String getCourseNameFromCode(String courseCode) {
        List<List<String>> coursesList = DataHandler.getCourses(facultyCB.getSelectedItem().toString(), departmentCB.getSelectedItem().toString());

        List<String> courseCodeList = coursesList.get(0);
        List<String> courseNameList = coursesList.get(1);

        int index = courseCodeList.indexOf(courseCode);

        return (index != -1) ? courseNameList.get(index) : "";
    }

    public static void main(String[] args) {
        new LecturerWindow();
    }
}
