package jp.thorie.timepatrol.common;


/**
 * 休暇種別
 * @author anonymous
 *
 */
public class Category {
	public static final int unknown = 0;
	public static final int nomal = 1;
	public static final int cheat = 2;
	public static final int paidHoliday = 3;
	public static final int holiday = 4;

	public static final String[] data = {
		"　　",
		"出勤",
		"欠勤",
		"有給",
		"休日"
	};

	public static String getDispStr(int category) {
		if(category == unknown) {
			return data[0];
		}
		if(category == nomal) {
			return data[1];
		}
		else if(category == cheat) {
			return data[2];
		}
		else if(category == paidHoliday) {
			return data[3];
		}
		else if(category == holiday) {
			return data[4];
		}
		else {
			return null;
		}
	}

	public static int getID(String category) {
		if(category == null) return -1;

		for(int i=0; i<data.length; i++) {
			if(category.equals(data[i])) {
				return i;
			}
		}

		return -1;
	}
}
