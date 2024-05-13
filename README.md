# IMUNet: Efficient Regression Architecture for Inertial IMU Navigation and Positioning (Android Implementation)
This repository contains an Android implementation for the [IMUNet](https://ieeexplore.ieee.org/abstract/document/10480886) paper. 

It has three parts:

1- An application for collecting a new dataset that uses the AR-Core API for collecting the ground truth using the SLAM techniques and IMU measurements.
The modification has been implemented on the code provided [here](https://github.com/higerra/TangoIMURecorder) and the Tango part for
collecting the ground truth trajectory has been replaced with AR-Core API that makes every android user able to collect a new dataset.


2- The test part of the [RONIN](https://github.com/Sachini/ronin) for the ResNet18 model using all the proposed models and some samples of the
collected dataset has been implemented on Android. Samples can be downloaded [here](https://www.dropbox.com/s/621v5lbf237gxg4/raw.zip?dl=0) and must be put in the raw folder.

3- A comparison has been implemented to show the efficiency and accuracy of the proposed model. The result can be seen in the video below:  


# Citation

@article{zeinali2024imunet,
  title={IMUNet: Efficient Regression Architecture for Inertial IMU Navigation and Positioning},
  author={Zeinali, Behnam and Zanddizari, Hadi and Chang, Morris J},
  journal={IEEE Transactions on Instrumentation and Measurement},
  year={2024},
  publisher={IEEE}
}
