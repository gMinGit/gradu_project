package com.example.scanwifi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * ���ı���ʾ�Ľ�����
 */
public class TextProgressBar extends ProgressBar {
    private String text;
    private Paint mPaint;
    private Rect m_rect;

    public TextProgressBar(Context context) {
        super(context);
        initText();
        m_rect = new Rect();
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText();
        m_rect = new Rect();
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText();
        m_rect = new Rect();
    }

    @Override
    public void setProgress(int progress) {
        setText(progress);
        super.setProgress(progress);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaint.getTextBounds(this.text, 0, this.text.length(), m_rect);
        int x = (getWidth() / 2) - m_rect.centerX();
        int y = (getHeight() / 2) - m_rect.centerY();
        canvas.drawText(this.text, x, y, this.mPaint);
    }

    // ��ʼ��������
    private void initText() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(Color.WHITE);

    }

    // ������������
    private void setText(int progress) {
        // int i = (int) ((progress * 1.0f / this.getMax()) * 100);
        this.text = String.valueOf(progress) + "/"
                + String.valueOf(this.getMax());
    }
}