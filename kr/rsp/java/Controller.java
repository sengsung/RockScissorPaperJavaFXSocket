package kr.rsp.java;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Controller {
    @FXML private Button btn_server;
    @FXML private Button btn_client;

    @FXML
    public void showServer(ActionEvent event) throws IOException {
        Stage stage = (Stage) btn_server.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("server.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("서버");
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("프로그램 종료할게요!");
                System.exit(0);
            }
        });
    }

    @FXML
    public void showClient(ActionEvent event) throws IOException {
        Stage stage = (Stage) btn_client.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("클라이언트");
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("프로그램 종료할게요!");
                System.exit(0);
            }
        });
    }

    // --------------------- 서버 --------------------
    @FXML private TextField s_txtbox_port;
    @FXML private TextField s_txtbox_max;
    @FXML private TextField s_txtbox_password;
    @FXML private Button s_btn_serverStart;
    @FXML private Button s_btn_gameStart;
    @FXML private CheckBox s_chkbox_autoRegame;
    @FXML private TextArea s_richtextbox_log;

    private ServerThread s_thread;
    @FXML
    public void serverStart() {
        s_btn_serverStart.setDisable(true);

        int port;
        int max;
        String password = s_txtbox_password.getText();

        // 초기 설정
        if (password.contains("|")) {
            this.slog("비밀번호에 사용할 수 없는 문자열이 있어요!");
            s_btn_serverStart.setDisable(false);
            return;
        }

        try {
            port = Integer.parseInt(s_txtbox_port.getText());

            if (port < 0 || port > 65535) {
                this.slog("포트가 범위에서 벗어났습니다! (0 ~ 65535)");
                s_btn_serverStart.setDisable(false);
                return;
            }
        } catch (Exception e) {
            this.slog("포트를 확인해주세요!");
            s_btn_serverStart.setDisable(false);
            return;
        }
        try {
            max = Integer.parseInt(s_txtbox_max.getText());
        } catch (Exception e) {
            this.slog("최대 인원을 확인해주세요!");
            s_btn_serverStart.setDisable(false);
            return;
        }

        // 코드 시작
        s_thread = new ServerThread(port, max , password, s_richtextbox_log, this);
        s_thread.start();
        s_btn_gameStart.setDisable(false);
    }

    public void gameStartAndEnd() {
        // 0 : 종료, 1 : 시작
        int type = s_btn_gameStart.getText().equals("게임시작") ? 1 : 0;

        if (type == 1) {
            int result = s_thread.gameStart();
            if (result == 1)
                s_btn_gameStart.setText("게임중지");
        } else {
            int result = s_thread.gameEnd();
            if (result == 1)
                s_btn_gameStart.setText("게임시작");
        }
    }

    public void gameReset() {

        int type = s_btn_gameStart.getText().equals("게임시작") ? 1 : 0;

        if (type == 1) {
            s_btn_gameStart.setText("게임중지");
        } else {
            s_btn_gameStart.setText("게임시작");
        }

        if (s_chkbox_autoRegame.isSelected()) {
            gameStartAndEnd();
        }
    }
    // --------------- 클라이언트 -------------
    @FXML private TextField c_txtbox_server;
    @FXML private TextField c_txtbox_port;
    @FXML private TextField c_txtbox_name;
    @FXML private TextField c_txtbox_password;
    @FXML private TextField c_txtbox_chat;
    @FXML private TextArea c_richtextbox_log;
    @FXML public Button c_btn_connect;
    @FXML private Button c_btn_send;
    @FXML private Button c_btn_r;
    @FXML private Button c_btn_s;
    @FXML private Button c_btn_p;

    ClientSocketThread c_socketThread;

    @FXML
    public void clientStart() {
        c_btn_connect.setDisable(true);

        String serverAddress;
        String name;
        String password;
        int port;

        // 사전 확인
        if ((serverAddress = c_txtbox_server.getText().trim()).equals("")) {
            clog("서버주소를 입력해주세요!");
            c_btn_connect.setDisable(false);
            return;
        }

        if ((name = c_txtbox_name.getText().trim()).equals("")) {
            clog("이름 입력해주세요!");
            c_btn_connect.setDisable(false);
            return;
        }

        password = c_txtbox_password.getText().trim();

        try {
            port = Integer.parseInt(c_txtbox_port.getText());
        } catch (Exception e) {
            clog("포트를 확인해주세요!");
            c_btn_connect.setDisable(false);
            return;
        }

        // 클라이언트 시작
        Socket sock;
        try {
            sock = new Socket(serverAddress, port);
            c_socketThread = new ClientSocketThread(sock, name, password, this);
        } catch (Exception e) {
            clog("소켓에 연결할 수 없는 것 같아요!");
            c_btn_connect.setDisable(false);
            return;
        }
        c_socketThread.start();
        c_btn_send.setDisable(false);
        c_txtbox_chat.setDisable(false);
    }

    @FXML
    public void sendChat() {
        String chat = c_txtbox_chat.getText().trim();
        c_txtbox_chat.setText("");

        if (chat.equals("")) return;
        if (chat.contains("|")) {
            slog("사용할 수 없는 문자열이 있어요!");
        }
        c_socketThread.sendChat(chat);

    }

    @FXML
    public void clickR() {
        c_socketThread.sendCode("game", 2);
        cDisableGame();
    }

    @FXML
    public void clickS() {
        c_socketThread.sendCode("game", 1);
        cDisableGame();
    }

    @FXML
    public void clickP() {
        c_socketThread.sendCode("game", 3);
        cDisableGame();
    }
    // ---------------------- ETC -----------------------
    private void slog(String msg) {
        s_richtextbox_log.appendText(msg + "\n");
    }

    public void clog(String msg) {
        c_richtextbox_log.appendText(msg + "\n");
    }

    public void cEnableGame() {
        c_btn_r.setDisable(false);
        c_btn_s.setDisable(false);
        c_btn_p.setDisable(false);
    }

    public void cDisableGame() {
        c_btn_r.setDisable(true);
        c_btn_s.setDisable(true);
        c_btn_p.setDisable(true);
    }
}
