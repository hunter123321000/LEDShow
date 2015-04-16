package com.hunter123321000.ledshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView implements Runnable {
	public enum MarqueeDirection {
		LEFT, RIGHT
	}

	private volatile boolean m_bIsStop = false;
	private int m_iTextWidth = 0;// ��r�e��
	private boolean m_bIsMeasure = false;
	private int m_iDistance = 1;
	private int m_iSpeed = 1;
	private int m_iScrollPosition = 0;
	private MarqueeDirection m_Direction = MarqueeDirection.RIGHT;

	
	public MarqueeTextView(Context context) {
		super(context);
		setEllipsize(TruncateAt.MARQUEE);
		setSingleLine(true);
		onResume();
	}
	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!m_bIsMeasure) { // ��r�e�ץu�n����@���N�n�աI
			getTextWidth();
			startScroolFrom0();
			m_bIsMeasure = true;
		}
		super.onDraw(canvas); // ���]�w�n�A�e�A�~���|���ݼv
	}

	public void onResume() {
		startScroll();
	}

	public void onPause() {
		stopScroll();
	}

	private void getTextWidth() {
		Paint paint = getPaint();
		String str = getText().toString();
		m_iTextWidth = (int) paint.measureText(str);
	}

	protected void startScroolFrom0() { // �]���O��l�Ȥ�V
		if (m_Direction == MarqueeDirection.RIGHT)
			m_iScrollPosition = -getWidth();
		else
			m_iScrollPosition = m_iTextWidth;

		scrollTo(m_iScrollPosition, 0);
	}

	@Override
	public void run() {
		if (m_bIsStop)
			return;

		// �]���O��l�Ȥ�V
		if (m_Direction == MarqueeDirection.RIGHT)
			m_iScrollPosition += m_iDistance;
		else
			m_iScrollPosition -= m_iDistance;
		scrollTo(m_iScrollPosition, 0);
		if ((m_Direction == MarqueeDirection.RIGHT && getScrollX() >= m_iTextWidth)
				|| // �ѥk�V����
				(m_Direction == MarqueeDirection.LEFT && getScrollX() <= -getWidth())) // �ѥ��V�k��
		{
			startScroolFrom0();
		}
		postDelayed(this, m_iSpeed);
	}

	public void startScroll() {
		m_bIsStop = false;
		removeCallbacks(this);
		post(this);
	}

	public void stopScroll() {
		m_bIsStop = true;
		removeCallbacks(this);
	}
}
