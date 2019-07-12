package kr.rsp.java;

import javafx.scene.control.Button;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerSocketThread extends Thread {
    private Socket sock;
    private String name;
    private HashMap<String, Object> hm;
    private ArrayList<String> nameList;

    private ServerThread s_thread;
    private PrintWriter pw;
    private BufferedReader br;
    private RSPGame rspGame;

    public ServerSocketThread(Socket sock, HashMap hm, ArrayList nameList, ServerThread s_thread, RSPGame rspGame) {
        this.sock = sock;
        this.hm = hm;
        this.nameList = nameList;
        this.s_thread = s_thread;
        this.rspGame = rspGame;
    }

    @Override
    public void run() {
        // 처음에 읽는건 "이름|비밀번호
        String line;
        try {
            pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            line = br.readLine();
            System.out.println(line);
            name = line.split("\\|")[0];

        } catch (Exception e) {
            return;
        }

        log("접속시도 : " + line);
        // 게임 상태 확인
        // 최대 인원 확인
        synchronized (hm) {
            //if ((boolean) hm.get("state")){
            //    send("err", 0);
            //    log("접속실패 : 이미 시작된 게임 : " + name);
            //    try {
           //         sock.close();
           //     } catch (Exception e) {
           //         e.printStackTrace();
           //     }
           //     return;
           // }

            if ((int) hm.get("cnt") >= (int) hm.get("max")) {
                sendErr(1);
                log("접속실패 : 정원 초과 : " + name);
                try {
                    sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // 비밀번호 확인
        String password = (String) hm.get("password");
        if (!password.equals("") && (line.split("\\|").length < 2 || !password.equals(line.split("\\|")[1]))) {
            sendErr(2);
            log("접속실패 : 잘못된 비밀번호 : " + name);
            try {
                sock.close();
                return;
            }
            catch (Exception e) {
            }
        }

        // 이름 중복 확인
        synchronized (nameList) {
            System.out.println(name);
            if (nameList.contains(name)) {
                sendErr(3);
                log("접속실패 : 중복된 이름 : " + name);
                try {
                    sock.close();
                    return;
                }
                catch (Exception e) {
                }
            }
            nameList.add(name);

        }
        synchronized (hm) {
            hm.put("cnt", ((int) hm.get("cnt")) + 1);
            ((ArrayList<PrintWriter>) hm.get("pwList")).add(pw);

        }

        // 이제 부터 통신 시작
        send("connectOK");
        s_thread.broadcast("new|" + name);
        log("접속성공 : " + name);
        try {
            while((line = br.readLine()) != null) {
                System.out.println(line);

                String command = line.split("\\|")[0];
                String content = line.split("\\|")[1];

                /*
                 커맨드 종류
                    chat
                    game
                 */
                // 채팅
                if (command.equals("chat")) {
                    log("<채팅>" + name + " : " + content);
                    s_thread.broadcast("chat|" + name + "|" + content);
                }

                // 가위바위보 고름
                if (command.equals("game")) {
                    synchronized (rspGame) {
                        log("가위바위보 냄 : " + name);

                        int result = rspGame.toss(name, Integer.parseInt(content));
                        switch (result) {
                            case 1:
                                log("가위바위보 : 완료 : " + name);
                                s_thread.broadcast("game|1|" + name);
                                break;
                            case 2:
                                log("가위바위보 : 완료 : " + name);
                                s_thread.broadcast("game|1|" + name);
                                synchronized (rspGame) {
                                    String gameResult = rspGame.getResult();
                                    if (gameResult.equals("2")) {
                                        s_thread.broadcast("game|0|0");
                                        s_thread.broadcast("game|3|무승부!");
                                    } else {
                                        String winner = rspGame.getResult().split("\\|")[2];
                                        winner = winner.substring(0, winner.length() - 1);
                                        s_thread.broadcast("game|2|" + winner);
                                    }
                                    try {
                                        s_thread.gameReset();
                                    } catch (Exception e) {}

                                    log("게임이 끝났습니다");
                                }
                                break;
                            case 3:
                                log("가위바위보 : 게임 중 아님 : " + name);
                                send("game|4");
                                break;
                            case 4:
                                log("가위바위보 : 이미 냄 : " + name);
                                send("game|5");
                                break;
                        }
                    }
                }
            }
        } catch (SocketException se) {
            se.toString();
            s_thread.log("클라이언트 연결 끊김 : " + name);
            synchronized (hm) {
                hm.put("cnt", (int) hm.get("cnt") - 1);

                int index = 0;
                for (String e : nameList) {
                    if (e.equals(name))
                        break;
                    index++;
                }
                nameList.remove(index);
                ((ArrayList) hm.get("pwList")).remove(index);
            }

            int result = rspGame.gameEnd();
            if (result == 1) {
                s_thread.broadcast("game|0|0");
                s_thread.log("게임 종료 : 클라이언트 연결 끊김");
                s_thread.gameReset();
            }

            s_thread.broadcast("game|3|'" + name + "'님의 접속이 끊어졌습니다");
        } catch (Exception e) {
        }
    }

    // -----------------------------------
    private void send(String msg) {
        pw.println(msg);
        pw.flush();
    }

    private void sendErr(int code) {
        /*
        에러
        0 : 이미 시작된 게임
        1 : 인원 초과
        2 : 비밀번호가 일치하지 않음
        3 : 중복되는 이름
        */
        pw.println("err|" + code);
        pw.flush();
    }

    private void log(String msg) {
        this.s_thread.log(msg);
    }
}

//class serverListenThread extends Thread{
//    public serverListenThread() {
//
//    }
//}