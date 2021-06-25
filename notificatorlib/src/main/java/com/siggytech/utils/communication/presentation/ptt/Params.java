package com.siggytech.utils.communication.presentation.ptt;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.siggytech.utils.communication.presentation.chat.CustomButtonAnimation;

public class Params {
    public int cornerRadius;
    public int width;
    public int height;
    public int color;
    public int colorPressed;
    public int duration;
    public int icon;
    public int strokeWidth;
    public int strokeColor;
    public String text;
    public CustomButtonAnimation.Listener animationListener;
    private Params() {
    }
    public static Params create() {
        return new Params();
    }
    public Params text(@NonNull String text) {
        this.text = text;
        return this;
    }
    public Params icon(@DrawableRes int icon) {
        this.icon = icon;
        return this;
    }
    public Params cornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }
    public Params width(int width) {
        this.width = width;
        return this;
    }
    public Params height(int height) {
        this.height = height;
        return this;
    }
    public Params color(int color) {
        this.color = color;
        return this;
    }
    public Params colorPressed(int colorPressed) {
        this.colorPressed = colorPressed;
        return this;
    }
    public Params duration(int duration) {
        this.duration = duration;
        return this;
    }
    public Params strokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }
    public Params strokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }
    public Params animationListener(CustomButtonAnimation.Listener animationListener) {
        this.animationListener = animationListener;
        return this;
    }
}
