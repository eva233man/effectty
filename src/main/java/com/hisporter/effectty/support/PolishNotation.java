package com.hisporter.effectty.support;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装波兰表达式
 * Created by zhangjp on 2016/3/23.
 */
public class PolishNotation {

    /**
     * 将表达式字符串转换成中缀表达式List
     */
    public List<String> trans(String s) {
        List<String> ls = new ArrayList<String>();//存储表达式
        int i = 0;
        String str;
        char c;
        do {
//            if ( s.startsWith("(") && (i==0 || i == (s.length()-1))) {   //最外层的()抛掉不用
//                i++;
//                continue;
//            }
            if ((c = s.charAt(i)) == 40 || (c = s.charAt(i)) == 41) {  //( )
                ls.add("" + c);
                i++;
            } else if ((c = s.charAt(i)) == 91 || (c = s.charAt(i)) == 93) { // [ ]
                ls.add("" + c);
                i++;
            } else if (((c = s.charAt(i)) < 48 && (c = s.charAt(i)) != 36) && (c = s.charAt(i)) != 40 && (c = s.charAt(i)) != 41
                    || (((c = s.charAt(i)) > 57) && (c = s.charAt(i)) < 65)
                    || (c = s.charAt(i)) == 124) {  // + - * / & | != >= <= = > <
                str = "";
                while (i < s.length() && (
                        ((c = s.charAt(i)) < 48 && (c = s.charAt(i)) != 36 && (c = s.charAt(i)) != 40 && (c = s.charAt(i)) != 41)
                                || (((c = s.charAt(i)) > 57) && (c = s.charAt(i)) < 65)
                                || (c = s.charAt(i)) == 124
                )) {
                    str += c;
                    i++;
                }
                ls.add(str);
            } else {
                str = "";
                while (i < s.length() && (
                        ((c = s.charAt(i)) >= 48 && (c = s.charAt(i)) <= 57) ||  //数字
                                ((c = s.charAt(i)) >= 65 && (c = s.charAt(i)) <= 90) ||  //英文字符
                                ((c = s.charAt(i)) >= 97 && (c = s.charAt(i)) <= 122) ||  //英文字符
                                ((c = s.charAt(i)) == 36)  //$
                )) {
                    str += c;
                    i++;
                }
                ls.add(str);
            }

        } while (i < s.length());
        return ls;
    }

    /**
     * 将中序表达式转换为逆波兰表达式
     */
    public List<String> parse(List<String> ls) {
        Stack ms1 = new Stack();
        List<String> lss = new ArrayList<String>();
        List<String> ls1 = new ArrayList<String>();
        int i = 0;
        for (String ss : ls) {
            if (ss.equals("(")) {
                i++;
                if (i > 1) {
                    ls1.add(ss);   //如果不是本次要处理的（ 直接压入ls1
                }
            } else if (i > 0 && !(i == 1 && ss.equals(")"))) {     //执行开始（后，直到碰到），之间的全部压入ls1.做后续的迭代处理
                ls1.add(ss);
                if (ss.equals(")")) {
                    i--;
                }
            } else if (i == 1 && ss.equals(")")) {
                List<String> res = parse(ls1);
                for (String str : res) {
                    lss.add(str);
                }
                ls1 = new ArrayList<String>();
                i--;
            } else if (ss.matches("[a-zA-Z0-9$]+")) {
                lss.add(ss);
            } else if (ss.equals("[")) {
                ms1.push(ss);
            } else if (ss.equals("]")) {
                while (!ms1.top.equals("[")) {
                    lss.add(ms1.pop());
                }
                ms1.pop();
            } else {
                while (ms1.size() != 0 && getValue(ms1.top) >= getValue(ss)) {
                    lss.add(ms1.pop());
                }
                ms1.push(ss);
            }
        }
        while (ms1.size() != 0) {
            lss.add(ms1.pop().trim());
        }
        return lss;
    }

