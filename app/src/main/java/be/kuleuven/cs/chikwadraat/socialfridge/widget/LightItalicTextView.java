package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A {@code TextView} in a light italic font.
 * Original from <a href="https://github.com/afollestad/Cards-UI">Cards-UI</a> by Aidan Follestad.
 */
public class LightItalicTextView extends TextView {

    public LightItalicTextView(Context context) {
        super(context);
        init(context);
    }

    public LightItalicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LightItalicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) return;
        try {
            setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
        } catch (RuntimeException e) {
            setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        }
    }
}
