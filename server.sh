#!/bin/bash

cd server
docker docker build -t dockerfile .
docker run -p 8080:8080 dockerfile .