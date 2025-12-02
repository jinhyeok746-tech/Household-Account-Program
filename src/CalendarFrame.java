import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public class CalendarFrame extends JFrame {

    private final String currentUserId;
    private final DataService dataService;
    private YearMonth currentMonth;
    private JLabel monthLabel;
    private JPanel calendarPanel;

    public CalendarFrame(String userId) {
        this.currentUserId = userId;
        this.dataService = new DataService();
        
        setTitle("가계부 달력 - " + userId);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        this.currentMonth = YearMonth.now();

        setLayout(new BorderLayout());

        // 1. 월 이동 패널 (북쪽)
        JPanel monthNavPanel = createMonthNavigationPanel();
        add(monthNavPanel, BorderLayout.NORTH);

        // 2. 달력 패널 (중앙)
        calendarPanel = new JPanel();
        calendarPanel.setLayout(new GridLayout(0, 7)); 
        add(calendarPanel, BorderLayout.CENTER);

        // 3. 하단 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("메인화면으로 돌아가기");
        backButton.addActionListener(e -> {
            dispose(); 
            new DashboardPanel(currentUserId).refreshData(); // 대시보드를 새로 열고 데이터 갱신
        });
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        drawCalendar();

        setVisible(true);
    }

    private JPanel createMonthNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton prevButton = new JButton("◀");
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        JButton nextButton = new JButton("▶");

        prevButton.addActionListener(e -> changeMonth(-1)); 
        nextButton.addActionListener(e -> changeMonth(1)); 

        panel.add(prevButton);
        panel.add(monthLabel);
        panel.add(nextButton);

        return panel;
    }

    private void changeMonth(int change) {
        currentMonth = currentMonth.plusMonths(change);
        drawCalendar();
    }

    public void drawCalendar() {
        calendarPanel.removeAll(); 
        monthLabel.setText(String.format("%d년 %d월", currentMonth.getYear(), currentMonth.getMonthValue()));

        // 요일 헤더 추가
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            if (day.equals("일")) dayLabel.setForeground(Color.RED);
            calendarPanel.add(dayLabel);
        }

        // 시작 요일 맞추기 (빈 칸 채우기)
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int isoDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; 
        int emptyCalls = isoDayOfWeek % 7; 
        int startDay = emptyCalls + 1;

        for (int i = 1; i < startDay; i++) {
            calendarPanel.add(new JPanel()); 
        }

        // 날짜 패널 추가
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            calendarPanel.add(createDatePanel(date));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
    
    private JPanel createDatePanel(LocalDate date) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setPreferredSize(new Dimension(100, 80)); // 셀 크기 지정

        // 날짜 표시
        JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()), JLabel.LEFT);
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        if (date.getDayOfWeek().getValue() == 7) dateLabel.setForeground(Color.RED); 
        panel.add(dateLabel, BorderLayout.NORTH);

        // 수익/지출 요약
        Map<String, Long> summary = dataService.getDailySummary(currentUserId, date);
        long income = summary.get("income");
        long expense = summary.get("expense");

        JLabel summaryLabel = new JLabel(
            String.format("<html><font color='blue'>수: %,d</font><br><font color='red'>지: %,d</font></html>", income, expense), 
            JLabel.LEFT
        );
        summaryLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        summaryLabel.setVerticalAlignment(SwingConstants.TOP);
        panel.add(summaryLabel, BorderLayout.CENTER);
        
        // 날짜 클릭 리스너 (등록/삭제 팝업)
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showEntryDialog(date);
            }
        });

        return panel;
    }

    private void showEntryDialog(LocalDate date) {
        new EntryDialog(this, currentUserId, date);
    }
}