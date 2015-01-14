package effect;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
/**
 * @2014 yangcong.tvgamecenter 动画
 * @author 文茂荣
 * @verison 1.2
 * @date 2014-09-10
 */
public class ScaleAnimEffect {
	private float fromXScale;
	private float toXScale;
	private float fromYScale;
	private float toYScale;
	private long duration;
	private float fromAlpha;
	private float toAlpha;

	// private long offSetDuration;

	/**
	 * 设置缩放参数
	 * 
	 * @param fromXScale
	 *            初始X轴缩放比例
	 * @param toXScale
	 *            目标X轴缩放比例
	 * @param fromYScale
	 *            初始Y轴缩放比例
	 * @param toYScale
	 *            目标Y轴缩放比例
	 * @param duration
	 *            动画持续时间
	 */
	public void setAttributs(float fromXScale, float toXScale,
			float fromYScale, float toYScale, long duration) {
		this.fromXScale = fromXScale;
		this.fromYScale = fromYScale;
		this.toXScale = toXScale;
		this.toYScale = toYScale;
		this.duration = duration;
	}

	public Animation createAnimation() {
		ScaleAnimation anim = new ScaleAnimation(fromXScale, toXScale,
				fromYScale, toYScale, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setFillAfter(true);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(duration);
		return anim;
	}

	public Animation alphaAnimation(float fromAlpha, float toAlpha,
			long duration, long offsetDuration) {
		AlphaAnimation anim = new AlphaAnimation(fromAlpha, toAlpha);
		anim.setDuration(duration);
		anim.setStartOffset(offsetDuration);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}
	public Animation translateAnimation(float fromXDelta, float toXDelta,
			float fromYDelta , float toYDelta) {
		TranslateAnimation anim = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}
}
