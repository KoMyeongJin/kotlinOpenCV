#include <jni.h>
#include <iostream>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>

using namespace cv;
using namespace std;

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_opencvkotlin_MainActivity_findRect(JNIEnv *env, jobject object,
                                                    jlong mat_addr_input) {
    Mat &input_image = *(Mat *) mat_addr_input;
    Mat img_gray;
    Mat five_by_five(5, 5, CV_8U, Scalar(1));
    double rect_width = 0;

    cvtColor(input_image, img_gray, COLOR_BGR2GRAY);


    // 길이 값이 이상하게 나올경우 조정
    threshold(img_gray, img_gray, 200, 255, THRESH_BINARY);
    //adaptiveThreshold(img_gray, img_gray, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 7, 7);

    Mat mask = getStructuringElement(MORPH_RECT, Size(11, 11), Point(1, 1));

    dilate (img_gray, img_gray, mask, Point(-1, -1), 3);
    erode (img_gray, img_gray, mask, Point(-1, -1), 3);


    vector<vector<Point>> contours;

    findContours(img_gray, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

    vector<Point2f> approx;

    for (size_t i = 0; i < contours.size(); i++) {
        approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true) * 0.02, true);

        int size = approx.size();

        if (size == 4 && isContourConvex(Mat(approx))) {
            rect_width = abs(approx[1].x - approx[2].x);
            drawContours(input_image, contours, i, Scalar(0, 255, 255), 10);
            if (rect_width >= input_image.cols / 2)
                rect_width = 0;
            else
                break;
        }

    }

    return static_cast<jdouble>(rect_width);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_opencvkotlin_MainActivity_imgWidth(
        JNIEnv *env,
        jobject object, jlong mat_addr_input) {

    Mat &input_img = *(Mat *) mat_addr_input;

    return static_cast<jint>(input_img.cols);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_opencvkotlin_MainActivity_imgHeight(
        JNIEnv *env,
        jobject object, jlong mat_addr_input) {

    Mat &input_img = *(Mat *) mat_addr_input;

    return static_cast<jint>(input_img.rows);
}

