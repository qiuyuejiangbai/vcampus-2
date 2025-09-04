package client.ui.dashboard.effects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** 轻量“呼吸感”动效：在 [0.9,1.0] 范围往复。 */
public class BreathingAnimator {
    public interface Listener { void onAlphaChanged(float alpha); }

    private final Timer timer;
    private float alpha = 1.0f;
    private float delta = -0.01f;

    public BreathingAnimator(final Listener listener) {
        int periodMs = 1400; // 1.4s 周期
        int step = 16;
        this.timer = new Timer(step, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                alpha += delta;
                if (alpha < 0.9f) { alpha = 0.9f; delta = +0.01f; }
                if (alpha > 1.0f) { alpha = 1.0f; delta = -0.01f; }
                listener.onAlphaChanged(alpha);
            }
        });
        this.timer.setInitialDelay(0);
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }
}


