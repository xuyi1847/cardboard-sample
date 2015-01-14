package com.google.vrtoolkit.cardboard.samples.treasurehunt;



import com.liehuzuo.vr.R;

import effect.ScaleAnimEffect;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageAdaptergame extends BaseAdapter {  
    private Context mContext;  
    private int selectItem;
    private LayoutInflater inflater;
    public static Integer[] mps = {  
    	R.drawable.icon_game_ymy, R.drawable.icon_game_zombie, R.drawable.icon_game_diveworld, 
    	R.drawable.icon_game_rollercoaster, R.drawable.icon_game_vrangrybots, R.drawable.icon_game_vrlogride,
    	
    };
    public static String[] text = {  
    	"","","","","","",
};
        public ImageAdaptergame(Context context) {  
        	super();
        mContext = context;  
		this.inflater = LayoutInflater.from(context);
    }  
        Holder holder = null;
       
    public int getCount() {   
        return mps.length;  
    }  
  
    public Object getItem(int position) {  
        return position;  
    }  
  
    public long getItemId(int position) {  
        return position;  
    }  
    public View getView(int position, View convertView, ViewGroup parent) {  
//        ImageView image = new ImageView(mContext);
//		
//		
//        image.setImageResource(mps[position]);  
//        image.setAdjustViewBounds(true);  
//        image.setLayoutParams(new Gallery.LayoutParams(  
//            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    	if (convertView == null) {
			holder = new Holder();
			convertView = inflater.inflate(
						R.layout.item, null);
			holder.settingImage = (ImageView) convertView
					.findViewById(R.id.setting_image);
			holder.setting_text = (TextView) convertView
					.findViewById(R.id.setting_text);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
			resetViewHolder(holder);
		}
		holder.settingImage.setImageResource(mps[position]);
		holder.setting_text.setText(text[position]);
		return convertView;
    } 
    class Holder {
		ImageView settingImage;
		TextView setting_text;
	}
    private void resetViewHolder(Holder holder) {
		holder.settingImage.setImageBitmap(null);

	}
}  