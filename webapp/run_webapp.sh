#!/usr/bin/env bash

source activate shimi
cd /home/nvidia/shimi/webapp/server
npm start &
cd /home/nvidia/shimi/webapp/client
npm start &
ssh -R shimi-webapp:80:localhost:8080 -R shimi-webapp-server:80:localhost:8081 serveo.net
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT