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
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

/**
 * Contains two sub-views to provide a simple stereo HUD.
 */
public class BrightSettingView extends LinearLayout {
	private static final String TAG = BrightSettingView.class
			.getSimpleName();
	private final CardboardOverlayEyeView mLeftView;
	private final CardboardOverlayEyeView mRightView;
	private AlphaAnimation mTextFadeAnimation;

	private int position1 = -1;
	private int position2 = -1;
	private int position3 = -1;
	private int position4 = -1;

	private Gallery gallery;
	private Gallery gallery2;

	private AudioManager audioManager=null; //音频
    
	float bright =(float) 0.7;
	
	private List<ListItem> items = new ArrayList<ListItem>();
	private Context mcontext;

	public BrightSettingView(Context context, AttributeSet attrs) {
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
			

			listView = new ListView(context, attrs);
			addView(listView);
			
			imageView = new ImageView(context, attrs);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setAdjustViewBounds(true); // Preserve aspect ratio.
			addView(imageView);

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
			listView.layout((int) leftMargin / 4, (int) topMargin,
					(int) (width), (int) (height));
			// Layout TextView
			leftMargin = offset * width;
			topMargin = height * verticalTextPos;
			textView.layout((int) leftMargin, (int) topMargin,
					(int) (leftMargin + width), (int) (topMargin + height
							* (1.0f - verticalTextPos)));
		}

	}

	private static final int[] images = new int[] { R.drawable.ic_launcher,
			R.drawable.ic_launcher};
	private static final int[] images3 = new int[] { R.drawable.ic_launcher,
		R.drawable.ic_launcher};

	private void initItems() {
		ListItem item = null;
		for (int i = 0; i < 1; i++) {
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
			
				gallery = (Gallery) convertView.findViewById(R.id.item_gallery);
				gallery.setAdapter(item.adapter);
				gallery.setSelection(0);
				gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						ImageView v = (ImageView) view;
						if (position1 != -1) {
							((ImageView) parent.getChildAt(position1
									- parent.getFirstVisiblePosition()))
									.setImageResource(images[position1]);
							if (position > position1) {
								gallery2.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT,
										null);
							}
							if (position < position1) {
								gallery2.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT,
										null);
							}
						}
						v.setImageResource(images3[position]);

						position1 = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
				gallery.setOnItemClickListener(new OnItemClickListener() { // 设置点击事件监听
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						WindowManager.LayoutParams lp = ((Activity) mcontext).getWindow().getAttributes();
						
						if(position==0)
						{
							if(bright>=1.0)
							{bright = (float) 1.0;}
							else
							{bright = (float) (0.1+bright);}
								
								Log.d("bright",""+bright);
			                  lp.screenBrightness = (float) bright;  
			                  ((Activity) mcontext).getWindow().setAttributes(lp);
							
						}
						if(position==1)
						{
							if(bright<=0.0)
							{bright = (float) 0.0;}
							else
							{bright = (float) (bright-0.1);}
							Log.d("bright",""+bright);
			                  lp.screenBrightness = (float) bright;  
			                  ((Activity) mcontext).getWindow().setAttributes(lp);
						}

					}
				});

			
			

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
				gallery2 = (Gallery) convertView
						.findViewById(R.id.item_gallery);
				gallery2.setAdapter(item.adapter);
				gallery2.setSelection(0);
				gallery2.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						ImageView v = (ImageView) view;
						if (position3 != -1) {
							((ImageView) parent.getChildAt(position3
									- parent.getFirstVisiblePosition()))
									.setImageResource(images[position3]);
						}

						v.setImageResource(images3[position]);
						position3 = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
				gallery2.setOnItemClickListener(new OnItemClickListener() { // 设置点击事件监听
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

					}
				});
			
			
			convertView.setTag(convertView);
			/*
			 * } else { convertView = (View) convertView.getTag(); Log.i("test",
			 * "go here convertView"); }
			 */
			return convertView;
		}

	}

}
