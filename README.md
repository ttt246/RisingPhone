# RisingAndroid
[![Android CI Develop](https://github.com/ttt246/RisingPhone/actions/workflows/android.yml/badge.svg?branch=develop)](https://github.com/ttt246/RisingPhone/actions/workflows/android.yml)
[![Android CI](https://github.com/ttt246/RisingPhone/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/ttt246/RisingPhone/actions/workflows/android.yml)

The goal of this project is to develop a phone operating system that replaces the traditional UI of an Android-based smartphone with ChatGPT. The AI will manage control of all apps via plugins, which can be prompted by the user.


## Demo

<p align='center'>
  <img align='center' src='assets/img/output.gif' width='250px' height='500px'/>
</p>


## Documentation

### Architecture

<p align='center'>
  <img align='center' src='assets/img/arch.jpg'/>
</p>

### Features

| Title  | Description  |
| ------------ | ------------ |
| General Chat | Users can chat with AI plugins. |
| Open Browser Automatically |  If a user is going to open browser, the app opens browser and search what a user wants automatically |
| Image Search System  | A user can search image on Android local storage |
| Send SMS | If a user says that send SMS, mobile open SMS editor and a user can send SMS using the editor. |

### Run locally
- Copy google-services.json into app folder of project

### CI/CD
- set google-services.json to github secrets

## Test
- Unit Test
- Instrumented Test

## Contributing
------------

Please refer to each project's style and contribution guidelines for submitting patches and additions. In general, we follow the "fork-and-pull" Git workflow.

 1. **Fork** the repo on GitHub
 2. **Clone** the project to your own machine
 3. **Commit** changes to your own branch
 4. **Push** your work back up to your fork
 5. Submit a **Pull request** so that we can review your changes

NOTE: Be sure to merge the latest from "upstream" before making a pull request!
