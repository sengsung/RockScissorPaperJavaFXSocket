package kr.rsp.java;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class RSPGame {
    private int max;
    private int cnt;
    private boolean gameState = false;
    private ArrayList<String> joinerList = new ArrayList<>();
    private ArrayList<Integer> tossList = new ArrayList<Integer>();

    public int gameStart(int max) {
        /*
            0 : 이미 진행 중
            1 : 완료
        */
        if (gameState)
            return 0;

        clear();
        this.max = max;
        cnt = 0;
        gameState = true;

        return 1;
    }

    public int gameEnd() {
        /*
            0 : 게임 중이 아님
            1 : 완료
        */
        if (!gameState)
            return 0;

        gameState = false;

        return 1;
    }

    public int toss(String name, int type) {
        /*
         1 완료
         2 완료 및 게임 종료
         3 게임 중이 아님
         4 이미 냈음
        */
        if (!gameState)
            return 3;

        if (joinerList.contains(name))
            return 4;

        joinerList.add(name);
        tossList.add(type);
        cnt++;

        if (cnt >= max) {
            gameEnd();
            return 2;
        }
        return 1;
    }

    public String getResult() {
        /*
        0 : 아직 진행 중인 게임
        1 | 승자1 | 승자2 | 승자3 ... |
        2 : 무승부
        */
        if (gameState)
            return "0";

        var result = chk();
        if(result == 0)
            return "2";

        System.out.println(getUser(result));
        return "1|" + result + "|" + getUser(result);
    }

    private int chk() {
        /*
        0 : 무승부
        1 : 가위 승
        2 : 바위 승
        3 : 보 승
        */
        HashMap<Integer, Boolean> hm = new HashMap<>();
        for (int e : tossList) {
            hm.put(e, true);
        }
        if (hm.size() != 2)
            return 0;

        int key1 = 0, key2 = 0;
        for (int e : hm.keySet()) {
            if (key1 == 0)
                key1 = e;
            else if (e != key1)
                key2 = e;
        }

        if (key1 > key2) {
            int tmp = key1;
            key1 = key2;
            key2 = tmp;
        }

        int winner;
        if (key1 == 1) {
            if (key2 == 2) {
                return 2;
            } else  {
                return 1;
            }
        } else {
            return 3;
        }
    }

    private String getUser(int type) {
        String users = "";
        for (int i = 0; i < tossList.toArray().length; i++) {
            if (tossList.get(i) == type) {
                users += joinerList.get(i) + ",";
            }
        }
        return users;
    }

    private void clear() {
        joinerList.clear();
        tossList.clear();
    }
}
