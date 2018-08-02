package socket.util;
import java.io.PrintStream;
import java.security.MessageDigest;

@SuppressWarnings("unused")
public class MD5Util
{

    public MD5Util()
    {
    }

    private static String byteArrayToHexString(byte b[])
    {
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b)
    {
        int n = b;
        if(n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String MD5Encode(String origin)
    {
        String resultString = null;
        try
        {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        }
        catch(Exception exception) { }
        return resultString;
    }
    
    public static String MD5Encode(String origin, String charset)
    {
        String resultString = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(origin.getBytes(charset)));
        }
        catch(Exception exception) { }
        return resultString;
    }

    public static void main(String agrs[])
    {
        System.out.println("CODING:"+MD5Encode("GLOBAL_ID:669288;DATE:2018-02-07;CARD_TYPE:C"));
    }

    private static final String hexDigits[] = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", 
        "A", "B", "C", "D", "E", "F"
    };

}
