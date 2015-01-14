package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import com.liehuzuo.vr.R;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoView1 extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ȥ��ͷ��title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ����ȫ��
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// ������Ļ����
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.video);
		VideoView vv = (VideoView) findViewById(R.id.videoView1);
		vv.setVideoPath("/mnt/sdcard/video_dragon.mp4");
		// ����ý�������
		vv.setMediaController(new MediaController(this));
		vv.start();
		vv.requestFocus();
	}
}
