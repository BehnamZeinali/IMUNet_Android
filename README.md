# IMUNet: Efficient Regression Architecture for IMU Navigation and Positioning (Android Implementation)
This repository contains an android implementation for the [IMUNet](https://arxiv.org/abs/2208.00068) paper. 

It has three parts:

1- An application for collecting a new dataset that uses the AR-Core API for collecting the ground truth using the SLAM techniques and IMU measurements.
The modification has been implemented on the code provided [here](https://github.com/higerra/TangoIMURecorder) and the Tango part for
collecting the ground truth trajectory has been replaced with AR-Core API that makes every android user able to collect a new dataset.


2- The test part of the [RONIN](https://github.com/Sachini/ronin) for the ResNet18 model using all the proposed models and some samples of the
collected dataset has been implemented on Android. Samples can be downloaded [here](https://www.dropbox.com/s/621v5lbf237gxg4/raw.zip?dl=0) and must be put in the raw folder.

3- A comparison has been implemented to show the efficiency and accuracy of the proposed model. The result can be seen in the video below:  


https://user-images.githubusercontent.com/29498989/181649816-e1adcb44-e899-445e-b1a0-f2467a0030dc.MP4
