package kr.rsp.java;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientSocketThread extends Thread {
    private Socket sock;
    private String name;
    private String password;

    private PrintWriter pw;
    private BufferedReader br;

    private Controller controller;

    public ClientSocketThread(Socket sock, String name, String password, Controller controller) {
        this.sock = sock;
        this.name = name;
        this.password = password;
        this.controller = controller;
    }

    @Override
    public void run() {
        String line;

        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            pw.println(name + "|" + password);
            pw.flush();

            while((line = br.readLine()) != null) {
                System.out.println(line);

                String[] line_split = line.split("\\|");
                String command = line_split[0];

                // 새로운 접속 new | 이름
                if (command.equals("new")) {
                    log("<시스템> '" + line_split[1] + "'님이 접속했습니다.");
                }

                // 채팅 chat | 이름 | 내용
                if (command.equals("chat")) {
                    log("<채팅> " + line_split[1] + " : " + line_split[2]);
                }

                // 게임
                /*
                    코드
                    0 : 게임 시작 및 종료
                        0 | 코드
                            코드
                            0 : 종료
                            1 : 시작
                    1 : 상대 가위바위보 누름
                        1 | 상대 이름

                    2 : 가위바위보 결과
                        2 | 이긴 상대들
                            내 결과
                            0 : 짐
                            1 : 이김
                    3 : 내용
                        3 | 내용
                    4 : 게임중이 아님
                    5 : 이미 냄
                */
                if (command.equals("game")) {
                    int code = Integer.parseInt(line_split[1]);
                    switch (code) {
                        case 0:
                            if (line_split[2].equals("1")) {
                                log("<시스템> 게임이 시작되었습니다!");
                                controller.cEnableGame();
                            } else {
                                log("<시스템> 게임이 종료되었습니다!");
                                controller.cDisableGame();
                            }
                            break;
                        case 1:
                            log("<시스템> " + line_split[2] + "님이 선택했습니다.");
                            break;
                        case 2:
                            log("<시스템> 게임이 종료되었습니다!");
                            log("<시스템> 승자 : " + line_split[2]);
                            break;
                        case 3:
                            log("<시스템> " + line_split[2]);
                            break;
                        case 4:
                            log("<시스템> 게임중이 아니에요!");
                            break;
                        case 5:
                            log("<시스템> 이미 냈어요!");
                            break;
                    }
                }

                if (command.equals("err")) {
                    int code = Integer.parseInt(line_split[1]);
                    switch (code) {
                        case 0:
                        case 1:
                            log("<시스템> 연결 거부 : 인원 초과");
                            break;
                        case 2:
                            log("<시스템> 연결 거부 : 잘못된 비밀번호");
                            break;
                        case 3:
                            log("<시스템> 연결 거부 : 중복되는 이름");
                            break;
                    }
                    sock.close();
                    controller.c_btn_connect.setDisable(false);
                    return;
                }

            }
//            ClientSendThread c_sendThread = new ClientSendThread(this, pw, br);
//            c_sendThread.start();
        } catch (SocketException se) {
          log("<시스템> 서버와의 연결이 끊어졌습니다!");
          controller.c_btn_connect.setDisable(false);
          return;
        } catch (Exception e) {
        }

    }

    public void send(String msg) {
        pw.println(msg);
        pw.flush();
    }

    public void sendCode(String type, int code) {
        pw.println(type + "|" + code);
        pw.flush();
    }

    public void sendChat(String msg) {
        pw.println("chat|" + msg);
        pw.flush();
    }

    public void log(String msg) {
        controller.clog(msg);
    }
}

//class ClientSendThread extends Thread{
//    private ClientSocketThread c_socketThread;
//    private PrintWriter pw;
//    private BufferedReader br;
//
//    public ClientSendThread(ClientSocketThread c_socketThread, PrintWriter pw, BufferedReader br) {
//        this.c_socketThread = c_socketThread;
//        this.pw = pw;
//        this.br = br;
//    }
//
//    @Override
//    public void run() {
//        String line;
//        try {
//            while((line = br.readLine()) != null) {
//                System.out.println(line);
//
//            }
//        } catch (Exception e) {
//
//        }
//    }
//}
