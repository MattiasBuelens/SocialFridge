package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A {@code TextView} in a light font.
 * Original from <a href="https://github.com/afollestad/Cards-UI">Cards-UI</a> by Aidan Follestad.
 */
public class LightTextView extends TextView {

    public LightTextView(Context context) {
        super(context);
        init(context);
    }

    public LightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        //if (isInEditMode()) return;

        Typeface typeface = getTypeface();
        int style = Typeface.NORMAL;
        if (typeface != null) {
            style = typeface.getStyle();
        }

        try {
            typeface = Typeface.create("sans-serif-light", style);
            setTypeface(typeface);
        } catch (RuntimeException e) {
            // Ignore
        }
    }
}
