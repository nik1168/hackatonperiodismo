#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <string>

using namespace std;
using namespace cv;

string window1 = "Window1";

int main(){

    VideoCapture inputVideo("Vid.avi");

    if (!inputVideo.isOpened()){
        cout  << "Could not open the input video" << endl;
        return -1;}

    Mat image;
    image = imread("mikasa.jpg", CV_LOAD_IMAGE_COLOR);
    cv::resize(image, image, cv::Size(200, 100)); ///Remember that x represent the columns and that y represents the rows

    Mat ycrcb;
    cvtColor(image,ycrcb,CV_BGR2YCrCb);
    vector<Mat> channels;
    split(ycrcb,channels);
    Mat result;
    merge(channels,ycrcb);
    cvtColor(ycrcb,result,CV_YCrCb2BGR);

    Mat src, src2;
    char c;

    for(;;) {
        inputVideo >> src;
            if (src.empty()) break;
        Mat ycrcb2;
        cvtColor(src, ycrcb2, CV_BGR2YCrCb);
        vector<Mat> channels2;
        split(ycrcb2,channels2);

        channels[0].copyTo(channels2[0].rowRange(100, 200).colRange(100, 300));
        channels[1].copyTo(channels2[1].rowRange(100, 200).colRange(100, 300));
        channels[2].copyTo(channels2[2].rowRange(100, 200).colRange(100, 300));

        merge(channels2,ycrcb2);
        cvtColor(ycrcb2,src,CV_YCrCb2BGR);
        imshow(window1, src);
        c = (char)cvWaitKey(52);
        if (c == 27) break;
    }

    return 0;
}

/**Example that shows the video inside the same video*/
/*int main(){

    VideoCapture inputVideo("Vid.avi");
    VideoCapture inputVideo2("Vid.avi");

    if (!inputVideo.isOpened()){
        cout  << "Could not open the input video" << endl;
        return -1;}

    Mat image;
    image = imread("mikasa.jpg", CV_LOAD_IMAGE_COLOR);
    cv::resize(image, image, cv::Size(200, 100)); ///Remember that x represent the columns and that y represents the rows

    Mat ycrcb;
    cvtColor(image,ycrcb,CV_BGR2YCrCb);
    vector<Mat> channels;
    split(ycrcb,channels);
    Mat result;
    merge(channels,ycrcb);
    cvtColor(ycrcb,result,CV_YCrCb2BGR);

    Mat src, src2;
    char c;

    for(;;) {
        inputVideo >> src;
        inputVideo2 >> src2;
        cv::resize(src2, src2, cv::Size(200, 100));
            if (src.empty()) break;
        Mat ycrcb2;
        cvtColor(src, ycrcb2, CV_BGR2YCrCb);
        vector<Mat> channels2;
        split(ycrcb2,channels2);

        Mat ycrcb3;
        cvtColor(src2, ycrcb3, CV_BGR2YCrCb);
        vector<Mat> channels3;
        split(ycrcb3,channels3);

        channels3[0].copyTo(channels2[0].rowRange(100, 200).colRange(100, 300));
        channels3[1].copyTo(channels2[1].rowRange(100, 200).colRange(100, 300));
        channels3[2].copyTo(channels2[2].rowRange(100, 200).colRange(100, 300));

        merge(channels2,ycrcb2);
        cvtColor(ycrcb2,src,CV_YCrCb2BGR);
        imshow(window1, src);
        c = (char)cvWaitKey(52);
        if (c == 27) break;
    }

    return 0;
}*/
