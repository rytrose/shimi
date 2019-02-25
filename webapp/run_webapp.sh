#!/usr/bin/env bash

source activate shimi
cd ./server
npm start &
cd ../client
npm start &
ssh -R shimi-webapp:80:localhost:8080 -R shimi-webapp-server:80:localhost:8081 serveo.net
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT