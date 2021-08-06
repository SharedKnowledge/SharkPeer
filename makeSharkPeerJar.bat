@ECHO off

SET outProdDir="out/production/SharkPeer/"
 
@rem Check if the destination Directory exist
IF not exist outProdDir (
	mkdir %outProdDir%
)

CD %outProdDir%
jar -cf SharkPeer.jar net
MOVE SharkPeer.jar ../../../SharkPeer.jar

@rem move back to the root of the project
CD ../../..