    /**
     * 根据话单行、查询入参和过滤条件计算是否匹配
     */
    public boolean match(String[] line, List<String> args, List<String> filters) {
        Stack ms2 = new Stack();
        //根据表达式计算过滤值
        for (String ss : filters) {
            if (ss.matches("[a-zA-Z0-9$]+")) {
                ms2.push(ss);
            } else {
                if (ss.equals("!!")) {
                    String b = ms2.pop();//取到后面的匹配值
                    boolean j = compare(null, b, ss);
                    ms2.push("" + j);
                } else {
                    String b = ms2.pop();//取到后面的匹配值
                    if (b.equals("null")) {
                        b = "";
                    }
                    String str1 = ms2.pop();
                    if (getValue(ss) > 2) {
                        int a = Integer.parseInt(str1);//取到详单记录的数组的下标
                        str1 = line[a];
                    }
                    String str2 = b;
                    //将$1 变成实际的值
                    if (b.startsWith("$")) {
                        int i = Integer.parseInt(b.substring(1));
                        str2 = args.get(i);
                    }
                    boolean j = compare(str1, str2, ss);
                    ms2.push("" + j);
                }
            }
        }
        return Boolean.parseBoolean(ms2.pop());
    }

    public boolean match(List<String> lineList, List<String> args, List<String> filters) {
        Stack ms2 = new Stack();
        //根据表达式计算过滤值
        for (String ss : filters) {
            if (ss.matches("[a-zA-Z0-9$]+")) {
                ms2.push(ss);
            } else {
                if (ss.equals("!!")) {
                    String b = ms2.pop();//取到后面的匹配值
                    boolean j = compare(null, b, ss);
                    ms2.push("" + j);
                } else {
                    String b = ms2.pop();//取到后面的匹配值
                    if (b.equals("null")) {
                        b = "";
                    }

                    String str1 = ms2.pop();
                    if (getValue(ss) > 2) {
                        int a = Integer.parseInt(str1);//取到详单记录的数组的下标
                        str1 = lineList.get(a);
                    }
                    String str2 = b;
                    //将$1 变成实际的值
                    if (b.startsWith("$")) {
                        int i = Integer.parseInt(b.substring(1));
                        str2 = args.get(i);
                    }
                    boolean j = compare(str1, str2, ss);
                    ms2.push("" + j);
                }
            }
        }
        return Boolean.parseBoolean(ms2.pop());
    }

    /**
     * 计算两个值的bool运算
     */
    public static boolean compare(String s1, String s2, String oper) {
        boolean result = false;
        if (oper.equals("=") || oper.equals("==")) {
            result = s1.equals(s2);
        } else if (oper.equals(">=")) {
            if (s1.compareTo(s2) >= 0) {
                result = true;
            } else {
                result = false;
            }
        } else if (oper.equals("<=")) {
            if (s1.compareTo(s2) <= 0) {
                result = true;
            } else {
                result = false;
            }
        } else if (oper.equals(">")) {
            if (s1.compareTo(s2) > 0) {
                result = true;
            } else {
                result = false;
            }
        } else if (oper.equals("<")) {
            if (s1.compareTo(s2) < 0) {
                result = true;
            } else {
                result = false;
            }
        } else if (oper.equals("!=") || oper.equals("!")) {
            result = !(s1.equals(s2));
        } else if (oper.equals("|") || oper.equals("||")) { //按或处理，不是位运算
            boolean b1 = Boolean.parseBoolean(s1);
            boolean b2 = Boolean.parseBoolean(s2);
            return b1 || b2;
        } else if (oper.equals("&") || oper.equals("&&")) { //按与处理，不是位运算
            boolean b1 = Boolean.parseBoolean(s1);
            boolean b2 = Boolean.parseBoolean(s2);
            return b1 && b2;
        } else if (oper.equals("!!")) {
            boolean b2 = Boolean.parseBoolean(s2);
            return !b2;
        }

        return result;
    }

    /**
     * 获取运算符优先级
     * +,-为1 *,/为2 ()为0
     */
    public static int getValue(String s) {
        if (s.equals("[")) {
            return 1;
        } else if (s.equals("]")) {
            return 1;
        } else if (s.equals("&")) {
            return 2;
        } else if (s.equals("|")) {
            return 2;
        } else if (s.equals("&&")) {
            return 2;
        } else if (s.equals("||")) {
            return 2;
        } else if (s.equals("=")) {
            return 3;
        } else if (s.equals("!")) {
            return 3;
        } else if (s.equals(">=")) {
            return 3;
        } else if (s.equals(">")) {
            return 3;
        } else if (s.equals("<=")) {
            return 3;
        } else if (s.equals("<")) {
            return 3;
        } else if (s.equals("!=")) {
            return 3;
        } else if (s.equals("==")) {
            return 3;
        } else if (s.equals("+")) {
            return 4;
        } else if (s.equals("-")) {
            return 4;
        } else if (s.equals("*")) {
            return 5;
        } else if (s.equals("/")) {
            return 5;
        } else if (s.equals("%")) {
            return 5;
        }
        return 0;
    }

