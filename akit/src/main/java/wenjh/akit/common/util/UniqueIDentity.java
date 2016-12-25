package wenjh.akit.common.util;

import java.util.Random;

public abstract class UniqueIDentity {
	private static Random randGen = new Random();
	private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private static String prefix = randomString(7) + "-";
    private static long id = 0;
    
	public synchronized static String nextId() {
		 return prefix + Long.toString(id++);
	}
	public static String randomString(int length) {
        if (length < 1) {
            return null;
        }
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }
	
}
