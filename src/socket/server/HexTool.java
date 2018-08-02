package socket.server;

import java.io.UnsupportedEncodingException;





public class HexTool {
	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串�?
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 十六进制转字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "gbk");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}
	/**
	 * 自动补空�?
	 * @param s
	 * @param b
	 * @return
	 */
	public static String blankStringToHex(String str, int b) {
//		String str = toHexString(s);
		while(true){
			//目标长度小于或�?是等于十六进制字符串长度直接返回
			if (str.length() >=b ) {
				break;
			}
			str=str+"20";
		}
		return str;
	}
	public static String bkg(String hexstr, int len) {
	//	System.out.println("数据�?+hexstr+"数据的长度是:"+hexstr.getBytes().length);
		int oo = len - hexstr.getBytes().length;
		if(oo<=0){
			return hexstr;
		}
	//	System.out.println("缺失的位数是" + oo);
		String f = "";
		for (int i = 0; i < oo; i++) {
			f += "F";
		}
		return hexstr + f;
	}

	/**
	 * 字符串转16进制转字符串
	 * @param s
	 * @return
	 */
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}
	
	/**
	 * 自动补零
	 * @param s 
	 * @param d
	 * @return
	 */
	public static String buLing(String s,int d){
		String str= s;
		if(s.length()<d*2){
			for (int i = 0; i < d*2-s.length(); i++) {
				str= str+"0";
			}
		}
		return str;
	}
	/**
	 * 十进制转十六进制字节，高位在后，地位在前
	 * @param dec int类型的十进制�?
	 * @return
	 */
	public static String decToHex(int dec) {
	    String hex = "";
	    while(dec != 0) {
	        String h = Integer.toString(dec & 0xff, 16);
	        if((h.length() & 0x01) == 1)
	            h = '0' + h;
	        hex = hex + h;
	        dec = dec >> 8;
	    }
	    return hex;
	}
	
	/**
	 * 左补�?
	 * @param str
	 * @param strLength
	 * @return
	 */
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				sb.append("0").append(str);//左补0
				//sb.append(str).append("0");//右补0
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}
	
	
	  /**
	   * 计算校验位
	   * @param chackData
	   * @return
	   */
	   public static byte XorCheck(byte[] chackData)
       {
           byte temp = chackData[0];
           for (int i = 1; i < chackData.length;i++ )
           {
               temp = (byte)(temp ^ chackData[i]);
           }
           return temp;
       }
	
	
	
	
	   /**
     * UCS2解码
     * 
     * @param src
     *            UCS2 源串
     * @return 解码后的UTF-16BE字符�?
     */
    public static String DecodeUCS2(String src) {
        byte[] bytes = new byte[src.length() / 2];

        for (int i = 0; i < src.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer
                    .parseInt(src.substring(i, i + 2), 16));
        }
        String reValue = null;
        try {
            reValue = new String(bytes, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            //throw new PduDecodeException(e);
        }
        return reValue;

    }
	/**
	 * String转int(高位在后，低位在前) 0100
	 */
	public static int toIntHex4(String hex) {
		byte[] src = HexTool.hexStringToBytes(hex);
		return (int) ((src[0] & 0xFF) | ((src[1] & 0xFF) << 8));
	}
}
