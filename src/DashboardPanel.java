import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DashboardPanel extends JFrame {

    private final DataService dataService;
    private final String currentUserId;

    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private DefaultListModel<String> ratioListModel;
    
    private JPanel bottomPanel;

    public DashboardPanel(String userId) {
        this.currentUserId = userId;
        this.dataService = new DataService();
        
        setTitle(userId + "님의 가계부 메인화면");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. 상단 정보 패널 (잔액, 월별 현황)
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        balanceLabel = new JLabel();
        incomeLabel = new JLabel();
        expenseLabel = new JLabel();

        infoPanel.add(createInfoCard("전체 잔액", balanceLabel, Color.BLUE));
        infoPanel.add(createInfoCard("이번 달 수익", incomeLabel, Color.GREEN.darker()));
        infoPanel.add(createInfoCard("이번 달 지출", expenseLabel, Color.RED));
        
        add(infoPanel, BorderLayout.NORTH);
        
        // 2. 중앙 카테고리 비율 패널
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("이번 달 카테고리별 지출 비율"));

        ratioListModel = new DefaultListModel<>();
        JList<String> ratioList = new JList<>(ratioListModel);
        centerPanel.add(new JScrollPane(ratioList), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 적금 추천 버튼 추가 
        JButton savingsButton = new JButton("적금 추천 받기");
        savingsButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        bottomPanel.add(savingsButton); 

        // 적금 추천 버튼 클릭 이벤트 처리
        savingsButton.addActionListener(e -> {
            new SavingsFrame(dataService, currentUserId); 
        });
        
        // 3. 하단 버튼 패널	
        JButton calendarButton = new JButton("달력으로 확인하기");
        calendarButton.addActionListener(e -> {
            dispose(); 
            new CalendarFrame(currentUserId); 
        });
        bottomPanel.add(calendarButton);
   
        // 패널을 프레임에 추가
        add(bottomPanel, BorderLayout.SOUTH);

        // 데이터 로드
        refreshData(); 
        
        setVisible(true);
    }
    
    // 이 메서드를 호출하여 화면의 모든 데이터를 DB에서 최신 정보로 갱신합니다.
    public void refreshData() {
        long totalBalance = dataService.getTotalBalance(currentUserId);
        long monthIncome = dataService.getCurrentMonthIncome(currentUserId);
        long monthExpense = dataService.getCurrentMonthExpense(currentUserId);
        
        // 1. 잔액/수익/지출 업데이트
        balanceLabel.setText(String.format("%,d원", totalBalance));
        incomeLabel.setText(String.format("%,d원", monthIncome));
        expenseLabel.setText(String.format("%,d원", monthExpense));

        // 2. 카테고리 비율 업데이트
        ratioListModel.clear();
        Map<String, Double> ratios = dataService.getCategoryExpenseRatio(currentUserId);
        
        if (monthExpense == 0) {
            ratioListModel.addElement("이번 달 지출 내역이 없습니다.");
        } else {
            for (Map.Entry<String, Double> entry : ratios.entrySet()) {
                String category = entry.getKey();
                double ratio = entry.getValue();
                
                long amount = (long) (monthExpense * ratio / 100.0); 

                String line = String.format("  - %s: %.1f%% (%,d원)", category, ratio, amount);
                ratioListModel.addElement(line);
            }
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createInfoCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
}