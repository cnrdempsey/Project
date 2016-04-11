package conor.ie.dcu.multimeterapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class DialCanvas extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
        float x,y;
        Paint paint;

        public DialCanvas(Context context) {
            super(context);
           // setOnTouchListener(this);
            //paint = new Paint();
            getHolder().addCallback(this);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(25);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeMiter(0);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            x = event.getX();
            y = event.getY();
            invalidate();
            return true;
        }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
