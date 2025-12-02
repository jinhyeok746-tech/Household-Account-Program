import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EntryDialog extends JDialog {

    private final CalendarFrame parentFrame;
    private final String userId;
    private final LocalDate date;
    private final DataService dataService;;

    private JComboBox<String> typeComboBox;
    private JComboBox<String> categoryComboBox;
    private JTextField amountField;
    private JTextField memoField;
    private JList<AccountEntry> entryList;
    private DefaultListModel<AccountEntry> listModel;

    private final String[] INCOME_CATS = {"월급", "용돈", "기타수익"};
    private final String[] EXPENSE_CATS = {"식비", "교통비", "생활용품", "취미/문화", "기타지출"};

    public EntryDialog(CalendarFrame parentFrame, String userId, LocalDate date) {
        super(parentFrame, date.toString() + " 가계부 항목 관리", true);
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.date = date;
        this.dataService = new DataService();
        
        setLayout(new BorderLayout(10, 10));
        setSize(550, 600);
        setLocationRelativeTo(parentFrame);

        add(createRegistrationPanel(), BorderLayout.NORTH);
        add(createListPanel(), BorderLayout.CENTER);
        
        loadEntryList();

        setVisible(true);
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("항목 등록"));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        typeComboBox = new JComboBox<>(new String[]{"수익", "지출"});
        categoryComboBox = new JComboBox<>(EXPENSE_CATS); 
        amountField = new JTextField();
        memoField = new JTextField();

        inputPanel.add(new JLabel("분류:"));
        inputPanel.add(typeComboBox);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categoryComboBox);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("메모:"));
        inputPanel.add(memoField);

        typeComboBox.addActionListener(e -> updateCategoryComboBox());
        
        JButton saveButton = new JButton("등록");
        saveButton.addActionListener(e -> saveEntry());

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private void updateCategoryComboBox() {
        String type = (String) typeComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();
        if ("수익".equals(type)) {
            for (String cat : INCOME_CATS) categoryComboBox.addItem(cat);
        } else {
            for (String cat : EXPENSE_CATS) categoryComboBox.addItem(cat);
        }
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("등록된 항목"));
        
        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JButton deleteButton = new JButton("선택 항목 삭제");
        deleteButton.addActionListener(e -> deleteSelectedEntry());

        panel.add(new JScrollPane(entryList), BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // 항목 리스트 불러오기
    private void loadEntryList() {
        listModel.clear();
        List<AccountEntry> entries = dataService.getEntriesByDate(userId, date);
        for (AccountEntry entry : entries) {
            listModel.addElement(entry);
        }
    }

    // 항목 등록 로직
    private void saveEntry() {
        try {
            String type = (String) typeComboBox.getSelectedItem();
            String category = (String) categoryComboBox.getSelectedItem();
            long amount = Long.parseLong(amountField.getText().trim());
            String memo = memoField.getText().trim();
            
            if (amount <= 0) throw new NumberFormatException();
            if (category == null || category.isEmpty()) throw new IllegalArgumentException("카테고리를 선택하세요.");

            AccountEntry newEntry = new AccountEntry(userId, date, type, category, amount, memo);
            
            if (dataService.addEntry(newEntry)) {
                JOptionPane.showMessageDialog(this, "등록 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                memoField.setText("");
                
                loadEntryList(); 
                parentFrame.drawCalendar(); // 달력 UI 갱신
            } else {
                JOptionPane.showMessageDialog(this, "등록 실패.", "오류", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "금액은 0보다 큰 숫자여야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "경고", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 항목 삭제 로직
    private void deleteSelectedEntry() {
        AccountEntry selectedEntry = entryList.getSelectedValue();
        if (selectedEntry == null) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "정말로 이 항목을 삭제하시겠습니까?\n" + selectedEntry.toString(), 
            "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dataService.deleteEntry(selectedEntry.getId())) {
                JOptionPane.showMessageDialog(this, "삭제 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                loadEntryList(); 
                parentFrame.drawCalendar(); // 달력 UI 갱신
            } else {
                JOptionPane.showMessageDialog(this, "삭제 실패.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}