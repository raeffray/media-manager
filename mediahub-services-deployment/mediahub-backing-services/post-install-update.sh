#!/bin/bash

docker exec -it mediahub-db mongosh mongodb://adminuser:qwaszx12@localhost:27017/mediahub-medias --authenticationDatabase admin --eval "$(<./mongo-config/create-user.js)"