package com.daeeun.sohnori.anythingmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MyProcess9Activity extends Activity {	
	
	@Override
	// 핸들러의 우선순위 예제이고 각 리스너 처리시 true를 리턴하면 세가지 모두 이벤트 처리된다.
	// 아니면 우선순위에 의해 높은 순위 이벤트만 처리된다.
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process9);
//		View vw = new MyView(this);
//		//리스너 - 1순위
//		vw.setOnTouchListener(new View.OnTouchListener() {			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(event.getAction()==MotionEvent.ACTION_DOWN) {
//					Toast.makeText(MyProcess9Activity.this, "Listener : Touch Event Received", 
//							Toast.LENGTH_SHORT).show();
//					return true;
//				}
//				return false;
//			}
//		});
//		
//		setContentView(vw);
	}
	
	protected class MyView extends View{
		public MyView(Context context) {
			super(context);
		}
		// 뷰의 콜백 메서드 2순위
		public boolean onTouchEvent(MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN) {
				Toast.makeText(MyProcess9Activity.this, "View : Touch Event Received", 
						Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	
	// 액티비티의 콜백 메서드 3순위
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN) {
			Toast.makeText(MyProcess9Activity.this, "Activity : Touch Event Received", 
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	public void mOnClick(View v){		
		finish();
	}
}
