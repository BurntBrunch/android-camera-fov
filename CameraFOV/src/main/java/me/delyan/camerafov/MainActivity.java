package me.delyan.camerafov;

import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String FOV_LABEL = "FOV factor (%.2f):";

    private View mAnimatedView;
    private SeekBar mDistanceSeekbar, mFOVSeekbar;
    private TextView mFOVLabel;

    static class RotateAnimation extends Animation {
        final static float MAX_ANGLE = -180f;
        static int sCameraDistance = 0;
        static float sFOVFactor = 1f;

        final int mWidth;
        final int mHeight;

        final Camera mCamera = new Camera();
        final Matrix mMatrix = new Matrix();

        final float[] mMatrixVals = new float[9];

        RotateAnimation(View view) {
            mWidth = view.getMeasuredWidth();
            mHeight = view.getMeasuredHeight();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mCamera.save();

            mCamera.translate(0, mHeight / 2, sCameraDistance); // middle of left edge

            if (interpolatedTime <= 0.5f) {
                // First half rotates from 0 to MAX_ANGLE
                mCamera.rotateY(interpolatedTime * 2f * MAX_ANGLE);
            } else {
                // Second half rotates back from MAX_ANGLE to 0
                mCamera.rotateY((1f - (interpolatedTime - 0.5f) * 2f) * MAX_ANGLE);
            }

            mCamera.getMatrix(mMatrix);
            mMatrix.postTranslate(mWidth / 2, 0); // put the left edge in the middle of the View rectangle

            mMatrix.getValues(mMatrixVals);
            mMatrixVals[Matrix.MPERSP_0] /= sFOVFactor; // magic happens here
            mMatrix.setValues(mMatrixVals);

            t.getMatrix().set(mMatrix);

            mCamera.restore();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAnimatedView = findViewById(R.id.Main_AnimatedView);

        mDistanceSeekbar = (SeekBar) findViewById(R.id.Main_CameraDistance_Seekbar);
        mFOVSeekbar = (SeekBar) findViewById(R.id.Main_FOV_Seekbar);
        mFOVLabel = (TextView) findViewById(R.id.Main_FOV_Label);

        mDistanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RotateAnimation.sCameraDistance = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFOVSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RotateAnimation.sFOVFactor = 1 + progress * 0.25f;
                mFOVLabel.setText(String.format(Locale.ENGLISH, FOV_LABEL, RotateAnimation.sFOVFactor));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void startAnimation() {
        Animation animation = new RotateAnimation(mAnimatedView);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(3000);

        mAnimatedView.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimatedView.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation();
            }
        }, 100);
    }
}
