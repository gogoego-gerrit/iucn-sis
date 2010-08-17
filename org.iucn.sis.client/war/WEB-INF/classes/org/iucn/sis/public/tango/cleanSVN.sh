#!/bin/bash

for d in ./*
do
	if [ ! -d $d ]
	then
	continue
	fi

	cd $d
	echo "Now in $d"
	cp ../cleanSVN.sh ./
	
	echo "Gutting svn stuff."
	rm -rf .svn/
	
	echo "Running clean.sh"
	sh cleanSVN.sh

	echo "Removing clean.sh"
	rm cleanSVN.sh

	cd ..

done
