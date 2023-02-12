//package org.pytorch.demo.speechrecognition;
//
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.graphics.Typeface;
//import android.util.AttributeSet;
//import android.view.View;
//
//import androidx.annotation.Nullable;
//
//public class CircleProgressBar extends View {
//    private int max;//最大进度
//    private int roundColor;//圈颜色
//    private int roundProgressColor;//进度颜色
//    private int textColor;//文字颜色
//    private float textSize;//文字大小
//    private float roundWidth;//圈宽度
//    private boolean textShow;//是否显示文字
//    private int progress;//当前进度
//    private Paint paint;
//    public static final int STROKE = 0;
//    public static final int FILL = 1;
//
//    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        paint = new Paint();
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressBar);
//        max = typedArray.getInteger(R.styleable.CustomProgressBar_max, 100);
//        roundColor = typedArray.getColor(R.styleable.CustomProgressBar_roundColor, Color.RED);
//        roundProgressColor = typedArray.getColor(R.styleable.CustomProgressBar_roundProgressColor, Color.BLUE);
//        textColor = typedArray.getColor(R.styleable.CustomProgressBar_textColor, Color.GREEN);
//        textSize = typedArray.getDimension(R.styleable.CustomProgressBar_textSize, 55);
//        roundWidth = typedArray.getDimension(R.styleable.CustomProgressBar_roundWidth, 10);
//        textShow = typedArray.getBoolean(R.styleable.CustomProgressBar_textShow, true);
//        typedArray.recycle();
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        //第一步 画圆环
//        int cx =getWidth()/2;//中心点x坐标
//        int cy = getHeight()/2;//中心点y坐标
//        int center = cx / 2;//中心点到最外层距离
//        float radius = center - roundWidth / 2;//画笔半径
//        paint.setColor(roundColor); //圆形颜色
//        /*
//        Paint.Style.FILL：填充内部
//        Paint.Style.FILL_AND_STROKE  ：填充内部和描边
//        Paint.Style.STROKE  ：描边
//        */
//        paint.setStyle(Paint.Style.STROKE);//描边 空心圆
//        paint.setStrokeWidth(roundWidth);//圆环宽度
//        paint.setAntiAlias(true);//抗锯齿
//        /*
//        drawCircle的四个参数
//        cx：圆心的x坐标。
//        cy：圆心的y坐标。
//        radius：圆的半径。
//        paint：绘制时所使用的画笔
//        */
//        canvas.drawCircle(cx, cy, radius, paint);
//
//        //第二步 画进度百分比 也就是中间文字
//        paint.setColor(textColor);//文字颜色
//        paint.setStrokeWidth(0);//文字笔画宽度
//        paint.setTextSize(textSize);//文字大小
//        paint.setTypeface(Typeface.DEFAULT_BOLD);//字体
//
//        int percent = (int) (progress / (float) max * 100);
//        String strPercent = percent + "%";
//        Paint.FontMetricsInt pfm = paint.getFontMetricsInt();//绘制文本对象
//        if (percent != 0) {
//            canvas.drawText(strPercent, cx - paint.measureText(strPercent) / 2,
//                    cy + (pfm.bottom - pfm.top) / 2 - pfm.bottom, paint);
//        }
//
//
//        // 第三步 画圆弧
//        RectF oval = new RectF(cx-radius, cy-radius,
//                cx + radius, cy + radius);
//        paint.setColor(roundProgressColor);
//        paint.setStrokeWidth(roundWidth);
//        paint.setStyle(Paint.Style.STROKE);
//        /*
//        setStrokeCap(Paint.Cap cap);
//        当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，
//        如圆形样式 Cap.ROUND,或方形样式Cap.SQUARE
//        */
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        canvas.drawArc(oval, 0, 360 * progress / max, false, paint);
//
//
//    }
//
//    public void setProgress(int progress) {
//        if (progress < 0) {
//            throw new IllegalArgumentException("进度Progress不能小于0");
//        }
//        if (progress > max) {
//            progress = max;
//        }
//        if (progress <= max) {
//            this.progress = progress;
//            postInvalidate();
//        }
//    }
//}
