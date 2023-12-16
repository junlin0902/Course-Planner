# CS346 Course Planner Android Project
## Instructions to Install
First, we need to set up the server. Within an Android emulator, such as Android Studio, navigate to the folder where you would like to install our application, open your terminal, and enter the following command:

`git clone https://git.uwaterloo.ca/dgrinton/cs346-project.git`

To run the server, either run the `server.sh` file or run the following commands from the `server` folder:

`docker docker build -t dockerfile .`

`docker run -p 8080:8080 dockerfile .`

To run the client, drag the [APK](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.4-installer.apk) from its installed location onto your emulated device. It will now work properly.

Navigate to the latest available release below, or using the link to view all software releases [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/tree/main/release) Download the latest installer APK, and drag onto your emulated Android device.
### Software Releases
### Final Release
- [Installer](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/release-installer.apk)
### v0.4
- [Release Notes](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.4-release-notes.md)
- [Installer](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.4-installer.apk)
#### v0.3
- [Release Notes](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.3-release-notes.md)
- [Installer](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.3-installer.apk)
#### v0.2
- [Release Notes](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.2-release-notes.md)
- [Installer](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.2-installer.apk)
#### v0.1
- [Release Notes](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.1-release-notes.md)
- [Installer](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/release/v0.1-installer.apk)

## Requirements
This application is aimed at providing an inclusive, all-in-one student course planning platform for all students attending the University of Waterloo. Offering a default [home page](https://git.uwaterloo.ca/dgrinton/cs346-project/-/issues/11?work_item_iid=12), the ability to [create and log in](https://git.uwaterloo.ca/dgrinton/cs346-project/-/issues/11?work_item_iid=17) to an account, a personalized [schedule](https://git.uwaterloo.ca/dgrinton/cs346-project/-/issues/11?work_item_iid=14), the ability to [search](https://git.uwaterloo.ca/dgrinton/cs346-project/-/issues/11?work_item_iid=12) for all existing and previously offered courses at UW, and [descriptions](https://git.uwaterloo.ca/dgrinton/cs346-project/-/issues/18) for every course are among the most prominent features of our Course Planner. For a full list of features and their associated issues, click [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/blob/main/requirements/features.md)
## Architecture and Design
### Proposal
View our project's proposal [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/wikis/Project-Proposal)
### Revised High-Level Architecture and Design
View the latest architecture and design diagrams [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/wikis/Project-Final-Architecture-and-Design)
## Reflections on Practices
Read about how the team reflects on our practices throughout development [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/wikis/Final-Reflection)
### Meeting Minutes
View the logged meeting minutes [here.](https://git.uwaterloo.ca/dgrinton/cs346-project/-/wikis/Meeting-Minutes)
