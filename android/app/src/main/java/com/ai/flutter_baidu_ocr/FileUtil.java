/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.ai.flutter_baidu_ocr;

import android.content.Context;

import java.io.File;

public class FileUtil {
    public static File getSaveFile(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file;
    }

    public static File getIdCardFrontFile(Context context) {
        File file = new File(context.getFilesDir(), "idCardFront.jpg");
        return file;
    }

    public static File getIdCardBackFile(Context context) {
        File file = new File(context.getFilesDir(), "idCardBack.jpg");
        return file;
    }

    public static File getBankCardFile(Context context) {
        File file = new File(context.getFilesDir(), "bankCard.jpg");
        return file;
    }

    public static File getVehicleFile(Context context) {
        File file = new File(context.getFilesDir(), "vehicle.jpg");
        return file;
    }

    public static File getDrivingFile(Context context) {
        File file = new File(context.getFilesDir(), "driving.jpg");
        return file;
    }
}
