package jp.thorie.timepatrol.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import jp.thorie.timepatrol.common.TimePatrolData;

public class TimePatrolIO {

	private static final TimePatrolIO me = new TimePatrolIO();
	private final Map<String, List<TimePatrolData>> timeCardAllMap = new HashMap<String, List<TimePatrolData>>();

	/**
	 * コンストラクタ（シングルトン
	 */
	private TimePatrolIO(){};

	/**
	 * インスタンス取得
	 * @return
	 */
	public static TimePatrolIO getInstance() {
		return me;
	}

	/**
	 * 指定された年月の勤怠表をファイルシステムから取得します。<br>
	 * 存在しないyyyymmが指定された場合、指定された月のデータを作成して返します。
	 *
	 * @param yyyymm
	 * @return
	 * @throws Exception
	 */
	public List<TimePatrolData> readTimeCard(String yyyymm, Context context) throws Exception {
		if(!timeCardAllMap.containsKey(yyyymm)) {
			String[] fAry = context.fileList();
			boolean existsFile = false;
			for(String s : fAry) {
				if(s.equals(yyyymm + ".tsv")) {
					existsFile = true;
					break;
				}
			}

			if(!existsFile) {
				this.timeCardAllMap.put(yyyymm, TimePatrolData.getOneMonth(yyyymm));
				return this.timeCardAllMap.get(yyyymm);
			}

			BufferedReader br = null;
			try {
				List<TimePatrolData> list = new ArrayList<TimePatrolData>(31);
				br = new BufferedReader(new InputStreamReader(context.openFileInput(yyyymm + ".tsv")));
				String line;
				while((line = br.readLine()) != null) {
					TimePatrolData tcd = TimePatrolData.createForData(line);
					list.add(tcd);
				}

				this.timeCardAllMap.put(yyyymm, list);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		return this.timeCardAllMap.get(yyyymm);
	}

	/**
	 * ファイルに書き込みます。
	 *
	 * @param list
	 * @throws IOException
	 */
	public static void writeTimeCard(String yyyymm, List<TimePatrolData> list, Context context) throws IOException {
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(yyyymm + ".tsv", Context.MODE_PRIVATE)));
			for(TimePatrolData tcd : list) {
				bw.write(tcd.create());
				bw.newLine();
			}
			bw.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if(bw != null) bw.close();
		}
	}

	/**
	 * メールで送るための添付ファイルへのパスを返します。
	 * @param yyyymm
	 * @return
	 * @throws IOException
	 */
	public String getTimeCardString(String yyyymm) throws IOException {
		List<TimePatrolData> list = timeCardAllMap.get(yyyymm);

		BufferedWriter bw = null;

		File sdDir = new File(Environment.getExternalStorageDirectory() + "/timepatrol/");
		if(!sdDir.exists()) {
			sdDir.mkdir();
		}

		File outputF = new File(sdDir.getAbsoluteFile() + "/" + yyyymm + ".tsv");

		try {
			//bw = new BufferedWriter(new OutputStreamWriter(new OutputStream(yyyymm + "mail.tsv", Context.MODE_PRIVATE)));
			bw = new BufferedWriter(new FileWriter(outputF));

			for(TimePatrolData tcd : list) {
				bw.write(tcd.createForMail());
				bw.write("\r\n");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if(bw != null) bw.close();
		}

		StringBuffer sb = new StringBuffer();
		for(TimePatrolData tcd : list) {
			sb.append(tcd.createForMail());
			sb.append("\r\n"); //windows向け
		}

		return outputF.getAbsolutePath();
	}


	/**
	 * 「今日」のファイル名を返します。
	 * @return
	 */
	public static String getTodayFilePath() {
		return getTodayYYYYMM() + ".tsv";
	}

	/**
	 * 「今日」のyyyymmを返します。
	 * @return
	 */
	public static String getTodayYYYYMM() {
		Calendar today = new GregorianCalendar();
		today.setTimeInMillis(System.currentTimeMillis());
		return today.get(Calendar.YEAR) + String.format("%02d", today.get(Calendar.MONTH)+1);
	}

	/**
	 * 「今日」のyyyy年mm月を返します。
	 * @return 2012年2月
	 */
	public static String getTodayYYYYMMDisp() {
		Calendar today = new GregorianCalendar();
		today.setTimeInMillis(System.currentTimeMillis());
		return today.get(Calendar.YEAR) + "年" + String.format("%2d", today.get(Calendar.MONTH)+1) + "月";
	}
}
