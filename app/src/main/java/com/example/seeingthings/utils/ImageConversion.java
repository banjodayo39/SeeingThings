package com.example.seeingthings.utils;

/**
 * Utility class for manipulating images.
 */
public class ImageConversion {

    private ImageConversion(){
        //Hiding default constructor
    }
    // This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
    // are normalized to eight bits.
    private static final int K_MAX_CHANNEL_VALUE = 262143;

    @SuppressWarnings("unused")
    public static int getYUVByteSize(final int width, final int height) {
        // The luminance plane requires 1 byte per pixel.
        final int ySize = width * height;

        // The UV plane works on 2x2 blocks, so dimensions with odd size must be rounded up.
        // Each 2x2 block takes 2 bytes to encode, one each for U and V.
        final int uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2;

        return ySize + uvSize;
    }

    private static int yuv2Rgb(int y, int u, int v) {
        // Adjust and check YUV values
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.

        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Clipping RGB values to be inside boundaries [ 0 , K_MAX_CHANNEL_VALUE ]
        if (r > K_MAX_CHANNEL_VALUE) r = K_MAX_CHANNEL_VALUE;
        else r = r < 0 ? 0 : r;
        if (g > K_MAX_CHANNEL_VALUE) g = K_MAX_CHANNEL_VALUE;
        else g = g < 0 ? 0 : g;
        if (b > K_MAX_CHANNEL_VALUE) b = K_MAX_CHANNEL_VALUE;
        else b = b < 0 ? 0 : b;

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }

    public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uvOffset = pUV + (i >> 1) * uvPixelStride;

                out[yp++] = yuv2Rgb(0xff & yData[pY + i], 0xff & uData[uvOffset], 0xff & vData[uvOffset]);
            }
        }
    }

}