    /**
     * 内部类：波兰后缀表达式所需要的栈
     */
    public class Stack {
        private List<String> l;
        private int size;
        public String top;

        public Stack() {
            l = new ArrayList<String>();
            size = 0;
            top = null;
        }

        public int size() {
            return size;
        }

        public void push(String s) {
            l.add(s);
            top = s;
            size++;
        }

        public String pop() {
            String s = l.get(size - 1);
            l.remove(size - 1);
            size--;
            top = size == 0 ? null : l.get(size - 1);
            return s;
        }
    }

    /**
     * 内部类：波兰后缀表达式所需要的栈，先进先出
     */
    public class FIFOStack {
        private List<String> l;
        private int size;
        public String top;

        public FIFOStack() {
            l = new ArrayList<String>();
            size = 0;
            top = null;
        }

        public int size() {
            return size;
        }

        public void push(String s) {
            l.add(s);
            top = s;
            size++;
        }

        public String pop() {
            String s = l.get(0);
            l.remove(0);
            size--;
            top = size == 0 ? null : l.get(0);
            return s;
        }
    }


    public static void main(String[] args) {
        PolishNotation polishNotation = new PolishNotation();

        //String str = "([00=mm]&[8>=$1]&[8<=$2])";
       /* String str = "([64>130701000000]&[17=000000]&([64>101220180000]&([83=1]|[83=2]|[83=3]))&([73!CMDM]&[73!cmdm]&[15!4000000002]&[15!4000000004]&[15!2000000009]&[15!4000000005]&[15!4000000006]&[15!2000000000]))";

        PolishNotation polishNotation = new PolishNotation();
        List<String> midList = polishNotation.trans(str);*/
//        for(String mid : midList){
//            System.out.println(mid);
//        }
        /*System.out.println("逆序");
        List<String> revList = polishNotation.parse(midList);
        for(String mid : revList){
            System.out.println(mid);
        }*/
//
//        boolean a =true;
//        String b = ""+a;
//        boolean c = Boolean.parseBoolean(b);
//        System.out.println(c);

        /*test match*/
        /*match(String[] line, List<String> args, List<String> filters)*/
        /*String testLine = "vc^|01^|460023451018866^|013044003610490^|15804620200^|1008611^|20160215181103^|10^|^|8613441707^|4573^|5191^|218e1^|60001^|HDS01O^|HBSM2TI^|000^|^|0^|a^|c^|000^|451^|451^|451^|451^|451^|9^|0^|0^|000^|020^|0^|^|0^|^|0^|0^|0^|^|0^|HRM81710^|160215181230^|1^|00000^|00^|0000000^|000000000^|00000^|0000000^|000000000^|000000000000000^|hn^|0^|^|^|1580462^|16^|02^|15^|451^|^|^|0^|0^|0^|218e160001^|00^|00000000^|00000000^|^|0^|^|20160215181500^|^|^|^|^|";
        String[] lines = testLine.split("\\^\\|");
        List<String> conds = new ArrayList();
        conds.add("15804620200");
        conds.add("20160201100000");
        conds.add("20160216195959");

        for (int i = 0; i < lines.length; i++) {
            System.out.println(i + "==>" + lines[i]);
        }


        List<String> filters = polishNotation.parse(polishNotation.trans("!![00=vc]&[28=0]&[06>=$1]&[06<=$2]&[64!1]"));
        System.out.println("===========================");
        System.out.println(filters);
        Boolean flag = polishNotation.match(lines, conds, filters);
        System.out.println(flag);*/

        System.out.println("============ 2 ==============");
        String testLine = "vc^|^|";
        //lines = testLine.split("\\^\\|");
        String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(testLine, "^|");
        System.out.println(lines.length);
        for (String s : lines) {
            System.out.println(s);
        }
        System.out.println("=======3====");
        System.out.println(lines[1]);
        List<String> filters = polishNotation.parse(polishNotation.trans("[00=vc]&&[01!=null]"));
        System.out.println(filters);
        Boolean flag = polishNotation.match(lines, null, filters);
        System.out.println(flag);

    }

}
