#include <iostream>
#include <stdio.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

/**Function Headers*/
void Display(Mat frame);

/**Global variables*/
string window_name = "Color indexing";
string window_normal_image = "Normal image";

int main( int argc, const char** argv )
{
    ///initialize variables
    CvCapture* capture;
    Mat frame;
    ///we define from which webacam we will aquire image

    capture = cvCaptureFromCAM( 0 );
    if( capture ){
        while( true ){
        frame = cvQueryFrame( capture );
            if( !frame.empty() ){
                imshow(window_normal_image, frame);
                Display(frame);
                }
            else{
                printf(" --(!) No captured frame -- Break!"); break; }
            int c = waitKey(10);
            if( (char)c == 'c' ) { break; }
        }///end of while
    }///end of capture
    return 0;
}

void Display(Mat frame){

        Mat_<Vec3b> I = frame;

        for (int i=0; i<frame.rows; i++){
            for(int j=0; j<frame.cols; j++){
                ///BGR
                if ( ( (float)I(i,j)[0] > 85 and (float)I(i,j)[0] < 125 ) and ( (float)I(i,j)[1] > 15 and (float)I(i,j)[1] < 40 ) and ( (float)I(i,j)[2] > 100 and (float)I(i,j)[2] < 125 ) ){
                I(i,j)[0] = (float)(I(i,j)[0]); ///Blue
                I(i,j)[1] = (float)(I(i,j)[1]); ///Green
                I(i,j)[2] = (float)(I(i,j)[2]); ///Red
                }
                else{
                I(i,j)[0] = 255; ///Blue
                I(i,j)[1] = 255; ///Green
                I(i,j)[2] = 255; ///Red
                }

            }///end of inner for
        }///end of outer for

        frame = I;

        imshow( window_name, frame );
}
