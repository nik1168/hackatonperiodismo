#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <sstream>
#include <string>
#include <stdio.h>

using namespace std;
using namespace cv;

/**Canny edge detector variables*/
int edgeThresh = 1;
int lowThreshold = 50;
int const max_lowThreshold = 100;
int ratio_variable = 3;
int kernel_size = 3;
string window_name = "Edge Map";

/**Global Variables*/
string window_normal_image = "Normal image display";
string gray_show = "Gray image display";
string histogram_show = "Image equalized";
string equalization_color = "Image equalized color";
string edge_detect_screen = "Edge detection screen";

/**Headers*/
void gray_transform(Mat frame);
void histogram_equalization(Mat frame);
void histogram_equalization_color(Mat frame);
void canny_edge_detector(Mat frame);

int main() {
    CvCapture* capture;
    Mat frame;
    capture = cvCaptureFromCAM( 0 );
    if( capture ){
        while( true ){
        frame = cvQueryFrame( capture );
            if( !frame.empty() ){
                imshow(window_normal_image, frame);
                //gray_transform(frame);
                histogram_equalization_color(frame);
                //canny_edge_detector(frame);
                }
            else{
                printf(" No captured frame "); break; }
            int c = waitKey(10);
            if( (char)c == 'c' ) { break; }
        }///end of while
    }///end of capture
    return 0;
} ///end main


void gray_transform(Mat frame){
    Mat_<Vec3b> gray_image = frame;
    Mat gray((int)frame.rows, (int)frame.cols, CV_8UC1);
    /// BGR remember!!
    for (int i=0; i<frame.rows; i++){
        for (int j=0; j<frame.cols; j++){
            gray.at<uchar>(i,j) = (float)(gray_image(i,j)[0] * 0.12) + (float)(gray_image(i,j)[1] * 0.30) + (float)(gray_image(i,j)[2] * 0.58) ;
        }///end for
    }///end for
    imshow(gray_show, gray);
}///end gray_transform

void histogram_equalization(Mat frame){
    Mat gray_image;
    cvtColor( frame , gray_image, CV_BGR2GRAY );
    string output = "Normal gray image";
    imshow(output, gray_image);
    equalizeHist( gray_image, gray_image);
    imshow(histogram_show, gray_image);
}

void histogram_equalization_color(Mat frame){
        Mat ycrcb;
        cvtColor(frame,ycrcb,CV_BGR2YCrCb);
        vector<Mat> channels;
        split(ycrcb,channels);
        equalizeHist(channels[0], channels[0]);
        Mat result;
        merge(channels,ycrcb);
        cvtColor(ycrcb,result,CV_YCrCb2BGR);
        imshow(equalization_color, result);
}

void canny_edge_detector(Mat frame){
    ///dst equal as frame
    Mat dst;
    dst.create( frame.size(), frame.type() );
    Mat src_gray, edges;
    cvtColor(frame, src_gray, CV_BGR2GRAY);
    /// Reduce noise with a kernel 3x3
    blur( src_gray, edges, Size(3,3) );
    /// Canny detector
    Canny( edges, edges, lowThreshold, lowThreshold*ratio_variable, kernel_size );
    /// Using Canny’s output as a mask, we display our result
    dst = Scalar::all(0);
    frame.copyTo( dst, edges);
    imshow( edge_detect_screen, dst );
}
