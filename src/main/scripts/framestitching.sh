#!/usr/bin/env bash
DIR=`pwd`
cd nash04
cnt=0
for i in 0*; do

	while read j; do
		#echo $j
		out=$(printf %010d $cnt).png
		echo $out
		convert $DIR/nash04/$i/$j $DIR/nash08/$i/$j $DIR/nash12/$i/$j +append -scale 1920 $DIR/nash/$out
		convert $DIR/mscb04/$i/$j $DIR/mscb08/$i/$j $DIR/mscb12/$i/$j +append -scale 1920 $DIR/mscb/$out
		convert $DIR/nash/$out $DIR/mscb/$out -append $DIR/all/$out
		cnt=$(($cnt + 1))
		echo $cnt
	done <<< "$(ls $i)"
done
