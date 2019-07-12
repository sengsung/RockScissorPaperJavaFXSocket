package kr.rsp.java;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerThread extends Thread {
    private int port;
    private int max;
    private String password;
    private TextArea richtextbox_log;

    private HashMap<String, Object> hm = new HashMap<>();
    private ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<PrintWriter> sockPWList = new ArrayList<>();

    private ServerSocketThread s_socketThread;
    private RSPGame rspGame;
    private Controller controller;
    private Button btn_start;

    public ServerThread(int port, int max, String password, TextArea richtextbox_log, Controller controller) {
        this.port = port;
        this.max = max;
        this.password = password;
        this.richtextbox_log = richtextbox_log;
        this.controller = controller;
    }

    @Override
    public void run() {
        rspGame = new RSPGame();

        try {
            hm.put("max", max);
            hm.put("cnt", 0);
            hm.put("password", password);
            hm.put("pwList", sockPWList);

            ServerSocket server = new ServerSocket(this.port);
            log("서버시작");
            while (true) {
                Socket sock = server.accept();
                s_socketThread = new ServerSocketThread(sock, hm, nameList, this, rspGame);
                s_socketThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
        0: 실패
        1: 성공
    */
    public int gameStart() {
        synchronized (hm) {
            if ((int) hm.get("cnt") < 2) {
                log("최소 두 명 이상이 접속해야 합니다!");
                log("현재 접속자 : " + hm.get("cnt") + "명");
                return 0;
            }
            synchronized (rspGame) {
                int result = rspGame.gameStart((int) hm.get("cnt"));
                if (result == 0) {
                    log("이미 진행 중인 게임");
                    return 0;
                }
                broadcast("game|0|1");
                log("게임 시작");
                return 1;
            }
        }
    }

    public int gameEnd() {
        synchronized (rspGame) {
            var result = rspGame.gameEnd();
            if (result == 0) {
                log("게임 중이 아님");
                return 0;
            }
            broadcast("game|3|관리자가 게임을 중지했습니다");
            broadcast("game|0|0");
            log("게임 중지");
            return 1;
        }

    }

    public void gameReset() {
        controller.gameReset();
   }

    public void broadcast(String msg) {
        for (PrintWriter pw : sockPWList ) {
            try {
                pw.println(msg);
                pw.flush();
            }
            catch (Exception e) {
                e.getStackTrace();
            }

        }
    }
    public void log(String msg) {
        richtextbox_log.appendText(msg + "\n");
    }
}