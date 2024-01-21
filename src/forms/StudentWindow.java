package forms;

import classes.Course;
import classes.Faculty;
import classes.Programs;
import classes.Student;
import com.google.gson.*;
import data.DataHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;

public class StudentWindow extends JFrame {
    private JPanel StudentWindow;
    private JTextField studentNoTF;
    private JTextField studentNameTF;
    private JLabel studentNumberLabel;
    private JLabel studentNameLabel;
    private JTextField studentSurnameTF;
    private JComboBox facultyCB;
    private JComboBox departmentCB;
    private JButton addStudentButton;
    private JButton clearButton;
    private JLabel departLabel;
    private JLabel facultyLabel;
    private JLabel surnameLabel;
    private JLabel courseLabel;
    private JComboBox courseCB;
    private JButton searchButton;
    private JTextField searchInputField;
    private JTable searchTable;

    Faculty faculty = new Faculty();
    String facultyName = faculty.facultyName;

    Programs programs = new Programs();
    String programName = programs.programName;

    private int newID;
    String selectedCourseCode;
    String selectedLecturer;

    public StudentWindow() {
        setTitle("Add New Student");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(400, 700);
        setLocation(550,250);
        setContentPane(StudentWindow);

        DefaultTableModel searchTableModel = new DefaultTableModel();
        searchTableModel.addColumn("Student Number");
        searchTableModel.addColumn("Student Name");
        searchTableModel.addColumn("Student Surname");
        searchTableModel.addColumn("Faculty");
        searchTableModel.addColumn("Program");
        searchTableModel.addRow(new Object[]{"ID", "Name", "Surname", "Faculty", "Program"});

        List<JsonObject> students = DataHandler.getAllStudents();

        if (students != null && !students.isEmpty()) {
            for (JsonObject student : students) {
                String studentName = student.get("studentName").getAsString();
                String studentSurname = student.get("studentSurname").getAsString();
                String studentID = student.get("studentNumber").getAsString();
                String faculty = student.get("faculty").getAsString();
                String program = student.get("program").getAsString();

                String[] studentData = {studentID, studentName, studentSurname, faculty, program};
                searchTableModel.addRow(studentData);
            }
        }

        searchTable.setModel(searchTableModel);

        newID = createStudentNumber();
        studentNoTF.setText(String.valueOf(newID));

        var facultyNames = DataHandler.getFacultyNames();
        for (String name : facultyNames) {
            facultyCB.addItem(name);
        }
        facultyCB.setSelectedIndex(-1);

        if (facultyCB.getSelectedIndex() == -1) {
            departmentCB.setEnabled(false);
            courseCB.setEnabled(false);
        }

        if (!departmentCB.isEnabled()) {
            departmentCB.setToolTipText("You have to choose faculty for see departments.");
            courseCB.setEnabled(false);
        }

        if (departmentCB.getSelectedIndex() == -1) {
            courseCB.setEnabled(false);
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
                    courseCB.setEnabled(false);
                    courseCB.removeAllItems();
                }
            }
        });

        departmentCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (departmentCB.getSelectedIndex() != -1) {
                    courseCB.setEnabled(true);
                    courseCB.removeAllItems();
                    courseCB.setSelectedIndex(-1);

                    List<List<String>> courseList = DataHandler.getCourses(facultyCB.getSelectedItem().toString(), departmentCB.getSelectedItem().toString());

                    List<String> courseCodeList = courseList.get(0);
                    List<String> courseNameList = courseList.get(1);
                    List<String> lecturerList = courseList.get(2);

                    for (String name : courseNameList) {
                        courseCB.addItem(name);
                    }

                    int selectedIndex = courseCB.getSelectedIndex();
                    selectedCourseCode = courseCodeList.get(selectedIndex);
                    selectedLecturer = lecturerList.get(selectedIndex);
                }
            }
        });

        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (studentNameTF.getText() == null || Objects.equals(studentNameTF.getText(), "") || Objects.equals(studentSurnameTF.getText(), "") || studentSurnameTF.getText() == null || facultyCB.getSelectedItem() == null || departmentCB.getSelectedItem() == null || courseCB.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(null, "Please fill in all blank areas!", "Blank Areas", JOptionPane.WARNING_MESSAGE);
                } else {
                    facultyName = facultyCB.getSelectedItem().toString();
                    programName = departmentCB.getSelectedItem().toString();

                    var JsonContent = DataHandler.readJsonFile("src/data/Students.json");

                    Course newCourse = new Course();
                    newCourse.courseCode = selectedCourseCode;
                    newCourse.courseName = courseCB.getSelectedItem().toString();
                    newCourse.lecturer = selectedLecturer;
                    Course[] coursesArray = { newCourse };

                    Student newStudent = new Student();
                    newStudent.studentNumber = newID;
                    newStudent.studentName = studentNameTF.getText();
                    newStudent.studentSurname = studentSurnameTF.getText();
                    newStudent.faculty = facultyName;
                    newStudent.program = programName;
                    newStudent.courses = coursesArray;

                    JsonContent.add(new Gson().toJsonTree(newStudent));
                    DataHandler.saveJsonData("src/data/Students.json", JsonContent);
                    JOptionPane.showMessageDialog(null, "Student added successfully!", "Great!", JOptionPane.INFORMATION_MESSAGE);

                    newID = createStudentNumber();
                    studentNoTF.setText(String.valueOf(newID));

                    searchTableModel.setRowCount(0);

                    List<JsonObject> students = DataHandler.getAllStudents();

                    if (students != null && !students.isEmpty()) {
                        for (JsonObject student : students) {
                            String studentName = student.get("studentName").getAsString();
                            String studentSurname = student.get("studentSurname").getAsString();
                            String studentID = student.get("studentNumber").getAsString();
                            String faculty = student.get("faculty").getAsString();
                            String program = student.get("program").getAsString();

                            String[] studentData = {studentID, studentName, studentSurname, faculty, program};
                            searchTableModel.addRow(studentData);
                        }
                    }
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newID = createStudentNumber();
                studentNoTF.setText(String.valueOf(newID));

                studentNameTF.setText("");
                studentSurnameTF.setText("");

                facultyCB.setSelectedIndex(-1);

                departmentCB.setSelectedIndex(-1);
                departmentCB.removeAllItems();
                departmentCB.setEnabled(false);

                courseCB.setSelectedIndex(-1);
                courseCB.removeAllItems();
                courseCB.setEnabled(false);
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var givenText = searchInputField.getText();

                if (givenText.isEmpty() || givenText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "No matching students found!", "Error", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    var results = DataHandler.getStudentByName(givenText);

                    StringBuilder resultMessage = new StringBuilder("Matching results:\n");

                    for (JsonObject result : results) {
                        resultMessage
                                .append("Name: ").append(result.get("studentName").getAsString()).append("\n")
                                .append("Surname: ").append(result.get("studentSurname").getAsString()).append("\n")
                                .append("Faculty: ").append(result.get("faculty").getAsString()).append("\n")
                                .append("Program: ").append(result.get("program").getAsString()).append("\n");
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

        setVisible(true);
    }

    private static int createStudentNumber() {
        JsonArray JsonContent = DataHandler.readJsonFile("src/data/Students.json");

        int maxNumber = 0;

        for (JsonElement student : JsonContent) {
            JsonObject studentObj = student.getAsJsonObject();
            int studentNumber = studentObj.getAsJsonPrimitive("studentNumber").getAsInt();

            if (studentNumber > maxNumber) {
                maxNumber = studentNumber;
            }
        }

        return maxNumber + 1;
    }

    public static void main(String[] args) {
        new StudentWindow();
    }
}
