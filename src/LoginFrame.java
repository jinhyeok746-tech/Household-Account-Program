import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final DataService dataService = new DataService();
    private JTextField idField;
    private JPasswordField pwField;

    public LoginFrame() {
        setTitle("가계부 프로그램 - 로그인");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));

        inputPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Password:"));
        pwField = new JPasswordField();
        inputPanel.add(pwField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton loginButton = new JButton("로그인");
        JButton registerButton = new JButton("회원가입");
        
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegister());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void attemptLogin() {
        String id = idField.getText();
        String pw = new String(pwField.getPassword());

        if (dataService.login(id, pw)) {
            JOptionPane.showMessageDialog(this, "로그인 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
            
            dispose(); // 현재 로그인 창 닫기
            new DashboardPanel(id); // 대시보드 화면 열기
            
        } else {
            JOptionPane.showMessageDialog(this, "ID 또는 비밀번호가 틀렸습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptRegister() {
        String id = idField.getText();
        String pw = new String(pwField.getPassword());

        if (id.isEmpty() || pw.isEmpty()) {
             JOptionPane.showMessageDialog(this, "ID와 비밀번호를 모두 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
             return;
        }

        if (dataService.registerUser(id, pw)) {
            JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 해주세요.", "성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "이미 존재하는 ID이거나 등록 실패.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}