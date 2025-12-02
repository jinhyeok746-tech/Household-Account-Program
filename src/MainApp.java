import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // Swing GUI를 안전하게 시작합니다.
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(); // 로그인 화면으로 프로그램 시작
        });
    }
}