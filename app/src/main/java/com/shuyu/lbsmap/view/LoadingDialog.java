package com.shuyu.lbsmap.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.shuyu.lbsmap.R;

public class LoadingDialog extends Dialog {

	private Context mContext;

	public LoadingDialog(Context context) {
		super(context, R.style.DialogTheme);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	public void init() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater .inflate(R.layout.loading_dialog, null);
		setContentView(view);
	}
}