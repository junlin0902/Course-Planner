# Course Planner Android Project
## General
![5493f2c83f4cb066d441d58da57cd6e](https://github.com/junlin0902/Course-Planner/assets/118623321/9a8f319f-e89a-40ea-a178-0cb612ea4c42)

## Goals
Keeping track of details for 5 different courses at a time can get confusing. Our goal is to introduce an all-in-one product that tracks various details of University of Waterloo courses.

## Instructions to Install
First, we need to set up the server. Within an Android emulator, such as Android Studio, navigate to the folder where you would like to install our application, open your terminal, and enter the following command:

`git clone https://git.uwaterloo.ca/dgrinton/cs346-project.git`

To run the server, either run the `server.sh` file or run the following commands from the `server` folder:

`docker docker build -t dockerfile .`

`docker run -p 8080:8080 dockerfile .`

To run the client, drag the [APK](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.4-installer.apk) from its installed location onto your emulated device. It will now work properly.

Download the latest installer APK, and drag onto your emulated Android device.
### Software Releases
See the release folder.

## Requirements
This application is aimed at providing an inclusive, all-in-one student course planning platform for all students attending the University of Waterloo. Offering a default home page, the ability to create and log into an account, a personalized schedule, the ability to search for all existing and previously offered courses at UW, and descriptions for every course are among the most prominent features of our Course Planner. For a full list of features and their associated issues.

## Architecture and Design
### Goals
Keeping track of details for 5 different courses at a time can get confusing. Our goal is to introduce an all-in-one product that tracks various details of University of Waterloo courses.
### Risks
- It is our first time learning and using Kotlin. We have some experience with similar languages such as Java, but not Kotlin itself.\
**Mitigation:** Start early on learning and using Kotlin, keep up during lectures and lab sections to observe how to use Kotlin and the various frameworks to build the application.
- We are also new to full-stack application development. We are quite comfortable with back-end, although we are not as familiar with front-end.\
**Mitigation:** Attend UI framework lectures and make sure we communicate well during the UI design so that nobody is "left behind" and we all understand why we are making these decisions.
## Milestones
**Sprint 1 Oct 4 - Oct 20** \
Focus on creating UI. \
**Sprint 2 Oct 25 - Nov 3** \
Focus on creating database to track user accounts & course details. \
**Sprint 3 Nov 8 - Nov 17** \
Focus on getting information from UW using the [Open Data API.](https://uwaterloo.ca/api/) \
**Sprint 4 Nov 22 - Dec 1** \
Focus on making the program asynchronous so it is consistent across all users.
### Problem
In spite of the existence of information available to students, the school-provided UI remains insufficiently visually appealing, and the resources themselves fail to connect to one another in a meaningful manner for students. Such examples include difficulty mapping pre-requisite chains, missing an interactive digital graduation course checklist, and personalization for an individual account. The status quo alternatives of UWFlow and Noteability also struggle in some of these areas. Our app will aim to address these pitfalls of existing software and provide University of Waterloo students with an all-in-one, easy-to-use digital platform for all course-related documents, data and information.
### Users
Our app is aimed for all students at the University of Waterloo who use Android phones. We did an interview with two students who use Android phones. One of the students had a problem that he spent a lot of time organizing the course materials. This student envisions an app where they can effortlessly manage all their course materials, including lecture slides and assignment solutions. They seek a solution that eliminates the need to repeatedly visit LEARN to redownload these resources. An intuitive file repository, coupled with a user-friendly interface, could significantly enhance their academic experience by making it easier to find, and organize.The second student encountered a problem was that there was not an all-in-one app that could organize both materials and course information. He is interested in features such as a streamlined class schedule display, allowing them to easily track their classes and deadlines.  Moreover, he wishs to take charge of their academic progress by manually entering their grades into the app to calculate and monitor their overall course performance. 
### Functional Requirements
The features we think are critical to this project are as follows:
- View course ratings and offerings
- View schedule of enrolled courses
- Track assignment/overall grades for courses
- Upload and store course materials
- View prof's related information like birthday
- View course pre/co/anti requesites
- Allow users to switch interface to different color (light/dark)
- Allow files to be sorted by file names
- Allow users to access help mode
- Import & export files



### Revised High-Level Architecture and Design
We are using layered architecture for our project. A diagram including a rough description of what each layer will do is as follows:
![image](https://github.com/junlin0902/Course-Planner/assets/118623321/461be919-8a3e-43a1-bba7-06b2f59a6f87)

### Final Business Layer UML
![image](https://github.com/junlin0902/Course-Planner/assets/118623321/991afe14-277f-4653-b938-92a37375b2eb)

