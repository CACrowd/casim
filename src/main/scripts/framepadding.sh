#/bin/bash
DIR=`pwd`
cd nash04
for i in 0*100; do
	cnt1=`ls $i | wc -l`
	if [ $cnt1 -le 0 ]; then
		continue
	fi
	cd $DIR
	cd nash08
	cnt2=`ls $i | wc -l`

	if [ $cnt1 -le $cnt2 ]; then
		cnt=$cnt2
	else
		cnt=$cnt1
	fi
	
	echo "padding: $cnt"
	#nash04 frame padding
	echo "Nash 0.4 m"
	cd $DIR
	cd nash04
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
			ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	#nash08 frame padding
	echo "Nash 0.8 m"
	cd $DIR
	cd nash08
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr  $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
                        ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	#nash12 frame padding
	echo "Nash 1.2 m"
	cd $DIR
	cd nash12
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr  $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
                        ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	#mscb04 frame padding
	echo "MSCB 0.4 m"
	cd $DIR
	cd mscb04
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr  $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
                        ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	#mscb08 frame padding
	echo "MSCB 0.8 m"
	cd $DIR
	cd mscb08
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr  $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
                        ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	#mscb12 frame padding
	echo "MSCB 1.2 m"
	cd $DIR
	cd mscb12
	curr=`ls $i | wc -l`
	if [ $curr -lt $cnt ]; then
		tmp=$(($curr - 1))
		ref=$(printf %010d $tmp).png
		echo $ref
		for j in `seq $curr  $(($cnt - 1))`; do
			padding=$(printf %010d $j).png
                        ln -s $ref $i/$padding
		done
	fi
	echo "========================="	
	sleep 1
done
