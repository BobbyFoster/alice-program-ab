package org.alicebot.ab.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarUtils {

	public static String formatTime(final String formatString, final long msSinceEpoch) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
		final Calendar cal = Calendar.getInstance();
		dateFormat.setCalendar(cal);
		return dateFormat.format(new Date(msSinceEpoch));
	}

	public static int timeZoneOffset() {
		final Calendar cal = Calendar.getInstance();
		final int offset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
		return offset;
	}

	public static String year() {
		final Calendar cal = Calendar.getInstance();
		return String.valueOf(cal.get(Calendar.YEAR));
	}

	public static String date() {
		final Calendar cal = Calendar.getInstance();
		cal.get(Calendar.YEAR);
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, yyyy");
		dateFormat.setCalendar(cal);
		return dateFormat.format(cal.getTime());
	}

	public static String date(String jformat, String locale, String timezone) {
		// HashSet<String> attributeNames = Utilities.stringSet("jformat","format","locale","timezone");
		if (jformat == null) {
			jformat = "EEE MMM dd HH:mm:ss zzz yyyy";
		}
		if (locale == null) {
			locale = Locale.US.getISO3Country();
		}
		if (timezone == null) {
			timezone = TimeZone.getDefault().getDisplayName();
		}
		// System.out.println("Format = "+format+" Locale = "+locale+" Timezone = "+timezone);
		String dateAsString = new Date().toString();
		try {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(jformat);
			dateAsString = simpleDateFormat.format(new Date());
		} catch (final Exception ex) {
			System.out.println("CalendarUtils.date Bad date: Format = " + jformat + " Locale = "
					+ locale + " Timezone = " + timezone);
			ex.printStackTrace();
		}
		// MagicBooleans.trace("CalendarUtils.date: "+dateAsString);
		return dateAsString;
	}

}
