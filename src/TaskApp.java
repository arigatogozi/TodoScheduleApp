import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaskApp extends JFrame {
    private final TaskManager manager = new TaskManager();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel clockLabel = new JLabel();

    private final JTextField titleField = new JTextField(12);
    private final JTextField dateField = new JTextField(10);
    private final JComboBox<String> priorityBox = new JComboBox<>(new String[]{"낮음", "보통", "높음"});
    private final JTextArea descriptionArea = new JTextArea(3, 20);

    public TaskApp() {
        setTitle("개인 일정/할 일 관리 프로그램");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"제목", "마감일", "중요도", "상태", "내용"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        add(createTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createInputPanel(), BorderLayout.SOUTH);

        loadTasksFromFile();
        startClockThread();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("나의 일정 관리", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 22));
        panel.add(title, BorderLayout.CENTER);
        panel.add(clockLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 1));
        JPanel line1 = new JPanel();
        line1.add(new JLabel("제목"));
        line1.add(titleField);
        line1.add(new JLabel("마감일 yyyy-MM-dd"));
        line1.add(dateField);
        line1.add(new JLabel("중요도"));
        line1.add(priorityBox);

        JPanel line2 = new JPanel();
        line2.add(new JLabel("내용"));
        line2.add(new JScrollPane(descriptionArea));

        form.add(line1);
        form.add(line2);

        JPanel buttons = new JPanel();
        JButton addButton = new JButton("추가");
        JButton completeButton = new JButton("완료/취소");
        JButton deleteButton = new JButton("삭제");
        JButton saveButton = new JButton("저장");
        JButton loadButton = new JButton("불러오기");

        addButton.addActionListener(e -> addTask());
        completeButton.addActionListener(e -> toggleComplete());
        deleteButton.addActionListener(e -> deleteTask());
        saveButton.addActionListener(e -> saveTasksToFile());
        loadButton.addActionListener(e -> loadTasksFromFile());

        buttons.add(addButton);
        buttons.add(completeButton);
        buttons.add(deleteButton);
        buttons.add(saveButton);
        buttons.add(loadButton);

        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addTask() {
        String title = titleField.getText().trim();
        String dueDate = dateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목을 입력하세요.");
            return;
        }

        try {
            LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "날짜 형식은 yyyy-MM-dd 입니다. 예: 2026-06-21");
            return;
        }

        Task task;
        if ("높음".equals(priority)) {
            task = new ImportantTask(title, description, dueDate);
        } else {
            task = new Task(title, description, dueDate, priority);
        }

        manager.addTask(task);
        refreshTable();
        clearInputs();
    }

    private void toggleComplete() {
        int row = table.getSelectedRow();
        Task task = manager.getTask(row);
        if (task == null) {
            JOptionPane.showMessageDialog(this, "완료 처리할 항목을 선택하세요.");
            return;
        }
        task.setCompleted(!task.isCompleted());
        refreshTable();
    }

    private void deleteTask() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        manager.removeTask(row);
        refreshTable();
    }

    private void saveTasksToFile() {
        try {
            FileManager.saveTasks(manager.getTasks());
            JOptionPane.showMessageDialog(this, "저장되었습니다.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void loadTasksFromFile() {
        try {
            manager.setTasks(FileManager.loadTasks());
            refreshTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "불러오기 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Task task : manager.getTasks()) {
            tableModel.addRow(new Object[]{
                    task.getTitle(),
                    task.getDueDate(),
                    task.getPriority(),
                    task.getStatusText(),
                    task.getDescription()
            });
        }
    }

    private void clearInputs() {
        titleField.setText("");
        dateField.setText("");
        priorityBox.setSelectedIndex(1);
        descriptionArea.setText("");
    }

    private void startClockThread() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                SwingUtilities.invokeLater(() -> clockLabel.setText("현재 날짜: " + LocalDate.now() + "  "));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskApp().setVisible(true));
    }
}
