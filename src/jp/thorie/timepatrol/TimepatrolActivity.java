package jp.thorie.timepatrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jp.thorie.timecard.R;
import jp.thorie.timepatrol.common.Category;
import jp.thorie.timepatrol.common.TimePatrolData;
import jp.thorie.timepatrol.io.TimePatrolIO;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimepatrolActivity extends Activity {
    private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    String targetYYYYMM = null;
    Map<String, List<TimePatrolData>> allMap = new HashMap<String, List<TimePatrolData>>();
	List<TimePatrolData> list;
	Context me;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private Timer locationTimer;
	long time;

    //TODO 設定関連
    Map<String, String> configMap = new HashMap<String, String>();

    //ダイアログ呼び出し時に使用する
    private LinearLayout eventRowView;
    private int selectedCategory;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		me = this;
		targetYYYYMM = TimePatrolIO.getTodayYYYYMM();

		//メインレイアウト
		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.ll_main);

		//ヘッダー
		mainLayout.addView(createHeader());

		//ボディ
		mainLayout.addView(createBody());

		//TODO gps開始
		//startLocationService();
	}

	public void onClickStartWork(View view) {
		LinearLayout v = (LinearLayout)view.getParent().getParent();
		this.eventRowView = v;

		TextView startWork = (TextView)view;
		String[] ary = startWork.getText().toString().split(":");

		new TimePickerDialog(TimepatrolActivity.this, new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				for(TimePatrolData tcd : list) {
					if(tcd.getId() == eventRowView.hashCode()) {
						TextView tvDate = (TextView)eventRowView.findViewById(R.id.tv_date);
						int day = Integer.parseInt(tvDate.getText().toString().substring(0, 2).trim());

						Calendar c = new GregorianCalendar();
						c.set(Integer.parseInt(targetYYYYMM.substring(0, 4)), Integer.parseInt(targetYYYYMM.substring(4, 6))-1, day, hourOfDay, minute);
						tcd.startedWork = c;
						TextView tvStartWork = (TextView)eventRowView.findViewById(R.id.tv_start_work);
						tvStartWork.setText(tcd.getDispWorkStart());
						break;
					}
				}


				try {
					TimePatrolIO.writeTimeCard(targetYYYYMM, list, me);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} , Integer.parseInt(ary[0]), Integer.parseInt(ary[1]), true)
		.show();
	}

	public void onClickEndWork(View view) {
		LinearLayout v = (LinearLayout)view.getParent().getParent();
		this.eventRowView = v;

		TextView endWork = (TextView)view;
		String[] ary = endWork.getText().toString().split(":");

		new TimePickerDialog(TimepatrolActivity.this, new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				for(TimePatrolData tcd : list) {
					if(tcd.getId() == eventRowView.hashCode()) {
						TextView tvDate = (TextView)eventRowView.findViewById(R.id.tv_date);
						int day = Integer.parseInt(tvDate.getText().toString().substring(0, 2).trim());

						Calendar c = new GregorianCalendar();
						c.set(Integer.parseInt(targetYYYYMM.substring(0, 4)), Integer.parseInt(targetYYYYMM.substring(4, 6))-1, day, hourOfDay, minute);
						tcd.endedWork = c;
						TextView tvEndWork = (TextView)eventRowView.findViewById(R.id.tv_end_work);
						tvEndWork.setText(tcd.getDispWorkEnd());
						break;
					}
				}

				try {
					TimePatrolIO.writeTimeCard(targetYYYYMM, list, me);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} , Integer.parseInt(ary[0]), Integer.parseInt(ary[1]), true)
		.show();
	}

	public void onClickCategory(View view) {
		LinearLayout v = (LinearLayout)view.getParent().getParent();
		this.eventRowView = v;

		TextView tvCategory = (TextView)view;
		String category = tvCategory.getText().toString();

		new AlertDialog.Builder(this)
			.setTitle("種別")
			.setSingleChoiceItems(Category.data, Category.getID(category), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					selectedCategory = which;
				}
			})
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					for(TimePatrolData tcd : list) {
						if(tcd.getId() == eventRowView.hashCode()) {
							tcd.category = selectedCategory;
							TextView tvCategory = (TextView)eventRowView.findViewById(R.id.tv_category);
							tvCategory.setText(Category.getDispStr(selectedCategory));
							break;
						}
					}

					try {
						TimePatrolIO.writeTimeCard(targetYYYYMM, list, me);
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			})
			.show();
	}

	/**
	 * 来月を表示します。
	 * @param view
	 */
	public void onClickNextMonth(View view) {
		//this.targetYYYYMM =
		if(this.targetYYYYMM.substring(4, 6).equals("12")) {
			String yyyy = String.valueOf(Integer.parseInt(this.targetYYYYMM.substring(0, 4))+1);
			this.targetYYYYMM = yyyy + "01";
		}
		else {
			this.targetYYYYMM = String.format(
					"%s%02d",
					this.targetYYYYMM.substring(0, 4),
					Integer.parseInt(this.targetYYYYMM.substring(4, 6))+1
					);
		}

		//メインレイアウト
		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.ll_main);
		mainLayout.removeAllViews();

		//ヘッダー
		mainLayout.addView(createHeader());

		//ボディ
		mainLayout.addView(createBody());
	}

	/**
	 * 前月を表示します。
	 * @param view
	 */
	public void onClickPreMonth(View view) {
		//this.targetYYYYMM =
		if(this.targetYYYYMM.substring(4, 6).equals("01")) {
			String yyyy = String.valueOf(Integer.parseInt(this.targetYYYYMM.substring(0, 4))-1);
			this.targetYYYYMM = yyyy + "12";
		}
		else {
			this.targetYYYYMM = String.format(
					"%s%02d",
					this.targetYYYYMM.substring(0, 4),
					Integer.parseInt(this.targetYYYYMM.substring(4, 6))-1
					);
		}

		//メインレイアウト
		LinearLayout mainLayout = (LinearLayout)findViewById(R.id.ll_main);
		mainLayout.removeAllViews();

		//ヘッダー
		mainLayout.addView(createHeader());

		//ボディ
		mainLayout.addView(createBody());
	}

	/**
	 * ボディを構築して返します。
	 * @return
	 */
	private LinearLayout createBody() {
		View mainBodyView = this.getLayoutInflater().inflate(R.layout.main_body, null);
		LinearLayout mainBodyLayout = (LinearLayout)mainBodyView.findViewById(R.id.ll_main_body);

		LinearLayout mainBodyLinearLayout = (LinearLayout)mainBodyView.findViewById(R.id.ll_main_inner_body);
		try {
			if(allMap.containsKey(targetYYYYMM)) {
				list = allMap.get(targetYYYYMM);
			}
			else {
				list = TimePatrolIO.getInstance().readTimeCard(targetYYYYMM, me);
				allMap.put(targetYYYYMM, new ArrayList<TimePatrolData>(list));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//月に対応する日付だけ繰り返し
		for(TimePatrolData tcd : list) {
			View v = this.getLayoutInflater().inflate(R.layout.main_body_row, null);

			//日付
			TextView tv = (TextView)v.findViewById(R.id.tv_date);
			tv.setText(tcd.getDispDay());

			//種別
			TextView tvCategory = (TextView)v.findViewById(R.id.tv_category);
			tvCategory.setText(tcd.getDispCategory());

			//出勤時刻
			TextView startWork = (TextView)v.findViewById(R.id.tv_start_work);
			startWork.setText(tcd.getDispWorkStart());

			//退勤時刻
			TextView work_end = (TextView)v.findViewById(R.id.tv_end_work);
			work_end.setText(tcd.getDispWorkEnd());

			//行
			LinearLayout llRow = (LinearLayout)v.findViewById(R.id.ll_main_body_row);
			tcd.setId(llRow.hashCode());

			mainBodyLinearLayout.addView(llRow);
		}

		return mainBodyLayout;
	}

    /**
     * ヘッダーを構築して返します。
     * @return
     */
    private LinearLayout createHeader() {
    	View v = this.getLayoutInflater().inflate(R.layout.main_header_month, null);

    	//表示対象の月
    	TextView tv = (TextView)v.findViewById(R.id.tv_current_month);
    	tv.setText(targetYYYYMM.substring(0, 4) + "年" + targetYYYYMM.substring(4, 6) + "月");

    	LinearLayout ll = (LinearLayout)v.findViewById(R.id.linearLayout1);

    	return ll;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu,menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		case R.id.item1:
			// メール送信
			Intent intent = new Intent(Intent.ACTION_SEND);
			//intent.putExtra(Intent.EXTRA_EMAIL, "");
			intent.putExtra(Intent.EXTRA_SUBJECT, "【勤務表】" + targetYYYYMM.substring(0, 4) + "年" + targetYYYYMM.substring(4, 6) + "月");
			intent.putExtra(Intent.EXTRA_TEXT, "本文なし");
			try {
				File f = new File(TimePatrolIO.getInstance().getTimeCardString(targetYYYYMM));

				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
			}
			intent.setType("text/plain");
			startActivity(intent);

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	void startLocationService() {
		stopLocationService();

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// 位置情報機能非搭載端末の場合
		if (locationManager == null) {
			// 何も行いません
			return;
		}

		// @see http://developer.android.com/reference/android/location/LocationManager.html#getBestProvider%28android.location.Criteria,%20boolean%29
		final Criteria criteria = new Criteria();
		// PowerRequirement は設定しないのがベストプラクティス
		// Accuracy は設定しないのがベストプラクティス
		//criteria.setAccuracy(Criteria.ACCURACY_FINE);	← Accuracy で最もやってはいけないパターン
		// 以下は必要により
		criteria.setBearingRequired(false);	// 方位不要
		criteria.setSpeedRequired(false);	// 速度不要
		criteria.setAltitudeRequired(false);	// 高度不要

		final String provider = locationManager.getBestProvider(criteria, true);
		if (provider == null) {
			// 位置情報が有効になっていない場合は、Google Maps アプリライクな [現在地機能を改善] ダイアログを起動します。
			new AlertDialog.Builder(this)
				.setTitle("現在地機能を改善")
				.setMessage("現在、位置情報は一部有効ではないものがあります。次のように設定すると、もっともすばやく正確に現在地を検出できるようになります:\n\n● 位置情報の設定でGPSとワイヤレスネットワークをオンにする\n\n● Wi-Fiをオンにする")
				.setPositiveButton("設定", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						// 端末の位置情報設定画面へ遷移
						try {
							startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
						} catch (final ActivityNotFoundException e) {
							// 位置情報設定画面がない糞端末の場合は、仕方ないので何もしない
						}
					}
				})
				.setNegativeButton("スキップ", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {}	// 何も行わない
				})
				.create()
				.show();

			stopLocationService();
			return;
		}

		// 最後に取得できた位置情報が5分以内のものであれば有効とします。
		final Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
		// XXX - 必要により判断の基準を変更してください。
		if (lastKnownLocation != null && (new Date().getTime() - lastKnownLocation.getTime()) <= (5 * 60 * 1000L)) {
			setLocation(lastKnownLocation);
			return;
		}

		// Toast の表示と LocationListener の生存時間を決定するタイマーを起動します。
		locationTimer = new Timer(true);
		time = 0L;
		final Handler handler = new Handler();
		locationTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						if (time == 1000L) {
							Toast.makeText(TimepatrolActivity.this, "現在地を特定しています。", Toast.LENGTH_LONG).show();
						} else if (time >= (30 * 1000L)) {
							Toast.makeText(TimepatrolActivity.this, "現在地を特定できませんでした。", Toast.LENGTH_LONG).show();
							stopLocationService();
						}
						time = time + 1000L;
					}
				});
			}
		}, 0L, 1000L);

		// 位置情報の取得を開始します。
		locationListener = new LocationListener() {
			public void onLocationChanged(final Location location) {
				setLocation(location);
			}
			public void onProviderDisabled(final String provider) {}
			public void onProviderEnabled(final String provider) {}
			public void onStatusChanged(final String provider, final int status, final Bundle extras) {}
		};
		locationManager.requestLocationUpdates(provider, 60000, 0, locationListener);
	}

	void stopLocationService() {
		if (locationTimer != null) {
			locationTimer.cancel();
			locationTimer.purge();
			locationTimer = null;
		}
		if (locationManager != null) {
			if (locationListener != null) {
				locationManager.removeUpdates(locationListener);
				locationListener = null;
			}
			locationManager = null;
		}
	}

	void setLocation(final Location location) {
		stopLocationService();

		// TODO: ここに位置情報が取得できた場合の処理を記述します。
	}

}