package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess7Activity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 방법1 XML 레이아웃 문서(컴파일된 이진정보)를 참조하여 액티비티를 채운다.
		//setContentView(R.layout.activity_process7);

		// 방법2 코드상에서 직접 레이아웃 객체를 생성하여 액티비티를 채운다.
//		LinearLayout linear = new LinearLayout(this);
//		LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.MATCH_PARENT);
//		linear.setOrientation(LinearLayout.VERTICAL);
//		linear.setBackgroundColor(Color.LTGRAY);
//		
//		TextView text = new TextView(this);
//		text.setText("TextView");
//		text.setGravity(Gravity.CENTER);
//		text.setTextColor(Color.RED);
//		text.setTextSize(20);
//			 //레이아웃에 대한 파라미터는 별도 지정해야한다.
//		LinearLayout.LayoutParams paramtext = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//		paramtext.setMargins(0, 30, 0, 30);		
//		linear.addView(text, paramtext);
//		
//		setContentView(linear, paramlinear);
		
		// 방법3 XML 리소스를 전개하여 뷰 객체를 만들어 액티비티를 채운다.
		// LayoutInflater inflater = LayoutInflater.from(this);
//		LayoutInflater inflater = (LayoutInflater)getSystemService(
//				Context.LAYOUT_INFLATER_SERVICE);
//		ScrollView scrollview = (ScrollView)inflater.inflate(R.layout.activity_process7, null);
//		setContentView(scrollview);
		
		// 방법4 리턴 값을 리소스로 전달한다.
		setContentView(View.inflate(this, R.layout.activity_process7, null));
		//DisplayMetrics dm = new DisplayMetrics();
		//getWindowManager().getDefaultDisplay().getMetrics(dm);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		String str = "widthPixels=" + dm.widthPixels +
				"\nheightPixels" + dm.heightPixels +
				"\ndensityDpi=" + dm.densityDpi +
				"\nscaledDensity=" + dm.scaledDensity +
				"\nxdpi=" + dm.xdpi +
				"\nydpi=" + dm.ydpi;
		TextView info = (TextView)findViewById(R.id.act7txtresult);
		info.setText(str);
		
	}
	public void mOnClick(View v){
		switch(v.getId()) {
		case R.id.act7btnhome:
			finish();
			break;
		case R.id.act7btndialog:
			new AlertDialog.Builder(this)
			.setTitle("알립니다.")
			.setMessage("대화상자를 열었습니다")
			.setIcon(R.drawable.ic_launcher).setPositiveButton("예", new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub					
				}
			}).setNegativeButton("아니오", null)
			.show();			
			break;
		case R.id.act7btndialog2:
			new AlertDialog.Builder(this)
			.setTitle("음식을 선택하시오.")
			.setIcon(R.drawable.ic_launcher)
			.setItems(new String[] {"짜장면", "우동", "짬뽕" }, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub					
				}
			})
			.setNegativeButton("취소", null)
			.show();
			break;
		}
		
	}
}
