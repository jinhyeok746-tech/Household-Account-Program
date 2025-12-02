import java.time.LocalDate;

public class AccountEntry {
    private int id; 
    private String userId;
    private LocalDate date;
    private String type; // "수익" 또는 "지출"
    private String category;
    private long amount;
    private String memo;

    public AccountEntry(int id, String userId, LocalDate date, String type, String category, long amount, String memo) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.memo = memo;
    }

    // 새 항목 등록 시
    public AccountEntry(String userId, LocalDate date, String type, String category, long amount, String memo) {
        this(-1, userId, date, type, category, amount, memo);
    }
    
    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public long getAmount() { return amount; }
    public String getMemo() { return memo; }

    // Setter for ID
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %,d원 (%s)", 
                             type.equals("수익") ? "수익" : "지출", 
                             category, amount, memo);
    }
}