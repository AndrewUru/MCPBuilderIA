package com.mcpbuilder.ia;

import android.content.Context;

final class AppColors {
    final int blue;
    final int teal;
    final int green;
    final int ink;
    final int muted;
    final int line;
    final int surface;
    final int card;
    final int codeBg;
    final int codeText;
    final int heroBg;
    final int heroLine;
    final int primarySoft;
    final int secondarySoft;
    final int inputHint;
    final int buttonLight;
    final int darkButton;
    final int statusLine;
    final int codeLine;

    private AppColors(Context context) {
        blue = color(context, R.color.color_primary);
        teal = color(context, R.color.color_secondary);
        green = color(context, R.color.color_success);
        ink = color(context, R.color.color_ink);
        muted = color(context, R.color.color_muted);
        line = color(context, R.color.color_line);
        surface = color(context, R.color.color_surface);
        card = color(context, R.color.color_card);
        codeBg = color(context, R.color.color_code_bg);
        codeText = color(context, R.color.color_code_text);
        heroBg = color(context, R.color.color_hero_bg);
        heroLine = color(context, R.color.color_hero_line);
        primarySoft = color(context, R.color.color_primary_soft);
        secondarySoft = color(context, R.color.color_secondary_soft);
        inputHint = color(context, R.color.color_input_hint);
        buttonLight = color(context, R.color.color_button_light);
        darkButton = color(context, R.color.color_dark_button);
        statusLine = color(context, R.color.color_status_line);
        codeLine = color(context, R.color.color_code_line);
    }

    static AppColors from(Context context) {
        return new AppColors(context);
    }

    private static int color(Context context, int id) {
        return context.getResources().getColor(id, context.getTheme());
    }
}
