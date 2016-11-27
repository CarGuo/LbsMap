package com.shuyu.lbsmap.utils;

/**
 * Created by shuyu on 2016/11/27.
 */

public class LocationLevelUtils {

    public static float returnCurZoom(float radius) {
        if (radius < 98.93076f) {
            return 21f;
        } else if (radius < 101.70005f) {
            return 19.7345f;
        } else if (radius < 104.47371f) {
            return 19.6995f;
        } else if (radius < 106.32285f) {
            return 19.6657f;
        } else if (radius < 109.0965f) {
            return 19.5934f;
        } else if (radius < 111.87016f) {
            return 19.5684f;
        } else if (radius < 116.49666f) {
            return 19.541f;
        } else if (radius < 120.65345f) {
            return 19.4138f;
        } else if (radius < 126.20082f) {
            return 19.3781f;
        } else if (radius < 130.36462f) {
            return 19.3323f;
        } else if (radius < 138.68234f) {
            return 19.2919f;
        } else if (radius < 140.53148f) {
            return 19.2207f;
        } else if (radius < 147.92796f) {
            return 19.1929f;
        } else if (radius < 151.62694f) {
            return 19.1564f;
        } else if (radius < 155.78674f) {
            return 19.1223f;
        } else if (radius < 159.02545f) {
            return 19.0875f;
        } else if (radius < 163.64554f) {
            return 19.0015f;
        } else if (radius < 174.74034f) {
            return 18.9538f;
        } else if (radius < 177.97632f) {
            return 18.9274f;
        } else if (radius < 182.13687f) {
            return 18.895f;
        } else if (radius < 183.52432f) {
            return 18.8811f;
        } else if (radius < 189.5334f) {
            return 18.8408f;
        } else if (radius < 206.17569f) {
            return 18.6291f;
        } else if (radius < 219.1197f) {
            return 18.5891f;
        } else if (radius < 226.51678f) {
            return 18.5787f;
        } else if (radius < 260.7257f) {
            return 18.3774f;
        } else if (radius < 268.26785f) {
            return 18.3051f;
        } else if (radius < 286.1517f) {
            return 18.247099f;
        } else if (radius < 297.70905f) {
            return 18.1866f;
        } else if (radius < 312.04016f) {
            return 18.1206f;
        } else if (radius < 323.5989f) {
            return 18.064499f;
        } else if (radius < 331.4579f) {
            return 17.992899f;
        } else if (radius < 339.77795f) {
            return 17.937698f;
        } else if (radius < 360.12024f) {
            return 17.9114f;
        } else if (radius < 377.2241f) {
            return 17.7766f;
        } else if (radius < 408.66064f) {
            return 17.7286f;
        } else if (radius < 426.69046f) {
            return 17.668598f;
        } else if (radius < 438.24808f) {
            return 17.6001f;
        } else if (radius < 469.6849f) {
            return 17.530499f;
        } else if (radius < 532.09686f) {
            return 17.347698f;
        } else if (radius < 532.09686f) {
            return 17.304798f;
        } else if (radius < 582.9517f) {
            return 17.078499f;
        } else if (radius < 705.4669f) {
            return 16.942299f;
        } else if (radius < 786.3748f) {
            return 16.7871f;
        } else if (radius < 802.09436f) {
            return 16.803398f;
        } else if (radius < 758.1726f) {
            return 16.621498f;
        } else if (radius < 881.1547f) {
            return 16.593998f;
        } else if (radius < 927.3891f) {
            return 16.4544f;
        } else if (radius < 989.80615f) {
            return 16.398499f;
        } else if (radius < 1068.4064f) {
            return 16.290699f;
        } else if (radius < 1109.0936f) {
            return 16.197798f;
        } else if (radius < 1238.0939f) {
            return 16.130999f;
        } else if (radius < 1333.8059f) {
            return 16.024698f;
        } else if (radius < 1366.1725f) {
            return 15.988798f;
        } else if (radius < 1459.1123f) {
            return 15.894698f;
        } else if (radius < 1501.6522f) {
            return 15.853099f;
        } else if (radius < 1573.786f) {
            return 15.785699f;
        } else if (radius < 1638.5223f) {
            return 15.711999f;
        } else if (radius < 1706.0342f) {
            return 15.669199f;
        } else if (radius < 1757.362f) {
            return 15.626199f;
        } else if (radius < 1910.4232f) {
            return 15.456799f;
        } else if (radius < 1990.053f) {
            return 15.436998f;
        } else if (radius < 2003.8346f) {
            return 15.228498f;
        } else if (radius < 2315.0627f) {
            return 15.181898f;
        } else if (radius < 2469.5269f) {
            return 15.136098f;
        } else if (radius < 2560.173f) {
            return 15.083098f;
        } else if (radius < 2641.57f) {
            return 15.038698f;
        } else if (radius < 2731.756f) {
            return 14.990198f;
        } else if (radius < 2993.073f) {
            return 14.857998f;
        } else if (radius < 3020.3613f) {
            return 14.792499f;
        } else if (radius < 3199.3596f) {
            return 14.728298f;
        } else if (radius < 3351.5356f) {
            return 14.695298f;
        } else if (radius < 3434.795f) {
            return 14.6596985f;
        } else if (radius < 3580.9644f) {
            return 14.599798f;
        } else if (radius < 3713.2598f) {
            return 14.547298f;
        } else if (radius < 3761.3682f) {
            return 14.467499f;
        } else if (radius < 3924.6619f) {
            return 14.404498f;
        } else if (radius < 4211.478f) {
            return 14.348898f;
        } else if (radius < 4260.9795f) {
            return 14.306198f;
        } else if (radius < 4487.206f) {
            return 14.274598f;
        } else if (radius < 4625.0757f) {
            return 14.230699f;
        } else if (radius < 4732.875f) {
            return 14.197398f;
        } else if (radius < 4812.9165f) {
            return 14.173298f;
        } else if (radius < 4906.376f) {
            return 14.129998f;
        } else if (radius < 4959.5845f) {
            return 14.111698f;
        } else if (radius < 5802.1904f) {
            return 13.903898f;
        } else if (radius < 5992.3853f) {
            return 13.857198f;
        } else if (radius < 6325.125f) {
            return 13.707499f;
        } else if (radius < 6721.2915f) {
            return 13.691598f;
        } else if (radius < 6947.6187f) {
            return 13.443898f;
        } else if (radius < 7982.176f) {
            return 13.3787985f;
        } else if (radius < 8442.349f) {
            return 13.265799f;
        } else if (radius < 9031.742f) {
            return 13.241698f;
        } else if (radius < 9342.899f) {
            return 13.216998f;
        } else if (radius < 9869.869f) {
            return 13.137898f;
        } else if (radius < 9981.475f) {
            return 13.121698f;
        } else if (radius < 10206.544f) {
            return 13.089598f;
        } else if (radius < 10555.745f) {
            return 13.041098f;
        } else if (radius < 11157.402f) {
            return 12.938998f;
        } else if (radius < 13126.327f) {
            return 12.691098f;
        } else if (radius < 13457.172f) {
            return 12.587698f;
        } else if (radius < 14458.173f) {
            return 12.557199f;
        } else if (radius < 14956.886f) {
            return 12.538798f;
        } else if (radius < 15414.386f) {
            return 12.4953985f;
        } else if (radius < 16668.418f) {
            return 12.328798f;
        } else if (radius < 17771.082f) {
            return 12.290498f;
        } else if (radius < 18847.986f) {
            return 12.205798f;
        } else if (radius < 19135.104f) {
            return 12.183998f;
        } else if (radius < 19607.326f) {
            return 12.001298f;
        } else if (radius < 22778.9f) {
            return 11.932999f;
        } else if (radius < 23507.2f) {
            return 11.816498f;
        } else if (radius < 25729.803f) {
            return 11.703198f;
        } else if (radius < 26721.828f) {
            return 11.658198f;
        } else if (radius < 27695.445f) {
            return 11.325798f;
        } else if (radius < 35546.63f) {
            return 11.215898f;
        } else if (radius < 37494.695f) {
            return 11.161298f;
        } else if (radius < 40346.1f) {
            return 11.110498f;
        } else if (radius < 40994.22f) {
            return 11.087598f;
        } else if (radius < 42689.348f) {
            return 11.029298f;
        } else if (radius < 45843.42f) {
            return 10.884898f;
        } else if (radius < 47203.418f) {
            return 10.846798f;
        } else if (radius < 50678.477f) {
            return 10.782798f;
        } else if (radius < 52620.434f) {
            return 10.677998f;
        } else if (radius < 55517.066f) {
            return 10.651898f;
        } else if (radius < 57547.473f) {
            return 10.600298f;
        } else if (radius < 59558.027f) {
            return 10.550998f;
        } else if (radius < 60270.883f) {
            return 10.533898f;
        } else if (radius < 61775.34f) {
            return 10.498598f;
        } else if (radius < 64879.375f) {
            return 10.342198f;
        } else if (radius < 68888.37f) {
            return 10.2832985f;
        } else if (radius < 72863.55f) {
            return 10.261798f;
        } else if (radius < 86640.195f) {
            return 10.0135975f;
        } else {
            return 9.953798f;
        }
    }
}
