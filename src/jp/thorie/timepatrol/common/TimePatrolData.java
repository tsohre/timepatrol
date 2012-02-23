package jp.thorie.timepatrol.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.R.integer;

public class TimePatrolData {
	private int id;
	public Calendar day;
	public int category;
	public Calendar startedWork;
	public Calendar endedWork;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TimePatrolData() {
		category = -1;
	}

	/**
	 * yyyy/mm の形式で日付を返します。
	 * @return
	 */
	public String getYYYYMM() {
		return day.get(Calendar.YEAR) + "/" + day.get(Calendar.MONTH);
	}

	/**
	 * yyyy/mm/dd の形式で日付を返します。
	 * @return
	 */
	public String getYYYYMMDD() {
		return day.get(Calendar.YEAR) + "/" + day.get(Calendar.MONTH) + "/" + getDispDayOfWeek(day.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 表示形式で曜日を返します。
	 * @param dayOfWeek
	 * @return
	 */
	private String getDispDayOfWeek(int dayOfWeek) {
		String retVal = null;

		switch (dayOfWeek) {
			case Calendar.SUNDAY:
				retVal = "(日)";
				break;
			case Calendar.MONDAY:
				retVal = "(月)";
				break;
			case Calendar.TUESDAY:
				retVal = "(火)";
				break;
			case Calendar.WEDNESDAY:
				retVal = "(水)";
				break;
			case Calendar.THURSDAY:
				retVal = "(木)";
				break;
			case Calendar.FRIDAY:
				retVal = "(金)";
				break;
			case Calendar.SATURDAY:
				retVal = "(土)";
				break;
			default:
				retVal = "";
				break;
		}

		return retVal;
	}

	/**
	 * アプリケーション上で表示する形式で、日付を返します。
	 *
	 * format:dd(day_of_week)
	 * ex:15(土)
	 *
	 * @return
	 */
	public String getDispDay() {
		return
				String.format(
						"%02d%s",
						day.get(Calendar.DAY_OF_MONTH),
						getDispDayOfWeek(day.get(Calendar.DAY_OF_WEEK))
				); //TODO 空白埋めだと、なぜか数値と空白の大きさが違うためにうまくいかない。
	}

	/**
	 * アプリケーション上で表示する形式で、種別を返します。
	 * @return
	 */
	public String getDispCategory() {
		if(category == -1) {
			return "";
		}
		else {
			return Category.getDispStr(category);
		}
	}

	/**
	 * アプリケーション上で表示する形式で、勤務開始時間を返します。
	 * @return
	 */
	public String getDispWorkStart() {
		if(startedWork == null) {
			return "";
		}
		else {
			return
					String.format("%02d:%02d",
						startedWork.get(Calendar.HOUR_OF_DAY),
						startedWork.get(Calendar.MINUTE)
					);
		}
	}

	/**
	 * アプリケーション上で表示する形式で、勤務終了時間を返します。
	 * @return
	 */
	public String getDispWorkEnd() {
		if(endedWork == null) {
			return "";
		}
		else {
			return
					String.format("%02d:%02d",
						endedWork.get(Calendar.HOUR_OF_DAY),
						endedWork.get(Calendar.MINUTE)
					);
		}
	}

	/**
	 * ファイルに書きこまれている行を渡すことで<br>
	 * 当クラスのインスタンスにして返します。
	 * @param row
	 * @throws Exception
	 */
	public static TimePatrolData createForData(String row) throws Exception {
		if(row == null) return null;

		String[] elements = row.split("\t");
		if(elements.length != 4) {
			throw new IllegalArgumentException("ファイルが壊れています。");
		}

		TimePatrolData tcd = new TimePatrolData();

		try {
			//day
			Calendar day = new GregorianCalendar();
			day.setTimeInMillis(Long.valueOf(elements[0]));
			tcd.day = day;

			//category
			tcd.category = Integer.parseInt(elements[1]);

			//work_start
			Calendar ws = new GregorianCalendar();
			ws.setTimeInMillis(Long.valueOf(elements[2]));
			tcd.startedWork = ws;

			//work_end
			Calendar we = new GregorianCalendar();
			we.setTimeInMillis(Long.valueOf(elements[3]));
			tcd.endedWork = we;
		}
		catch(Exception e) {
			//androidの例外処理わからん・・・。
			System.err.println("ファイルが壊れてます。");
			e.printStackTrace();
			throw e;
		}

		return tcd;
	}

	/**
	 * メールに書き込むための文字列にして返します。
	 * @return
	 */
	public String createForMail() {
		StringBuffer sb = new StringBuffer();
//			sb
//				//.append(day.get(Calendar.DAY_OF_MONTH)).append("\t") //日付
//				.append(startedWork.get(Calendar.YEAR)).append("/").append(startedWork.get(Calendar.MONTH)+1).append("/").append(startedWork.get(Calendar.DAY_OF_MONTH)) //開始
//					.append(" ").append(startedWork.get(Calendar.HOUR_OF_DAY)).append(":").append(startedWork.get(Calendar.MINUTE)).append("\t")
//				.append(endedWork.get(Calendar.YEAR)).append("/").append(endedWork.get(Calendar.MONTH)+1).append("/").append(endedWork.get(Calendar.DAY_OF_MONTH)) //終了
//					.append(" ").append(endedWork.get(Calendar.HOUR_OF_DAY)).append(":").append(endedWork.get(Calendar.MINUTE)).append("\t")
//				.append("\t") //働いた時間
//				.append("2\t") //休み時間（今は固定
//				.append(Category.getDispStr(category).replaceAll("　", ""))
//			;

		sb
			.append(startedWork.get(Calendar.HOUR_OF_DAY)).append(":").append(startedWork.get(Calendar.MINUTE)).append("\t") //出勤時間
			.append(endedWork.get(Calendar.HOUR_OF_DAY)).append(":").append(endedWork.get(Calendar.MINUTE)).append("\t") //退勤時間
			.append("2").append("\t") //TODO 休み時間は固定
			.append(getWorkTime(startedWork, endedWork) - 2).append("\t") //実労時間
			.append(Category.getDispStr(category))
			;

		return sb.toString();
	}

	/**
	 * 日付の引き算を行います。<br>
	 * 渡された時間を考慮し、日付が回っていてもある程度対応します。<br>
	 * 24時間以上連続して働くことなどできません！帰れ！！
	 * @return
	 */
	public static int getWorkTime(Calendar startedWork, Calendar endedWork) {
		Calendar _start = new GregorianCalendar();
		_start.setTimeInMillis(startedWork.getTimeInMillis());

		Calendar _end = new GregorianCalendar();
		_end.setTimeInMillis(endedWork.getTimeInMillis());


		//深夜0時を回ったと思われる場合
		if(_end.compareTo(_start) < 0) {
			_end.add(Calendar.HOUR_OF_DAY, 24);
		}

		long l = _end.getTimeInMillis() - _start.getTimeInMillis();
		return (int)(l / (1000 * 60 * 60));
	}

	/**
	 * ファイルに書き込むための文字列にして返します。
	 * @param tcd
	 * @return
	 */
	public String create() {
		StringBuffer sb = new StringBuffer();
		sb
			.append(day.getTimeInMillis()).append("\t")
			.append(category).append("\t")
			.append(startedWork.getTimeInMillis()).append("\t")
			.append(endedWork.getTimeInMillis())
			;
		return sb.toString();
	}

	/**
	 * すべての情報がない場合、trueを返します。
	 * @return
	 */
	public boolean isNoData() {
		return day == null && category == -1 && startedWork == null && endedWork == null;
	}

	/**
	 * 指定された月の分のTimeCardDataを返します。
	 * @param yyyymm
	 * @return
	 */
	public static List<TimePatrolData> getOneMonth(String yyyymm) {
		if(yyyymm == null) return null;

		int year = Integer.parseInt(yyyymm.substring(0, 4));
		int month = Integer.parseInt(yyyymm.substring(4)) - 1;

		Calendar c = new GregorianCalendar();
		c.set(year, month, 1);
		int max = c.getActualMaximum(Calendar.DATE);

		List<TimePatrolData> retList = new ArrayList<TimePatrolData>(max);
		for (int i=0; i<max; i++) {
			TimePatrolData tcd = new TimePatrolData();

			Calendar dayC = new GregorianCalendar();
			dayC.set(year, month, i+1);
			tcd.day = dayC;

			tcd.category = Category.unknown;

			Calendar startC = new GregorianCalendar();
			startC.set(year, month, i+1, 0, 0);
			tcd.startedWork = startC;

			Calendar endC = new GregorianCalendar();
			endC.set(year, month, i+1, 0, 0);
			tcd.endedWork = endC;

			retList.add(tcd);
		}

		return retList;
	}
}
