/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import java.util.ArrayList;
import java.util.List;
import android.media.AudioManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.liehuzuo.vr.R;
import com.liehuzuo.vr.effect.Blur;

import effect.ScaleAnimEffect;

/**
 * Contains two sub-views to provide a simple stereo HUD.
 */
public class MyGameView extends LinearLayout {
	private static final String TAG = CardboardOverlayView.class
			.getSimpleName();
	private final CardboardOverlayEyeView mLeftView;
	private final CardboardOverlayEyeView mRightView;
	private AlphaAnimation mTextFadeAnimation;

	private int position1 = -1;
	private int position2 = -1;
	private int position3 = -1;
	private int position4 = -1;

	static Gallery gallery;
	static Gallery gallery2;

	private AudioManager audioManager=null; //音频
	ScaleAnimEffect animEffect = new ScaleAnimEffect();
	private List<ListItem> items = new ArrayList<ListItem>();
	private Context mcontext;

	public MyGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(HORIZONTAL);
		mcontext = context;
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, 1.0f);
		params.setMargins(0, 0, 0, 0);

		mLeftView = new CardboardOverlayEyeView(context, attrs);
		mLeftView.setLayoutParams(params);
		addView(mLeftView);

		mRightView = new CardboardOverlayEyeView(context, attrs);
		mRightView.setLayoutParams(params);
		addView(mRightView);

		// Set some reasonable defaults.
		setDepthOffset(0.016f);
		setColor(Color.rgb(150, 255, 180));
		setVisibility(View.VISIBLE);

		mTextFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
		mTextFadeAnimation.setDuration(5000);
		
		

	}

	public void show3DToast(String message) {
		setText(message);
		setTextAlpha(1f);
		mTextFadeAnimation.setAnimationListener(new EndAnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				setTextAlpha(0f);
			}
		});
		startAnimation(mTextFadeAnimation);

	}

	public void showlist() {
		showlistview();
		//showimage(R.drawable.bg_game_ymy);
		//setBackground(R.drawable.bg1);
	}

	private abstract class EndAnimationListener implements
			Animation.AnimationListener {
		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	}
	private void setBackground(int id)
	{
		mLeftView.setBackgroundResource(id);
		mRightView.setBackgroundResource(id);
	}
	private void setDepthOffset(float offset) {
		mLeftView.setOffset(offset);
		mRightView.setOffset(-offset);
	}

	private void setText(String text) {
		mLeftView.setText(text);
		mRightView.setText(text);
	}

	private void setTextAlpha(float alpha) {
		mLeftView.setTextViewAlpha(alpha);
		mRightView.setTextViewAlpha(alpha);
	}

	private void setColor(int color) {
		mLeftView.setColor(color);
		mRightView.setColor(color);
	}

	private void showlistview() {
		initItems();
		mLeftView.setleftListViewAdapter();
		mRightView.setrightListViewAdapter();
	}
	private void showimage(int id)
	{
		mLeftView.setimage(id);
		mRightView.setimage(id);
	}

	/**
	 * A simple view group containing some horizontally centered text underneath
	 * a horizontally centered image.
	 * 
	 * This is a helper class for CardboardOverlayView.
	 */
	private class CardboardOverlayEyeView extends ViewGroup {
		private final ImageView imageView;
		private final TextView textView;
		private final ListView listView;
		private float offset;

		public CardboardOverlayEyeView(Context context, AttributeSet attrs) {
			super(context, attrs);
			

			
			
			imageView = new ImageView(context, attrs);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setAdjustViewBounds(true); // Preserve aspect ratio.
			addView(imageView);
			
			listView = new ListView(context, attrs);
			addView(listView);

			textView = new TextView(context, attrs);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
			textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
			textView.setGravity(Gravity.CENTER);
			textView.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
			addView(textView);
		}

		public void setColor(int color) {
			//imageView.setColorFilter(color);
			textView.setTextColor(color);
		}

		public void setText(String text) {
			textView.setText(text);
		}

		public void setTextViewAlpha(float alpha) {
			textView.setAlpha(alpha);
		}

		public void setOffset(float offset) {
			this.offset = offset;
		}
		public void setimage(int id)
		{
			imageView.setImageResource(id);
		}
//		public void setImageDialog(int i) {
//			audioManager=(AudioManager)mcontext.getSystemService(Service.AUDIO_SERVICE);
//			imageView.setImageResource(R.drawable.bk);
//			imageView1.setImageResource(R.drawable.plus);
//			imageView2.setImageResource(R.drawable.minus);
//			if(i==1)
//			{
//			imageView1.setOnClickListener(new ImageView.OnClickListener() {
//				public void onClick(View v) {
//					
//				}
//			});
//			imageView2.setOnClickListener(new ImageView.OnClickListener() {
//				public void onClick(View v) {
//					
//				}
//			});
//			}
//			if(i==2)
//			{
//			imageView1.setOnClickListener(new ImageView.OnClickListener() {
//				public void onClick(View v) {
//					audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
//	                        AudioManager.ADJUST_RAISE, 
//	                        AudioManager.FLAG_SHOW_UI);    //调高声音
//	                
//
//				}
//			});
//			imageView2.setOnClickListener(new ImageView.OnClickListener() {
//				public void onClick(View v) {
//					audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
//	                        AudioManager.ADJUST_LOWER, 
//	                        AudioManager.FLAG_SHOW_UI);//调低声音
//					
//				}
//			});
//			}
//		}
//		public boolean onKeyDown(int keyCode, KeyEvent event) {  
//			        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  
//			        	imageView.setVisibility(View.GONE);
//						imageView1.setVisibility(View.GONE);
//						imageView2.setVisibility(View.GONE);
//			           return true;  
//			       } else  
//			            return super.onKeyDown(keyCode, event);  
//			    }  

		public void setleftListViewAdapter() {
			listView.setDividerHeight(0);
			listView.setVerticalScrollBarEnabled(true);
//			listView.setBackgroundColor(Color.GRAY);
			listView.setFocusable(false);
			listView.setClickable(false);
			new Thread(new Runnable() {
				public void run() {
					listView.setAdapter(new CustomSimpleAdapter(mcontext, items));
				}
			}).start();

		}

		public void setrightListViewAdapter() {
			listView.setDividerHeight(0);
			listView.setVerticalScrollBarEnabled(true);
			listView.setFocusable(false);
			listView.setClickable(false);
			new Thread(new Runnable() {
				public void run() {
					listView.setAdapter(new CustomSimpleAdapter1(mcontext,
							items));
				}
			}).start();

		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom) {
			// Width and height of this ViewGroup.
			final int width = right - left;
			final int height = bottom - top;

			// The size of the image, given as a fraction of the dimension as a
			// ViewGroup. We multiply
			// both width and heading with this number to compute the image's
			// bounding box. Inside the
			// box, the image is the horizontally and vertically centered.
			final float imageSize = 0.12f;

			// The fraction of this ViewGroup's height by which we shift the
			// image off the ViewGroup's
			// center. Positive values shift downwards, negative values shift
			// upwards.
			final float verticalImageOffset = -0.07f;

			// Vertical position of the text, specified in fractions of this
			// ViewGroup's height.
			final float verticalTextPos = 0.52f;

			// Layout ImageView
			float imageMargin = (1.0f - imageSize) / 2.0f;
			float leftMargin = (int) (width * (imageMargin + offset));
			float topMargin = (int) (height * (imageMargin + verticalImageOffset));
			imageView.layout(0,0,(int) (width), (int) (height));
			
			listView.layout(0, 0,
					(int) (width), (int) (height));
			// Layout TextView
			leftMargin = offset * width;
			topMargin = height * verticalTextPos;
			textView.layout((int) leftMargin, (int) topMargin,
					(int) (leftMargin + width), (int) (topMargin + height
							* (1.0f - verticalTextPos)));
		}

	}

	private static final int[] images = new int[] { R.drawable.icon_folder,
			R.drawable.icon_game, R.drawable.icon_setting,R.drawable.icon_store,R.drawable.icon_video };
	
	
	private void initItems() {
		ListItem item = null;
		for (int i = 0; i < 2; i++) {
			item = new ListItem();
			
				item.itemImages = images;

			item.initAdapter(mcontext);
			items.add(item);
		}
	}

	public class CustomSimpleAdapter extends BaseAdapter {
		private List<ListItem> items;
		private LayoutInflater layoutInflater;

		public CustomSimpleAdapter(Context context, List<ListItem> items) {
			// TODO Auto-generated constructor stub
			this.items = items;
			layoutInflater = (LayoutInflater) (context
					.getSystemService(context.LAYOUT_INFLATER_SERVICE));
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.items, null);
			ListItem item = this.items.get(position);
			if (position == 0) {
				ImageAdaptergame adapter;
		    	adapter = new ImageAdaptergame(mcontext);
		    	gallery = (Gallery) convertView.findViewById(R.id.item_gallery);
	    		gallery.setAdapter(adapter);
	    		gallery.setClickable(false);
	    		gallery.setFocusable(false);
	    		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
	    			@Override
	    			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//	    				ImageView v = (ImageView) view;
	    				animEffect.setAttributs(1.40f, 1.38f, 1.40f, 1.38f,300);
						Animation anim2 = animEffect.createAnimation();
						animEffect.setAttributs(1.0f, 1.0f, 1.0f, 1.0f, 0);
						Animation anim1 = animEffect.createAnimation();
						if (position1 != -1) {
							parent.getChildAt(
									position - parent.getFirstVisiblePosition())
									.findViewById(R.id.settingsframelayout)
									.startAnimation(anim2);
							parent.getChildAt(
									position1 - parent.getFirstVisiblePosition())
									.findViewById(R.id.settingsframelayout)
									.startAnimation(anim1);
//							if (position > position1) {
//								gallery2.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT,
//										null);
//							}
//							if (position < position1) {
//								gallery2.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT,
//										null);
//							}
						}
//						v.setImageResource(ImageAdapter.mps[position]);
						
						position1 = position;
//							Log.d("log",""+position1);
					
	    			
				
						
						if (position1 == 0) {
							setBackground(R.drawable.bg_game_ymy);
						}
						if (position1 == 1) {
							setBackground(R.drawable.bg_game_zombie);
						}
						if (position1 == 2) {
							setBackground(R.drawable.bg_game_diveworld);
						}
						if (position1 == 3) {
							setBackground(R.drawable.bg_game_rollercoaster);
						}
						if (position1 == 4) {
							setBackground(R.drawable.bg_game_vrangrybots);
						}
						if (position1 == 5) {
							setBackground(R.drawable.bg_game_vrlogride);
						}
						
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
				gallery.setOnItemClickListener(new OnItemClickListener() { // 设置点击事件监听
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if(position==0)
						{
						GetStart("com.yh.yuanmingyuan");
							
						}
//						if(position==1)
//						{
//						GetStart("com.hx.VR");
//						}
//						if(position==2)
//						{
//						GetStart("com.hx.VR");
//						}
//						if(position==3)
//						{
//						GetStart("com.hx.VR");
//						}
//						if(position==4)
//						{
//						GetStart("com.hx.VR");
//						}
//						if(position==5)
//						{
//						GetStart("com.hx.VR");
//						}
//						if(position==5)
//						{
//						GetStart("com.hx.VR");
//						}
						
					}
				});

			}
			

			convertView.setTag(convertView);
			/*
			 * } else { convertView = (View) convertView.getTag(); Log.i("test",
			 * "go here convertView"); }
			 */
			return convertView;
		}

	}

	public class CustomSimpleAdapter1 extends BaseAdapter {
		private List<ListItem> items;
		private LayoutInflater layoutInflater;

		public CustomSimpleAdapter1(Context context, List<ListItem> items) {
			// TODO Auto-generated constructor stub
			this.items = items;
			layoutInflater = (LayoutInflater) (context
					.getSystemService(context.LAYOUT_INFLATER_SERVICE));
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.items, null);
			ListItem item = this.items.get(position);

			if (position == 0) {
				ImageAdaptergame adapter;
		    	adapter = new ImageAdaptergame(mcontext);
		    	gallery2 = (Gallery) convertView.findViewById(R.id.item_gallery);
	    		gallery2.setAdapter(adapter);
	    		gallery2.setClickable(false);
	    		gallery2.setFocusable(false);
	    		gallery2.setOnItemSelectedListener(new OnItemSelectedListener() {
	    			@Override
	    			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//	    				ImageView v = (ImageView) view;
	    				animEffect.setAttributs(1.40f, 1.38f, 1.40f, 1.38f,300);
						Animation anim2 = animEffect.createAnimation();
						animEffect.setAttributs(1.0f, 1.0f, 1.0f, 1.0f, 0);
						Animation anim1 = animEffect.createAnimation();
						if (position2 != -1) {
							parent.getChildAt(
									position - parent.getFirstVisiblePosition())
									.findViewById(R.id.settingsframelayout)
									.startAnimation(anim2);
							parent.getChildAt(
									position2 - parent.getFirstVisiblePosition())
									.findViewById(R.id.settingsframelayout)
									.startAnimation(anim1);
						}
//						v.setImageResource(ImageAdapter.mps[position]);
						
						position2 = position;
	    			}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
				gallery2.setOnItemClickListener(new OnItemClickListener() { // 设置点击事件监听
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Toast.makeText(mcontext,
								"img " + (position + 1) + " selected",
								Toast.LENGTH_SHORT).show();

					}
				});
			}
			
			convertView.setTag(convertView);
			/*
			 * } else { convertView = (View) convertView.getTag(); Log.i("test",
			 * "go here convertView"); }
			 */
			return convertView;
		}

	}
	private void GetStart(String packageName) {
		String classname = null;
		PackageManager pm = mcontext.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);
		final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
		for (ResolveInfo r : apps) {
			ComponentInfo ci = r.activityInfo;
			classname = ci.name;
		}

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		ComponentName cn = new ComponentName(packageName, classname);

		intent.setComponent(cn);
		mcontext.startActivity(intent);

	}

}
