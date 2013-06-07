package wmas.util;

import java.awt.Color;

public class Util {
	public static double round(double x) {
		return Math.round(x * 1000000) / 1000000.0;
	}

	public static int toInteger(double x) {
		return (int) Math.round(x);
	}

	public static String colorToString(Color c) {
		String r = "";
		if (c == null)
			return r;
		if (c != null)
			r = Integer.toHexString(c.getRGB() & 0xffffff);
		while (r.length() < 6)
			r = "0" + r;
		return "#" + r;
	}

	public static Color colorFromString(String s) {
		if (s == null)
			return null;
		if (s.equals(""))
			return null;
		return Color.decode(s);
	}

	public static String getFormatted(double t, int format) {
		String s = Double.toString(t);
		String[] s2 = s.split("[eE]");
		s = s2[0];
		String e = "";
		if (s2.length > 1) {
			e = "e" + s2[1];
		}
		if (s.length() > format - e.length()) {
			s = s.substring(0, format - e.length());
		} else
			while (s.length() != format - e.length())
				s += "0";
		return s + e;
	}

	public static String getFormatted(double t) {
		return getFormatted(t, 10);
	}
}
