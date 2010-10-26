#!/bin/sh
for d in ./*
do
        if [ ! -d $d ]
        then
        continue
        fi

        cd $d
        echo "Now in $d"

        mkdir src

        cd ..

done
