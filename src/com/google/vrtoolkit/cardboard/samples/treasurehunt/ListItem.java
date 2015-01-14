package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ListItem {
	public String title, describe;
	public int[] itemImages;
	public ImageAdapter adapter;

	public void initAdapter(Context context) {
		this.adapter = new ImageAdapter(context);
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return itemImages.length;
		}

		@Override
		public Object getItem(int i) {
			// TODO Auto-generated method stub
			return i;
		}

		@Override
		public long getItemId(int i) {
			// TODO Auto-generated method stub
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewgroup) {
			view = new ImageView(mContext);
			((ImageView) view).setImageBitmap(ListItem.readBitmap(mContext,
					itemImages[i % itemImages.length]));
			view.setLayoutParams(new Gallery.LayoutParams(192, 192));
			return view;
		}

	}

	public static Bitmap readBitmap(Context context, int resId) {
		BitmapFactory.Options options = new Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, options);
	}
}
