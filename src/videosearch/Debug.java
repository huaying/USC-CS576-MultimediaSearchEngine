package videosearch;

/**
 * Created by huayingt on 4/16/15.
 */
public class Debug {
    static public boolean debug = true;
    static public void print(String s){
        if(debug) {
            System.out.println(s);
        }
    }
    static public void print(Object [] s){
        if(debug) {
            for (int i = 0; i < s.length; i++) {
                if (i == s.length - 1) {
                    System.out.println(s[i]);
                } else {
                    System.out.print(s[i] + ", ");
                }
            }
        }
    }
}
