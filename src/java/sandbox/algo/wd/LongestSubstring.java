
public class LongestSubstring {
    public static void main(String[] args) {
        String[] testStrings = new String[]{
                "abcabcbb",
                "bbbbbbbb",
                "dvdf",
                "pwwkew",
                "aabaab!bb"
        };
        for (String t : testStrings) {
            System.out.println(t + " = " + new LongestSubstring().calculate(t));
        }
    }

    public int calculate(String s) {
        int l = s.length();
        if (l < 2) return l;

        int[] visited = new int[128];
        int maxLength = 0;
        int start = 0;

        for (int i = 0; i < visited.length; i++) {
            visited[i] = -1;
        }
        visited[0] = s.charAt(0);
        for (int i = 1; i < l; i++) {
            int curByte = s.charAt(i);
            if (visited[curByte] < 0) {
                maxLength = Math.max(maxLength, i - start);
            } else {
                int tillIndex = visited[curByte];
                while (start < tillIndex + 1) {
                    visited[s.charAt(start++)] = -1;
                }
            }
            visited[curByte] = i;
        }
        return maxLength + 1;
    }
}