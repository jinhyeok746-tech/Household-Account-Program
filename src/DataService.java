import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataService {

    private static final String DB_URL = "jdbc:sqlite:account_book.db";

    public DataService() {
        try {
            // SQLite JDBC 드라이버 로드
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC 드라이버를 찾을 수 없습니다. JAR 파일을 Classpath에 추가해주세요.");
            e.printStackTrace();
        }
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 사용자 테이블 생성
            String userTableSql = "CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, password TEXT NOT NULL)";
            stmt.execute(userTableSql);

            // 가계부 항목 테이블 생성
            String entryTableSql = "CREATE TABLE IF NOT EXISTS entries (" +
                                   "entry_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                   "user_id TEXT NOT NULL, " +
                                   "date TEXT NOT NULL, " + 
                                   "type TEXT NOT NULL, " + 
                                   "category TEXT, " +
                                   "amount INTEGER NOT NULL, " +
                                   "memo TEXT, " +
                                   "FOREIGN KEY(user_id) REFERENCES users(id))";
            stmt.execute(entryTableSql);
            
            // 테스트 계정 삽입 및 데이터 추가 (최초 실행 시만)
            if (!login("test", "1234")) {
                 registerUser("test", "1234");
                 // 테스트 데이터: 2025년 12월 기준
                 LocalDate today = LocalDate.of(2025, 12, 18);
                 addEntry(new AccountEntry("test", today.minusDays(3), "수익", "월급", 3000000, "12월 월급"));
                 addEntry(new AccountEntry("test", today.minusDays(2), "지출", "식비", 15000, "점심 식사"));
                 addEntry(new AccountEntry("test", today.minusDays(1), "지출", "교통비", 50000, "대중교통 카드 충전"));
            }

        } catch (SQLException e) {
            System.err.println("DB 초기화 중 오류 발생: " + e.getMessage());
        }
    }
    
    // --- 회원 기능 ---

    public boolean registerUser(String id, String password) {
        String sql = "INSERT INTO users (id, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean login(String id, String password) {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- 가계부 데이터 기능 (CRUD) ---
    
    public boolean addEntry(AccountEntry entry) {
        String sql = "INSERT INTO entries (user_id, date, type, category, amount, memo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, entry.getUserId());
            pstmt.setString(2, entry.getDate().toString());
            pstmt.setString(3, entry.getType());
            pstmt.setString(4, entry.getCategory());
            pstmt.setLong(5, entry.getAmount());
            pstmt.setString(6, entry.getMemo());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entry.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteEntry(int entryId) {
        String sql = "DELETE FROM entries WHERE entry_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AccountEntry> getEntriesByDate(String userId, LocalDate date) {
        List<AccountEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM entries WHERE user_id = ? AND date = ? ORDER BY entry_id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(new AccountEntry(
                    rs.getInt("entry_id"),
                    rs.getString("user_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("type"),
                    rs.getString("category"),
                    rs.getLong("amount"),
                    rs.getString("memo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    // --- 통계 기능 ---
    
    public long getTotalBalance(String userId) {
        String sql = "SELECT SUM(CASE WHEN type = '수익' THEN amount ELSE 0 END) - " +
                     "SUM(CASE WHEN type = '지출' THEN amount ELSE 0 END) AS balance " +
                     "FROM entries WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getMonthlyTotal(String userId, YearMonth yearMonth, String type) {
        String start = yearMonth.atDay(1).toString();
        String end = yearMonth.atEndOfMonth().toString();
        
        String sql = "SELECT SUM(amount) AS total FROM entries WHERE user_id = ? AND type = ? AND date BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, start);
            pstmt.setString(4, end);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public long getCurrentMonthIncome(String userId) {
        return getMonthlyTotal(userId, YearMonth.now(), "수익");
    }

    public long getCurrentMonthExpense(String userId) {
        return getMonthlyTotal(userId, YearMonth.now(), "지출");
    }

    public Map<String, Double> getCategoryExpenseRatio(String userId) {
        Map<String, Double> ratios = new HashMap<>();
        long totalExpense = getCurrentMonthExpense(userId);

        if (totalExpense == 0) {
            return ratios;
        }

        String start = YearMonth.now().atDay(1).toString();
        String end = YearMonth.now().atEndOfMonth().toString();
        
        String sql = "SELECT category, SUM(amount) AS sum_amount FROM entries " +
                     "WHERE user_id = ? AND type = '지출' AND date BETWEEN ? AND ? GROUP BY category";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, start);
            pstmt.setString(3, end);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String category = rs.getString("category");
                long amount = rs.getLong("sum_amount");
                double ratio = (double) amount * 100.0 / totalExpense;
                ratios.put(category, ratio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratios;
    }

    public Map<String, Long> getDailySummary(String userId, LocalDate date) {
        Map<String, Long> summary = new HashMap<>();
        summary.put("income", 0L);
        summary.put("expense", 0L);

        String sql = "SELECT type, SUM(amount) AS total FROM entries WHERE user_id = ? AND date = ? GROUP BY type";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                long total = rs.getLong("total");
                if (type.equals("수익")) {
                    summary.put("income", total);
                } else if (type.equals("지출")) {
                    summary.put("expense", total);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public int getMonthlyNetIncome(String userId, String yearMonth) {
        int totalIncome = 0;
        int totalExpense = 0;
        
        // 1. 총 수입 계산
        String incomeSql = "SELECT SUM(amount) FROM entries WHERE user_id = ? AND strftime('%Y-%m', date) = ? AND type = '수익'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(incomeSql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, yearMonth);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalIncome = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("월별 총 수입 조회 중 오류 발생: " + e.getMessage());
        }

        // 2. 총 지출 계산
        String expenseSql = "SELECT SUM(amount) FROM entries WHERE user_id = ? AND strftime('%Y-%m', date) = ? AND type = '지출'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(expenseSql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, yearMonth);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalExpense = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("월별 총 지출 조회 중 오류 발생: " + e.getMessage());
        }

        return totalIncome - totalExpense;
    }
}