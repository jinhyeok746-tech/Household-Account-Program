import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class SavingsFrame extends JFrame {
    private final DataService dataService;
    private final String currentUserId;
    private final JLabel netIncomeLabel;
    private final JTextArea recommendationArea;
    private YearMonth currentMonth;

    public SavingsFrame(DataService dataService, String userId) {
        this.dataService = dataService;
        this.currentUserId = userId;
        this.currentMonth = YearMonth.now();

        setTitle("적금 추천 시스템");
        setSize(400, 500);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 폰트 설정
        Font titleFont = new Font("Malgun Gothic", Font.BOLD, 18);
        Font textFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        
        // --- 상단 패널 (월 이동 및 순수익 표시) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("적금 추천 시뮬레이터", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel monthPanel = new JPanel(new FlowLayout());
        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");
        netIncomeLabel = new JLabel("", SwingConstants.CENTER);
        netIncomeLabel.setFont(textFont);
        
        prevButton.addActionListener(e -> updateMonth(-1));
        nextButton.addActionListener(e -> updateMonth(1));
        
        monthPanel.add(prevButton);
        monthPanel.add(netIncomeLabel);
        monthPanel.add(nextButton);
        topPanel.add(monthPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // --- 중앙 패널 (추천 결과) ---
        recommendationArea = new JTextArea();
        recommendationArea.setFont(textFont);
        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(recommendationArea);
        
        add(scrollPane, BorderLayout.CENTER);

        // 초기 데이터 로드 및 추천
        updateMonth(0); 
        
        setVisible(true);
    }

    private void updateMonth(int monthDelta) {
        currentMonth = currentMonth.plusMonths(monthDelta);
        
        // yyyy-MM 형식으로 포맷
        String yearMonthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // 순수익 계산
        int netIncome = dataService.getMonthlyNetIncome(currentUserId, yearMonthStr);
        
        netIncomeLabel.setText(
            currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월")) + 
            " 순수익: " + String.format("%,d원", netIncome)
        );
        
        // 적금 추천 로직 실행
        recommendSavings(netIncome);
    }

    private void recommendSavings(int netIncome) {
        String recommendationText;
        
        if (netIncome < 0) {
            recommendationText = "해당 월은 지출이 수입보다 많았습니다.\n" +
                                 "적금보다는 지출 절약을 통한 재정 안정화가 우선입니다.";
        } else if (netIncome >= 3000000) {
            // 순수익 300만원 이상
            recommendationText = "[프리미엄 적금 B 추천]\n\n" +
                                 "월 200만원 이상** 납입이 가능하며, 높은 금액을 위한 특별 우대금리가 적용되는 고액 정기 적금 B를 추천합니다.\n" +
                                 "특징: 최대 5년, 최고 금리 연 5.5% (조건 충족 시).\n" +
                                 "팁: 월 300만원을 1년 적금 시, 약 3,750만원의 목돈을 만들 수 있습니다.";
        } else if (netIncome >= 1000000) {
            // 순수익 100만원 이상 300만원 미만
            recommendationText = "[표준형 적금 A 추천]\n\n" +
                                 "월 100만원~300만원** 납입이 적절하며, 조건 없이 기본 금리가 높은 표준 정기 적금 A를 추천합니다.\n" +
                                 "특징: 최대 3년, 기본 금리 연 4.2%.\n" +
                                 "팁: 월 150만원을 2년 적금 시, 약 3,780만원의 목돈을 만들 수 있습니다.";
        } else if (netIncome > 0) {
            // 순수익 1만원 이상 100만원 미만
            recommendationText = "[자유 적립식 적금 추천]\n\n" +
                                 "월 50만원 이하 자유롭게 납입 가능한 자유 적립식 적금을 추천합니다.\n" +
                                 "특징: 유동적인 수입에 맞춰 납입 가능, 기본 금리 연 3.5%.\n" +
                                 "팁: 매월 여윳돈이 생길 때마다 납입하여 저축 습관을 기르세요.";
        } else {
             // 순수익 0원
             recommendationText = "해당 월은 순수익이 0원입니다.\n" +
                                  "지출을 점검하고, 최소한의 금액(예: 월 10만원)부터 시작하는 소액 적금 상품을 고려해 보세요.";
        }
        
        recommendationArea.setText(recommendationText);
        recommendationArea.setCaretPosition(0); 
    }
}
