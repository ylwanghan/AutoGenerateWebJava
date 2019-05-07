package wanghan.jsp.util;

public class StringUtil {
	/**
	 * String转float
	 * @param str
	 * @return
	 */
	public static float strToFlo(String str) {
		float i = 0;
		try {
			i = Integer.parseInt(str);
		} catch (Exception e) {
		}
		return i;
	}

	public static int StringToInt(String parameter) {
		int i = 0;
		try {
			i = Integer.parseInt(parameter);
		} catch (Exception e) {
		}
		return i;
	}

	
	

}

