package com.daeeun.sohnori.anythingmanager;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.daeeun.sohnori.anythingmanager.R;

public class MyProcess11Activity extends Activity {
	ArrayList<String> arGeneral = new ArrayList<String>();
	ArrayAdapter<String> Adapter;
	ListView list;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process11);
		
		// 리소스를 읽어들여 해당 값을 변경하는 코드
		Resources res = getResources();
		TextView txt = (TextView)findViewById(R.id.act11txttest);
		
		String str = res.getString(R.string.act11txttest);
		txt.setText(str);
		//@SuppressWarnings("deprecation")
		int txtcolor = res.getColor(R.color.act11txtcolortest);
		txt.setTextColor(txtcolor);
		float txtsize = res.getDimension(R.dimen.act11txtsizetest);
		txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtsize);
		
		// 리스트 뷰 테스트		
		arGeneral.add("김유신");
		arGeneral.add("이순신");
		arGeneral.add("강감찬");
		arGeneral.add("을지문덕");		
		Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
				arGeneral);
		//ArrayAdapter<CharSequence> Adapter;
		//Adapter = ArrayAdapter.createFromResource(this, R.array.country, 
		//		android.R.layout.simple_list_item_1);
		list = (ListView)findViewById(R.id.act10list1);
		list.setAdapter(Adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		list.setDivider(new ColorDrawable(Color.YELLOW));
		list.setDividerHeight(3);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String mes;
				mes = "Select Item = " + arGeneral.get(position);
				Toast.makeText(MyProcess11Activity.this, mes, Toast.LENGTH_SHORT).show();
			}
		});															
	}
	public void mOnClick(View v){
		finish();
	}
}
